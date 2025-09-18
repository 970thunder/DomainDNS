package com.domaindns.user.controller;

import com.domaindns.auth.entity.User;
import com.domaindns.auth.mapper.UserMapper;
import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import com.domaindns.user.mapper.PointsMapper;
import com.domaindns.user.model.PointsTransaction;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserPointsController {
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PointsMapper pointsMapper;

    public UserPointsController(JwtService jwtService, UserMapper userMapper, PointsMapper pointsMapper) {
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.pointsMapper = pointsMapper;
    }

    @GetMapping("/points")
    public ApiResponse<Map<String, Object>> points(@RequestHeader("Authorization") String authorization,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        long userId = currentUserId(authorization);
        User u = userMapper.findById(userId);
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<PointsTransaction> list = pointsMapper.list(userId, offset, size);
        int total = pointsMapper.count(userId);
        Map<String, Object> m = new HashMap<>();
        m.put("balance", u == null ? 0 : (u.getPoints() == null ? 0 : u.getPoints()));
        m.put("list", list);
        m.put("total", total);
        m.put("page", page);
        m.put("size", size);
        return ApiResponse.ok(m);
    }

    private long currentUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new RuntimeException("未登录");
        String token = authorization.substring(7);
        Jws<Claims> jws = jwtService.parse(token);
        return Long.parseLong(jws.getBody().getSubject());
    }
}
