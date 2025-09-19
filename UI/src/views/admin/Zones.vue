<template>
	<main class="main container">
		<div class="card">
			<h3>Zones 列表（选择启用分发）</h3>
			<div class="row" style="margin-bottom:12px;">
				<button class="btn outline" @click="refresh">刷新列表</button>
				<button class="btn primary" @click="syncAll">手动同步所有 Zones</button>
				<span class="spacer"></span>
				<input class="input" style="max-width:260px;" placeholder="搜索域名..." v-model="keyword" />
			</div>
			<table class="table">
				<thead>
					<tr>
						<th>域名</th>
						<th>Zone ID</th>
						<th>状态</th>
						<th>同步时间</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="r in filteredRows" :key="r.zoneId">
						<td>{{ r.name }}</td>
						<td>{{ r.zoneId }}</td>
						<td>
							<span class="badge" :class="r.status === '启用' ? 'success' : ''">{{ r.status }}</span>
						</td>
						<td>{{ r.syncedAt }}</td>
						<td class="row">
							<button class="btn outline" @click="toggle(r)">{{ r.status === '启用' ? '禁用' : '启用'
							}}</button>
							<button class="btn outline" @click="$router.push('/admin/dns-records')">查看记录</button>
							<button class="btn primary" @click="syncOne(r)">同步记录</button>
						</td>
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
	{ name: 'example.com', zoneId: 'abcd1234', status: '启用', syncedAt: '2025-09-01 12:30' },
	{ name: 'example.net', zoneId: 'efgh5678', status: '禁用', syncedAt: '2025-08-31 20:10' },
])

const filteredRows = computed(() => {
	const k = keyword.value.trim().toLowerCase()
	if (!k) return rows.value
	return rows.value.filter(r => r.name.toLowerCase().includes(k))
})

const refresh = () => { }
const syncAll = () => { }
const syncOne = (r) => { }
const toggle = (r) => { r.status = r.status === '启用' ? '禁用' : '启用' }
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

.table tr:hover td {
	background: #f8fafc;
}

.row {
	display: flex;
	gap: 8px;
	align-items: center;
	flex-wrap: nowrap;
}

.spacer {
	flex: 1;
}

.input {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #cbd5e1;
	border-radius: 10px;
	outline: none;
}

.input:focus {
	border-color: #6366f1;
	box-shadow: 0 0 0 3px rgba(99, 102, 241, .15);
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
@media (max-width: 768px) {
	.card {
		padding: 12px;
	}

	.input {
		width: 100%;
	}

	.row {
		flex-wrap: wrap;
		gap: 8px;
	}

	.table th,
	.table td {
		padding: 10px 8px;
		font-size: 13px;
	}

	/* 将操作按钮堆叠显示，避免溢出 */
	.table td.row {
		flex-direction: column;
		align-items: stretch;
	}

	.table td.row .btn {
		width: 100%;
	}
}
</style>
