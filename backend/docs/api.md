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

#### 2.8 邀请/卡密/支付✅

##### 2.8.1 邀请码（ADMIN）✅
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

##### 2.8.2 卡密（ADMIN）✅
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

##### 2.8.3 订单（ADMIN）✅
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

##  二、Postman 测试样例（含 curl）

准备：
- 基础地址：`http://localhost:8080`
- Content-Type：`application/json`
- 登录后将返回的 `token` 放到 Postman 的 `Authorization: Bearer <token>`

### 1) 发送注册验证码

```bash
curl -X POST http://localhost:8080/api/auth/register/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```

#### 2) 用户注册（带邮箱验证码）

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"User@123","emailCode":"123456"}'
```

#### 3) 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"User@123"}'
```

#### 4) 找回密码-发送验证码（限流）

```bash
curl -X POST http://localhost:8080/api/auth/forgot \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'
```

#### 5) 找回密码-重置

```bash
curl -X POST http://localhost:8080/api/auth/reset \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","code":"123456","newPassword":"User@456"}'
```

#### 6) 管理员首次注册（仅当系统无 ADMIN）

```bash
curl -X POST http://localhost:8080/api/auth/admin/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"Admin@123"}'
```

#### 7) 管理员登录

```bash
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

#### 8) 注销（写入 JWT 黑名单）

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <粘贴你的token>"
```

---

### 9) Admin - Cloudflare 账户（cf-accounts）

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

### 10) Admin - Zones

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

### 11) Admin - DNS 记录

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

### 12) Admin - 邀请码

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

### 13) Admin - 卡密

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

### 14) Admin - 订单

- 列表：
```bash
curl -X GET "http://localhost:8080/api/admin/orders?status=PAID&page=1&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

---

### 15) User - 充值/订单

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
