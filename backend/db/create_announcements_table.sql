-- 创建公告表
-- 执行时间：2025-01-20

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content LONGTEXT NOT NULL COMMENT '公告内容（支持HTML格式）',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT' COMMENT '状态：草稿、已发布、已归档',
    priority TINYINT DEFAULT 1 COMMENT '优先级：1-普通，2-重要，3-紧急',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    INDEX idx_announcements_status (status),
    INDEX idx_announcements_published (published_at),
    INDEX idx_announcements_created_by (created_by),
    CONSTRAINT fk_announcements_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';
