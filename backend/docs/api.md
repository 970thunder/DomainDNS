## 一、DomainDNS 后端 API 文档（v1）

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
- 7. 通用约定（Header/分页/排序/错误示例/权限矩阵/变更记录）

---

### 1. 公共/认证

#### 1.0 发送注册验证码（邮件）✅
- 方法：POST `/api/auth/register/send-code`
- 说明：向邮箱发送 6 位注册验证码，10 分钟有效；同邮箱 1 分钟最多 3 次

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| email | string | 是 | 收件邮箱 |

响应：`{ code:0 }`

错误：`请求过于频繁，请稍后再试`

#### 1.1 注册（用户，需邮箱验证码）✅
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

积分奖励规则：
- **首次注册**：赠送 `initial_register_points` 积分（默认 5 分）
- **使用邀请码**：被邀请人额外获得 `invitee_points` 积分（默认 3 分），邀请人获得 `inviter_points` 积分（默认 3 分）
- **邀请码校验**：验证邀请码有效性、状态、过期时间、使用次数限制
- **积分流水**：所有积分变动都会记录在 `points_transactions` 表中

错误示例：
```json
{ "code": 40001, "message": "邀请码无效或已失效" }
{ "code": 40001, "message": "邀请码已过期" }
{ "code": 40001, "message": "邀请码使用次数已达上限" }
```

#### 1.2 登录（用户）✅
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

#### 1.3 注销（JWT 黑名单）✅
- 方法：POST `/api/auth/logout`
- 说明：后端解析当前 JWT，将其 `jti` 写入 Redis 黑名单，TTL 与 token 过期一致

响应：`{ code:0 }`

#### 1.4 找回密码 - 发送验证码（限流）✅
- 方法：POST `/api/auth/forgot`
- 说明：向邮箱发送 6 位验证码，默认 10 分钟有效；同邮箱 1 分钟最多 3 次

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| email | string | 是 | 收件邮箱（需已注册） |

响应：`{ code:0 }`

错误：`请求过于频繁，请稍后再试`、`邮箱不存在`

#### 1.5 找回密码 - 重置✅
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

#### 2.1 管理员注册（仅首次启动允许）✅
- 方法：POST `/api/auth/admin/register`
- 说明：系统内无任何 ADMIN 时允许注册；一旦存在管理员，接口拒绝并提示使用登录或邮箱找回

请求参数（同用户注册，但无需 emailCode）：`{ username,email,password }`

响应（成功）：`{ code:0, data:{ userId } }`

错误：`管理员已存在，禁止再次注册，请使用账号密码登录或通过邮箱找回密码`

#### 2.2 管理员登录✅
- 方法：POST `/api/auth/admin/login`
- 说明：返回 `role=ADMIN` 的 JWT

请求参数：同 1.2

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": { "token": "<JWT>", "role": "ADMIN" } }
```

#### 2.3 Cloudflare 账户✅
- 新增：POST `/api/admin/cf-accounts`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 否 | 账户名称 |
| email | string | 否 | 账户邮箱 |
| apiType | enum | 否 | GLOBAL_KEY / API_TOKEN（默认 GLOBAL_KEY） |
| apiKey | string | 是 | API 凭证（加密存储，不回显） |
| enabled | boolean | 否 | 是否启用 |

- 列表：GET `/api/admin/cf-accounts`✅
- 更新：PUT `/api/admin/cf-accounts/{id}`（字段可选传，`apiKey` 不变更可不传）✅
- 删除：DELETE `/api/admin/cf-accounts/{id}`（若未来有关联 zone 建议阻止删除）✅
- 启用：POST `/api/admin/cf-accounts/{id}/enable`✅
- 禁用：POST `/api/admin/cf-accounts/{id}/disable`✅
- 测试连接：POST `/api/admin/cf-accounts/{id}/test`✅

更新示例（仅修改名称/启用状态）：
```bash
curl -X PUT http://localhost:8080/api/admin/cf-accounts/1 \
  -H "Authorization: Bearer <admin_token>" -H "Content-Type: application/json" \
  -d '{"name":"主账号-1","enabled":true}'
