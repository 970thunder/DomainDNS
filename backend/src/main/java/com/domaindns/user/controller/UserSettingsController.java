package com.domaindns.user.controller;

import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import com.domaindns.settings.SettingsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户系统设置控制器
 * 提供用户可访问的系统设置信息
 */
@RestController
@RequestMapping("/api/user/settings")
public class UserSettingsController {
    private final SettingsService settingsService;
    private final JwtService jwtService;

    public UserSettingsController(SettingsService settingsService, JwtService jwtService) {
        this.settingsService = settingsService;
        this.jwtService = jwtService;
    }

    /**
     * 获取用户可访问的系统设置
     * 只返回用户需要知道的设置，不包含敏感信息
     */
    @GetMapping
    public ApiResponse<Map<String, String>> getUserSettings(@RequestHeader("Authorization") String authorization) {
        // 验证用户权限
        validateUserAuth(authorization);

        // 获取所有设置
        Map<String, String> allSettings = settingsService.getAll();

        // 只返回用户需要知道的设置
        Map<String, String> userSettings = new java.util.HashMap<>();

        // 积分相关设置
        if (allSettings.containsKey("domain_cost_points")) {
            userSettings.put("domain_cost_points", allSettings.get("domain_cost_points"));
        }
        if (allSettings.containsKey("default_ttl")) {
            userSettings.put("default_ttl", allSettings.get("default_ttl"));
        }
        if (allSettings.containsKey("max_domains_per_user")) {
            userSettings.put("max_domains_per_user", allSettings.get("max_domains_per_user"));
        }

        // 注册奖励设置（用户可能想了解）
        if (allSettings.containsKey("initial_register_points")) {
            userSettings.put("initial_register_points", allSettings.get("initial_register_points"));
        }
        if (allSettings.containsKey("invitee_points")) {
            userSettings.put("invitee_points", allSettings.get("invitee_points"));
        }
        if (allSettings.containsKey("inviter_points")) {
            userSettings.put("inviter_points", allSettings.get("inviter_points"));
        }

        return ApiResponse.ok(userSettings);
    }

    /**
     * 验证用户权限
     */
    private void validateUserAuth(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        try {
            Jws<Claims> jws = jwtService.parse(token);
            String role = jws.getBody().get("role", String.class);
            if (!"USER".equals(role)) {
                throw new RuntimeException("权限不足，需要用户权限");
            }
        } catch (Exception e) {
            throw new RuntimeException("Token无效或已过期");
        }
    }
}
