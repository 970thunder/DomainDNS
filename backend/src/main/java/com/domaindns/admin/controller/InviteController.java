package com.domaindns.admin.controller;

import com.domaindns.admin.mapper.InviteMapper;
import com.domaindns.admin.model.InviteCode;
import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/invites")
public class InviteController {
    private final InviteMapper mapper;
    private final SecureRandom random = new SecureRandom();
    private final JwtService jwtService;

    public InviteController(InviteMapper mapper, JwtService jwtService) {
        this.mapper = mapper;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestHeader("Authorization") String authorization,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "ownerUserId", required = false) Long ownerUserId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<InviteCode> list = mapper.list(keyword, ownerUserId, offset, size);
        int total = mapper.count(keyword, ownerUserId);
        Map<String, Object> m = new HashMap<>();
        m.put("list", list);
        m.put("total", total);
        m.put("page", page);
        m.put("size", size);
        return ApiResponse.ok(m);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> generate(@RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> body) {
        // 验证管理员权限
        validateAdminAuth(authorization);
        Long ownerUserId = body.get("ownerUserId") == null ? null : Long.valueOf(body.get("ownerUserId").toString());
        Integer maxUses = body.get("maxUses") == null ? 0 : Integer.valueOf(body.get("maxUses").toString());
        Integer validDays = body.get("validDays") == null ? null : Integer.valueOf(body.get("validDays").toString());
        LocalDateTime expiredAt = validDays == null ? null : LocalDateTime.now().plusDays(validDays);
        String code = randomCode(8);
        mapper.insert(code, ownerUserId, maxUses, expiredAt);
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        return ApiResponse.ok(m);
    }

    private String randomCode(int len) {
        final String dict = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            sb.append(dict.charAt(random.nextInt(dict.length())));
        return sb.toString();
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
