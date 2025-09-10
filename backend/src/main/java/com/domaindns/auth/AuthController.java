package com.domaindns.auth;

import com.domaindns.auth.dto.AuthDtos.ForgotPasswordReq;
import com.domaindns.auth.dto.AuthDtos.LoginReq;
import com.domaindns.auth.dto.AuthDtos.LoginResp;
import com.domaindns.auth.dto.AuthDtos.RegisterReq;
import com.domaindns.auth.dto.AuthDtos.RegisterResp;
import com.domaindns.auth.dto.AuthDtos.ResetPasswordReq;
import com.domaindns.auth.service.AuthService;
import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final StringRedisTemplate redis;

    public AuthController(AuthService authService, JwtService jwtService, StringRedisTemplate redis) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.redis = redis;
    }

    // ---------------- 用户 ----------------
    @PostMapping("/register")
    public ApiResponse<RegisterResp> register(@Valid @RequestBody RegisterReq req) {
        RegisterResp resp = authService.registerUser(req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResp> login(@Valid @RequestBody LoginReq req) {
        LoginResp resp = authService.loginUser(req);
        return ApiResponse.ok(resp);
    }

    // ---------------- 管理员 ----------------
    @PostMapping("/admin/register")
    public ApiResponse<RegisterResp> adminRegister(@Valid @RequestBody RegisterReq req) {
        RegisterResp resp = authService.registerAdmin(req);
        return ApiResponse.ok(resp);
    }

    @PostMapping("/admin/login")
    public ApiResponse<LoginResp> adminLogin(@Valid @RequestBody LoginReq req) {
        LoginResp resp = authService.loginAdmin(req);
        return ApiResponse.ok(resp);
    }

    // ---------------- 找回密码 ----------------
    @PostMapping("/forgot")
    public ApiResponse<Void> forgot(@Valid @RequestBody ForgotPasswordReq req) {
        authService.startReset(req.email);
        return ApiResponse.ok(null);
    }

    @PostMapping("/reset")
    public ApiResponse<Void> reset(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req.email, req.code, req.newPassword);
        return ApiResponse.ok(null);
    }

    // ---------------- 注销 ----------------
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Jws<Claims> jws = jwtService.parse(token);
                String jti = jws.getBody().getId();
                Date exp = jws.getBody().getExpiration();
                if (jti != null && exp != null) {
                    long ttl = Math.max(1, (exp.getTime() - System.currentTimeMillis()) / 1000);
                    redis.opsForValue().set("jwt:blacklist:" + jti, "1", Duration.ofSeconds(ttl));
                }
            } catch (Exception ignored) {
            }
        }
        return ApiResponse.ok(null);
    }
}
