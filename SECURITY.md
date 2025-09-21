# 安全配置说明

## ⚠️ 重要安全提醒

本项目包含敏感信息，请严格按照以下步骤配置，确保不会泄露敏感数据。

## 敏感信息清单

### 1. 数据库配置
- 数据库用户名和密码
- 数据库连接URL

### 2. 邮件配置
- 邮箱账号和密码
- SMTP服务器配置

### 3. 加密密钥
- JWT密钥
- AES加密密钥

### 4. 第三方服务
- GitHub API Token
- Cloudflare API Key

### 5. 其他敏感信息
- 生产环境域名
- 内部服务地址

## 配置步骤

### 1. 后端配置

#### 开发环境
```bash
# 复制配置模板
cp backend/src/main/resources/application-dev.yml.template backend/src/main/resources/application-dev.yml

# 编辑配置文件，填入真实信息
vim backend/src/main/resources/application-dev.yml
```

#### 生产环境
```bash
# 复制配置模板
cp backend/src/main/resources/application-prod.yml.template backend/src/main/resources/application-prod.yml

# 创建环境变量文件
cp env.example .env

# 编辑环境变量文件
vim .env
```

### 2. 前端配置

#### 开发环境
```bash
# 复制环境变量模板
cp UI/env.development.template UI/env.development

# 编辑配置文件
vim UI/env.development
```

#### 生产环境
```bash
# 复制环境变量模板
cp UI/env.production.template UI/env.production

# 编辑配置文件
vim UI/env.production
```

## 安全最佳实践

### 1. 密钥管理
- 使用强密码（至少16位，包含大小写字母、数字、特殊字符）
- JWT密钥至少256位
- AES密钥必须32位
- 定期更换密钥

### 2. 环境隔离
- 开发、测试、生产环境使用不同的配置
- 生产环境使用环境变量，不要硬编码
- 开发环境可以使用配置文件，但不要提交到版本控制

### 3. 访问控制
- 限制数据库访问IP
- 使用防火墙保护服务端口
- 定期更新依赖包

### 4. 监控和审计
- 启用访问日志
- 监控异常登录
- 定期检查安全漏洞

## 部署安全检查清单

- [ ] 所有敏感配置文件已从版本控制中移除
- [ ] 生产环境使用环境变量配置
- [ ] 数据库密码已更换为强密码
- [ ] JWT密钥已更换为随机生成的长密钥
- [ ] AES密钥已更换为32位随机字符串
- [ ] 邮件密码使用应用专用密码
- [ ] GitHub Token权限最小化
- [ ] 防火墙已正确配置
- [ ] SSL证书已正确安装
- [ ] 定期备份已配置

## 紧急情况处理

如果发现敏感信息泄露：

1. **立即更换所有密钥和密码**
2. **检查访问日志，确认是否有异常访问**
3. **通知相关用户更改密码**
4. **更新所有环境配置**
5. **加强监控和审计**

## 联系方式

如有安全问题，请立即联系项目维护者。

---

**记住：安全无小事，配置需谨慎！**
