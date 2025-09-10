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

### 目录
- 1. 公共/认证
- 2. 管理员（ADMIN）
- 3. 用户（USER）
- 4. 统一错误码
- 5. 环境与安全说明

---

### 1. 公共/认证

#### 1.1 注册
- 方法：POST `/api/auth/register`
- 说明：创建新用户，支持邀请码积分奖励

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名（唯一） |
| email | string | 否 | 邮箱（唯一，建议必填） |
| password | string | 是 | 密码（明文，后端存储哈希） |
| inviteCode | string | 否 | 邀请码（有效则给邀请双方加积分） |

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": { "userId": 1 } }
```

#### 1.2 登录
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

#### 1.3 注销
- 方法：POST `/api/auth/logout`
- 说明：前端删除 token；可选在服务端加入 token 黑名单

---

### 2. 管理员（ADMIN）
需要 `role=ADMIN`。

#### 2.1 Cloudflare 账户
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

#### 2.2 Zones 管理
- 手动同步：POST `/api/admin/zones/sync`
- 列表：GET `/api/admin/zones`
- 启用/禁用分发：POST `/api/admin/zones/{id}/enable` `{enabled:boolean}`

#### 2.3 DNS 记录
- 同步某 zone 记录：POST `/api/admin/zones/{zoneId}/sync-records`
- 列表：GET `/api/admin/zones/{zoneId}/records`

#### 2.4 系统设置
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

#### 2.5 用户与审计
- 用户列表：GET `/api/admin/users?page=&size=&status=&role=`
- 积分调整：POST `/api/admin/users/{id}/points` `{delta:int, remark:string}`
- 审计日志：GET `/api/admin/audit?page=&size=&action=`

#### 2.6 邀请/卡密/支付
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
| 40001 | 参数校验失败 |
| 40101 | 未登录或 token 失效 |
| 40301 | 权限不足（需要 ADMIN） |
| 40901 | 资源冲突（如域名已存在） |
| 42901 | 访问过于频繁（限流） |
| 50000 | 系统内部错误 |

---

### 5. 环境与安全说明
- dev：开启 Swagger、调试日志、CF API 使用沙箱或 Mock
- prod：关闭详细错误、开启速率限制/审计、加密存储敏感信息（CF Key）
- 跨域：严格按域名白名单配置
- 幂等：外部调用与扣积分流程应具备幂等保护
- 审计：重要操作写入 `audit_logs`，便于追踪
