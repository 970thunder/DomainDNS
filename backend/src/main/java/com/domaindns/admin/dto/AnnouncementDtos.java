package com.domaindns.admin.dto;

import java.time.LocalDateTime;

public class AnnouncementDtos {

    public static class CreateReq {
        public String title;
        public String content;
        public String status;
        public Integer priority;
    }

    public static class UpdateReq {
        public String title;
        public String content;
        public String status;
        public Integer priority;
    }

    public static class ListResp {
        public Long id;
        public String title;
        public String content;
        public String status;
        public Integer priority;
        public LocalDateTime publishedAt;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public Long createdBy;
        public String createdByUsername;
    }

    public static class DetailResp {
        public Long id;
        public String title;
        public String content;
        public String status;
        public Integer priority;
        public LocalDateTime publishedAt;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public Long createdBy;
        public String createdByUsername;
    }
}
