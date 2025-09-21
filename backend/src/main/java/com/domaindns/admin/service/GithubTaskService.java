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
    public boolean verifyAndRewardStar(Long userId, Long taskId, String githubUsername) {
        try {
            // 获取任务信息
            GithubTask task = githubTaskMapper.findById(taskId);
            if (task == null) {
                logger.error("任务不存在: {}", taskId);
                return false;
            }

            // 验证GitHub用户名
            if (!githubApiService.validateGithubUsername(githubUsername)) {
                logger.error("GitHub用户名不存在: {}", githubUsername);
                return false;
            }

            // 检查用户是否已Star该仓库
            boolean isStarred = githubApiService.checkUserStarredRepository(
                    githubUsername, task.getRepositoryOwner(), task.getRepositoryName());

            if (!isStarred) {
                logger.info("用户 {} 未Star仓库 {}/{}", githubUsername, task.getRepositoryOwner(), task.getRepositoryName());
                return false;
            }

            // 检查用户是否已经完成过该任务
            UserGithubTask userTask = githubTaskMapper.findUserTask(userId, taskId);

            if (userTask != null && userTask.getIsStarred()) {
                logger.info("用户 {} 已经完成过任务 {}", userId, taskId);
                return false;
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
            return true;

        } catch (Exception e) {
            logger.error("验证GitHub Star状态失败: {}", e.getMessage(), e);
            return false;
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
