#!/bin/bash

# DomainDNS 配置设置脚本
# 用于快速设置开发环境配置

echo "🔧 开始配置 DomainDNS 项目..."

# 检查是否已存在配置文件
if [ -f "backend/src/main/resources/application-dev.yml" ]; then
    echo "⚠️  检测到已存在 application-dev.yml 文件"
    read -p "是否要备份现有配置？(y/n): " backup
    if [ "$backup" = "y" ] || [ "$backup" = "Y" ]; then
        cp backend/src/main/resources/application-dev.yml backend/src/main/resources/application-dev.yml.backup.$(date +%Y%m%d_%H%M%S)
        echo "✅ 已备份现有配置"
    fi
fi

# 复制配置模板
echo "📋 复制配置模板..."
cp backend/src/main/resources/application-dev.yml.template backend/src/main/resources/application-dev.yml
cp backend/src/main/resources/application-prod.yml.template backend/src/main/resources/application-prod.yml

# 复制环境变量模板
if [ ! -f ".env" ]; then
    cp env.example .env
    echo "✅ 已创建 .env 文件"
fi

echo ""
echo "🎉 配置模板已准备完成！"
echo ""
echo "📝 接下来请手动编辑以下文件："
echo "   1. backend/src/main/resources/application-dev.yml"
echo "   2. backend/src/main/resources/application-prod.yml"
echo "   3. .env"
echo ""
echo "⚠️  重要提醒："
echo "   - 请使用强密码"
echo "   - 不要将真实配置提交到版本控制"
echo "   - 生产环境使用环境变量"
echo ""
echo "🔒 安全配置完成后，请运行："
echo "   git add .gitignore"
echo "   git commit -m 'Add security configuration'"
echo ""
