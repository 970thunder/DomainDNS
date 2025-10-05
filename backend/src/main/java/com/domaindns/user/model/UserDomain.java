package com.domaindns.user.model;

import java.time.LocalDateTime;

public class UserDomain {
    private Long id;
    private Long userId;
    private Long zoneId;
    private Long dnsRecordId; // local dns_records.id
    private String subdomainPrefix;
    private String fullDomain;
    private String status;
    private String remark;
    private String recordType;
    private String recordValue;
    private Integer recordTtl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public Long getDnsRecordId() {
        return dnsRecordId;
    }

    public void setDnsRecordId(Long dnsRecordId) {
        this.dnsRecordId = dnsRecordId;
    }

    public String getSubdomainPrefix() {
        return subdomainPrefix;
    }

    public void setSubdomainPrefix(String subdomainPrefix) {
        this.subdomainPrefix = subdomainPrefix;
    }

    public String getFullDomain() {
        return fullDomain;
    }

    public void setFullDomain(String fullDomain) {
        this.fullDomain = fullDomain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getRecordValue() {
        return recordValue;
    }

    public void setRecordValue(String recordValue) {
        this.recordValue = recordValue;
    }

    public Integer getRecordTtl() {
        return recordTtl;
    }

    public void setRecordTtl(Integer recordTtl) {
        this.recordTtl = recordTtl;
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
}
