# DomainDNS 前端项目

## 项目简介

DomainDNS 是一个基于 Vue 3 + Vite 的现代化前端应用，提供域名管理和 DNS 记录配置功能。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 状态管理库
- **Element Plus** - Vue 3 组件库

## 环境配置

### 开发环境
- **前端地址**: http://localhost:3000
- **API 地址**: http://localhost:8080
- **环境文件**: `.env.development`

### 生产环境
- **API 地址**: 需要配置为实际的生产环境地址
- **环境文件**: `.env.production`

## 快速开始

### 1. 安装依赖
```bash
npm install
```

### 2. 配置环境变量
复制环境配置文件并修改相应的配置：
```bash
# 开发环境
cp .env.development .env.development.local

# 生产环境
cp .env.production .env.production.local
```

### 3. 启动开发服务器
```bash
npm run dev
```

### 4. 构建生产版本
```bash
npm run build
```

## 可用脚本

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动开发服务器 |
| `npm run dev:prod` | 以生产环境配置启动开发服务器 |
| `npm run build` | 构建生产版本 |
| `npm run build:dev` | 构建开发版本 |
| `npm run preview` | 预览构建结果 |
| `npm run preview:prod` | 以生产环境配置预览 |
| `npm run lint` | 代码检查 |

## 项目结构

```
UI/
├── src/
│   ├── assets/          # 静态资源
│   ├── config/          # 配置文件
│   │   └── env.js       # 环境配置管理
│   ├── router/          # 路由配置
│   ├── stores/          # 状态管理
│   ├── utils/           # 工具函数
│   │   ├── api.js       # API 请求工具
│   │   └── test-env.js  # 环境配置测试
│   └── views/           # 页面组件
│       ├── admin/       # 管理员页面
│       └── user/        # 用户页面
├── prototype/           # 原型文件（开发参考）
├── .env.development     # 开发环境配置
├── .env.production      # 生产环境配置
├── vite.config.js       # Vite 配置
└── package.json         # 项目配置
```

## 环境变量说明

### 开发环境 (.env.development)
```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=DomainDNS - 开发环境
VITE_APP_ENV=development
```

### 生产环境 (.env.production)
```bash
VITE_API_BASE_URL=https://your-domain.com
VITE_APP_TITLE=DomainDNS
VITE_APP_ENV=production
```

## API 集成

项目使用统一的 API 工具 (`src/utils/api.js`) 处理所有 API 请求：

### 基本用法
```javascript
import { apiGet, apiPost } from '@/utils/api.js'

// GET 请求
const data = await apiGet('/api/users')

// POST 请求
const result = await apiPost('/api/users', { name: 'John' })
```

### 带认证的请求
```javascript
import { createAuthenticatedApi } from '@/utils/api.js'

const token = localStorage.getItem('admin_token')
const api = createAuthenticatedApi(token)

const data = await api.get('/api/admin/users')
```

## 配置管理

所有配置都集中在 `src/config/env.js` 中管理：

- **API_CONFIG**: API 相关配置
- **APP_CONFIG**: 应用配置
- **STORAGE_CONFIG**: 存储键名配置
- **ERROR_CODES**: 错误码配置
- **DEBUG_CONFIG**: 调试配置

## 开发指南

### 1. 添加新页面
1. 在 `src/views/` 下创建新的 Vue 组件
2. 在 `src/router/index.js` 中添加路由配置
3. 更新导航菜单

### 2. 添加新的 API 接口
1. 在 `src/utils/api.js` 中添加新的 API 方法
2. 在组件中使用 API 方法
3. 处理错误和加载状态

### 3. 环境配置
1. 修改对应的 `.env` 文件
2. 在 `src/config/env.js` 中添加新的配置项
3. 重启开发服务器

## 部署

详细的部署指南请参考 [DEPLOYMENT.md](./DEPLOYMENT.md)

### 快速部署
```bash
# 1. 配置生产环境变量
# 2. 构建生产版本
npm run build

# 3. 部署 dist/ 目录到 Web 服务器
```

## 故障排除

### 1. 环境变量不生效
- 确认变量名以 `VITE_` 开头
- 重启开发服务器
- 检查 `.env` 文件位置

### 2. API 请求失败
- 检查 `VITE_API_BASE_URL` 配置
- 确认后端服务运行状态
- 查看浏览器控制台错误信息

### 3. 构建失败
- 检查 Node.js 版本 (推荐 18+)
- 清除 node_modules 重新安装
- 检查代码语法错误

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License
