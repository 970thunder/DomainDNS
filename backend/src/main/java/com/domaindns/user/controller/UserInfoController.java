package com.domaindns.user.controller;

import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import com.domaindns.auth.entity.User;
import com.domaindns.auth.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/info")
public class UserInfoController {
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public UserInfoController(UserMapper userMapper, JwtService jwtService) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorization) {
        long userId = getCurrentUserId(authorization);

        // 从数据库获取用户信息
        User user = userMapper.findById(userId);
        if (user == null) {
            return ApiResponse.error(50000, "用户不存在");
        }

        // 构建返回数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("status", getStatusText(user.getStatus()));

        return ApiResponse.ok(userInfo);
    }

    private long getCurrentUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        Jws<Claims> jws = jwtService.parse(token);
        String role = jws.getBody().get("role", String.class);
        if (!"USER".equals(role)) {
            throw new RuntimeException("权限不足，需要用户权限");
        }
        return Long.parseLong(jws.getBody().getSubject());
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "ACTIVE";
            case 0:
                return "INACTIVE";
            case -1:
                return "BANNED";
            default:
                return "未知";
        }
    }
}
