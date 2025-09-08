<template>
	<el-card class="card">
		<template #header>我的邀请码</template>
		<el-space>
			<el-input model-value="ABCD1234" style="width:240px;" />
			<el-button>生成/重置</el-button>
			<el-button type="primary">复制邀请链接</el-button>
		</el-space>
		<div style="margin-top:8px;font-size:12px;color:#64748b;">新用户使用你的邀请码注册，你与对方均可获得积分（默认 3 点，可由管理员配置）。</div>
	</el-card>
	<el-card class="card" style="margin-top:16px;">
		<template #header>邀请明细</template>
		<el-table :data="rows">
			<el-table-column prop="user" label="用户" />
			<el-table-column prop="time" label="时间" />
			<el-table-column prop="points" label="奖励积分" />
		</el-table>
	</el-card>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ user:'new_user_1', time:'2025-09-01', points:'+3' },
])
</script>
<style scoped>
</style>
