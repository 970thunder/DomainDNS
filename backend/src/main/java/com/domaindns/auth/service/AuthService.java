package com.domaindns.auth.service;

import com.domaindns.auth.dto.AuthDtos.LoginReq;
import com.domaindns.auth.dto.AuthDtos.LoginResp;
import com.domaindns.auth.dto.AuthDtos.RegisterReq;
import com.domaindns.auth.dto.AuthDtos.RegisterResp;
import com.domaindns.auth.entity.User;
import com.domaindns.auth.mapper.UserMapper;
import com.domaindns.common.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public AuthService(UserMapper userMapper, JwtService jwtService, JavaMailSender mailSender,
            StringRedisTemplate redis, RateLimiter rateLimiter) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
        this.redis = redis;
        this.rateLimiter = rateLimiter;
    }

    public void sendRegisterCode(String email) {
        boolean allowed = rateLimiter.tryConsume("rl:regcode:" + email, 3, Duration.ofMinutes(1));
        if (!allowed)
            throw new IllegalArgumentException("请求过于频繁，请稍后再试");
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        redis.opsForValue().set("regcode:" + email, code, Duration.ofMinutes(10));
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank())
            message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("DomainDNS 注册验证码");
        message.setText("您的注册验证码为：" + code + "，10分钟内有效。");
        mailSender.send(message);
    }

    @Transactional
    public RegisterResp registerUser(RegisterReq req) {
        if (req.email == null || req.email.isBlank())
            throw new IllegalArgumentException("邮箱必填");
        String key = "regcode:" + req.email;
        String expect = redis.opsForValue().get(key);
        if (expect == null || !expect.equals(req.emailCode))
            throw new IllegalArgumentException("邮箱验证码无效或已过期");
        RegisterResp resp = doRegister(req.username, req.email, req.password, "USER");
        redis.delete(key);
        return resp;
    }

    @Transactional
    public RegisterResp registerAdmin(RegisterReq req) {
        int adminCount = userMapper.countByRole("ADMIN");
        if (adminCount > 0) {
            throw new IllegalArgumentException("管理员已存在，禁止再次注册，请使用账号密码登录或通过邮箱找回密码");
        }
        return doRegister(req.username, req.email, req.password, "ADMIN");
    }

    @Transactional
    public RegisterResp registerAdminSimple(String username, String email, String password) {
        int adminCount = userMapper.countByRole("ADMIN");
        if (adminCount > 0) {
            throw new IllegalArgumentException("管理员已存在，禁止再次注册，请使用账号密码登录或通过邮箱找回密码");
        }
        return doRegister(username, email, password, "ADMIN");
    }

    public LoginResp loginUser(LoginReq req) {
        return doLogin(req, "USER");
    }

    public LoginResp loginAdmin(LoginReq req) {
        return doLogin(req, "ADMIN");
    }

    private RegisterResp doRegister(String username, String email, String password, String role) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("用户名必填");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("密码必填");
        if (userMapper.findByUsername(username) != null)
            throw new IllegalArgumentException("用户名已存在");
        if (email != null && !email.isBlank() && userMapper.findByEmail(email) != null)
            throw new IllegalArgumentException("邮箱已存在");
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setDisplayName(username);
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
        boolean allowed = rateLimiter.tryConsume("rl:reset:" + email, 3, Duration.ofMinutes(1));
        if (!allowed)
            throw new IllegalArgumentException("请求过于频繁，请稍后再试");
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        String key2 = "pwdreset:" + email;
        redis.opsForValue().set(key2, code, Duration.ofMinutes(10));
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank())
            message.setFrom(mailFrom);
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
