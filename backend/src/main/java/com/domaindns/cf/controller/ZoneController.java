package com.domaindns.cf.controller;

import com.domaindns.cf.model.Zone;
import com.domaindns.cf.service.ZoneService;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/zones")
public class ZoneController {
    private final ZoneService service;

    public ZoneController(ZoneService service) {
        this.service = service;
    }

    @PostMapping("/sync")
    public ApiResponse<Map<String, Integer>> sync(@RequestBody(required = false) Map<String, Long> body) {
        Long accId = body != null ? body.get("cfAccountId") : null;
        int saved = (accId == null) ? service.syncAll() : service.syncByAccount(accId);
        Map<String, Integer> m = new HashMap<>();
        m.put("saved", saved);
        return ApiResponse.ok(m);
    }

    @GetMapping
    public ApiResponse<List<Zone>> list(@RequestParam(value = "enabled", required = false) Integer enabled,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "cfAccountId", required = false) Long cfAccountId) {
        return ApiResponse.ok(service.list(enabled, name, cfAccountId));
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
}
