<template>
	<div class="cf-accounts">
		<el-card class="proto-card">
			<template #header>
				<h3 class="card-title">添加 Cloudflare 账户</h3>
			</template>
			<el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px" class="proto-form"
				@submit.prevent="handleSubmit">
				<el-form-item label="账户名称" prop="name">
					<el-input v-model="formData.name" placeholder="例如：主账号" :disabled="isLoading" />
				</el-form-item>
				<el-form-item label="邮箱" prop="email">
					<el-input v-model="formData.email" placeholder="name@example.com" :disabled="isLoading" />
				</el-form-item>
				<el-form-item label="认证类型" prop="apiType">
					<el-select v-model="formData.apiType" style="width: 100%;" :disabled="isLoading">
						<el-option label="GLOBAL_KEY" value="GLOBAL_KEY" />
						<el-option label="API_TOKEN" value="API_TOKEN" />
					</el-select>
				</el-form-item>
				<el-form-item label="API Key / Token" prop="apiKey">
					<el-input v-model="formData.apiKey" type="password" placeholder="输入 API Key 或 Token"
						:disabled="isLoading" show-password />
				</el-form-item>
				<el-form-item>
					<div class="form-actions">
						<button class="btn primary" :disabled="isLoading" @click="handleSubmit">
							{{ isLoading ? '保存中...' : '保存账户' }}
						</button>
						<button class="btn outline" :disabled="isLoading" @click="handleTestConnection">
							{{ isLoading ? '测试中...' : '测试连接' }}
						</button>
					</div>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card class="proto-card" style="margin-top:16px;">
			<template #header>
				<div class="card-header">
					<h3 class="card-title">账户列表</h3>
					<button class="btn primary small" :disabled="isLoading" @click="loadAccounts">
						{{ isLoading ? '加载中...' : '刷新' }}
					</button>
				</div>
			</template>
			<el-table :data="accounts" class="proto-table" v-loading="isLoading">
				<el-table-column prop="name" label="名称" />
				<el-table-column prop="email" label="邮箱" />
				<el-table-column prop="apiType" label="类型" />
				<el-table-column label="状态">
					<template #default="{ row }">
						<span class="badge" :class="row.enabled ? 'success' : ''">
							{{ row.enabled ? '启用' : '禁用' }}
						</span>
					</template>
				</el-table-column>
				<el-table-column label="创建时间">
					<template #default="{ row }">
						{{ formatDate(row.createdAt) }}
					</template>
				</el-table-column>
				<el-table-column label="操作" width="280">
					<template #default="{ row }">
						<div class="table-actions">
							<button class="btn small" :class="row.enabled ? 'warning' : 'success'"
								:disabled="row.loading" @click="toggleAccount(row)">
								{{ row.loading ? '处理中...' : (row.enabled ? '禁用' : '启用') }}
							</button>
							<button class="btn outline small" :disabled="row.testing" @click="testConnection(row)">
								{{ row.testing ? '测试中...' : '测试' }}
							</button>
							<button class="btn outline small" @click="editAccount(row)">
								编辑
							</button>
							<!-- <button class="btn danger small" @click="deleteAccount(row)">
								删除
							</button> -->
						</div>
					</template>
				</el-table-column>
			</el-table>
		</el-card>

		<!-- 编辑对话框 -->
		<el-dialog v-model="editDialogVisible" title="编辑账户" width="500px">
			<el-form :model="editFormData" :rules="formRules" ref="editFormRef" label-width="120px">
				<el-form-item label="账户名称" prop="name">
					<el-input v-model="editFormData.name" />
				</el-form-item>
				<el-form-item label="邮箱" prop="email">
					<el-input v-model="editFormData.email" />
				</el-form-item>
				<el-form-item label="认证类型" prop="apiType">
					<el-select v-model="editFormData.apiType" style="width: 100%;">
						<el-option label="GLOBAL_KEY" value="GLOBAL_KEY" />
						<el-option label="API_TOKEN" value="API_TOKEN" />
					</el-select>
				</el-form-item>
				<el-form-item label="API Key / Token" prop="apiKey">
					<el-input v-model="editFormData.apiKey" type="password" placeholder="留空表示不修改" show-password />
				</el-form-item>
			</el-form>
			<template #footer>
				<div class="dialog-actions">
					<button class="btn outline" @click="editDialogVisible = false">取消</button>
					<button class="btn primary" :disabled="isLoading" @click="handleUpdate">
						{{ isLoading ? '保存中...' : '保存' }}
					</button>
				</div>
			</template>
		</el-dialog>
	</div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiGet, apiPost, apiPut, apiDelete } from '@/utils/api.js'
import { useAuthStore } from '@/stores/auth.js'

// 认证store
const authStore = useAuthStore()

// 响应式数据
const isLoading = ref(false)
const accounts = ref([])
const editDialogVisible = ref(false)
const formRef = ref()
const editFormRef = ref()

