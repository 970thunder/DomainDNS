package com.domaindns.settings;

import com.domaindns.common.ApiResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class SettingsController {
    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Map<String, String>> get() {
        return ApiResponse.ok(service.getAll());
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody Map<String, String> body) {
        service.update(body);
        return ApiResponse.ok(null);
    }
}
