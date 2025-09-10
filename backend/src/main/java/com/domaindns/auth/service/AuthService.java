package com.domaindns.auth.service;

import com.domaindns.auth.dto.AuthDtos.LoginReq;
import com.domaindns.auth.dto.AuthDtos.LoginResp;
import com.domaindns.auth.dto.AuthDtos.RegisterReq;
import com.domaindns.auth.dto.AuthDtos.RegisterResp;
import com.domaindns.auth.entity.User;
import com.domaindns.auth.mapper.UserMapper;
import com.domaindns.common.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Random;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redis;
    private final RateLimiter rateLimiter;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper, JwtService jwtService, JavaMailSender mailSender,
            StringRedisTemplate redis, RateLimiter rateLimiter) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
        this.redis = redis;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public RegisterResp registerUser(RegisterReq req) {
        return doRegister(req, "USER");
    }

    @Transactional
    public RegisterResp registerAdmin(RegisterReq req) {
        int adminCount = userMapper.countByRole("ADMIN");
        if (adminCount > 0) {
            throw new IllegalArgumentException("管理员已存在，禁止再次注册，请使用账号密码登录或通过邮箱找回密码");
        }
        return doRegister(req, "ADMIN");
    }

    public LoginResp loginUser(LoginReq req) {
        return doLogin(req, "USER");
    }

    public LoginResp loginAdmin(LoginReq req) {
        return doLogin(req, "ADMIN");
    }

    private RegisterResp doRegister(RegisterReq req, String role) {
        if (req.username == null || req.username.isBlank())
            throw new IllegalArgumentException("用户名必填");
        if (req.password == null || req.password.isBlank())
            throw new IllegalArgumentException("密码必填");
        if (userMapper.findByUsername(req.username) != null)
            throw new IllegalArgumentException("用户名已存在");
        if (req.email != null && !req.email.isBlank() && userMapper.findByEmail(req.email) != null)
            throw new IllegalArgumentException("邮箱已存在");
        User u = new User();
        u.setUsername(req.username);
        u.setEmail(req.email);
        u.setPasswordHash(encoder.encode(req.password));
        u.setDisplayName(req.username);
        u.setInviteCode(null);
        u.setInviterId(null);
        u.setPoints(0);
        u.setRole(role);
        u.setStatus(1);
        userMapper.insert(u);
        RegisterResp resp = new RegisterResp();
        resp.userId = u.getId();
        return resp;
    }

    private LoginResp doLogin(LoginReq req, String requiredRole) {
        User u = null;
        if (req.username != null && !req.username.isBlank()) {
            u = userMapper.findByUsername(req.username);
        }
        if (u == null && req.email != null && !req.email.isBlank()) {
            u = userMapper.findByEmail(req.email);
        }
        if (u == null)
            throw new IllegalArgumentException("用户不存在或密码错误");
        if (!encoder.matches(req.password, u.getPasswordHash()))
            throw new IllegalArgumentException("用户不存在或密码错误");
        if (!requiredRole.equals(u.getRole()))
            throw new IllegalArgumentException("角色不匹配：需要" + requiredRole);
        LoginResp resp = new LoginResp();
        resp.role = u.getRole();
        resp.token = jwtService.issueToken(String.valueOf(u.getId()), u.getRole());
        return resp;
    }

    public void startReset(String email) {
        User u = userMapper.findByEmail(email);
        if (u == null)
            throw new IllegalArgumentException("邮箱不存在");
        // 限流：每邮箱每分钟最多 3 次
        boolean allowed = rateLimiter.tryConsume("rl:reset:" + email, 3, Duration.ofMinutes(1));
        if (!allowed)
            throw new IllegalArgumentException("请求过于频繁，请稍后再试");
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        String key = "pwdreset:" + email;
        redis.opsForValue().set(key, code, Duration.ofMinutes(10));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("DomainDNS 密码重置");
        message.setText("您的验证码为：" + code + "，10分钟内有效。");
        mailSender.send(message);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        String key = "pwdreset:" + email;
        String expect = redis.opsForValue().get(key);
        if (expect == null || !expect.equals(code)) {
            throw new IllegalArgumentException("验证码无效或已过期");
        }
        User u = userMapper.findByEmail(email);
        if (u == null)
            throw new IllegalArgumentException("邮箱不存在");
        String hash = encoder.encode(newPassword);
        userMapper.updatePassword(u.getId(), hash);
        redis.delete(key);
    }
}
