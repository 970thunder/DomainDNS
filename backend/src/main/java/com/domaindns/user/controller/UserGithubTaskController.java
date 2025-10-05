package com.domaindns.user.controller;

import com.domaindns.admin.dto.GithubTaskDtos;
import com.domaindns.admin.model.GithubTask;
import com.domaindns.admin.model.UserGithubTask;
import com.domaindns.admin.service.GithubTaskService;
import com.domaindns.auth.service.JwtService;
import com.domaindns.common.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户GitHub任务控制器
 */
@RestController
@RequestMapping("/api/user/github-tasks")
public class UserGithubTaskController {

    @Autowired
    private GithubTaskService githubTaskService;

    @Autowired
    private JwtService jwtService;

    /**
     * 获取用户可参与的任务列表
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getUserTasks(
            @RequestHeader("Authorization") String authorization) {
        try {
            long userId = getCurrentUserId(authorization);

            List<Map<String, Object>> tasks = githubTaskService.getUserTasks(userId);
            return ApiResponse.ok(tasks);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取任务详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getTaskDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        try {
            long userId = getCurrentUserId(authorization);

            GithubTask task = githubTaskService.getTaskById(id);
            if (task == null) {
                return ApiResponse.error(50000, "任务不存在");
            }

            // 检查用户是否已完成该任务
            boolean isCompleted = githubTaskService.isTaskCompleted(userId, id);
            UserGithubTask userTask = githubTaskService.getUserTaskStatus(userId, id);

            Map<String, Object> result = new HashMap<>();
            result.put("task", task);
            result.put("isCompleted", isCompleted);
            result.put("userTask", userTask);

            return ApiResponse.ok(result);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 验证Star状态并领取奖励
     */
    @PostMapping("/{id}/verify")
    public ApiResponse<Map<String, Object>> verifyStar(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody GithubTaskDtos.VerifyStarReq request) {
        try {
            long userId = getCurrentUserId(authorization);

            // 验证请求参数
            if (request.getGithubUsername() == null || request.getGithubUsername().trim().isEmpty()) {
                return ApiResponse.error(50000, "GitHub用户名不能为空");
            }

            // 验证并奖励
            Map<String, Object> result = githubTaskService.verifyAndRewardStar(
                    userId, id, request.getGithubUsername().trim());

            return ApiResponse.ok(result);

        } catch (Exception e) {
            return ApiResponse.error(50000, "验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户任务完成记录
     */
    @GetMapping("/records")
    public ApiResponse<List<UserGithubTask>> getUserTaskRecords(
            @RequestHeader("Authorization") String authorization) {
        try {
            long userId = getCurrentUserId(authorization);

            List<UserGithubTask> records = githubTaskService.getUserTaskRecords(userId);
            return ApiResponse.ok(records);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取任务记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户已完成的任务
     */
    @GetMapping("/completed")
    public ApiResponse<List<UserGithubTask>> getUserCompletedTasks(
            @RequestHeader("Authorization") String authorization) {
        try {
            long userId = getCurrentUserId(authorization);

            List<UserGithubTask> completedTasks = githubTaskService.getUserCompletedTasks(userId);
            return ApiResponse.ok(completedTasks);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取已完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户任务统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getUserTaskStats(
            @RequestHeader("Authorization") String authorization) {
        try {
            long userId = getCurrentUserId(authorization);

            List<UserGithubTask> allRecords = githubTaskService.getUserTaskRecords(userId);
            List<UserGithubTask> completedTasks = githubTaskService.getUserCompletedTasks(userId);

            int totalTasks = allRecords.size();
            int completedCount = completedTasks.size();
            int totalPointsEarned = completedTasks.stream()
                    .mapToInt(UserGithubTask::getPointsAwarded)
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTasks", totalTasks);
            stats.put("completedCount", completedCount);
            stats.put("totalPointsEarned", totalPointsEarned);
            stats.put("completionRate", totalTasks > 0 ? (double) completedCount / totalTasks * 100 : 0);

            return ApiResponse.ok(stats);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取任务统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID
     */
    private long getCurrentUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        Jws<Claims> jws = jwtService.parse(token);
        String role = jws.getBody().get("role", String.class);
        if (!"USER".equals(role)) {
            throw new RuntimeException("权限不足，需要用户权限");
        }
        return Long.parseLong(jws.getBody().getSubject());
    }
}
