package com.domaindns.admin.dto;

import java.time.LocalDateTime;

/**
 * GitHub任务相关DTO类
 */
public class GithubTaskDtos {

    /**
     * 创建GitHub任务请求
     */
    public static class CreateReq {
        private String title;
        private String description;
        private String repositoryUrl;
        private String repositoryOwner;
        private String repositoryName;
        private Integer rewardPoints;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        // 构造函数
        public CreateReq() {
        }

        // Getter和Setter方法
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public void setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
        }

        public String getRepositoryOwner() {
            return repositoryOwner;
        }

        public void setRepositoryOwner(String repositoryOwner) {
            this.repositoryOwner = repositoryOwner;
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public Integer getRewardPoints() {
            return rewardPoints;
        }

        public void setRewardPoints(Integer rewardPoints) {
            this.rewardPoints = rewardPoints;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
    }

    /**
     * 更新GitHub任务请求
     */
    public static class UpdateReq {
        private String title;
        private String description;
        private String repositoryUrl;
        private String repositoryOwner;
        private String repositoryName;
        private Integer rewardPoints;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        // 构造函数
        public UpdateReq() {
        }

        // Getter和Setter方法
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public void setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
        }

        public String getRepositoryOwner() {
            return repositoryOwner;
        }

        public void setRepositoryOwner(String repositoryOwner) {
            this.repositoryOwner = repositoryOwner;
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public Integer getRewardPoints() {
            return rewardPoints;
        }

        public void setRewardPoints(Integer rewardPoints) {
            this.rewardPoints = rewardPoints;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
    }

    /**
     * GitHub任务列表响应
     */
    public static class ListResp {
        private Long id;
        private String title;
        private String description;
        private String repositoryUrl;
        private String repositoryOwner;
        private String repositoryName;
        private Integer rewardPoints;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime createdAt;
        private Boolean isCompleted;
        private Boolean isStarred;
        private Integer pointsAwarded;

        // 构造函数
        public ListResp() {
        }

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRepositoryUrl() {
            return repositoryUrl;
        }

        public void setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
        }

        public String getRepositoryOwner() {
            return repositoryOwner;
        }

        public void setRepositoryOwner(String repositoryOwner) {
            this.repositoryOwner = repositoryOwner;
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public Integer getRewardPoints() {
            return rewardPoints;
        }

        public void setRewardPoints(Integer rewardPoints) {
            this.rewardPoints = rewardPoints;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public Boolean getIsCompleted() {
            return isCompleted;
        }

        public void setIsCompleted(Boolean isCompleted) {
            this.isCompleted = isCompleted;
        }

        public Boolean getIsStarred() {
            return isStarred;
        }

        public void setIsStarred(Boolean isStarred) {
            this.isStarred = isStarred;
        }

        public Integer getPointsAwarded() {
            return pointsAwarded;
        }

        public void setPointsAwarded(Integer pointsAwarded) {
            this.pointsAwarded = pointsAwarded;
        }
    }

    /**
     * 验证Star状态请求
     */
    public static class VerifyStarReq {
        private Long taskId;
        private String githubUsername;

        // 构造函数
        public VerifyStarReq() {
        }

        public VerifyStarReq(Long taskId, String githubUsername) {
            this.taskId = taskId;
            this.githubUsername = githubUsername;
        }

        // Getter和Setter方法
        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public String getGithubUsername() {
            return githubUsername;
        }

        public void setGithubUsername(String githubUsername) {
            this.githubUsername = githubUsername;
        }
    }
}