```

删除示例：
```bash
curl -X DELETE http://localhost:8080/api/admin/cf-accounts/1 \
  -H "Authorization: Bearer <admin_token>"
```

#### 2.4 Zones 管理✅
- 手动同步：POST `/api/admin/zones/sync`（可选传 `cfAccountId`）
- 列表：GET `/api/admin/zones?enabled=&name=&cfAccountId=`
- 启用/禁用分发：✅
  - 启用：POST `/api/admin/zones/{id}/enable`
  - 禁用：POST `/api/admin/zones/{id}/disable`

#### 2.5 DNS 记录
- 同步某 zone 记录：POST `/api/admin/zones/{zoneId}/sync-records`（注意：这里的 `zoneId` 为本地数据库 zone 主键 `zones.id`）✅
- 列表：GET `/api/admin/zones/{zoneId}/records?type=&name=`✅
- 新增：POST `/api/admin/zones/{zoneId}/records`✅
- 更新：PUT `/api/admin/zones/{zoneId}/records/{recordId}`（`recordId` 为 Cloudflare 的记录 ID）
- 删除：DELETE `/api/admin/zones/{zoneId}/records/{recordId}`

新增/更新请求体（直接传 Cloudflare 记录 JSON，示例）：✅

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| type | string | 是 | A / AAAA / CNAME / TXT ... |
| name | string | 是 | 完整主机名，如 `a.example.com` |
| content | string | 是 | IP / 域名 / 文本 |
| ttl | int | 否 | 默认 120 |
| proxied | boolean | 否 | 仅部分类型支持 |

示例（新增 A 记录）：✅
```bash
curl -X POST http://localhost:8080/api/admin/zones/1/records \
  -H "Authorization: Bearer <admin_token>" -H "Content-Type: application/json" \
  -d '{"type":"A","name":"a.example.com","content":"1.2.3.4","ttl":120,"proxied":false}'
```

示例（更新 TTL/代理）：✅
```bash
curl -X PUT http://localhost:8080/api/admin/zones/1/records/<cf_record_id> \
  -H "Authorization: Bearer <admin_token>" -H "Content-Type: application/json" \
  -d '{"ttl":300,"proxied":true}'
```

示例（删除）：✅
```bash
curl -X DELETE http://localhost:8080/api/admin/zones/1/records/<cf_record_id> \
  -H "Authorization: Bearer <admin_token>"
```

说明：
- 写操作先请求 Cloudflare 成功后，再 upsert 到本地；删除暂依赖后续同步清理（可扩展本地删除）。
- 若 Cloudflare 返回 `success=false`，接口将返回错误并包含 `errors`。

#### 2.6 系统设置✅
- 获取：GET `/api/admin/settings`
- 更新：PUT `/api/admin/settings`

请求参数（PUT 为整体或部分键值对，均以字符串提交）：

| 字段 | 类型 | 说明 |
|---|---|---|
| initial_register_points | string(int) | 注册赠送积分（默认 1） |
| invitee_points | string(int) | 被邀请人奖励积分（默认 3） |
| inviter_points | string(int) | 邀请人奖励积分（默认 3） |
| domain_cost_points | string(int) | 申请域名消耗积分（默认 5） |
| max_domains_per_user | string(int) | 单用户域名上限 |
| default_ttl | string(int) | 默认 TTL（如 120） |
| sync_cron_expression | string | 同步 cron 表达式 |

返回（GET）：
```json
{
  "code": 0,
  "data": {
    "initial_register_points": "1",
    "invitee_points": "3",
    "inviter_points": "3",
    "domain_cost_points": "5",
    "default_ttl": "120",
    "max_domains_per_user": "5",
    "sync_cron_expression": "0 */5 * * * *"
  }
}
```

#### 2.7 用户与审计✅
- 用户列表：GET `/api/admin/users?page=&size=&status=&role=`✅
- 积分调整：POST `/api/admin/users/{id}/points` `{delta:int, remark:string}✅`
- 审计日志：GET `/api/admin/audit?page=&size=&action=`✅

#### 2.8 公告管理✅
- 列表：GET `/api/admin/announcements`✅
- 详情：GET `/api/admin/announcements/{id}`✅
- 创建：POST `/api/admin/announcements`✅
- 更新：PUT `/api/admin/announcements/{id}`✅
- 删除：DELETE `/api/admin/announcements/{id}`✅
- 发布：POST `/api/admin/announcements/{id}/publish`✅
- 归档：POST `/api/admin/announcements/{id}/archive`✅

请求参数（创建/更新）：
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | string | 是 | 公告标题（最大200字符） |
| content | string | 是 | 公告内容 |
| status | string | 是 | 状态：DRAFT/PUBLISHED/ARCHIVED |
| priority | int | 是 | 优先级：1-普通，2-重要，3-紧急 |

响应示例（列表）：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "title": "系统维护通知",
      "content": "系统将于今晚进行维护...",
      "status": "PUBLISHED",
      "priority": 2,
      "publishedAt": "2024-09-18T10:00:00",
      "createdAt": "2024-09-18T09:00:00",
      "updatedAt": "2024-09-18T10:00:00",
      "createdBy": 1,
      "createdByUsername": "admin"
    }
  ]
}
```

