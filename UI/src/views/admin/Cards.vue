<template>
	<el-card class="card">
		<template #header>生成卡密</template>
		<el-form label-width="120px" class="container">
			<el-row :gutter="12">
				<el-col :span="8"><el-form-item label="面值（积分）"><el-input placeholder="例如：10" /></el-form-item></el-col>
				<el-col :span="8"><el-form-item label="数量"><el-input placeholder="例如：100" /></el-form-item></el-col>
				<el-col :span="8"><el-form-item label="有效期（天）"><el-input placeholder="例如：30" /></el-form-item></el-col>
			</el-row>
			<el-form-item>
				<el-button type="primary">生成</el-button>
				<el-button>导出 CSV</el-button>
			</el-form-item>
		</el-form>
	</el-card>
	<el-card class="card" style="margin-top:16px;">
		<template #header>卡密列表</template>
		<el-table :data="rows">
			<el-table-column prop="code" label="卡密" />
			<el-table-column prop="amount" label="面值" />
			<el-table-column prop="status" label="状态" />
			<el-table-column prop="createdAt" label="生成时间" />
			<el-table-column prop="expiredAt" label="过期时间" />
			<el-table-column prop="user" label="使用者" />
		</el-table>
	</el-card>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ code:'XXXX-YYYY-ZZZZ', amount:10, status:'未使用', createdAt:'2025-09-01', expiredAt:'2025-10-01', user:'-' },
])
</script>
<style scoped>
</style>
