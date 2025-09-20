package com.domaindns.admin.controller;

import com.domaindns.admin.mapper.AdminUserMapper;
import com.domaindns.admin.model.AdminUser;
import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import com.domaindns.user.mapper.PointsMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserMapper mapper;
    private final PointsMapper pointsMapper;
    private final JwtService jwtService;

    public AdminUserController(AdminUserMapper mapper, PointsMapper pointsMapper, JwtService jwtService) {
        this.mapper = mapper;
        this.pointsMapper = pointsMapper;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestHeader("Authorization") String authorization,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        // 验证管理员权限
        validateAdminAuth(authorization);

        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<AdminUser> list = mapper.list(status, role, offset, size);
        int total = mapper.count(status, role);
        Map<String, Object> m = new HashMap<>();
        m.put("list", list);
        m.put("total", total);
        m.put("page", page);
        m.put("size", size);
        return ApiResponse.ok(m);
    }

    @PostMapping("/{id}/points")
    public ApiResponse<Map<String, Object>> adjust(@RequestHeader("Authorization") String authorization,
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        // 验证管理员权限
        validateAdminAuth(authorization);

        Integer delta = (Integer) body.getOrDefault("delta", 0);
        String remark = (String) body.getOrDefault("remark", "管理员调整积分");

        // 获取用户当前积分
        AdminUser user = mapper.findById(id);
        if (user == null) {
            return ApiResponse.error(40001, "用户不存在");
        }

        Integer currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        Integer newPoints = currentPoints + delta;

        // 更新用户积分
        int rows = mapper.adjustPoints(id, delta);
        if (rows == 0) {
            return ApiResponse.error(50000, "更新失败");
        }

        // 记录积分交易
        pointsMapper.insertTxn(
                id, // userId
                delta, // changeAmount
                newPoints, // balanceAfter
                "ADMIN_ADJUST", // type
                remark, // remark
                null // relatedId
        );

        Map<String, Object> m = new HashMap<>();
        m.put("updated", rows);
        m.put("oldPoints", currentPoints);
        m.put("newPoints", newPoints);
        m.put("delta", delta);
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
