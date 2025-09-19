<template>
	<main class="main container">
		<div class="card">
			<h3>用户列表</h3>
			<div class="row" style="margin-bottom:12px;">
				<input class="input" style="max-width:240px;" placeholder="搜索用户名/邮箱" v-model="keyword" />
				<select class="select" style="max-width:160px;" v-model="role">
					<option value="">全部角色</option>
					<option value="USER">USER</option>
					<option value="ADMIN">ADMIN</option>
				</select>
				<button class="btn outline" @click="applyFilter">筛选</button>
				<span class="spacer"></span>
				<button class="btn primary" @click="openCreate">创建管理员</button>
			</div>
			<table class="table">
				<thead>
					<tr>
						<th>ID</th>
						<th>用户名</th>
						<th>邮箱</th>
						<th>积分</th>
						<th>角色</th>
						<th>状态</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="u in filtered" :key="u.id">
						<td>{{ u.id }}</td>
						<td>{{ u.username }}</td>
						<td>{{ u.email }}</td>
						<td>{{ u.points }}</td>
						<td>{{ u.role }}</td>
						<td><span class="badge" :class="u.status === 1 ? 'success' : ''">{{ u.status === 1 ? '启用' : '禁用'
								}}</span></td>
						<td class="row">
							<button class="btn outline" @click="adjust(u)">加/减积分</button>
							<button class="btn outline" @click="toggleRole(u)">设为ADMIN</button>
							<button class="btn danger" @click="toggleStatus(u)">{{ u.status === 1 ? '禁用' : '启用'
							}}</button>
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
const role = ref('')
const users = ref([
	{ id: 1, username: 'alice', email: 'alice@example.com', points: 8, role: 'USER', status: 1 },
	{ id: 2, username: 'admin', email: 'admin@example.com', points: 100, role: 'ADMIN', status: 1 }
])

const filtered = computed(() => users.value.filter(u =>
	(u.username.includes(keyword.value) || u.email.includes(keyword.value)) && (role.value ? u.role === role.value : true)
))

const applyFilter = () => { }
const openCreate = () => { }
const adjust = (u) => { }
const toggleRole = (u) => { u.role = u.role === 'ADMIN' ? 'USER' : 'ADMIN' }
const toggleStatus = (u) => { u.status = u.status === 1 ? 0 : 1 }
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

.input,
.select {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #cbd5e1;
	border-radius: 10px;
}

.input:focus,
.select:focus {
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

.btn.danger {
	background: #ef4444;
	color: #fff;
}

.btn.danger:hover {
	background: #dc2626;
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
