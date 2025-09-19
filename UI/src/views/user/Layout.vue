<template>
	<div>
		<header class="app-header">
			<div class="app-header-inner container">
				<div class="brand">HyperNym</div>
				<div class="spacer"></div>
				<el-button text @click="$router.push('/admin/login')">管理端登录</el-button>
			</div>
		</header>
		<div class="container" style="padding-top:16px;">
			<el-menu mode="horizontal" :router="true" :default-active="$route.path">
				<el-menu-item index="/user/dashboard">主页</el-menu-item>
				<el-menu-item index="/user/apply">申请域名</el-menu-item>
				<el-menu-item index="/user/domains">我的域名</el-menu-item>
				<el-menu-item index="/user/invite">邀请</el-menu-item>
				<el-menu-item index="/user/recharge">充值</el-menu-item>
				<el-menu-item index="/user/login">退出</el-menu-item>
			</el-menu>
			<div class="page">
				<router-view />
			</div>
		</div>
		<footer class="footer container">
			© 2025 HyperNym-DomainDNS
		</footer>
	</div>
</template>

<script setup>
</script>

<style scoped>
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
</style>
