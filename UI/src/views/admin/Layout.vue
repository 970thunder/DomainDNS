<template>
	<div>
		<header class="app-header">
			<div class="app-header-inner container">
				<div class="brand">DomainDNS • Admin</div>
				<div class="spacer"></div>
				<el-button text @click="$router.push('/user/login')">切换到用户端</el-button>
			</div>
		</header>
		<div class="container" style="padding-top:16px;">
			<el-menu mode="horizontal" :router="true" :default-active="$route.path">
				<el-menu-item index="/admin/dashboard">仪表板</el-menu-item>
				<el-menu-item index="/admin/cf-accounts">Cloudflare账户</el-menu-item>
				<el-menu-item index="/admin/zones">域名与同步</el-menu-item>
				<el-menu-item index="/admin/dns-records">DNS记录</el-menu-item>
				<el-menu-item index="/admin/users">用户管理</el-menu-item>
				<el-menu-item index="/admin/invites">邀请管理</el-menu-item>
				<el-menu-item index="/admin/cards">卡密管理</el-menu-item>
				<el-menu-item index="/admin/settings">系统设置</el-menu-item>
			</el-menu>
			<div class="page">
				<router-view />
			</div>
		</div>
	</div>
</template>

<script setup>
</script>

<style scoped>
</style>
