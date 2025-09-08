<template>
	<div class="container" style="display:grid;place-items:center;min-height:100vh;">
		<el-card style="width:420px;">
			<template #header>登录</template>
			<el-form label-width="100px">
				<el-form-item label="用户名/邮箱"><el-input placeholder="输入用户名或邮箱" /></el-form-item>
				<el-form-item label="密码"><el-input type="password" placeholder="输入密码" /></el-form-item>
				<el-form-item>
					<el-button type="primary" @click="$router.push('/user/dashboard')">登录</el-button>
					<el-button @click="$router.push('/user/register')">去注册</el-button>
				</el-form-item>
			</el-form>
		</el-card>
	</div>
</template>
<script setup>
</script>
<style scoped>
</style>
