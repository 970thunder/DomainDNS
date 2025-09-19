<template>
	<main class="main container">
		<div class="card">
			<h3>example.com — DNS 记录</h3>
			<div class="row" style="margin-bottom:12px;">
				<input class="input" style="max-width:260px;" placeholder="搜索记录..." v-model="keyword" />
				<select class="select" style="max-width:160px;" v-model="type">
					<option value="">全部类型</option>
					<option value="A">A</option>
					<option value="AAAA">AAAA</option>
					<option value="CNAME">CNAME</option>
					<option value="TXT">TXT</option>
				</select>
				<button class="btn outline" @click="reload">刷新</button>
				<span class="spacer"></span>
				<button class="btn primary" @click="sync">从 Cloudflare 同步</button>
			</div>
			<table class="table">
				<thead>
					<tr>
						<th>名称</th>
						<th>类型</th>
						<th>内容</th>
						<th>TTL</th>
						<th>代理</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="r in filteredRows" :key="r.name + r.type">
						<td>{{ r.name }}</td>
						<td>{{ r.type }}</td>
						<td>{{ r.content }}</td>
						<td>{{ r.ttl }}</td>
						<td><span class="badge" :class="r.proxied === '是' ? 'success' : ''">{{ r.proxied }}</span></td>
						<td class="row">
							<button class="btn outline" @click="edit(r)">编辑</button>
							<button class="btn danger" @click="remove(r)">删除</button>
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
const type = ref('')
const rows = ref([
	{ name: 'a.example.com', type: 'A', content: '1.2.3.4', ttl: 120, proxied: '否' },
	{ name: 'www.example.com', type: 'CNAME', content: 'app.host.com', ttl: 300, proxied: '是' },
])

const filteredRows = computed(() => {
	return rows.value.filter(r =>
		(r.name.includes(keyword.value) || r.content.includes(keyword.value)) &&
		(type.value ? r.type === type.value : true)
	)
})

const reload = () => { }
const sync = () => { }
const edit = (r) => { }
const remove = (r) => { }
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

.input,
.select {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #cbd5e1;
	border-radius: 10px;
	outline: none;
}

.input:focus,
.select:focus {
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

.btn.danger {
	background: #ef4444;
	color: #fff;
}

.btn.danger:hover {
	background: #dc2626;
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

/* 响应式：移动端友好 */
@media (max-width: 768px) {
	.card {
		padding: 12px;
	}

	.input,
	.select {
		width: 100%;
	}

	.row {
		flex-wrap: wrap;
		gap: 8px;
	}

	/* 表格在小屏可横向滚动，避免内容溢出 */
	.table {
		display: block;
		overflow-x: auto;
		-webkit-overflow-scrolling: touch;
	}

	.table th,
	.table td {
		padding: 10px 8px;
		font-size: 13px;
	}

	/* 操作列按钮纵向排列，避免横向滚动 */
	.table td.row {
		flex-direction: column;
		align-items: stretch;
	}

	.table td.row .btn {
		width: 100%;
	}

	/* 顶部工具栏按钮在小屏占满宽度，避免溢出 */
	.row .btn {
		width: 100%;
	}
}
</style>
