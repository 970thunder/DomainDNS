package com.domaindns.user.controller;

import com.domaindns.admin.mapper.OrderMapper;
import com.domaindns.admin.model.PaymentOrder;
import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserPaymentController {
    private final OrderMapper orderMapper;
    private final JwtService jwtService;
    private final SecureRandom random = new SecureRandom();

    public UserPaymentController(OrderMapper orderMapper, JwtService jwtService) {
        this.orderMapper = orderMapper;
        this.jwtService = jwtService;
    }

    @PostMapping("/recharge")
    public ApiResponse<Map<String, Object>> createOrder(@RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> body) {
        long userId = currentUserId(authorization);
        double amount = Double.parseDouble(body.getOrDefault("amount", 0).toString());
        int points = Integer.parseInt(body.getOrDefault("points", 0).toString());
        String provider = String.valueOf(body.getOrDefault("provider", "mock"));
        String orderNo = genOrderNo();
        orderMapper.insert(userId, orderNo, amount, points, provider);
        Map<String, Object> m = new HashMap<>();
        m.put("orderNo", orderNo);
        m.put("payUrl", provider.equals("mock") ? ("http://localhost/mock-pay/" + orderNo) : null);
        return ApiResponse.ok(m);
    }

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> myOrders(@RequestHeader("Authorization") String authorization,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        long userId = currentUserId(authorization);
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<PaymentOrder> list = orderMapper.list(status, userId, offset, size);
        int total = orderMapper.count(status, userId);
        Map<String, Object> m = new HashMap<>();
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

    private String genOrderNo() {
        long ts = System.currentTimeMillis();
        int rnd = 100000 + random.nextInt(900000);
        return "O" + ts + rnd;
    }
}
