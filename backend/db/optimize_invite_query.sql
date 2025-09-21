-- 优化邀请奖励查询性能的索引
-- 为 points_transactions 表添加复合索引，优化按用户ID和类型查询的性能

-- 添加复合索引：user_id + type + created_at
-- 这个索引可以高效支持我们的查询条件：WHERE user_id = ? AND type IN ('INVITE_REWARD', 'INVITER_REWARD') ORDER BY created_at DESC
ALTER TABLE points_transactions 
ADD INDEX idx_points_user_type_time (user_id, type, created_at);

-- 为 related_id 添加索引，优化 JOIN 查询
ALTER TABLE points_transactions 
ADD INDEX idx_points_related_id (related_id);

-- 验证索引是否创建成功
SHOW INDEX FROM points_transactions;