#### 2.9 GitHub任务管理✅

##### 2.9.1 GitHub任务（ADMIN）✅
- 创建：POST `/api/admin/github-tasks`
- 列表：GET `/api/admin/github-tasks`
- 详情：GET `/api/admin/github-tasks/{id}`
- 更新：PUT `/api/admin/github-tasks/{id}`
- 删除：DELETE `/api/admin/github-tasks/{id}`
- 统计：GET `/api/admin/github-tasks/{id}/stats`

请求参数（创建/更新）：
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | string | 是 | 任务标题（最大200字符） |
| description | string | 否 | 任务描述 |
| repositoryUrl | string | 是 | GitHub仓库链接 |
| repositoryOwner | string | 是 | 仓库所有者 |
| repositoryName | string | 是 | 仓库名称 |
| rewardPoints | int | 是 | 奖励积分 |
| status | string | 否 | 状态：ACTIVE/INACTIVE/COMPLETED |
| startTime | string | 否 | 开始时间（ISO格式） |
| endTime | string | 否 | 结束时间（ISO格式） |

响应示例（列表）：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "title": "Star HyperNym 主仓库",
      "description": "请为我们的主仓库添加Star，支持我们的开源项目！",
      "repositoryUrl": "https://github.com/hypernym/hypernym",
      "repositoryOwner": "hypernym",
      "repositoryName": "hypernym",
      "rewardPoints": 50,
      "status": "ACTIVE",
      "startTime": "2025-01-20T00:00:00",
      "endTime": "2025-02-20T00:00:00",
      "createdAt": "2025-01-20T10:00:00",
      "createdBy": 1
    }
  ]
}
```

统计响应示例：
```json
{
  "code": 0,
  "data": {
    "taskId": 1,
    "taskTitle": "Star HyperNym 主仓库",
    "completionCount": 15,
    "rewardPoints": 50
  }
}
```

#### 2.10 邀请/卡密/支付✅

##### 2.10.1 邀请码（ADMIN）✅
- 列表：GET `/api/admin/invites?ownerUserId=&page=&size=`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| ownerUserId | long | 否 | 归属用户 ID（为空返回全部） |
| page | int | 否 | 页码（默认 1） |
| size | int | 否 | 每页大小（默认 20） |

响应：`{ code:0, data: { list,total,page,size } }`

- 生成：POST `/api/admin/invites`

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| ownerUserId | long | 否 | 归属用户 ID（为空表示通用码） |
| maxUses | int | 否 | 最多可使用次数（默认 0=不限制） |
| validDays | int | 否 | 有效天数（为空则不过期） |

响应：`{ code:0, data: { code } }`

说明：生成后默认为 `status=ACTIVE`，`used_count=0`，若传入 `validDays` 则计算 `expired_at`。

##### 2.10.2 卡密（ADMIN）✅
- 列表：GET `/api/admin/cards?status=&page=&size=`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| status | string | 否 | 过滤状态（如 ACTIVE/USED/EXPIRED） |
| page | int | 否 | 页码（默认 1） |
| size | int | 否 | 每页大小（默认 20） |

响应：`{ code:0, data: { list,total,page,size } }`

- 批量生成：POST `/api/admin/cards/generate`

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| count | int | 是 | 生成数量 |
| points | int | 是 | 每张卡密可充值积分数 |
| validDays | int | 否 | 有效天数（为空则不过期） |

响应：`{ code:0, data: { count } }`

说明：卡密一经生成即为 `ACTIVE`，被用户兑换后变为 `USED`，过期后为 `EXPIRED`（状态字段以实现为准）。

##### 2.10.3 订单（ADMIN）✅
- 列表：GET `/api/admin/orders?status=&userId=&page=&size=`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| status | string | 否 | 订单状态（PENDING/PAID/CANCELLED/FAILED） |
| userId | long | 否 | 用户 ID |
| page | int | 否 | 页码（默认 1） |
| size | int | 否 | 每页大小（默认 20） |

响应：`{ code:0, data: { list,total,page,size } }`

---

### 3. 用户（USER）

#### 3.1 可分发域名✅
- GET `/api/zones`：返回已启用分发的 zone 列表

#### 3.1.1 子域名可用性搜索（公开）✅
- GET `/api/domains/search?prefix=abc`
- 说明：输入子域名前缀，返回所有已启用分发的 `zones` 下候选完整域名，并标注是否可用（基于本地镜像 DNS 记录判断占用）。

响应示例：
```json
{
  "code": 0,
  "data": [
    { "domain": "abc.example.com", "available": true },
    { "domain": "abc.hyper99.shop", "available": false, "reason": "occupied" }
  ]
}
```

#### 3.2 申请子域名✅（含可用性校验与积分扣减）
- POST `/api/user/domains/apply`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| zoneId | string | 是 | 目标 Zone（支持：本地 `zones.id`、Cloudflare `zone_id`、或域名 `name`） |
| prefix | string | 是 | 子域名前缀 |
| type | string | 是 | 记录类型（A/AAAA/CNAME/TXT...） |
| value | string | 是 | 记录值（IP/域名/文本） |
| ttl | int | 否 | TTL（默认取系统设置 `default_ttl`，如未传） |
| remark | string | 否 | 备注 |

响应（同步成功示例）：

```json
{ "code": 0, "message": "ok", "data": { "domainId": 1001, "status": "ACTIVE" } }
```

说明与规则：
- 申请前置校验：
  - 若该用户已申请过相同完整域名（前缀+zone）→ 返回错误 `你已申请过该子域名`。
  - 若该完整域名在 Cloudflare 已存在 DNS 记录（以本地镜像为准）→ 返回错误 `该子域名已被占用`。
- 记录创建：先在 Cloudflare 成功创建 DNS 记录后，才会扣减积分与落库。
- TTL：未传 `ttl` 时，使用系统设置 `default_ttl`（默认 120）。
- 记录校验：`A` 记录需为 IPv4；`AAAA` 记录需为 IPv6；`CNAME/TXT` 简要校验。
- 积分消耗：以系统设置 `domain_cost_points` 为基准（默认 10 分）。根据 TLD 乘数扣减：
  - `.cn` / `.com`：2.0 倍（20 分）
  - `.top`：1.5 倍（15 分）
  - 其它：1.0 倍（10 分）
- 余额保护：若余额不足扣减所需积分，直接返回错误 `积分不足`，不会创建记录或写入流水。

错误示例：
```json
{ "code": 40001, "message": "你已申请过该子域名" }
{ "code": 40001, "message": "该子域名已被占用" }
{ "code": 40001, "message": "积分不足" }
{ "code": 50000, "message": "{Cloudflare 错误响应体}" }
```

示例（申请 A 记录）：
```bash
curl -X POST http://localhost:8080/api/user/domains/apply \
  -H "Authorization: Bearer <user_token>" -H "Content-Type: application/json" \
  -d '{
    "zoneId":"c7ff25782f8f4054c9fa25d570d56f6a",
    "prefix":"abc",
    "type":"A",
    "value":"1.1.1.1",
    "remark":"我的测试记录"
  }'
