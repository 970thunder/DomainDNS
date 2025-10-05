package com.domaindns.admin.service;

import com.domaindns.admin.mapper.GithubTaskMapper;
import com.domaindns.user.mapper.PointsMapper;
import com.domaindns.admin.model.GithubTask;
import com.domaindns.admin.model.UserGithubTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub任务服务类
 */
@Service
public class GithubTaskService {

    private static final Logger logger = LoggerFactory.getLogger(GithubTaskService.class);

    @Autowired
    private GithubTaskMapper githubTaskMapper;

    @Autowired
    private PointsMapper pointsMapper;

    @Autowired
    private GithubApiService githubApiService;

    /**
     * 创建GitHub任务
     */
    @Transactional
    public GithubTask createTask(GithubTask task) {
        task.setStatus("ACTIVE");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        githubTaskMapper.insert(task);
        logger.info("创建GitHub任务成功: {}", task.getTitle());
        return task;
    }

    /**
     * 更新GitHub任务
     */
    @Transactional
    public boolean updateTask(GithubTask task) {
        task.setUpdatedAt(LocalDateTime.now());
        int result = githubTaskMapper.update(task);
        logger.info("更新GitHub任务: {}, 结果: {}", task.getTitle(), result > 0 ? "成功" : "失败");
        return result > 0;
    }

    /**
     * 删除GitHub任务
     */
    @Transactional
    public boolean deleteTask(Long taskId) {
        int result = githubTaskMapper.deleteById(taskId);
        logger.info("删除GitHub任务: {}, 结果: {}", taskId, result > 0 ? "成功" : "失败");
        return result > 0;
    }

    /**
     * 获取所有任务（管理员）
     */
    public List<Map<String, Object>> getAllTasks() {
        return githubTaskMapper.findAllWithCompletionCount();
    }

    /**
     * 获取活跃任务（用户）
     */
    public List<GithubTask> getActiveTasks() {
        return githubTaskMapper.findActiveTasks();
    }

    /**
     * 根据ID获取任务
     */
    public GithubTask getTaskById(Long taskId) {
        return githubTaskMapper.findById(taskId);
    }

    /**
     * 获取用户任务列表
     */
    public List<Map<String, Object>> getUserTasks(Long userId) {
        return githubTaskMapper.findUserTaskDetails(userId);
    }

    /**
     * 验证用户Star状态并奖励积分
     */
    @Transactional
    public Map<String, Object> verifyAndRewardStar(Long userId, Long taskId, String githubUsername) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取任务信息
            GithubTask task = githubTaskMapper.findById(taskId);
            if (task == null) {
                logger.error("任务不存在: {}", taskId);
                result.put("success", false);
                result.put("message", "任务不存在");
                return result;
            }

            // 验证GitHub用户名
            if (!githubApiService.validateGithubUsername(githubUsername)) {
                logger.error("GitHub用户名不存在: {}", githubUsername);
                result.put("success", false);
                result.put("message", "GitHub用户名不存在或无效");
                return result;
            }

            // 检查该GitHub用户名是否已经被其他用户用于完成该任务
            UserGithubTask existingTask = githubTaskMapper.findUserTaskByGithubUsername(githubUsername, taskId);
            if (existingTask != null && !existingTask.getUserId().equals(userId)) {
                logger.info("GitHub用户名 {} 已被其他用户用于完成任务 {}", githubUsername, taskId);
                result.put("success", false);
                result.put("message", "该GitHub用户名已被其他用户使用，每个GitHub用户名只能用于完成一次任务");
                return result;
            }

            // 检查用户是否已经完成过该任务
            UserGithubTask userTask = githubTaskMapper.findUserTask(userId, taskId);

            if (userTask != null && userTask.getIsStarred()) {
                logger.info("用户 {} 已经完成过任务 {}", userId, taskId);
                result.put("success", false);
                result.put("message", "您已经完成过该任务");
                return result;
            }

            // 检查用户是否已Star该仓库
            boolean isStarred = githubApiService.checkUserStarredRepository(
                    githubUsername, task.getRepositoryOwner(), task.getRepositoryName());

            if (!isStarred) {
                logger.info("用户 {} 未Star仓库 {}/{}", githubUsername, task.getRepositoryOwner(), task.getRepositoryName());
                result.put("success", false);
                result.put("message", "请先Star该仓库后再进行验证");
                return result;
            }

            // 创建或更新用户任务记录
            if (userTask == null) {
                userTask = new UserGithubTask(userId, taskId);
                userTask.setGithubUsername(githubUsername);
                userTask.setIsStarred(true);
                userTask.setPointsAwarded(task.getRewardPoints());
                userTask.setCompletedAt(LocalDateTime.now());
                githubTaskMapper.insertUserTask(userTask);
            } else {
                userTask.setGithubUsername(githubUsername);
                userTask.setIsStarred(true);
                userTask.setPointsAwarded(task.getRewardPoints());
                userTask.setCompletedAt(LocalDateTime.now());
                githubTaskMapper.updateUserTask(userTask);
            }

            // 更新用户积分并记录交易
            pointsMapper.adjust(userId, task.getRewardPoints());
            pointsMapper.insertTxn(userId, task.getRewardPoints(), null, "GITHUB_STAR_REWARD",
                    "完成GitHub Star任务: " + task.getTitle(), null);

            logger.info("用户 {} 完成GitHub Star任务 {}，获得 {} 积分", userId, taskId, task.getRewardPoints());
            result.put("success", true);
            result.put("message", "验证成功！您已获得 " + task.getRewardPoints() + " 积分奖励");
            result.put("pointsAwarded", task.getRewardPoints());
            return result;

        } catch (Exception e) {
            logger.error("验证GitHub Star状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "验证失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取用户任务完成记录
     */
    public List<UserGithubTask> getUserTaskRecords(Long userId) {
        return githubTaskMapper.findUserTasks(userId);
    }

    /**
     * 获取用户已完成的任务
     */
    public List<UserGithubTask> getUserCompletedTasks(Long userId) {
        return githubTaskMapper.findUserCompletedTasks(userId);
    }

    /**
     * 统计任务完成情况
     */
    public int getTaskCompletionCount(Long taskId) {
        return githubTaskMapper.countTaskCompletions(taskId);
    }

    /**
     * 检查用户是否已完成指定任务
     */
    public boolean isTaskCompleted(Long userId, Long taskId) {
        UserGithubTask userTask = githubTaskMapper.findUserTask(userId, taskId);
        return userTask != null && userTask.getIsStarred();
    }

    /**
     * 获取用户任务完成状态
     */
    public UserGithubTask getUserTaskStatus(Long userId, Long taskId) {
        return githubTaskMapper.findUserTask(userId, taskId);
    }
}
