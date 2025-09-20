package com.domaindns.settings;

import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class SettingsController {
    private final SettingsService service;
    private final JwtService jwtService;

    public SettingsController(SettingsService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ApiResponse<Map<String, String>> get(@RequestHeader("Authorization") String authorization) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        return ApiResponse.ok(service.getAll());
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> body) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        service.update(body);
        return ApiResponse.ok(null);
    }

    // 验证管理员权限
    private void validateAdminAuth(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        try {
            Jws<Claims> jws = jwtService.parse(token);
            String role = jws.getBody().get("role", String.class);
            if (!"ADMIN".equals(role)) {
                throw new RuntimeException("权限不足，需要管理员权限");
            }
        } catch (Exception e) {
            throw new RuntimeException("Token无效或已过期");
        }
    }
}