```

#### 3.3 我的域名✅
- GET `/api/user/domains`：分页查询我的子域名
- DELETE `/api/user/domains/{id}`：释放子域名（删除对应 DNS 记录），并返还 50% 创建时消耗的积分

#### 3.4 积分✅
- GET `/api/user/points`：返回当前积分余额与积分流水（分页）

响应示例：
```json
{
  "code": 0,
  "data": {
    "balance": 85,
    "list": [
      {
        "id": 123,
        "userId": 1,
        "changeAmount": -15,
        "balanceAfter": null,
        "type": "DOMAIN_APPLY",
        "remark": "申请域名 abc.hyper99.top 扣除 15 积分",
        "relatedId": null,
        "createdAt": "2025-09-18T06:18:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 20
  }
}
```

#### 3.5 邀请✅
- POST `/api/user/invite/generate`：生成/重置邀请码
- GET `/api/user/invite/mycode`：查看我的邀请码与使用情况

##### 3.5.1 生成/重置邀请码✅
- 方法：POST `/api/user/invite/generate`
- 说明：为当前用户生成或重置邀请码，若已存在则重置为新的邀请码

请求参数（可选）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| maxUses | int | 否 | 最多可使用次数（默认不限制） |
| validDays | int | 否 | 有效天数（默认不过期） |

响应示例：
```json
{
  "code": 0,
  "data": {
    "code": "ABC12345",
    "maxUses": 10,
    "validDays": 30,
    "expiredAt": "2024-10-18T06:18:00"
  }
}
```

##### 3.5.2 查看我的邀请码✅
- 方法：GET `/api/user/invite/mycode`
- 说明：查看当前用户的邀请码信息与使用情况

响应示例：
```json
{
  "code": 0,
  "data": {
    "hasCode": true,
    "code": "ABC12345",
    "maxUses": 10,
    "usedCount": 3,
    "remainingUses": 7,
    "status": "ACTIVE",
    "createdAt": "2024-09-18T06:18:00",
    "expiredAt": "2024-10-18T06:18:00"
  }
}
```

无邀请码时：
```json
{
  "code": 0,
  "data": {
    "hasCode": false
  }
}
```

示例（生成邀请码）：
```bash
curl -X POST http://localhost:8080/api/user/invite/generate \
  -H "Authorization: Bearer <user_token>" -H "Content-Type: application/json" \
  -d '{"maxUses":10,"validDays":30}'
```

示例（查看邀请码）：
```bash
curl -X GET http://localhost:8080/api/user/invite/mycode \
  -H "Authorization: Bearer <user_token>"
```

#### 3.6 系统设置✅
- GET `/api/user/settings`：获取用户可访问的系统设置

响应示例：
```json
{
  "code": 0,
  "data": {
    "domain_cost_points": "10",
    "default_ttl": "120",
    "max_domains_per_user": "5",
    "initial_register_points": "1",
    "invitee_points": "3",
    "inviter_points": "3"
  }
}
```

说明：只返回用户需要知道的设置，不包含敏感的管理员设置。

#### 3.7 用户信息✅
- GET `/api/user/info`：获取当前用户的基本信息

响应示例：
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "createdAt": "2024-09-18T06:18:00",
    "status": "ACTIVE"
  }
}
```

说明：返回用户的基本信息，包括用户名、邮箱、创建时间、账户状态等。

#### 3.8 GitHub任务✅

##### 3.8.1 任务列表✅
- 方法：GET `/api/user/github-tasks`
- 说明：获取用户可参与的GitHub Star任务列表

响应示例：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "title": "Star HyperNym 主仓库",
      "description": "请为我们的主仓库添加Star，支持我们的开源项目！",
      "repositoryUrl": "https://github.com/hypernym/hypernym",
      "repositoryOwner": "hypernym",
      "repositoryName": "hypernym",
      "rewardPoints": 50,
      "status": "ACTIVE",
      "startTime": "2025-01-20T00:00:00",
      "endTime": "2025-02-20T00:00:00",
      "isCompleted": false,
      "isStarred": false,
      "pointsAwarded": 0
    }
  ]
}
```

##### 3.8.2 任务详情✅
- 方法：GET `/api/user/github-tasks/{id}`
- 说明：获取指定任务的详细信息

响应示例：
```json
{
  "code": 0,
  "data": {
    "task": {
      "id": 1,
      "title": "Star HyperNym 主仓库",
      "description": "请为我们的主仓库添加Star，支持我们的开源项目！",
      "repositoryUrl": "https://github.com/hypernym/hypernym",
      "repositoryOwner": "hypernym",
      "repositoryName": "hypernym",
      "rewardPoints": 50,
      "status": "ACTIVE"
    },
    "isCompleted": false,
    "userTask": null
  }
}
```

##### 3.8.3 验证Star状态✅
- 方法：POST `/api/user/github-tasks/{id}/verify`
- 说明：验证用户是否已Star指定仓库并发放积分奖励

请求参数：
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| githubUsername | string | 是 | GitHub用户名 |

响应示例：
```json
{
  "code": 0,
  "data": {
    "success": true,
    "message": "验证成功！您已获得 50 积分奖励",
    "pointsAwarded": 50
  }
}
```

错误示例：
```json
{
  "code": 40001,
  "message": "验证失败，请确保您已Star该仓库"
}
```

##### 3.8.4 任务记录✅
- 方法：GET `/api/user/github-tasks/records`
- 说明：获取用户的任务参与记录

响应示例：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "taskId": 1,
      "githubUsername": "alice",
      "isStarred": true,
      "pointsAwarded": 50,
      "completedAt": "2025-01-20T10:30:00",
      "createdAt": "2025-01-20T10:30:00"
    }
  ]
}
```

