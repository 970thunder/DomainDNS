package com.domaindns.cf.controller;

import com.domaindns.cf.model.Zone;
import com.domaindns.cf.service.ZoneService;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserZoneController {
    private final ZoneService zoneService;

    public UserZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping("/zones")
    public ApiResponse<List<Zone>> listEnabledZones(@RequestParam(value = "name", required = false) String name) {
        return ApiResponse.ok(zoneService.list(1, name, null));
    }

    // 公共搜索：输入子域名前缀，返回所有可用 zone 的可注册完整域名
    @GetMapping("/domains/search")
    public ApiResponse<List<String>> searchDomains(@RequestParam("prefix") String prefix) {
        List<Zone> zones = zoneService.list(1, null, null);
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        if (prefix == null || prefix.isEmpty())
            return ApiResponse.ok(result);
        for (Zone z : zones) {
            result.add(prefix + "." + z.getName());
        }
        return ApiResponse.ok(result);
    }
}
