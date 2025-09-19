<template>
	<div class="cf-accounts">
		<el-card class="proto-card">
			<template #header>
				<h3 class="card-title">添加 Cloudflare 账户</h3>
			</template>
			<el-form label-width="120px" class="proto-form">
				<el-form-item label="账户名称">
					<el-input placeholder="例如：主账号" />
				</el-form-item>
				<el-form-item label="邮箱">
					<el-input placeholder="name@example.com" />
				</el-form-item>
				<el-form-item label="认证类型">
					<el-select model-value="GLOBAL_KEY" style="width: 100%;">
						<el-option label="GLOBAL_KEY" value="GLOBAL_KEY" />
						<el-option label="API_TOKEN" value="API_TOKEN" />
					</el-select>
				</el-form-item>
				<el-form-item label="API Key / Token">
					<el-input placeholder="仅原型，后端将加密存储" />
				</el-form-item>
				<el-form-item>
					<el-button type="primary">保存账户</el-button>
					<el-button plain>测试连接</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card class="proto-card" style="margin-top:16px;">
			<template #header>
				<h3 class="card-title">账户列表</h3>
			</template>
			<el-table :data="tableData" class="proto-table">
				<el-table-column prop="name" label="名称" />
				<el-table-column prop="email" label="邮箱" />
				<el-table-column prop="type" label="类型" />
				<el-table-column label="状态">
					<template #default="{ row }">
						<span class="badge" :class="row.status === '启用' ? 'success' : ''">{{ row.status }}</span>
					</template>
				</el-table-column>
				<el-table-column label="操作" width="220">
					<template #default>
						<el-button plain>禁用</el-button>
						<el-button plain>同步 Zones</el-button>
					</template>
				</el-table-column>
			</el-table>
		</el-card>
	</div>
</template>
<script setup>
import { ref } from 'vue'

const tableData = ref([
	{ name: '主账号', email: 'admin@example.com', type: 'GLOBAL_KEY', status: '启用' }
])
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

/* ===== 还原原型按钮样式 ===== */
.proto-form :deep(.el-button--primary),
.proto-table :deep(.el-button--primary) {
	background: #6366f1;
	border-color: #6366f1;
}

.proto-form :deep(.el-button--primary:hover),
.proto-table :deep(.el-button--primary:hover) {
	background: #4f46e5;
	border-color: #4f46e5;
}

.proto-form :deep(.el-button.is-plain),
.proto-table :deep(.el-button.is-plain) {
	background: #fff;
	border-color: #cbd5e1;
	color: #0f172a;
}

.proto-form :deep(.el-button.is-plain:hover),
.proto-table :deep(.el-button.is-plain:hover) {
	background: #f8fafc;
}

/* 表单标签字号颜色对齐原型 */
.proto-form :deep(.el-form-item__label) {
	color: #475569;
	font-size: 12px;
}
</style>