##### 3.8.5 已完成任务✅
- 方法：GET `/api/user/github-tasks/completed`
- 说明：获取用户已完成的任务列表

##### 3.8.6 任务统计✅
- 方法：GET `/api/user/github-tasks/stats`
- 说明：获取用户任务完成统计

响应示例：
```json
{
  "code": 0,
  "data": {
    "totalTasks": 5,
    "completedCount": 3,
    "totalPointsEarned": 120,
    "completionRate": 60.0
  }
}
```

#### 3.9 充值❌
- POST `/api/user/recharge`：创建订单（返回支付链接/二维码）
- GET `/api/user/orders`：查看我的订单

请求体（创建订单）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| amount | number | 是 | 金额（元）或根据套餐选择 |
| points | int | 否 | 期望获得积分（与金额对应，后端校验） |
| provider | string | 否 | 支付渠道（如 mock/alipay/wechat，视实现） |

响应（创建）：`{ code:0, data:{ orderNo, payUrl? } }`

示例（创建订单）：
```bash
curl -X POST http://localhost:8080/api/user/recharge \
  -H "Authorization: Bearer <user_token>" -H "Content-Type: application/json" \
  -d '{"amount":9.9,"points":10,"provider":"mock"}'
```

示例（我的订单）：
```bash
curl -X GET "http://localhost:8080/api/user/orders?status=PAID&page=1&size=20" \
  -H "Authorization: Bearer <user_token>"
```

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

