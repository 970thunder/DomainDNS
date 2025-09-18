package com.domaindns.cf.controller;

import com.domaindns.cf.mapper.DnsRecordMapper;
import com.domaindns.cf.model.Zone;
import com.domaindns.cf.service.ZoneService;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserZoneController {
    private final ZoneService zoneService;
    private final DnsRecordMapper dnsRecordMapper;

    public UserZoneController(ZoneService zoneService, DnsRecordMapper dnsRecordMapper) {
        this.zoneService = zoneService;
        this.dnsRecordMapper = dnsRecordMapper;
    }

    @GetMapping("/zones")
    public ApiResponse<List<Zone>> listEnabledZones(@RequestParam(value = "name", required = false) String name) {
        return ApiResponse.ok(zoneService.list(1, name, null));
    }

    // 公共搜索：输入子域名前缀，返回所有可用 zone 的可注册完整域名
    @GetMapping("/domains/search")
    public ApiResponse<List<Map<String, Object>>> searchDomains(@RequestParam("prefix") String prefix) {
        List<Zone> zones = zoneService.list(1, null, null);
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        if (prefix == null || prefix.isEmpty())
            return ApiResponse.ok(result);
        for (Zone z : zones) {
            String full = prefix + "." + z.getName();
            boolean exists = dnsRecordMapper.countByZoneAndName(z.getId(), full) > 0;
            Map<String, Object> item = new HashMap<>();
            item.put("domain", full);
            item.put("available", !exists);
            if (exists)
                item.put("reason", "occupied");
            result.add(item);
        }
        return ApiResponse.ok(result);
    }
}
