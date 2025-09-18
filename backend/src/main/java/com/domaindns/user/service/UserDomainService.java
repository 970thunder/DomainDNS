package com.domaindns.user.service;

import com.domaindns.cf.mapper.ZoneMapper;
import com.domaindns.cf.model.Zone;
import com.domaindns.cf.service.DnsRecordService;
import com.domaindns.cf.mapper.DnsRecordMapper;
import com.domaindns.auth.mapper.UserMapper;
import com.domaindns.auth.entity.User;
import com.domaindns.user.mapper.PointsMapper;
import com.domaindns.user.mapper.UserDomainMapper;
import com.domaindns.settings.SettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

@Service
public class UserDomainService {
    private final ZoneMapper zoneMapper;
    private final PointsMapper pointsMapper;
    private final UserDomainMapper userDomainMapper;
    private final SettingsService settingsService;
    private final UserMapper userMapper;
    private final DnsRecordService dnsRecordService;
    private final DnsRecordMapper dnsRecordMapper;

    public UserDomainService(ZoneMapper zoneMapper, PointsMapper pointsMapper, UserDomainMapper userDomainMapper,
            SettingsService settingsService, UserMapper userMapper, DnsRecordService dnsRecordService,
            DnsRecordMapper dnsRecordMapper) {
        this.zoneMapper = zoneMapper;
        this.pointsMapper = pointsMapper;
        this.userDomainMapper = userDomainMapper;
        this.settingsService = settingsService;
        this.userMapper = userMapper;
        this.dnsRecordService = dnsRecordService;
        this.dnsRecordMapper = dnsRecordMapper;
    }

    @Transactional
    public void applySubdomain(Long userId, Object zoneIdOrKey, String prefix, String type, String value, Integer ttl,
            String remark) {
        Zone z = resolveZone(zoneIdOrKey);
        if (z == null || z.getEnabled() == null || z.getEnabled() == 0)
            throw new IllegalArgumentException("zone 不可用");

        int baseCost = getBaseCost();
        double multiplier = tldMultiplier(z.getName());
        int cost = (int) Math.ceil(baseCost * multiplier);

        User u = userMapper.findById(userId);
        if (u == null)
            throw new IllegalArgumentException("用户不存在");
        if (u.getPoints() == null || u.getPoints() < cost)
            throw new IllegalStateException("积分不足");

        // 先在 Cloudflare 创建 DNS 记录，成功后再扣积分和落库
        int ttlToUse = ttl != null ? ttl : getDefaultTtl();
        validateRecord(type, value);
        String bodyJson = buildCfRecordJson(prefix + "." + z.getName(), type, value, ttlToUse);
        String fullDomain = prefix + "." + z.getName();

        // 前置重复校验：同用户是否已申请过该域名
        if (userDomainMapper.countByUserAndDomain(userId, fullDomain) > 0)
            throw new IllegalArgumentException("你已申请过该子域名");
        // Cloudflare 侧是否已有记录（本地镜像）
        if (dnsRecordMapper.countByZoneAndName(z.getId(), fullDomain) > 0)
            throw new IllegalArgumentException("该子域名已被占用");
        try {
            dnsRecordService.create(z.getId(), bodyJson);
        } catch (Exception e) {
            throw new IllegalStateException("创建 DNS 记录失败: " + e.getMessage());
        }

        userDomainMapper.insert(userId, z.getId(), null, prefix, fullDomain, remark);

        // 扣积分并记录流水
        pointsMapper.adjust(userId, -cost);
        pointsMapper.insertTxn(userId, -cost, null, "DOMAIN_APPLY",
                "申请域名 " + fullDomain + " 扣除 " + cost + " 积分", null);
    }

    private Zone resolveZone(Object zoneIdOrKey) {
        if (zoneIdOrKey == null)
            return null;
        try {
            Long id = Long.valueOf(zoneIdOrKey.toString());
            return zoneMapper.findById(id);
        } catch (NumberFormatException ignore) {
        }
        String s = zoneIdOrKey.toString();
        Zone z = zoneMapper.findByCfZoneId(s);
        if (z != null)
            return z;
        return zoneMapper.findByName(s);
    }

    private int getBaseCost() {
        Map<String, String> all = settingsService.getAll();
        String v = all.getOrDefault("domain_cost_points", "10");
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 10;
        }
    }

    private double tldMultiplier(String zoneName) {
        if (zoneName == null)
            return 1.0;
        String lower = zoneName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".cn") || lower.endsWith(".com"))
            return 2.0;
        if (lower.endsWith(".top"))
            return 1.5;
        return 1.0;
    }

    private String buildCfRecordJson(String name, String type, String value, Integer ttl) {
        StringBuilder sb = new StringBuilder();
        sb.append('{')
                .append("\"type\":\"").append(type).append("\",")
                .append("\"name\":\"").append(name).append("\",")
                .append("\"content\":\"").append(value).append("\",")
                .append("\"ttl\":").append(ttl).append(",")
                .append("\"proxied\":false");
        sb.append('}');
        return sb.toString();
    }

    private int getDefaultTtl() {
        Map<String, String> all = settingsService.getAll();
        String v = all.getOrDefault("default_ttl", "120");
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 120;
        }
    }

    private void validateRecord(String type, String value) {
        if (type == null || type.isEmpty())
            throw new IllegalArgumentException("记录类型不能为空");
        String t = type.toUpperCase(Locale.ROOT);
        if (t.equals("A")) {
            if (!value.matches("^(?:\\d{1,3}\\.){3}\\d{1,3}$"))
                throw new IllegalArgumentException("A 记录需要 IPv4 地址");
        } else if (t.equals("AAAA")) {
            if (!value.contains(":"))
                throw new IllegalArgumentException("AAAA 记录需要 IPv6 地址");
        } else if (t.equals("CNAME") || t.equals("TXT")) {
            // 基础校验略过，可按需增强
        }
    }
}
