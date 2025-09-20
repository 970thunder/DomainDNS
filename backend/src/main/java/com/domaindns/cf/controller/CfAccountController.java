package com.domaindns.cf.controller;

import com.domaindns.auth.service.JwtService;
import com.domaindns.cf.dto.CfAccountDtos.CreateReq;
import com.domaindns.cf.dto.CfAccountDtos.ItemResp;
import com.domaindns.cf.dto.CfAccountDtos.UpdateReq;
import com.domaindns.cf.service.CfAccountService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cf-accounts")
public class CfAccountController {
    private final CfAccountService service;
    private final JwtService jwtService;

    public CfAccountController(CfAccountService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> create(@RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateReq body) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        Long id = service.create(body);
        Map<String, Long> m = new HashMap<>();
        m.put("id", id);
        return ApiResponse.ok(m);
    }

    @GetMapping
    public ApiResponse<List<ItemResp>> list(@RequestHeader("Authorization") String authorization,
            @RequestParam(value = "enabled", required = false) Integer enabled) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        return ApiResponse.ok(service.list(enabled));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@RequestHeader("Authorization") String authorization,
            @PathVariable Long id, @RequestBody UpdateReq body) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        service.update(id, body);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        service.delete(id);
        return ApiResponse.ok(null);
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

    @PostMapping("/{id}/test")
    public ApiResponse<Map<String, Object>> test(@RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        boolean ok = service.test(id);
        Map<String, Object> m = new HashMap<>();
        m.put("ok", ok);
        m.put("message", ok ? "ok" : "failed");
        return ApiResponse.ok(m);
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
