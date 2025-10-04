# HyFreeDom 前端部署指南

## 环境配置

### 开发环境
- **API 地址**: `http://localhost:8080`
- **前端地址**: `http://localhost:3000`
- **环境文件**: `env.development`

### 生产环境
- **API 地址**: `https://your-domain.com` (需要修改)
- **前端地址**: 部署到服务器后的地址
- **环境文件**: `env.production`

## 部署步骤

### 1. 配置生产环境

#### 修改生产环境配置
编辑 `env.production` 文件：
```bash
# 生产环境配置
VITE_API_BASE_URL=https://your-api-domain.com
VITE_APP_TITLE=HyFreeDom
VITE_APP_ENV=production
```

#### 修改 Vite 配置（如需要）
编辑 `vite.config.js` 中的代理配置，确保生产环境正确。

### 2. 构建生产版本

#### 安装依赖
```bash
npm install
```

#### 构建生产版本
```bash
# 构建生产环境版本
npm run build

# 或者构建开发环境版本（用于测试）
npm run build:dev
```

### 3. 部署到服务器

#### 方式一：静态文件部署
1. 将 `dist` 目录下的所有文件上传到 Web 服务器
2. 配置 Web 服务器（Nginx/Apache）指向 `dist` 目录
3. 确保服务器支持 SPA 路由（配置 fallback 到 index.html）

#### 方式二：Docker 部署
创建 `Dockerfile`：
```dockerfile
# 构建阶段
FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

创建 `nginx.conf`：
```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;
    
    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;
        
        # 支持 SPA 路由
        location / {
            try_files $uri $uri/ /index.html;
        }
        
        # API 代理（如果需要）
        location /api/ {
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

构建和运行 Docker 容器：
```bash
# 构建镜像
docker build -t domaindns-ui .

# 运行容器
docker run -d -p 80:80 --name domaindns-ui domaindns-ui
```

### 4. Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;
    
    # 支持 SPA 路由
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

### 5. HTTPS 配置

#### 使用 Let's Encrypt
```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
# 添加：0 12 * * * /usr/bin/certbot renew --quiet
```

#### Nginx HTTPS 配置
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # 其他配置...
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

## 环境变量说明

| 变量名 | 说明 | 开发环境 | 生产环境 |
|--------|------|----------|----------|
| `VITE_API_BASE_URL` | API 基础地址 | `http://localhost:8080` | `https://your-api-domain.com` |
| `VITE_APP_TITLE` | 应用标题 | `HyFreeDom - 开发环境` | `HyFreeDom` |
| `VITE_APP_ENV` | 环境标识 | `development` | `production` |

## 开发命令

```bash
# 开发环境启动
npm run dev

# 生产环境预览
npm run dev:prod

# 构建生产版本
npm run build

# 构建开发版本
npm run build:dev

# 预览构建结果
npm run preview

# 代码检查
npm run lint
```

## 故障排除

### 1. API 请求失败
- 检查 `VITE_API_BASE_URL` 配置是否正确
- 确认后端服务是否正常运行
- 检查网络连接和防火墙设置

### 2. 路由问题
- 确保 Web 服务器配置了 SPA 路由支持
- 检查 `try_files` 配置是否正确

### 3. 静态资源加载失败
- 检查文件路径是否正确
- 确认 Web 服务器配置了正确的 MIME 类型

### 4. 环境变量不生效
- 确认环境变量以 `VITE_` 开头
- 重启开发服务器
- 检查 `.env` 文件是否在正确位置

## 性能优化

### 1. 构建优化
- 启用代码分割
- 压缩静态资源
- 移除未使用的代码

### 2. 缓存策略
- 静态资源长期缓存
- API 响应适当缓存
- 使用 CDN 加速

### 3. 监控
- 添加错误监控
- 性能监控
- 用户行为分析