##  二、Postman 测试样例（含 curl）✅

准备：
- 基础地址：`http://localhost:8080`
- Content-Type：`application/json`
- 登录后将返回的 `token` 放到 Postman 的 `Authorization: Bearer <token>`

### 1) 发送注册验证码✅

```bash
curl -X POST http://localhost:8080/api/auth/register/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```

#### 2) 用户注册（带邮箱验证码）✅

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"User@123","emailCode":"123456"}'
```

#### 3) 用户登录✅

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"User@123"}'
```

#### 4) 找回密码-发送验证码（限流）✅

```bash
curl -X POST http://localhost:8080/api/auth/forgot \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```

#### 5) 找回密码-重置✅

```bash
curl -X POST http://localhost:8080/api/auth/reset \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","code":"123456","newPassword":"User@456"}'
```

#### 6) 管理员首次注册（仅当系统无 ADMIN）✅

```bash
curl -X POST http://localhost:8080/api/auth/admin/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"Admin@123"}'
```

#### 7) 管理员登录✅

```bash
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

#### 8) 注销（写入 JWT 黑名单）✅

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <粘贴你的token>"
```

---

### 9) Admin - Cloudflare 账户（cf-accounts）✅

- 创建（GLOBAL_KEY）：✅
```bash
curl -X POST http://localhost:8080/api/admin/cf-accounts \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"主账号","email":"you@example.com","apiType":"GLOBAL_KEY","apiKey":"<your_global_key>","enabled":true}'
```

