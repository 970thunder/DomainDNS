<template>
	<el-card class="card">
		<template #header>邀请码列表</template>
		<el-space>
			<el-input placeholder="搜索邀请码/用户" style="width:240px;" />
			<el-button>筛选</el-button>
			<el-button type="primary">生成邀请码</el-button>
		</el-space>
		<el-table style="margin-top:12px;" :data="rows">
			<el-table-column prop="code" label="邀请码" />
			<el-table-column prop="owner" label="归属用户" />
			<el-table-column prop="used" label="已使用次数" />
			<el-table-column prop="status" label="状态" />
			<el-table-column prop="createdAt" label="创建时间" />
			<el-table-column label="操作">
				<template #default>
					<el-button>禁用</el-button>
					<el-button>查看明细</el-button>
				</template>
			</el-table-column>
		</el-table>
	</el-card>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ code:'ABCD1234', owner:'alice', used:2, status:'有效', createdAt:'2025-08-30' },
])
</script>
<style scoped>
</style>
