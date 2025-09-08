<template>
	<div class="container" style="display:grid;place-items:center;min-height:100vh;">
		<el-card style="width:420px;">
			<template #header>注册</template>
			<el-form label-width="100px">
				<el-form-item label="用户名"><el-input placeholder="输入用户名" /></el-form-item>
				<el-form-item label="邮箱"><el-input placeholder="name@example.com" /></el-form-item>
				<el-form-item label="密码"><el-input type="password" placeholder="设置密码" /></el-form-item>
				<el-form-item label="邀请码"><el-input placeholder="输入邀请码享积分奖励（可选）" /></el-form-item>
				<el-form-item>
					<el-button type="primary" @click="$router.push('/user/dashboard')">注册并登录</el-button>
					<el-button @click="$router.push('/user/login')">已有账号？登录</el-button>
				</el-form-item>
			</el-form>
		</el-card>
	</div>
</template>
<script setup>
</script>
<style scoped>
</style>
