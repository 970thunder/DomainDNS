package com.domaindns.admin.mapper;

import com.domaindns.admin.model.GithubTask;
import com.domaindns.admin.model.UserGithubTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * GitHub任务Mapper接口
 */
@Mapper
public interface GithubTaskMapper {

    /**
     * 创建GitHub任务
     */
    int insert(GithubTask task);

    /**
     * 根据ID查询任务
     */
    GithubTask findById(@Param("id") Long id);

    /**
     * 查询所有任务（管理员）
     */
    List<GithubTask> findAll();

    /**
     * 查询所有任务（管理员，包含完成数）
     */
    List<Map<String, Object>> findAllWithCompletionCount();

    /**
     * 查询活跃任务（用户）
     */
    List<GithubTask> findActiveTasks();

    /**
     * 更新任务
     */
    int update(GithubTask task);

    /**
     * 删除任务
     */
    int deleteById(@Param("id") Long id);

    /**
     * 创建用户任务记录
     */
    int insertUserTask(UserGithubTask userTask);

    /**
     * 查询用户任务记录
     */
    UserGithubTask findUserTask(@Param("userId") Long userId, @Param("taskId") Long taskId);

    /**
     * 更新用户任务记录
     */
    int updateUserTask(UserGithubTask userTask);

    /**
     * 查询用户所有任务记录
     */
    List<UserGithubTask> findUserTasks(@Param("userId") Long userId);

    /**
     * 查询用户已完成的任务
     */
    List<UserGithubTask> findUserCompletedTasks(@Param("userId") Long userId);

    /**
     * 统计任务完成情况
     */
    int countTaskCompletions(@Param("taskId") Long taskId);

    /**
     * 查询用户任务详情（包含任务信息）
     */
    List<Map<String, Object>> findUserTaskDetails(@Param("userId") Long userId);

    /**
     * 根据GitHub用户名和任务ID查询用户任务记录
     */
    UserGithubTask findUserTaskByGithubUsername(@Param("githubUsername") String githubUsername,
            @Param("taskId") Long taskId);
}
