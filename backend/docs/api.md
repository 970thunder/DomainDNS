## DomainDNS 后端 API 文档（v1）

说明：所有接口返回统一结构：

```json
{ "code": 0, "message": "ok", "data": { ... } }
```

- **code**：0 表示成功，其他为错误码
- **鉴权**：除公开接口外，均需携带 `Authorization: Bearer <JWT>`
- **环境**：
  - 开发环境：`spring.profiles.active=dev`，连接本地 MySQL/Redis，允许调试日志
  - 生产环境：`spring.profiles.active=prod`，严格 CORS、限流、错误隐藏、连接生产依赖
- **Redis**：用于验证码、限流与 JWT 黑名单

### 目录
- 1. 公共/认证
- 2. 管理员（ADMIN）
- 3. 用户（USER）
- 4. 统一错误码
- 5. 环境与安全说明
- 6. Postman 测试样例（含 curl）

---

### 1. 公共/认证

#### 1.0 发送注册验证码（邮件）
- 方法：POST `/api/auth/register/send-code`
- 说明：向邮箱发送 6 位注册验证码，10 分钟有效；同邮箱 1 分钟最多 3 次

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| email | string | 是 | 收件邮箱 |

响应：`{ code:0 }`

错误：`请求过于频繁，请稍后再试`

#### 1.1 注册（用户，需邮箱验证码）
- 方法：POST `/api/auth/register`
- 说明：创建新用户，需先通过 1.0 获取邮箱验证码

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名（唯一） |
| email | string | 是 | 邮箱（唯一） |
| password | string | 是 | 密码（明文，后端存储哈希） |
| emailCode | string | 是 | 邮箱验证码（6 位） |
| inviteCode | string | 否 | 邀请码（有效则给邀请双方加积分） |

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": { "userId": 1 } }
```

#### 1.2 登录（用户）
- 方法：POST `/api/auth/login`
- 说明：用户名或邮箱 + 密码登录，返回 JWT

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 否 | 用户名（username/email 至少一项） |
| email | string | 否 | 邮箱 |
| password | string | 是 | 密码 |

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": { "token": "<JWT>", "role": "USER" } }
```

#### 1.3 注销（JWT 黑名单）
- 方法：POST `/api/auth/logout`
- 说明：后端解析当前 JWT，将其 `jti` 写入 Redis 黑名单，TTL 与 token 过期一致

响应：`{ code:0 }`

#### 1.4 找回密码 - 发送验证码（限流）
- 方法：POST `/api/auth/forgot`
- 说明：向邮箱发送 6 位验证码，默认 10 分钟有效；同邮箱 1 分钟最多 3 次

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| email | string | 是 | 收件邮箱（需已注册） |

响应：`{ code:0 }`

错误：`请求过于频繁，请稍后再试`、`邮箱不存在`

#### 1.5 找回密码 - 重置
- 方法：POST `/api/auth/reset`
- 说明：校验验证码并更新新密码

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| email | string | 是 | 邮箱 |
| code | string | 是 | 6 位验证码 |
| newPassword | string | 是 | 新密码 |

响应：`{ code:0 }`

---

### 2. 管理员（ADMIN）
需要 `role=ADMIN`。

#### 2.1 管理员注册（仅首次启动允许）
- 方法：POST `/api/auth/admin/register`
- 说明：系统内无任何 ADMIN 时允许注册；一旦存在管理员，接口拒绝并提示使用登录或邮箱找回

请求参数（同用户注册，但无需 emailCode）：`{ username,email,password }`

响应（成功）：`{ code:0, data:{ userId } }`

错误：`管理员已存在，禁止再次注册，请使用账号密码登录或通过邮箱找回密码`

#### 2.2 管理员登录
- 方法：POST `/api/auth/admin/login`
- 说明：返回 `role=ADMIN` 的 JWT

