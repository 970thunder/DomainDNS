package com.domaindns.cf.controller;

import com.domaindns.auth.service.JwtService;
import com.domaindns.cf.model.Zone;
import com.domaindns.cf.service.ZoneService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/zones")
public class ZoneController {
    private final ZoneService service;
    private final JwtService jwtService;

    public ZoneController(ZoneService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping("/sync")
    public ApiResponse<Map<String, Integer>> sync(@RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) Map<String, Long> body) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        Long accId = body != null ? body.get("cfAccountId") : null;
        int saved = (accId == null) ? service.syncAll() : service.syncByAccount(accId);
        Map<String, Integer> m = new HashMap<>();
        m.put("saved", saved);
        return ApiResponse.ok(m);
    }

    @GetMapping
    public ApiResponse<List<Zone>> list(@RequestHeader("Authorization") String authorization,
            @RequestParam(value = "enabled", required = false) Integer enabled,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "cfAccountId", required = false) Long cfAccountId) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        return ApiResponse.ok(service.list(enabled, name, cfAccountId));
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        service.setEnabled(id, true);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        service.setEnabled(id, false);
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
