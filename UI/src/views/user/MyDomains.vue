<template>
	<el-card class="card">
		<template #header>我的域名</template>
		<el-table :data="rows">
			<el-table-column prop="name" label="域名" />
			<el-table-column prop="type" label="记录类型" />
			<el-table-column prop="value" label="记录值" />
			<el-table-column prop="createdAt" label="创建时间" />
			<el-table-column prop="status" label="状态" />
			<el-table-column label="操作">
				<template #default>
					<el-button>编辑备注</el-button>
					<el-button type="danger">释放</el-button>
				</template>
			</el-table-column>
		</el-table>
	</el-card>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ name:'a.example.com', type:'A', value:'1.2.3.4', createdAt:'2025-09-01', status:'ACTIVE' },
])
</script>
<style scoped>
</style>