- 列表：✅
```bash
curl -X GET "http://localhost:8080/api/admin/cf-accounts" \
  -H "Authorization: Bearer <admin_token>"
```

- 启用：✅
```bash
curl -X POST http://localhost:8080/api/admin/cf-accounts/1/enable \
  -H "Authorization: Bearer <admin_token>"
```

- 禁用：✅
```bash
curl -X POST http://localhost:8080/api/admin/cf-accounts/1/disable \
  -H "Authorization: Bearer <admin_token>"
```

- 测试连接：✅
```bash
curl -X POST http://localhost:8080/api/admin/cf-accounts/1/test \
  -H "Authorization: Bearer <admin_token>"
```

---

### 10) Admin - Zones✅

- 手动同步（所有启用账户）：
```bash
curl -X POST http://localhost:8080/api/admin/zones/sync \
  -H "Authorization: Bearer <admin_token>"
```

- 手动同步（指定账户）：
```bash
curl -X POST http://localhost:8080/api/admin/zones/sync \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"cfAccountId":1}'
```

- 列表：
```bash
curl -X GET "http://localhost:8080/api/admin/zones?enabled=1&name=example&cfAccountId=1" \
  -H "Authorization: Bearer <admin_token>"
```

- 启用分发：
```bash
curl -X POST http://localhost:8080/api/admin/zones/1/enable \
  -H "Authorization: Bearer <admin_token>"
```

- 禁用分发：
```bash
curl -X POST http://localhost:8080/api/admin/zones/1/disable \
  -H "Authorization: Bearer <admin_token>"
```

---

### 11) Admin - DNS 记录✅

- 同步某 Zone 记录：
```bash
curl -X POST http://localhost:8080/api/admin/zones/1/sync-records \
  -H "Authorization: Bearer <admin_token>"
```

- 列表：
```bash
curl -X GET "http://localhost:8080/api/admin/zones/1/records?type=A&name=a" \
  -H "Authorization: Bearer <admin_token>"
```

---

### 12) Admin - 邀请码✅

- 列表：
```bash
curl -X GET "http://localhost:8080/api/admin/invites?page=1&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

- 生成：
```bash
curl -X POST http://localhost:8080/api/admin/invites \
  -H "Authorization: Bearer <admin_token>" -H "Content-Type: application/json" \
  -d '{"ownerUserId":1,"maxUses":10,"validDays":30}'
```

---

### 13) Admin - 卡密✅

- 列表：
```bash
curl -X GET "http://localhost:8080/api/admin/cards?status=ACTIVE&page=1&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

- 批量生成：
```bash
curl -X POST http://localhost:8080/api/admin/cards/generate \
  -H "Authorization: Bearer <admin_token>" -H "Content-Type: application/json" \
  -d '{"count":50,"points":5,"validDays":365}'
```

---

### 14) Admin - 订单✅

- 列表：
```bash
curl -X GET "http://localhost:8080/api/admin/orders?status=PAID&page=1&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

---

### 15) User - 系统设置✅

- 获取用户设置：
```bash
curl -X GET http://localhost:8080/api/user/settings \
  -H "Authorization: Bearer <user_token>"
```

### 16) User - 用户信息✅

- 获取用户信息：
```bash
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer <user_token>"
```

### 17) User - 邀请明细✅

- 获取邀请奖励记录：
```bash
curl -X GET "http://localhost:8080/api/user/invite/details?page=1&size=20" \
  -H "Authorization: Bearer <user_token>"
