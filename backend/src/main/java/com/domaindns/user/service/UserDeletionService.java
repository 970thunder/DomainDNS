package com.domaindns.user.service;

import com.domaindns.auth.entity.User;
import com.domaindns.auth.mapper.UserMapper;
import com.domaindns.user.mapper.UserDomainMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserDeletionService {
    private final UserMapper userMapper;
    private final UserDomainMapper userDomainMapper;

    public UserDeletionService(UserMapper userMapper, UserDomainMapper userDomainMapper) {
        this.userMapper = userMapper;
        this.userDomainMapper = userDomainMapper;
    }

    /**
     * 检查用户是否可以注销
     */
    public Map<String, Object> checkDeletionEligibility(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("用户状态异常，无法注销");
        }

        // 检查是否有未释放的域名
        int domainCount = userDomainMapper.countByUser(userId);
        List<com.domaindns.user.model.UserDomain> domains = userDomainMapper.listByUser(userId, 0, 100);

        // 获取用户积分
        Integer points = user.getPoints();
        if (points == null) {
            points = 0;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("canDelete", domainCount == 0);
        result.put("domainCount", domainCount);
        result.put("domains", domains);
        result.put("points", points);
        result.put("username", user.getUsername());
        result.put("displayName", user.getDisplayName());

        if (domainCount > 0) {
            result.put("message", "请先释放所有申请的域名才能注销账号");
        } else {
            result.put("message", "可以注销账号，将放弃所有积分");
        }

        return result;
    }

    /**
     * 注销用户账号（软删除）
     */
    @Transactional
    public void deleteUserAccount(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 再次检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("用户状态异常，无法注销");
        }

        // 再次检查是否有未释放的域名
        int domainCount = userDomainMapper.countByUser(userId);
        if (domainCount > 0) {
            throw new IllegalArgumentException("还有未释放的域名，无法注销账号");
        }

        // 软删除：将状态设置为2（已注销）
        int updated = userMapper.updateStatus(userId, 2);
        if (updated == 0) {
            throw new RuntimeException("账号注销失败");
        }

        // 记录注销日志（可选）
        System.out.println("用户账号已注销: " + user.getUsername() + " (ID: " + userId + "), 放弃积分: " + user.getPoints());
    }
}
