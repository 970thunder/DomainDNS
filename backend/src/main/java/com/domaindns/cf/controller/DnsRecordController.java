package com.domaindns.cf.controller;

import com.domaindns.cf.model.DnsRecord;
import com.domaindns.cf.service.DnsRecordService;
import com.domaindns.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/zones/{zoneId}")
public class DnsRecordController {
    private final DnsRecordService service;

    public DnsRecordController(DnsRecordService service) {
        this.service = service;
    }

    @PostMapping("/sync-records")
    public ApiResponse<Void> sync(@PathVariable("zoneId") Long zoneDbId) {
        service.syncZoneRecords(zoneDbId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/records")
    public ApiResponse<List<DnsRecord>> list(@PathVariable("zoneId") Long zoneDbId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "name", required = false) String name) {
        return ApiResponse.ok(service.list(zoneDbId, type, name));
    }

    @PostMapping("/records")
    public ApiResponse<Void> create(@PathVariable("zoneId") Long zoneDbId, @RequestBody String body) throws Exception {
        service.create(zoneDbId, body);
        return ApiResponse.ok(null);
    }

    @PutMapping("/records/{recordId}")
    public ApiResponse<Void> update(@PathVariable("zoneId") Long zoneDbId, @PathVariable String recordId,
            @RequestBody String body) throws Exception {
        service.update(zoneDbId, recordId, body);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/records/{recordId}")
    public ApiResponse<Void> delete(@PathVariable("zoneId") Long zoneDbId, @PathVariable String recordId)
            throws Exception {
        service.delete(zoneDbId, recordId);
        return ApiResponse.ok(null);
    }
}