请求参数：同 1.2

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": { "token": "<JWT>", "role": "ADMIN" } }
```

#### 2.3 Cloudflare 账户
- 新增：POST `/api/admin/cf-accounts`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 否 | 账户名称 |
| email | string | 否 | 账户邮箱 |
| apiType | enum | 否 | GLOBAL_KEY / API_TOKEN |
| apiKey | string | 是 | API 凭证（加密存储） |
| enabled | boolean | 否 | 是否启用 |

- 列表：GET `/api/admin/cf-accounts`
- 启用/禁用：POST `/api/admin/cf-accounts/{id}/enable` `{enabled:boolean}`

#### 2.4 Zones 管理
- 手动同步：POST `/api/admin/zones/sync`
- 列表：GET `/api/admin/zones`
- 启用/禁用分发：POST `/api/admin/zones/{id}/enable` `{enabled:boolean}`

#### 2.5 DNS 记录
- 同步某 zone 记录：POST `/api/admin/zones/{zoneId}/sync-records`
- 列表：GET `/api/admin/zones/{zoneId}/records`

#### 2.6 系统设置
- 获取：GET `/api/admin/settings`
- 更新：PUT `/api/admin/settings`

请求参数（示例字段）：

| 字段 | 类型 | 说明 |
|---|---|---|
| initial_register_points | int | 注册赠送积分 |
| invitee_points | int | 被邀请人奖励积分 |
| inviter_points | int | 邀请人奖励积分 |
| domain_cost_points | int | 申请域名消耗积分 |
| max_domains_per_user | int | 单用户域名上限 |
| default_ttl | int | 默认 TTL |
| sync_cron_expression | string | 同步 cron |

#### 2.7 用户与审计
- 用户列表：GET `/api/admin/users?page=&size=&status=&role=`
- 积分调整：POST `/api/admin/users/{id}/points` `{delta:int, remark:string}`
- 审计日志：GET `/api/admin/audit?page=&size=&action=`

#### 2.8 邀请/卡密/支付
- 邀请码：GET `/api/admin/invites`、POST `/api/admin/invites` 生成
- 卡密：GET `/api/admin/cards`、POST `/api/admin/cards/generate`
- 订单：GET `/api/admin/orders`

---

### 3. 用户（USER）

#### 3.1 可分发域名
- GET `/api/zones`：返回已启用分发的 zone 列表

#### 3.2 申请子域名
- POST `/api/user/domains/apply`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| zoneId | long | 是 | 目标 zone ID |
| prefix | string | 是 | 子域名前缀 |
| type | string | 是 | 记录类型（A/AAAA/CNAME/TXT...） |
| value | string | 是 | 记录值（IP/域名/文本） |
| ttl | int | 否 | TTL（默认系统设置） |
| remark | string | 否 | 备注 |

响应（同步成功示例）：

```json
{ "code": 0, "message": "ok", "data": { "domainId": 1001, "status": "ACTIVE" } }
```

#### 3.3 我的域名
- GET `/api/user/domains`：分页查询
- DELETE `/api/user/domains/{id}`：释放子域名

#### 3.4 积分
- GET `/api/user/points`：余额 + 流水

#### 3.5 邀请
- POST `/api/user/invite/generate`：生成/重置邀请码
- GET `/api/user/invite/mycode`：查看我的邀请码与使用情况

#### 3.6 充值
- POST `/api/user/recharge`：创建订单（返回支付链接/二维码）
- GET `/api/user/orders`：查看我的订单

---

### 4. 统一错误码

| code | 说明 |
|---|---|
| 0 | 成功 |
| 40001 | 参数校验失败/频率限制触发/验证码错误 |
| 40101 | 未登录或 token 失效（JWT 黑名单） |
| 40301 | 权限不足（需要 ADMIN） |
| 40901 | 资源冲突（如域名已存在/管理员已存在/邮箱已存在） |
| 42901 | 访问过于频繁（限流） |
| 50000 | 系统内部错误 |

---

### 5. 环境与安全说明
- dev：开启 Swagger（可选）、调试日志、CF API 使用沙箱或 Mock、Redis 本地
- prod：关闭详细错误、开启速率限制/审计、加密存储敏感信息（CF Key）、Redis 高可用
- CORS：按域名白名单配置
- 幂等：外部调用与扣积分流程应具备幂等保护
- 审计：重要操作写入 `audit_logs`
- Redis 使用：
  - 注册验证码键：`regcode:<email>`（TTL 10 分钟）
  - 找回密码验证码键：`pwdreset:<email>`（TTL 10 分钟）
  - JWT 黑名单键：`jwt:blacklist:<jti>`（TTL=token 剩余过期时间）
  - 限流键：`rl:regcode:<email>`、`rl:reset:<email>`（固定窗口，1 分钟 3 次）

---

### 6. Postman 测试样例（含 curl）

准备：
- 基础地址：`http://localhost:8080`
- Content-Type：`application/json`
- 登录后将返回的 `token` 放到 Postman 的 `Authorization: Bearer <token>`

1) 发送注册验证码
```bash
curl -X POST http://localhost:8080/api/auth/register/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```

2) 用户注册（带邮箱验证码）
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"User@123","emailCode":"123456"}'
```

3) 用户登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"User@123"}'
```

4) 找回密码-发送验证码（限流）
```bash
curl -X POST http://localhost:8080/api/auth/forgot \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```

5) 找回密码-重置
```bash
curl -X POST http://localhost:8080/api/auth/reset \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","code":"123456","newPassword":"User@456"}'
```

6) 管理员首次注册（仅当系统无 ADMIN）
```bash
curl -X POST http://localhost:8080/api/auth/admin/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"Admin@123"}'
```

7) 管理员登录
```bash
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

8) 注销（写入 JWT 黑名单）
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <粘贴你的token>"
```
