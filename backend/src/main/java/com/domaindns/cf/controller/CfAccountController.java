package com.domaindns.cf.controller;

import com.domaindns.cf.dto.CfAccountDtos.CreateReq;
import com.domaindns.cf.dto.CfAccountDtos.ItemResp;
import com.domaindns.cf.dto.CfAccountDtos.UpdateReq;
import com.domaindns.cf.service.CfAccountService;
import com.domaindns.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cf-accounts")
public class CfAccountController {
    private final CfAccountService service;

    public CfAccountController(CfAccountService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody CreateReq body) {
        Long id = service.create(body);
        Map<String, Long> m = new HashMap<>();
        m.put("id", id);
        return ApiResponse.ok(m);
    }

    @GetMapping
    public ApiResponse<List<ItemResp>> list(@RequestParam(value = "enabled", required = false) Integer enabled) {
        return ApiResponse.ok(service.list(enabled));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody UpdateReq body) {
        service.update(id, body);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        service.setEnabled(id, true);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        service.setEnabled(id, false);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Map<String, Object>> test(@PathVariable Long id) {
        boolean ok = service.test(id);
        Map<String, Object> m = new HashMap<>();
        m.put("ok", ok);
        m.put("message", ok ? "ok" : "failed");
        return ApiResponse.ok(m);
    }
}
