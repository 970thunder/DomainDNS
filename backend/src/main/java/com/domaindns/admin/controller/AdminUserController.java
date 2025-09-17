package com.domaindns.admin.controller;

import com.domaindns.admin.mapper.AdminUserMapper;
import com.domaindns.admin.model.AdminUser;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserMapper mapper;

    public AdminUserController(AdminUserMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
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
    public ApiResponse<Map<String, Object>> adjust(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer delta = (Integer) body.getOrDefault("delta", 0);
        int rows = mapper.adjustPoints(id, delta);
        Map<String, Object> m = new HashMap<>();
        m.put("updated", rows);
        return ApiResponse.ok(m);
    }
}
