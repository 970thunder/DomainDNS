package com.domaindns.admin.service;

import com.domaindns.admin.dto.AnnouncementDtos;
import com.domaindns.admin.mapper.AnnouncementMapper;
import com.domaindns.admin.model.Announcement;
import com.domaindns.auth.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {
    private final AnnouncementMapper announcementMapper;
    private final UserMapper userMapper;

    public AnnouncementService(AnnouncementMapper announcementMapper, UserMapper userMapper) {
        this.announcementMapper = announcementMapper;
        this.userMapper = userMapper;
    }

    public List<AnnouncementDtos.ListResp> getAllAnnouncements() {
        List<Announcement> announcements = announcementMapper.findAll();
        return announcements.stream().map(this::convertToListResp).collect(Collectors.toList());
    }

    public List<AnnouncementDtos.ListResp> getPublishedAnnouncements(int limit) {
        List<Announcement> announcements = announcementMapper.findPublished(limit);
        return announcements.stream().map(this::convertToListResp).collect(Collectors.toList());
    }

    public AnnouncementDtos.DetailResp getAnnouncementById(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            return null;
        }
        return convertToDetailResp(announcement);
    }

    public Long createAnnouncement(AnnouncementDtos.CreateReq req, Long createdBy) {
        Announcement announcement = new Announcement();
        announcement.setTitle(req.title);
        announcement.setContent(req.content);
        announcement.setStatus(req.status != null ? req.status : "DRAFT");
        announcement.setPriority(req.priority != null ? req.priority : 1);
        announcement.setCreatedBy(createdBy);

        if ("PUBLISHED".equals(announcement.getStatus())) {
            announcement.setPublishedAt(LocalDateTime.now());
        }

        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    public void updateAnnouncement(Long id, AnnouncementDtos.UpdateReq req) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new IllegalArgumentException("公告不存在");
        }

        announcement.setTitle(req.title);
        announcement.setContent(req.content);
        announcement.setStatus(req.status);
        announcement.setPriority(req.priority);

        if ("PUBLISHED".equals(announcement.getStatus()) && announcement.getPublishedAt() == null) {
            announcement.setPublishedAt(LocalDateTime.now());
        }

        announcementMapper.update(announcement);
    }

    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    public void publishAnnouncement(Long id) {
        announcementMapper.updateStatus(id, "PUBLISHED", LocalDateTime.now());
    }

    public void archiveAnnouncement(Long id) {
        announcementMapper.updateStatus(id, "ARCHIVED", null);
    }

    private AnnouncementDtos.ListResp convertToListResp(Announcement announcement) {
        AnnouncementDtos.ListResp resp = new AnnouncementDtos.ListResp();
        resp.id = announcement.getId();
        resp.title = announcement.getTitle();
        resp.content = announcement.getContent();
        resp.status = announcement.getStatus();
        resp.priority = announcement.getPriority();
        resp.publishedAt = announcement.getPublishedAt();
        resp.createdAt = announcement.getCreatedAt();
        resp.updatedAt = announcement.getUpdatedAt();
        resp.createdBy = announcement.getCreatedBy();

        // 获取创建者用户名
        if (announcement.getCreatedBy() != null) {
            var user = userMapper.findById(announcement.getCreatedBy());
            resp.createdByUsername = user != null ? user.getUsername() : "未知用户";
        }

        return resp;
    }

    private AnnouncementDtos.DetailResp convertToDetailResp(Announcement announcement) {
        AnnouncementDtos.DetailResp resp = new AnnouncementDtos.DetailResp();
        resp.id = announcement.getId();
        resp.title = announcement.getTitle();
        resp.content = announcement.getContent();
        resp.status = announcement.getStatus();
        resp.priority = announcement.getPriority();
        resp.publishedAt = announcement.getPublishedAt();
        resp.createdAt = announcement.getCreatedAt();
        resp.updatedAt = announcement.getUpdatedAt();
        resp.createdBy = announcement.getCreatedBy();

        // 获取创建者用户名
        if (announcement.getCreatedBy() != null) {
            var user = userMapper.findById(announcement.getCreatedBy());
            resp.createdByUsername = user != null ? user.getUsername() : "未知用户";
        }

        return resp;
    }
}
