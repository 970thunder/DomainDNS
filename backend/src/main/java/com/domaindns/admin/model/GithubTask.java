package com.domaindns.admin.model;

import java.time.LocalDateTime;

/**
 * GitHub Star任务实体类
 */
public class GithubTask {
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
    private LocalDateTime updatedAt;
    private Long createdBy;

    // 构造函数
    public GithubTask() {
    }

    public GithubTask(String title, String description, String repositoryUrl,
            String repositoryOwner, String repositoryName, Integer rewardPoints) {
        this.title = title;
        this.description = description;
        this.repositoryUrl = repositoryUrl;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.rewardPoints = rewardPoints;
        this.status = "ACTIVE";
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "GithubTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", repositoryOwner='" + repositoryOwner + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", rewardPoints=" + rewardPoints +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                '}';
    }
}
