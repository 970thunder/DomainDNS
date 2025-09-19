package com.domaindns.user.controller;

import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import com.domaindns.user.service.UserInviteService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/invite")
public class UserInviteController {
    private final UserInviteService service;
    private final JwtService jwtService;

    public UserInviteController(UserInviteService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
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

    private long currentUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new RuntimeException("未登录");
        String token = authorization.substring(7);
        Jws<Claims> jws = jwtService.parse(token);
        return Long.parseLong(jws.getBody().getSubject());
    }
}
