-- DomainDNS MySQL 数据库结构
-- 引擎: InnoDB, 字符集: utf8mb4
-- 适配 MySQL 8.0+

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- 如需创建独立数据库可取消注释
CREATE DATABASE IF NOT EXISTS `domaindns` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `domaindns`;

-- ------------------------------------------------------------
-- 表：users（用户）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(50) NOT NULL UNIQUE,
	email VARCHAR(100) UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	display_name VARCHAR(100),
	invite_code VARCHAR(32) UNIQUE,
	inviter_id BIGINT NULL,
	points INT DEFAULT 0,
	role ENUM('USER','ADMIN') DEFAULT 'USER',
	status TINYINT DEFAULT 1,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	INDEX idx_users_inviter_id (inviter_id),
	CONSTRAINT fk_users_inviter FOREIGN KEY (inviter_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 表：system_settings（系统设置：键值对）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_settings (
	k VARCHAR(100) PRIMARY KEY,
	v TEXT,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 可选：预置常用配置键（仅示例）
-- INSERT INTO system_settings(k, v) VALUES
-- ('initial_register_points','1'),('invitee_points','3'),('inviter_points','3'),('domain_cost_points','5'),
-- ('sync_cron_expression','0 */5 * * * *'),('default_ttl','120'),('max_domains_per_user','5'),('allow_user_set_invite','1');

-- ------------------------------------------------------------
-- 表：cf_accounts（Cloudflare 账户）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cf_accounts (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(100),
	email VARCHAR(100),
	api_key VARCHAR(255),
	api_type ENUM('GLOBAL_KEY','API_TOKEN') DEFAULT 'GLOBAL_KEY',
	enabled TINYINT DEFAULT 1,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 表：zones（Cloudflare 区域/域名）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS zones (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	cf_account_id BIGINT,
	zone_id VARCHAR(128) NOT NULL,
	name VARCHAR(255) NOT NULL,
	status VARCHAR(50),
	enabled TINYINT DEFAULT 0,
	synced_at TIMESTAMP NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	UNIQUE KEY uk_zones_account_zone (cf_account_id, zone_id),
	INDEX idx_zones_name (name),
	CONSTRAINT fk_zones_cf_account FOREIGN KEY (cf_account_id) REFERENCES cf_accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 表：dns_records（DNS 记录，本地镜像/存储）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dns_records (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	zone_id BIGINT,
	cf_record_id VARCHAR(128),
	name VARCHAR(255),
	type VARCHAR(10),
	content VARCHAR(255),
	ttl INT,
	proxied TINYINT DEFAULT 0,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	UNIQUE KEY uk_dns_records_zone_cf (zone_id, cf_record_id),
	INDEX idx_dns_records_zone (zone_id),
	INDEX idx_dns_records_name (name),
	CONSTRAINT fk_dns_records_zone FOREIGN KEY (zone_id) REFERENCES zones(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 表：user_domains（用户申请的子域名）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_domains (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT,
	zone_id BIGINT,
	dns_record_id BIGINT,
	subdomain_prefix VARCHAR(128),
	full_domain VARCHAR(255),
	status ENUM('ACTIVE','PENDING','REVOKED') DEFAULT 'ACTIVE',
	remark VARCHAR(255),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	UNIQUE KEY uk_user_domains_user_domain (user_id, full_domain),
	INDEX idx_user_domains_user (user_id),
	INDEX idx_user_domains_zone (zone_id),
	INDEX idx_user_domains_status (status),
	CONSTRAINT fk_user_domains_user FOREIGN KEY (user_id) REFERENCES users(id),
	CONSTRAINT fk_user_domains_zone FOREIGN KEY (zone_id) REFERENCES zones(id),
	CONSTRAINT fk_user_domains_dns_record FOREIGN KEY (dns_record_id) REFERENCES dns_records(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 表：points_transactions（积分流水/总账）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS points_transactions (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT,
	change_amount INT,
	balance_after INT,
	type VARCHAR(50),
	remark VARCHAR(255),
	related_id BIGINT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	INDEX idx_points_user_time (user_id, created_at),
	INDEX idx_points_type (type),
	CONSTRAINT fk_points_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 表：audit_logs（审计日志：管理员/用户操作）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	actor_id BIGINT,
	action VARCHAR(100),
	target VARCHAR(100),
	detail TEXT,
	ip VARCHAR(50),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	INDEX idx_audit_actor_time (actor_id, created_at),
	INDEX idx_audit_action (action),
	CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 扩展表：invite_codes（邀请码，可由用户/管理员管理）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invite_codes (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	code VARCHAR(64) NOT NULL UNIQUE,
	owner_user_id BIGINT NOT NULL,
	max_uses INT DEFAULT 0, -- 0 表示不限制
	used_count INT DEFAULT 0,
	status ENUM('ACTIVE','DISABLED','EXPIRED') DEFAULT 'ACTIVE',
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	expired_at TIMESTAMP NULL,
	INDEX idx_invite_owner (owner_user_id),
	CONSTRAINT fk_invite_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 扩展表：payment_orders（充值/支付订单，兑换积分）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_orders (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT NOT NULL,
	order_no VARCHAR(64) NOT NULL UNIQUE,
	amount_decimal DECIMAL(12,2) NOT NULL,
	points INT NOT NULL,
	status ENUM('PENDING','PAID','CANCELLED','FAILED') DEFAULT 'PENDING',
	provider VARCHAR(50),
	provider_txn_id VARCHAR(128),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	INDEX idx_payment_user_time (user_id, created_at),
	CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 扩展表：sync_jobs（同步任务记录，Zones/Records）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sync_jobs (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	zone_id BIGINT,
	job_type ENUM('ZONES','DNS_RECORDS') NOT NULL,
	status ENUM('PENDING','RUNNING','SUCCESS','FAILED') DEFAULT 'PENDING',
	message VARCHAR(500),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	INDEX idx_sync_zone_time (zone_id, created_at),
	INDEX idx_sync_status (status),
	CONSTRAINT fk_sync_zone FOREIGN KEY (zone_id) REFERENCES zones(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 扩展表：rate_limit（限流表：按 IP/用户维度）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rate_limit (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	key_name VARCHAR(128) NOT NULL,
	bucket VARCHAR(64) NOT NULL,
	counter INT NOT NULL,
	window_start TIMESTAMP NOT NULL,
	UNIQUE KEY uk_rate_key_bucket (key_name, bucket)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 可选：常用统计视图（仅示例，如不需要可注释）
-- ------------------------------------------------------------
-- CREATE VIEW v_zone_record_counts AS
-- SELECT z.id AS zone_id, z.name AS zone_name, COUNT(r.id) AS record_count
-- FROM zones z LEFT JOIN dns_records r ON r.zone_id = z.id
-- GROUP BY z.id, z.name;

-- CREATE VIEW v_user_active_7d AS
-- SELECT DISTINCT u.id AS user_id
-- FROM users u
-- JOIN audit_logs a ON a.actor_id = u.id
-- WHERE a.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY);

-- ------------------------------------------------------------
-- 可选：预置管理员账号（仅示例，password_hash 请在应用中正确生成）
-- ------------------------------------------------------------
-- INSERT INTO users(username, email, password_hash, role, status, points)
-- VALUES ('admin','admin@example.com','$2y$10$REPLACE_WITH_BCRYPT_HASH','ADMIN',1,0);
