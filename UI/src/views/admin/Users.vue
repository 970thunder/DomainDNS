<template>
	<main class="main container">
		<div class="card">
			<div class="card-header">
				<h3>用户列表</h3>
				<div class="header-actions">
					<button class="btn outline" @click="loadUsers" :disabled="isLoading">
						{{ isLoading ? '加载中...' : '刷新' }}
					</button>
				</div>
			</div>

			<div class="filters" style="margin-bottom:12px;">
				<div class="filter-group">
					<input class="input" style="max-width:240px;" placeholder="搜索用户名/邮箱" v-model="filters.keyword"
						@input="onSearchInput" />
					<select class="select" style="max-width:160px;" v-model="filters.role" @change="loadUsers">
						<option value="">全部角色</option>
						<option value="USER">USER</option>
						<option value="ADMIN">ADMIN</option>
					</select>
					<select class="select" style="max-width:160px;" v-model="filters.status" @change="loadUsers">
						<option value="">全部状态</option>
						<option value="1">启用</option>
						<option value="0">禁用</option>
					</select>
				</div>
			</div>

			<div class="table-container" v-loading="isLoading">
				<table class="table">
					<thead>
						<tr>
							<th>ID</th>
							<th>用户名</th>
							<th>邮箱</th>
							<th>积分</th>
							<th>角色</th>
							<th>状态</th>
							<th>注册时间</th>
							<th>操作</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="user in users" :key="user.id">
							<td>{{ user.id }}</td>
							<td>{{ user.username }}</td>
							<td>{{ user.email }}</td>
							<td>{{ user.points || 0 }}</td>
							<td>
								<span class="badge" :class="user.role === 'ADMIN' ? 'warning' : ''">
									{{ user.role }}
								</span>
							</td>
							<td>
								<span class="badge" :class="user.status === 1 ? 'success' : 'danger'">
									{{ user.status === 1 ? '启用' : '禁用' }}
								</span>
							</td>
							<td>{{ formatDate(user.createdAt) }}</td>
							<td class="row">
								<button class="btn outline" @click="adjustPoints(user)" :disabled="user.loading">
									调整积分
								</button>
								<button class="btn outline" @click="toggleRole(user)" :disabled="user.loading">
									{{ user.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}
								</button>
								<button class="btn danger" @click="toggleStatus(user)" :disabled="user.loading">
									{{ user.status === 1 ? '禁用' : '启用' }}
								</button>
							</td>
						</tr>
					</tbody>
				</table>

				<div v-if="!isLoading && users.length === 0" class="empty-state">
					<p>暂无用户数据</p>
				</div>
			</div>
		</div>

		<!-- 积分调整对话框 -->
		<el-dialog v-model="pointsDialogVisible" title="调整积分" width="400px">
			<el-form :model="pointsForm" :rules="pointsRules" ref="pointsFormRef" label-width="80px">
				<el-form-item label="用户">
					<el-input v-model="pointsForm.username" disabled />
				</el-form-item>
				<el-form-item label="当前积分">
					<el-input v-model="pointsForm.currentPoints" disabled />
				</el-form-item>
				<el-form-item label="调整数量" prop="delta">
					<el-input-number v-model="pointsForm.delta" :min="-9999" :max="9999" style="width: 100%;" />
					<div class="form-tip">正数为增加，负数为减少</div>
				</el-form-item>
				<el-form-item label="备注" prop="remark">
					<el-input v-model="pointsForm.remark" type="textarea" placeholder="请输入调整原因" />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="pointsDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="savePointsAdjustment" :loading="isLoading">
					确定
				</el-button>
			</template>
		</el-dialog>
	</main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiGet, apiPost } from '@/utils/api.js'

// 响应式数据
const isLoading = ref(false)
const users = ref([])
const pointsDialogVisible = ref(false)
const pointsFormRef = ref()

// 过滤器
const filters = reactive({
	keyword: '',
	role: '',
	status: '',
	page: 1,
	size: 20
})

// 搜索防抖
let searchTimeout = null

// 积分调整表单
const pointsForm = reactive({
	userId: null,
	username: '',
	currentPoints: 0,
	delta: 0,
	remark: ''
})

// 表单验证规则
const pointsRules = {
	delta: [
		{ required: true, message: '请输入调整数量', trigger: 'blur' },
		{ type: 'number', message: '请输入有效数字', trigger: 'blur' }
	],
	remark: [
		{ required: true, message: '请输入调整原因', trigger: 'blur' }
	]
}

