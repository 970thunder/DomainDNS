<template>
	<main class="main container">
		<div class="card">
			<h3>邀请码列表</h3>
			<div class="row" style="margin-bottom:12px;">
				<input class="input" style="max-width:240px;" placeholder="搜索邀请码/用户" v-model="keyword" />
				<button class="btn outline" @click="filter">筛选</button>
				<span class="spacer"></span>
				<button class="btn primary" @click="create">生成邀请码</button>
			</div>
			<table class="table">
				<thead>
					<tr>
						<th>邀请码</th>
						<th>归属用户</th>
						<th>已使用次数</th>
						<th>状态</th>
						<th>创建时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="r in filteredRows" :key="r.code">
						<td>{{ r.code }}</td>
						<td>{{ r.owner }}</td>
						<td>{{ r.used }}</td>
						<td><span class="badge success">{{ r.status }}</span></td>
						<td>{{ r.createdAt }}</td>
						<td class="row"><button class="btn outline" @click="disable(r)">禁用</button><button
								class="btn outline" @click="detail(r)">查看明细</button></td>
					</tr>
				</tbody>
			</table>
		</div>
	</main>
</template>

<script setup>
import { ref, computed } from 'vue'

const keyword = ref('')
const rows = ref([
	{ code: 'ABCD1234', owner: 'alice', used: 2, status: '有效', createdAt: '2025-08-30' }
])

const filteredRows = computed(() => rows.value.filter(r => r.code.includes(keyword.value) || r.owner.includes(keyword.value)))
const filter = () => { }
const create = () => { }
const disable = (r) => { }
const detail = (r) => { }
</script>

<style scoped>
.card {
	background: #fff;
	border: 1px solid #e2e8f0;
	border-radius: 12px;
	padding: 16px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, .04);
}

.row {
	display: flex;
	gap: 8px;
	align-items: center;
	flex-wrap: wrap;
}

.spacer {
	flex: 1;
}

.input {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #cbd5e1;
	border-radius: 10px;
}

.input:focus {
	border-color: #6366f1;
	box-shadow: 0 0 0 3px rgba(99, 102, 241, .15);
	outline: none;
}

.btn {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	padding: 10px 14px;
	border-radius: 10px;
	border: 1px solid transparent;
	font-weight: 600;
	cursor: pointer;
}

.btn.primary {
	background: #6366f1;
	color: #fff;
}

.btn.primary:hover {
	background: #4f46e5;
}

.btn.outline {
	background: #fff;
	border-color: #cbd5e1;
	color: #0f172a;
}

.btn.outline:hover {
	background: #f8fafc;
}

.table {
	width: 100%;
	border-collapse: collapse;
	border: 1px solid #e2e8f0;
	border-radius: 12px;
	overflow: hidden;
}

.table th,
.table td {
	text-align: left;
	padding: 12px 10px;
	border-bottom: 1px solid #e2e8f0;
	font-size: 14px;
}

.table th {
	background: #f1f5f9;
	color: #0f172a;
}

.badge {
	display: inline-flex;
	align-items: center;
	padding: 4px 8px;
	border-radius: 999px;
	font-size: 12px;
	border: 1px solid #e2e8f0;
	color: #334155;
	background: #f8fafc;
}

.badge.success {
	background: #ecfdf5;
	color: #065f46;
	border-color: #a7f3d0;
}
</style>
