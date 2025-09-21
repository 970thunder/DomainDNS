package com.domaindns.user.dto;

import java.time.LocalDateTime;

public class InviteRewardDto {
    private Long id;
    private String username;
    private Integer points;
    private String remark;
    private LocalDateTime createdAt;

    public InviteRewardDto() {
    }

    public InviteRewardDto(Long id, String username, Integer points, String remark, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.points = points;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
