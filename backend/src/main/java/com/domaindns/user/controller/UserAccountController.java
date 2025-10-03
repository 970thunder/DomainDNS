package com.domaindns.user.controller;

import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import com.domaindns.user.service.UserDeletionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/account")
public class UserAccountController {
    private final UserDeletionService userDeletionService;
    private final JwtService jwtService;

    public UserAccountController(UserDeletionService userDeletionService, JwtService jwtService) {
        this.userDeletionService = userDeletionService;
        this.jwtService = jwtService;
    }

    @GetMapping("/deletion/check")
    public ApiResponse<Map<String, Object>> checkDeletionEligibility(
            @RequestHeader("Authorization") String authorization) {
        long userId = getCurrentUserId(authorization);
        return ApiResponse.ok(userDeletionService.checkDeletionEligibility(userId));
    }

    @PostMapping("/deletion")
    public ApiResponse<Map<String, Object>> deleteAccount(@RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> body) {
        long userId = getCurrentUserId(authorization);

        Boolean confirmDeletion = (Boolean) body.get("confirmDeletion");
        Boolean confirmPointsLoss = (Boolean) body.get("confirmPointsLoss");

        if (confirmDeletion == null || !confirmDeletion) {
            return ApiResponse.error(40001, "请确认注销操作");
        }

        if (confirmPointsLoss == null || !confirmPointsLoss) {
            return ApiResponse.error(40002, "请确认放弃积分");
        }

        userDeletionService.deleteUserAccount(userId);

        Map<String, Object> result = Map.of(
                "message", "账号注销成功",
                "deletedAt", java.time.LocalDateTime.now());

        return ApiResponse.ok(result);
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
}
