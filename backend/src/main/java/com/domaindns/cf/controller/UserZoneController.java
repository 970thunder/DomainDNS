package com.domaindns.cf.controller;

import com.domaindns.cf.model.Zone;
import com.domaindns.cf.service.ZoneService;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