// 表单数据
const formData = reactive({
	name: '',
	email: '',
	apiType: 'GLOBAL_KEY',
	apiKey: '',
	enabled: true
})

const editFormData = reactive({
	id: null,
	name: '',
	email: '',
	apiType: 'GLOBAL_KEY',
	apiKey: ''
})

// 表单验证规则
const formRules = {
	name: [
		{ required: true, message: '请输入账户名称', trigger: 'blur' }
	],
	email: [
		{ required: true, message: '请输入邮箱', trigger: 'blur' },
		{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
	],
	apiType: [
		{ required: true, message: '请选择认证类型', trigger: 'change' }
	],
	apiKey: [
		{ required: true, message: '请输入 API Key 或 Token', trigger: 'blur' }
	]
}

// 加载账户列表
const loadAccounts = async () => {
	try {
		isLoading.value = true

		// 调试信息
		console.log('当前管理员token:', authStore.adminToken)
		console.log('管理员登录状态:', authStore.isAdminLoggedIn)

		const response = await apiGet('/api/admin/cf-accounts', { token: authStore.adminToken })
		accounts.value = response.data || []
	} catch (error) {
		ElMessage.error('加载账户列表失败: ' + error.message)
		console.error('API调用错误:', error)
	} finally {
		isLoading.value = false
	}
}

// 创建账户
const handleSubmit = async () => {
	try {
		await formRef.value.validate()
		isLoading.value = true

		const response = await apiPost('/api/admin/cf-accounts', formData, { token: authStore.adminToken })

		ElMessage.success('账户创建成功')

		// 重置表单
		Object.assign(formData, {
			name: '',
			email: '',
			apiType: 'GLOBAL_KEY',
			apiKey: '',
			enabled: true
		})
		formRef.value.resetFields()

		// 重新加载列表
		await loadAccounts()
	} catch (error) {
		ElMessage.error('创建账户失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 测试连接（新建时）
const handleTestConnection = async () => {
	try {
		await formRef.value.validate()
		isLoading.value = true

		// 这里需要先创建临时账户进行测试，或者后端提供测试接口
		ElMessage.info('测试连接功能需要先保存账户')
	} catch (error) {
		ElMessage.error('请先填写完整信息')
	} finally {
		isLoading.value = false
	}
}

// 测试连接（已存在账户）
const testConnection = async (account) => {
	try {
		account.testing = true
		const response = await apiPost(`/api/admin/cf-accounts/${account.id}/test`, {}, { token: authStore.adminToken })
		ElMessage.success('连接测试成功')
	} catch (error) {
		ElMessage.error('连接测试失败: ' + error.message)
	} finally {
		account.testing = false
	}
}

// 切换账户状态
const toggleAccount = async (account) => {
	try {
		account.loading = true
		const action = account.enabled ? 'disable' : 'enable'
		await apiPost(`/api/admin/cf-accounts/${account.id}/${action}`, {}, { token: authStore.adminToken })

		account.enabled = !account.enabled
		ElMessage.success(`账户已${account.enabled ? '启用' : '禁用'}`)
	} catch (error) {
		ElMessage.error('操作失败: ' + error.message)
	} finally {
		account.loading = false
	}
}

// 编辑账户
const editAccount = (account) => {
	Object.assign(editFormData, {
		id: account.id,
		name: account.name,
		email: account.email,
		apiType: account.apiType,
		apiKey: '' // 不显示现有 API Key
	})
	editDialogVisible.value = true
}

// 更新账户
const handleUpdate = async () => {
	try {
		await editFormRef.value.validate()
		isLoading.value = true

		const updateData = {
			name: editFormData.name,
			email: editFormData.email,
			apiType: editFormData.apiType
		}

		// 只有填写了新的 API Key 才更新
		if (editFormData.apiKey) {
			updateData.apiKey = editFormData.apiKey
		}

		await apiPut(`/api/admin/cf-accounts/${editFormData.id}`, updateData, { token: authStore.adminToken })

		ElMessage.success('账户更新成功')
		editDialogVisible.value = false
		await loadAccounts()
	} catch (error) {
		ElMessage.error('更新账户失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// // 删除账户
// const deleteAccount = async (account) => {
// 	try {
// 		await ElMessageBox.confirm(
// 			`确定要删除账户 "${account.name}" 吗？此操作不可恢复。`,
// 			'确认删除',
// 			{
// 				confirmButtonText: '确定',
// 				cancelButtonText: '取消',
// 				type: 'warning'
// 			}
// 		)

// 		await apiDelete(`/api/admin/cf-accounts/${account.id}`)
// 		ElMessage.success('账户删除成功')
// 		await loadAccounts()
// 	} catch (error) {
// 		if (error !== 'cancel') {
// 			ElMessage.error('删除账户失败: ' + error.message)
// 		}
// 	}
// }

// 格式化日期
const formatDate = (dateString) => {
	if (!dateString) return '-'
	const date = new Date(dateString)
	return date.toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
	loadAccounts()
})
</script>
<style scoped>
.cf-accounts {
	display: flex;
	flex-direction: column;
	gap: 16px;
}

.proto-card {
	border-radius: 12px;
	border: 1px solid #e2e8f0;
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.proto-card :deep(.el-card__header) {
	padding: 16px;
	border-bottom: 1px solid #e2e8f0;
}

.proto-card :deep(.el-card__body) {
	padding: 16px;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.card-title {
	margin: 0;
	font-size: 16px;
	color: #0f172a;
	font-weight: 600;
}

.proto-form :deep(.el-form-item) {
	margin-bottom: 12px;
}

.proto-table :deep(.el-table__header th) {
	background: #f1f5f9;
	color: #0f172a;
}

.proto-table :deep(th),
.proto-table :deep(td) {
	padding: 12px 10px;
	font-size: 14px;
}

.proto-table :deep(.el-table__row:hover>td) {
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

/* ===== 还原原型输入框样式 ===== */
.proto-form :deep(.el-input),
.proto-form :deep(.el-select) {
	width: 100%;
}

.proto-form :deep(.el-input__wrapper),
.proto-form :deep(.el-select .el-select__wrapper) {
	border-radius: 10px;
	box-shadow: 0 0 0 1px #cbd5e1 inset !important;
	padding: 2px 12px;
	background: #fff;
}

.proto-form :deep(.is-focus .el-input__wrapper),
.proto-form :deep(.el-input__wrapper.is-focus),
.proto-form :deep(.el-select .is-focused .el-select__wrapper) {
	box-shadow: 0 0 0 1px #6366f1 inset, 0 0 0 3px rgba(99, 102, 241, .15) !important;
}

.proto-form :deep(.el-input__inner) {
	height: 36px;
}

.proto-form :deep(.el-input__inner::placeholder) {
	color: #94a3b8;
}

/* Element Plus 按钮样式覆盖 - 保持一致性 */
.proto-form :deep(.el-button),
.proto-table :deep(.el-button) {
	border-radius: 8px;
	font-weight: 500;
	transition: all 0.2s ease;
}

.proto-form :deep(.el-button--primary) {
	background: #6366f1;
	border-color: #6366f1;
}

.proto-form :deep(.el-button--primary:hover) {
	background: #4f46e5;
	border-color: #4f46e5;
	transform: translateY(-1px);
	box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.proto-form :deep(.el-button.is-plain) {
	background: #fff;
	border-color: #cbd5e1;
	color: #475569;
}

.proto-form :deep(.el-button.is-plain:hover) {
	background: #f8fafc;
	border-color: #94a3b8;
	transform: translateY(-1px);
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 表单标签字号颜色对齐原型 */
.proto-form :deep(.el-form-item__label) {
	color: #475569;
	font-size: 12px;
}

/* 按钮基础样式 */
.btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 6px;
	padding: 10px 16px;
	border-radius: 8px;
	border: 1px solid transparent;
	font-weight: 500;
	font-size: 14px;
	cursor: pointer;
	transition: all 0.2s ease;
	min-height: 36px;
	white-space: nowrap;
}

.btn:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

.btn.small {
	padding: 6px 12px;
	font-size: 12px;
	min-height: 28px;
}

/* 主要按钮 */
.btn.primary {
	background: #6366f1;
	color: #fff;
	border-color: #6366f1;
}

.btn.primary:hover:not(:disabled) {
	background: #4f46e5;
	border-color: #4f46e5;
	transform: translateY(-1px);
	box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

/* 轮廓按钮 */
.btn.outline {
	background: #fff;
	border-color: #cbd5e1;
	color: #475569;
}

.btn.outline:hover:not(:disabled) {
	background: #f8fafc;
	border-color: #94a3b8;
	transform: translateY(-1px);
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 成功按钮 */
.btn.success {
	background: #10b981;
	color: #fff;
	border-color: #10b981;
}

.btn.success:hover:not(:disabled) {
	background: #059669;
	border-color: #059669;
	transform: translateY(-1px);
	box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

/* 警告按钮 - 蓝紫色 */
.btn.warning {
	background: #8b5cf6;
	color: #fff;
	border-color: #8b5cf6;
}

.btn.warning:hover:not(:disabled) {
	background: #7c3aed;
	border-color: #7c3aed;
	transform: translateY(-1px);
	box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

/* 危险按钮 */
.btn.danger {
	background: #ef4444;
	color: #fff;
	border-color: #ef4444;
}

.btn.danger:hover:not(:disabled) {
	background: #dc2626;
	border-color: #dc2626;
	transform: translateY(-1px);
	box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

/* 按钮组布局 */
.form-actions {
	display: flex;
	gap: 12px;
	align-items: center;
}

.table-actions {
	display: flex;
	gap: 8px;
	align-items: center;
	flex-wrap: wrap;
}

.dialog-actions {
	display: flex;
	gap: 12px;
	justify-content: flex-end;
	align-items: center;
}
</style>
