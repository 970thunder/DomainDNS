package com.domaindns.user.service;

import com.domaindns.admin.mapper.InviteMapper;
import com.domaindns.admin.model.InviteCode;
import com.domaindns.auth.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserInviteService {
    private final InviteMapper inviteMapper;
    private final UserMapper userMapper;
    private final SecureRandom random = new SecureRandom();

    public UserInviteService(InviteMapper inviteMapper, UserMapper userMapper) {
        this.inviteMapper = inviteMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public Map<String, Object> generateOrResetInviteCode(Long userId, Integer maxUses, Integer validDays) {
        // 检查是否已有邀请码
        InviteCode existing = inviteMapper.findByOwnerUserId(userId);

        String code = generateInviteCode();
        LocalDateTime expiredAt = validDays != null ? LocalDateTime.now().plusDays(validDays) : null;

        if (existing != null) {
            // 重置现有邀请码
            inviteMapper.updateByOwnerUserId(userId, code, maxUses, expiredAt);
        } else {
            // 创建新邀请码
            inviteMapper.insert(code, userId, maxUses, expiredAt);
        }

        // 更新用户表中的 invite_code 字段
        userMapper.updateInviteCode(userId, code);

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("maxUses", maxUses);
        result.put("validDays", validDays);
        result.put("expiredAt", expiredAt);
        return result;
    }

    public Map<String, Object> getMyInviteCode(Long userId) {
        InviteCode invite = inviteMapper.findByOwnerUserId(userId);
        if (invite == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasCode", false);
            return result;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasCode", true);
        result.put("code", invite.getCode());
        result.put("maxUses", invite.getMaxUses());
        result.put("usedCount", invite.getUsedCount());
        result.put("status", invite.getStatus());
        result.put("createdAt", invite.getCreatedAt());
        result.put("expiredAt", invite.getExpiredAt());

        // 计算剩余使用次数
        if (invite.getMaxUses() != null && invite.getMaxUses() > 0) {
            result.put("remainingUses", invite.getMaxUses() - invite.getUsedCount());
        } else {
            result.put("remainingUses", "unlimited");
        }

        return result;
    }

    private String generateInviteCode() {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
