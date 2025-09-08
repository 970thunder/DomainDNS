<template>
	<div>
		<header class="app-header">
			<div class="app-header-inner container">
				<div class="brand">DomainDNS • User</div>
				<div class="spacer"></div>
				<el-button text @click="$router.push('/admin/dashboard')">切换到管理端</el-button>
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
	</div>
</template>

<script setup>
</script>

<style scoped>
</style>
