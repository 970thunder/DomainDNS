-- 插入测试公告数据
-- 执行时间：2025-01-20

-- 插入测试公告（假设管理员用户ID为1）
INSERT INTO announcements (title, content, status, priority, published_at, created_by) VALUES
('欢迎使用 HyperNym 域名服务', 
'<h2>欢迎使用 HyperNym 域名服务！</h2><p>感谢您选择我们的服务。以下是使用指南：</p><ul><li><strong>申请域名</strong>：在"申请域名"页面可以申请子域名</li><li><strong>管理域名</strong>：在"我的域名"页面管理已申请的域名</li><li><strong>邀请好友</strong>：使用邀请码邀请好友，双方都能获得积分奖励</li><li><strong>积分充值</strong>：在"充值"页面可以购买积分</li></ul><p>如有任何问题，请联系客服。</p>', 
'PUBLISHED', 2, NOW(), 1),

('系统维护通知', 
'<h3>系统维护通知</h3><p>系统将于 <strong>今晚 23:00-01:00</strong> 进行例行维护，期间服务可能短暂中断。</p><p>维护内容：</p><ol><li>数据库优化</li><li>服务器升级</li><li>安全补丁更新</li></ol><p>感谢您的理解与支持！</p>', 
'PUBLISHED', 3, NOW(), 1),

('新功能上线', 
'<h3>🎉 新功能上线</h3><p>我们很高兴地宣布，以下新功能已经上线：</p><ul><li>📱 <strong>移动端优化</strong>：更好的移动端体验</li><li>🔔 <strong>公告系统</strong>：重要通知及时推送</li><li>📊 <strong>数据统计</strong>：更详细的使用统计</li></ul><p>快去体验这些新功能吧！</p>', 
'PUBLISHED', 1, NOW(), 1);
