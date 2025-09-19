<template>
	<main class="main container">
		<div class="card">
			<h3>生成卡密</h3>
			<div class="form">
				<div class="grid cols-3">
					<div class="input-row"><label class="label">面值（积分）</label><input class="input" v-model="form.amount"
							placeholder="例如：10" /></div>
					<div class="input-row"><label class="label">数量</label><input class="input" v-model="form.count"
							placeholder="例如：100" /></div>
					<div class="input-row"><label class="label">有效期（天）</label><input class="input" v-model="form.days"
							placeholder="例如：30" /></div>
				</div>
				<div class="row">
					<button class="btn primary" @click="generate">生成</button>
					<button class="btn outline" @click="exportCsv">导出 CSV</button>
				</div>
			</div>
		</div>

		<div class="card" style="margin-top:16px;">
			<h3>卡密列表</h3>
			<table class="table">
				<thead>
					<tr>
						<th>卡密</th>
						<th>面值</th>
						<th>状态</th>
						<th>生成时间</th>
						<th>过期时间</th>
						<th>使用者</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="r in rows" :key="r.code">
						<td>{{ r.code }}</td>
						<td>{{ r.amount }}</td>
						<td><span class="badge success">{{ r.status }}</span></td>
						<td>{{ r.createdAt }}</td>
						<td>{{ r.expiredAt }}</td>
						<td>{{ r.user }}</td>
					</tr>
				</tbody>
			</table>
		</div>
	</main>
</template>

<script setup>
import { reactive, ref } from 'vue'

const form = reactive({ amount: '', count: '', days: '' })
const rows = ref([
	{ code: 'XXXX-YYYY-ZZZZ', amount: 10, status: '未使用', createdAt: '2025-09-01', expiredAt: '2025-10-01', user: '-' }
])

const generate = () => { }
const exportCsv = () => { }
</script>

<style scoped>
/* Inlined minimal styles (do not depend on external prototype) */
.card {
	background: #fff;
	border: 1px solid #e2e8f0;
	border-radius: 12px;
	padding: 16px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, .04);
}

.grid {
	display: grid;
	gap: 16px;
}

.grid.cols-3 {
	grid-template-columns: repeat(3, minmax(0, 1fr));
}

.form {
	display: grid;
	gap: 12px;
	max-width: 640px;
}

.input-row {
	display: grid;
	gap: 8px;
}

.label {
	font-size: 12px;
	color: #475569;
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

.row {
	display: flex;
	gap: 8px;
	align-items: center;
	flex-wrap: wrap;
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

/* 响应式：窄屏优化 */
@media (max-width: 960px) {
	.grid.cols-3 {
		grid-template-columns: 1fr;
	}
}

@media (max-width: 768px) {
	.card {
		padding: 12px;
	}

	.form {
		max-width: 100%;
	}

	.table th,
	.table td {
		padding: 10px 8px;
		font-size: 13px;
	}
}
</style>