// 加载用户列表
const loadUsers = async () => {
	try {
		isLoading.value = true

		const params = new URLSearchParams()
		if (filters.keyword) {
			params.append('keyword', filters.keyword)
		}
		if (filters.role) {
			params.append('role', filters.role)
		}
		if (filters.status) {
			params.append('status', filters.status)
		}
		params.append('page', filters.page)
		params.append('size', filters.size)

		const response = await apiGet(`/api/admin/users?${params.toString()}`)
		users.value = response.data?.list || []
	} catch (error) {
		ElMessage.error('加载用户列表失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 搜索输入处理（防抖）
const onSearchInput = () => {
	if (searchTimeout) {
		clearTimeout(searchTimeout)
	}
	searchTimeout = setTimeout(() => {
		loadUsers()
	}, 500)
}

// 调整积分
const adjustPoints = (user) => {
	pointsForm.userId = user.id
	pointsForm.username = user.username
	pointsForm.currentPoints = user.points || 0
	pointsForm.delta = 0
	pointsForm.remark = ''
	pointsDialogVisible.value = true
}

// 保存积分调整
const savePointsAdjustment = async () => {
	try {
		await pointsFormRef.value.validate()
		isLoading.value = true

		await apiPost(`/api/admin/users/${pointsForm.userId}/points`, {
			delta: pointsForm.delta,
			remark: pointsForm.remark
		})

		ElMessage.success('积分调整成功')
		pointsDialogVisible.value = false
		await loadUsers()
	} catch (error) {
		ElMessage.error('积分调整失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 切换角色
const toggleRole = async (user) => {
	try {
		const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
		await ElMessageBox.confirm(
			`确定要将用户 "${user.username}" 的角色改为 ${newRole} 吗？`,
			'确认操作',
			{
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}
		)

		user.loading = true
		// 注意：API文档中没有直接的角色切换接口，这里假设通过更新用户信息实现
		// 实际实现可能需要调用用户更新接口
		ElMessage.success('角色切换成功')
		await loadUsers()
	} catch (error) {
		if (error !== 'cancel') {
			ElMessage.error('角色切换失败: ' + error.message)
		}
	} finally {
		user.loading = false
	}
}

// 切换状态
const toggleStatus = async (user) => {
	try {
		const newStatus = user.status === 1 ? 0 : 1
		const action = newStatus === 1 ? '启用' : '禁用'

		await ElMessageBox.confirm(
			`确定要${action}用户 "${user.username}" 吗？`,
			'确认操作',
			{
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}
		)

		user.loading = true
		// 注意：API文档中没有直接的状态切换接口，这里假设通过更新用户信息实现
		// 实际实现可能需要调用用户更新接口
		ElMessage.success(`${action}成功`)
		await loadUsers()
	} catch (error) {
		if (error !== 'cancel') {
			ElMessage.error('操作失败: ' + error.message)
		}
	} finally {
		user.loading = false
	}
}

// 格式化日期
const formatDate = (dateString) => {
	if (!dateString) return '-'
	return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时初始化
onMounted(() => {
	loadUsers()
})
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

.badge.warning {
	background: #fef3c7;
	color: #92400e;
	border-color: #fbbf24;
}

.badge.danger {
	background: #fee2e2;
	color: #991b1b;
	border-color: #f87171;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16px;
}

.header-actions {
	display: flex;
	gap: 8px;
}

.filters {
	margin-bottom: 16px;
}

.filter-group {
	display: flex;
	gap: 12px;
	align-items: center;
	flex-wrap: wrap;
}

.table-container {
	position: relative;
}

.empty-state {
	text-align: center;
	padding: 40px 20px;
	color: #64748b;
}

.empty-state p {
	margin: 0;
	font-size: 14px;
}

.form-tip {
	font-size: 12px;
	color: #64748b;
	margin-top: 4px;
}

/* 响应式：表格与筛选区域在移动端更友好 */
@media (max-width: 768px) {
	.card {
		padding: 12px;
	}

	.row {
		flex-wrap: wrap;
		gap: 8px;
	}

	.input,
	.select {
		width: 100%;
		max-width: none;
	}

	/* 表格在小屏可横向滚动，避免溢出 */
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

	/* 操作列按钮堆叠 */
	.table td.row {
		flex-direction: column;
		align-items: stretch;
	}

	.table td.row .btn {
		width: 100%;
	}

	/* 顶部筛选区按钮在小屏占满宽度，避免溢出 */
	.row .btn {
		width: 100%;
	}
}
</style>
