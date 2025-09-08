<template>
	<el-card class="card">
		<template #header>用户列表</template>
		<el-space>
			<el-input placeholder="搜索用户名/邮箱" style="width:240px;" />
			<el-select placeholder="全部角色" style="width:160px;">
				<el-option label="全部角色" value="" />
				<el-option label="USER" value="USER" />
				<el-option label="ADMIN" value="ADMIN" />
			</el-select>
			<el-button>筛选</el-button>
			<el-button type="primary">创建管理员</el-button>
		</el-space>
		<el-table style="margin-top:12px;" :data="rows">
			<el-table-column prop="id" label="ID" />
			<el-table-column prop="username" label="用户名" />
			<el-table-column prop="email" label="邮箱" />
			<el-table-column prop="points" label="积分" />
			<el-table-column prop="role" label="角色" />
			<el-table-column prop="status" label="状态" />
			<el-table-column label="操作">
				<template #default>
					<el-button>加/减积分</el-button>
					<el-button>设为ADMIN</el-button>
					<el-button type="danger">禁用</el-button>
				</template>
			</el-table-column>
		</el-table>
	</el-card>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ id:1, username:'alice', email:'alice@example.com', points:8, role:'USER', status:'启用' },
])
</script>
<style scoped>
</style>
