package com.domaindns.admin.controller;

import com.domaindns.admin.dto.GithubTaskDtos;
import com.domaindns.admin.model.GithubTask;
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
 * 管理员GitHub任务控制器
 */
@RestController
@RequestMapping("/api/admin/github-tasks")
public class GithubTaskController {

    @Autowired
    private GithubTaskService githubTaskService;

    @Autowired
    private JwtService jwtService;

    /**
     * 创建GitHub任务
     */
    @PostMapping
    public ApiResponse<GithubTask> createTask(
            @RequestHeader("Authorization") String authorization,
            @RequestBody GithubTaskDtos.CreateReq request) {
        try {
            long adminId = getCurrentAdminId(authorization);

            GithubTask task = new GithubTask();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setRepositoryUrl(request.getRepositoryUrl());
            task.setRepositoryOwner(request.getRepositoryOwner());
            task.setRepositoryName(request.getRepositoryName());
            task.setRewardPoints(request.getRewardPoints());
            task.setStartTime(request.getStartTime());
            task.setEndTime(request.getEndTime());
            task.setCreatedBy(adminId);

            GithubTask createdTask = githubTaskService.createTask(task);
            return ApiResponse.ok(createdTask);

        } catch (Exception e) {
            return ApiResponse.error(50000, "创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新GitHub任务
     */
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateTask(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody GithubTaskDtos.UpdateReq request) {
        try {
            getCurrentAdminId(authorization);

            GithubTask task = githubTaskService.getTaskById(id);
            if (task == null) {
                return ApiResponse.error(50000, "任务不存在");
            }

            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setRepositoryUrl(request.getRepositoryUrl());
            task.setRepositoryOwner(request.getRepositoryOwner());
            task.setRepositoryName(request.getRepositoryName());
            task.setRewardPoints(request.getRewardPoints());
            task.setStatus(request.getStatus());
            task.setStartTime(request.getStartTime());
            task.setEndTime(request.getEndTime());

            boolean success = githubTaskService.updateTask(task);
            return ApiResponse.ok(success);

        } catch (Exception e) {
            return ApiResponse.error(50000, "更新任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除GitHub任务
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteTask(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        try {
            getCurrentAdminId(authorization);

            boolean success = githubTaskService.deleteTask(id);
            return ApiResponse.ok(success);

        } catch (Exception e) {
            return ApiResponse.error(50000, "删除任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有任务（管理员）
     */
    @GetMapping
    public ApiResponse<List<GithubTask>> getAllTasks(
            @RequestHeader("Authorization") String authorization) {
        try {
            getCurrentAdminId(authorization);

            List<GithubTask> tasks = githubTaskService.getAllTasks();
            return ApiResponse.ok(tasks);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取任务
     */
    @GetMapping("/{id}")
    public ApiResponse<GithubTask> getTaskById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        try {
            getCurrentAdminId(authorization);

            GithubTask task = githubTaskService.getTaskById(id);
            if (task == null) {
                return ApiResponse.error(50000, "任务不存在");
            }

            return ApiResponse.ok(task);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务完成统计
     */
    @GetMapping("/{id}/stats")
    public ApiResponse<Map<String, Object>> getTaskStats(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        try {
            getCurrentAdminId(authorization);

            GithubTask task = githubTaskService.getTaskById(id);
            if (task == null) {
                return ApiResponse.error(50000, "任务不存在");
            }

            int completionCount = githubTaskService.getTaskCompletionCount(id);

            Map<String, Object> stats = new HashMap<>();
            stats.put("taskId", id);
            stats.put("taskTitle", task.getTitle());
            stats.put("completionCount", completionCount);
            stats.put("rewardPoints", task.getRewardPoints());

            return ApiResponse.ok(stats);

        } catch (Exception e) {
            return ApiResponse.error(50000, "获取任务统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前管理员ID
     */
    private long getCurrentAdminId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        Jws<Claims> jws = jwtService.parse(token);
        String role = jws.getBody().get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("权限不足，需要管理员权限");
        }
        return Long.parseLong(jws.getBody().getSubject());
    }
}
