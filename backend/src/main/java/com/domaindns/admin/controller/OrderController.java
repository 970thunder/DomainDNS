package com.domaindns.admin.controller;

import com.domaindns.admin.mapper.OrderMapper;
import com.domaindns.admin.model.PaymentOrder;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderController {
    private final OrderMapper mapper;

    public OrderController(OrderMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<PaymentOrder> list = mapper.list(status, userId, offset, size);
        int total = mapper.count(status, userId);
        Map<String, Object> m = new HashMap<>();
        m.put("list", list);
        m.put("total", total);
        m.put("page", page);
        m.put("size", size);
        return ApiResponse.ok(m);
    }
}
