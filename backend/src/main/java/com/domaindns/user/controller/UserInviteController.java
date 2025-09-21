package com.domaindns.user.controller;

import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import com.domaindns.user.service.UserInviteService;
import com.domaindns.user.mapper.PointsMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/invite")
public class UserInviteController {
    private final UserInviteService service;
    private final JwtService jwtService;
    private final PointsMapper pointsMapper;

    public UserInviteController(UserInviteService service, JwtService jwtService, PointsMapper pointsMapper) {
        this.service = service;
        this.jwtService = jwtService;
        this.pointsMapper = pointsMapper;
    }

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(@RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) Map<String, Object> body) {
        long userId = currentUserId(authorization);

        Integer maxUses = null;
        Integer validDays = null;
        if (body != null) {
            if (body.get("maxUses") != null) {
                maxUses = Integer.valueOf(body.get("maxUses").toString());
            }
            if (body.get("validDays") != null) {
                validDays = Integer.valueOf(body.get("validDays").toString());
            }
        }

        Map<String, Object> result = service.generateOrResetInviteCode(userId, maxUses, validDays);
        return ApiResponse.ok(result);
    }

    @GetMapping("/mycode")
    public ApiResponse<Map<String, Object>> myCode(@RequestHeader("Authorization") String authorization) {
        long userId = currentUserId(authorization);
        Map<String, Object> result = service.getMyInviteCode(userId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/details")
    public ApiResponse<Map<String, Object>> details(@RequestHeader("Authorization") String authorization,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        long userId = currentUserId(authorization);

        // 参数验证
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100); // 限制最大100条
        int offset = (page - 1) * size;

        // 查询邀请奖励记录
        var inviteRewards = pointsMapper.getInviteRewards(userId, offset, size);
        int total = pointsMapper.countInviteRewards(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", inviteRewards);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return ApiResponse.ok(result);
    }

    private long currentUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new RuntimeException("未登录");
        String token = authorization.substring(7);
        Jws<Claims> jws = jwtService.parse(token);
        return Long.parseLong(jws.getBody().getSubject());
    }
}
