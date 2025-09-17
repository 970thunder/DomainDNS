package com.domaindns.admin.controller;

import com.domaindns.admin.mapper.AuditMapper;
import com.domaindns.admin.model.AuditLog;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {
    private final AuditMapper mapper;

    public AuditController(AuditMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<AuditLog> list = mapper.list(action, offset, size);
        int total = mapper.count(action);
        Map<String, Object> m = new HashMap<>();
        m.put("list", list);
        m.put("total", total);
        m.put("page", page);
        m.put("size", size);
        return ApiResponse.ok(m);
    }
}
