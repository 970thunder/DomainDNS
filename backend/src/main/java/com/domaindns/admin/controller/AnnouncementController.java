package com.domaindns.admin.controller;

import com.domaindns.admin.dto.AnnouncementDtos;
import com.domaindns.admin.service.AnnouncementService;
import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;
    private final JwtService jwtService;

    public AnnouncementController(AnnouncementService announcementService, JwtService jwtService) {
        this.announcementService = announcementService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ApiResponse<List<AnnouncementDtos.ListResp>> getAllAnnouncements(
            @RequestHeader("Authorization") String authorization) {
        getCurrentAdminId(authorization);
        List<AnnouncementDtos.ListResp> announcements = announcementService.getAllAnnouncements();
        return ApiResponse.ok(announcements);
    }

    @GetMapping("/published")
    public ApiResponse<List<AnnouncementDtos.ListResp>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "10") int limit) {
        List<AnnouncementDtos.ListResp> announcements = announcementService.getPublishedAnnouncements(limit);
        return ApiResponse.ok(announcements);
    }

    @GetMapping("/{id}")
    public ApiResponse<AnnouncementDtos.DetailResp> getAnnouncementById(@PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        getCurrentAdminId(authorization);
        AnnouncementDtos.DetailResp announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return ApiResponse.error(50000, "公告不存在");
        }
        return ApiResponse.ok(announcement);
    }

    @PostMapping
    public ApiResponse<Long> createAnnouncement(@RequestBody AnnouncementDtos.CreateReq req,
            @RequestHeader("Authorization") String authorization) {
        long adminId = getCurrentAdminId(authorization);
        Long announcementId = announcementService.createAnnouncement(req, adminId);
        return ApiResponse.ok(announcementId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementDtos.UpdateReq req,
            @RequestHeader("Authorization") String authorization) {
        getCurrentAdminId(authorization);
        announcementService.updateAnnouncement(id, req);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        getCurrentAdminId(authorization);
        announcementService.deleteAnnouncement(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishAnnouncement(@PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        getCurrentAdminId(authorization);
        announcementService.publishAnnouncement(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archiveAnnouncement(@PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        getCurrentAdminId(authorization);
        announcementService.archiveAnnouncement(id);
        return ApiResponse.ok(null);
    }

    private long getCurrentAdminId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        Jws<Claims> jws = jwtService.parse(token);
        String role = jws.getBody().get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("权限不足，需要管理员权限");
        }
        return Long.parseLong(jws.getBody().getSubject());
    }
}