```

**响应示例：**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 123,
        "username": "new_user_1",
        "points": 3,
        "remark": "邀请奖励",
        "createdAt": "2025-01-20T10:30:00"
      }
    ],
    "total": 5,
    "page": 1,
    "size": 20
  }
}
```

**说明：**
- `page`: 页码，默认1
- `size`: 每页数量，默认20，最大100
- `username`: 被邀请用户的用户名
- `points`: 获得的积分数量
- `remark`: 备注信息
- `createdAt`: 奖励时间

### 18) Admin - GitHub任务管理✅

- 创建任务：
```bash
curl -X POST http://localhost:8080/api/admin/github-tasks \
  -H "Authorization: Bearer <admin_token>" -H "Content-Type: application/json" \
  -d '{
    "title": "Star HyperNym 主仓库",
    "description": "请为我们的主仓库添加Star，支持我们的开源项目！",
    "repositoryUrl": "https://github.com/hypernym/hypernym",
    "repositoryOwner": "hypernym",
    "repositoryName": "hypernym",
    "rewardPoints": 50,
    "startTime": "2025-01-20T00:00:00",
    "endTime": "2025-02-20T00:00:00"
  }'
```

- 任务列表：
```bash
curl -X GET http://localhost:8080/api/admin/github-tasks \
  -H "Authorization: Bearer <admin_token>"
```

- 任务统计：
```bash
curl -X GET http://localhost:8080/api/admin/github-tasks/1/stats \
  -H "Authorization: Bearer <admin_token>"
```

### 19) User - GitHub任务✅

- 获取任务列表：
```bash
curl -X GET http://localhost:8080/api/user/github-tasks \
  -H "Authorization: Bearer <user_token>"
```

- 验证Star状态：
```bash
curl -X POST http://localhost:8080/api/user/github-tasks/1/verify \
  -H "Authorization: Bearer <user_token>" -H "Content-Type: application/json" \
  -d '{"githubUsername": "alice"}'
```

- 获取任务记录：
```bash
curl -X GET http://localhost:8080/api/user/github-tasks/records \
  -H "Authorization: Bearer <user_token>"
```

- 获取任务统计：
```bash
curl -X GET http://localhost:8080/api/user/github-tasks/stats \
  -H "Authorization: Bearer <user_token>"
```

### 20) User - 充值/订单✅

- 创建订单：
```bash
curl -X POST http://localhost:8080/api/user/recharge \
  -H "Authorization: Bearer <user_token>" -H "Content-Type: application/json" \
  -d '{"amount":9.9,"points":10,"provider":"mock"}'
```

- 我的订单：
```bash
curl -X GET "http://localhost:8080/api/user/orders?status=&page=1&size=20" \
  -H "Authorization: Bearer <user_token>"
```

##  三、通用约定

- Header：`Content-Type: application/json`；鉴权接口除外，其他接口需 `Authorization: Bearer <JWT>`
- 分页：若返回列表较大，统一采用 `?page=&size=`（当前部分接口返回全量，后续可按需分页）
- 排序：默认按 `id DESC` 或时间倒序
- 错误示例：
```json
{ "code": 40001, "message": "参数校验失败" }
{ "code": 40101, "message": "Token 已失效" }
{ "code": 40301, "message": "权限不足" }
{ "code": 42901, "message": "请求过于频繁，请稍后再试" }
```
- 权限矩阵（简化）：
  - 认证：`/api/auth/**`（部分公开）
  - 管理：`/api/admin/**`（ADMIN）
  - 用户：`/api/user/**`（USER）
- 幂等与一致性：
  - 外部写操作（Cloudflare）以 CF 成功为准，再落库；同步接口用于修复漂移
  - 重要写操作建议前端传入防重 `Idempotency-Key`（后续可扩展）
- 限流：注册验证码/找回密码默认 1 分钟 3 次；可扩展至登录/注册按 IP 和账号限流
- 版本与变更：当前为 v1，后续变更将追加到本页“通用约定/变更记录”处
