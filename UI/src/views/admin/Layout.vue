<template>
	<div class="admin-layout">
		<header class="app-header">
			<div class="app-header-inner container">
				<div class="brand">DomainDNS • Admin</div>
				<nav class="nav">
					<el-menu mode="horizontal" :router="true" :default-active="$route.path" :ellipsis="false"
						class="nav-menu">
						<el-menu-item index="/admin/dashboard">仪表板</el-menu-item>
						<el-menu-item index="/admin/cf-accounts">CF账户</el-menu-item>
						<el-menu-item index="/admin/zones">域名同步</el-menu-item>
						<el-menu-item index="/admin/dns-records">DNS记录</el-menu-item>
						<el-menu-item index="/admin/users">用户管理</el-menu-item>
						<el-menu-item index="/admin/invites">邀请管理</el-menu-item>
						<el-menu-item index="/admin/cards">卡密管理</el-menu-item>
						<el-menu-item index="/admin/settings">系统设置</el-menu-item>
					</el-menu>
				</nav>
				<div class="header-actions">
					<el-button text @click="$router.push('/user/login')">切换到用户端</el-button>
				</div>
			</div>
		</header>
		<main class="main container">
			<router-view />
		</main>
		<footer class="footer container">
			© 2025 DomainDNS
		</footer>
	</div>
</template>

<script setup>
import { ref } from 'vue'

const isCollapsed = ref(false)
</script>

<style scoped>
.admin-layout {
	min-height: 100vh;
	background: #f8fafc;
}

.app-header {
	background: #0f172a;
	color: #fff;
	padding: 10px 0;
	position: sticky;
	top: 0;
	z-index: 1000;
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.app-header-inner {
	display: flex;
	align-items: center;
	gap: 12px;
	max-width: 1200px;
	margin: 0 auto;
	padding: 0 16px;
	min-height: 56px;
}

.brand {
	font-weight: 700;
	font-size: 18px;
	letter-spacing: 0.4px;
	color: #fff;
	flex-shrink: 0;
	min-width: 140px;
}

.nav {
	flex: 1;
	display: flex;
	justify-content: center;
	min-width: 0;
	overflow: hidden;
}

.nav-menu {
	background: transparent;
	border: none;
	display: flex;
	flex-wrap: nowrap;
	overflow-x: auto;
	overflow-y: hidden;
	scrollbar-width: none;
	-ms-overflow-style: none;
}

.nav-menu::-webkit-scrollbar {
	display: none;
}

.nav-menu .el-menu-item {
	color: #e2e8f0;
	border-bottom: none;
	padding: 6px 10px;
	height: auto;
	line-height: 1;
	border-radius: 8px;
	white-space: nowrap;
	flex-shrink: 0;
	min-width: fit-content;
	font-size: 14px;
}

.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
	color: #fff;
	background: #1e293b;
	border-bottom-color: transparent;
}

/* 移除 element-plus 横向菜单默认下划线 */
:deep(.el-menu--horizontal>.el-menu-item.is-active) {
	border-bottom: 0 !important;
}

:deep(.el-menu--horizontal>.el-menu-item) {
	border-bottom: 0 !important;
}

.header-actions {
	display: flex;
	align-items: center;
	gap: 12px;
	flex-shrink: 0;
	min-width: 120px;
}

/* 顶部右侧“切换到用户端”按钮颜色 */
.header-actions :deep(.el-button) {
	color: #e2e8f0;
}

.main {
	padding: 24px 16px;
	min-height: calc(100vh - 120px);
}

.footer {
	padding: 16px;
	color: #64748b;
	font-size: 14px;
	text-align: center;
	border-top: 1px solid #e2e8f0;
}

.container {
	max-width: 1200px;
	margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 1024px) {
	.nav-menu .el-menu-item {
		padding: 0 8px;
		font-size: 13px;
	}

	.brand {
		font-size: 16px;
		min-width: 120px;
	}

	.header-actions {
		min-width: 100px;
	}
}

@media (max-width: 768px) {
	.app-header-inner {
		flex-direction: column;
		gap: 8px;
		padding: 8px 16px;
		min-height: auto;
	}

	.brand {
		order: 1;
		align-self: flex-start;
	}

	.nav {
		order: 2;
		width: 100%;
		justify-content: flex-start;
	}

	.nav-menu {
		width: 100%;
		justify-content: flex-start;
	}

	.nav-menu .el-menu-item {
		padding: 0 8px;
		font-size: 12px;
	}

	.header-actions {
		order: 3;
		align-self: flex-end;
		margin-top: 8px;
	}
}

@media (max-width: 480px) {
	.main {
		padding: 16px 8px;
	}

	.brand {
		font-size: 14px;
		min-width: 100px;
	}

	.nav-menu .el-menu-item {
		padding: 0 6px;
		font-size: 11px;
	}

	.header-actions .el-button {
		font-size: 12px;
		padding: 4px 8px;
	}
}
</style>
