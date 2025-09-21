package com.domaindns.admin.model;

import java.time.LocalDateTime;

/**
 * 用户GitHub任务完成记录实体类
 */
public class UserGithubTask {
    private Long id;
    private Long userId;
    private Long taskId;
    private String githubUsername;
    private Boolean isStarred;
    private Integer pointsAwarded;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public UserGithubTask() {
    }

    public UserGithubTask(Long userId, Long taskId) {
        this.userId = userId;
        this.taskId = taskId;
        this.isStarred = false;
        this.pointsAwarded = 0;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserGithubTask{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskId=" + taskId +
                ", githubUsername='" + githubUsername + '\'' +
                ", isStarred=" + isStarred +
                ", pointsAwarded=" + pointsAwarded +
                ", completedAt=" + completedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
