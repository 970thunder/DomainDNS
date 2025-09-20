<template>
	<div class="card">
		<h3>我的邀请码</h3>
		<div class="row">
			<input class="input" style="max-width:240px;" value="ABCD1234">
			<button class="btn outline">生成/重置</button>
			<button class="btn primary">复制邀请链接</button>
		</div>
		<p class="small muted">新用户使用你的邀请码注册，你与对方均可获得积分（默认 3 点，可由管理员配置）。</p>
	</div>

	<div class="card" style="margin-top:16px;">
		<h3>邀请明细</h3>
		<table class="table">
			<thead>
				<tr>
					<th>用户</th>
					<th>时间</th>
					<th>奖励积分</th>
				</tr>
			</thead>
			<tbody>
				<tr v-for="row in rows" :key="row.user">
					<td data-label="用户">{{ row.user }}</td>
					<td data-label="时间">{{ row.time }}</td>
					<td data-label="奖励积分">{{ row.points }}</td>
				</tr>
			</tbody>
		</table>
	</div>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ user: 'new_user_1', time: '2025-09-01', points: '+3' },
])
</script>
<style scoped></style>
