package com.domaindns.admin.controller;

import com.domaindns.admin.mapper.CardMapper;
import com.domaindns.admin.model.Card;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cards")
public class CardController {
    private final CardMapper mapper;
    private final SecureRandom random = new SecureRandom();

    public CardController(CardMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<Card> list = mapper.list(status, offset, size);
        int total = mapper.count(status);
        Map<String, Object> m = new HashMap<>();
        m.put("list", list);
        m.put("total", total);
        m.put("page", page);
        m.put("size", size);
        return ApiResponse.ok(m);
    }

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(@RequestBody Map<String, Object> body) {
        int count = Integer.parseInt(body.getOrDefault("count", 0).toString());
        int points = Integer.parseInt(body.getOrDefault("points", 0).toString());
        Integer validDays = body.get("validDays") == null ? null : Integer.valueOf(body.get("validDays").toString());
        LocalDateTime expiredAt = validDays == null ? null : LocalDateTime.now().plusDays(validDays);
        int created = 0;
        for (int i = 0; i < count; i++) {
            String code = randomCode(16);
            created += mapper.insert(code, points, expiredAt);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("count", created);
        return ApiResponse.ok(m);
    }

    private String randomCode(int len) {
        final String dict = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            sb.append(dict.charAt(random.nextInt(dict.length())));
        return sb.toString();
    }
}
