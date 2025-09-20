<template>
	<div class="card">
		<h3>我的域名</h3>
		<table class="table">
			<thead>
				<tr>
					<th>域名</th>
					<th>记录类型</th>
					<th>记录值</th>
					<th>创建时间</th>
					<th>状态</th>
					<th>操作</th>
				</tr>
			</thead>
			<tbody>
				<tr v-for="row in rows" :key="row.name">
					<td data-label="域名">{{ row.name }}</td>
					<td data-label="记录类型">{{ row.type }}</td>
					<td data-label="记录值">{{ row.value }}</td>
					<td data-label="创建时间">{{ row.createdAt }}</td>
					<td data-label="状态"><span class="badge success">{{ row.status }}</span></td>
					<td data-label="操作" class="row">
						<button class="btn outline">编辑备注</button>
						<button class="btn danger">释放</button>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ name: 'a.example.com', type: 'A', value: '1.2.3.4', createdAt: '2025-09-01', status: 'ACTIVE' },
])
</script>
<style scoped></style>
