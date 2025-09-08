<template>
	<el-card class="card">
		<template #header>添加 Cloudflare 账户</template>
		<el-form label-width="120px" class="container">
			<el-form-item label="账户名称"><el-input placeholder="例如：主账号" /></el-form-item>
			<el-form-item label="邮箱"><el-input placeholder="name@example.com" /></el-form-item>
			<el-form-item label="认证类型"><el-select model-value="GLOBAL_KEY"><el-option label="GLOBAL_KEY" value="GLOBAL_KEY" /><el-option label="API_TOKEN" value="API_TOKEN" /></el-select></el-form-item>
			<el-form-item label="API Key / Token"><el-input placeholder="仅原型，后端将加密存储" /></el-form-item>
			<el-form-item><el-button type="primary">保存账户</el-button><el-button>测试连接</el-button></el-form-item>
		</el-form>
	</el-card>
	<el-card class="card" style="margin-top:16px;">
		<template #header>账户列表</template>
		<el-table :data="[{name:'主账号',email:'admin@example.com',type:'GLOBAL_KEY',status:'启用'}]">
			<el-table-column prop="name" label="名称" />
			<el-table-column prop="email" label="邮箱" />
			<el-table-column prop="type" label="类型" />
			<el-table-column prop="status" label="状态" />
			<el-table-column label="操作">
				<template #default>
					<el-button>禁用</el-button>
					<el-button>同步 Zones</el-button>
				</template>
			</el-table-column>
		</el-table>
	</el-card>
</template>
<script setup>
</script>
<style scoped>
</style>
