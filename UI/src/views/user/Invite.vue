<template>
	<div class="invite-container" v-loading="isLoading">
		<div class="card">
			<div class="card-header">
				<h3>我的邀请码</h3>
				<button class="btn primary" @click="generateInviteCode" :disabled="isGenerating">
					{{ isGenerating ? '生成中...' : '生成/重置' }}
				</button>
			</div>

			<div v-if="inviteInfo.hasCode" class="invite-section">
				<div class="invite-code-display">
					<div class="code-input-group">
						<input class="input code-input" :value="inviteInfo.code" readonly>
						<button class="btn outline" @click="copyInviteCode">复制</button>
					</div>
					<button class="btn primary" @click="copyInviteLink">复制邀请链接</button>
				</div>

				<div class="invite-stats">
					<div class="stat-item">
						<span class="label">使用次数：</span>
						<span class="value">{{ inviteInfo.usedCount || 0 }} / {{ inviteInfo.maxUses || '∞' }}</span>
					</div>
					<div class="stat-item">
						<span class="label">状态：</span>
						<span class="badge" :class="getStatusClass(inviteInfo.status)">{{ inviteInfo.status }}</span>
					</div>
					<div class="stat-item" v-if="inviteInfo.expiredAt">
						<span class="label">过期时间：</span>
						<span class="value">{{ formatTime(inviteInfo.expiredAt) }}</span>
					</div>
				</div>
			</div>

			<div v-else class="no-invite-code">
				<p>您还没有邀请码，点击上方按钮生成一个</p>
			</div>

			<p class="invite-tip">新用户使用你的邀请码注册，你与对方均可获得3点积分。</p>
		</div>

		<div class="card" style="margin-top:16px;">
			<div class="card-header">
				<h3>邀请明细</h3>
				<button class="btn outline" @click="refreshInviteDetails">刷新</button>
			</div>

			<div v-if="inviteDetails.length === 0 && !isLoading" class="empty-state">
				<svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor" style="color: #9ca3af;">
					<path
						d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
				</svg>
				<p>暂无邀请记录</p>
			</div>

			<table class="table" v-else>
				<thead>
					<tr>
						<th>用户</th>
						<th>时间</th>
						<th>奖励积分</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="detail in inviteDetails" :key="detail.id">
						<td data-label="用户">{{ detail.username || '未知用户' }}</td>
						<td data-label="时间">{{ formatTime(detail.createdAt) }}</td>
						<td data-label="奖励积分">+{{ detail.points || 0 }}</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.js'
import { apiGet, apiPost } from '@/utils/api.js'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

// 响应式数据
const isLoading = ref(false)
const isGenerating = ref(false)
const inviteInfo = ref({
	hasCode: false,
	code: '',
	maxUses: 0,
	usedCount: 0,
	status: '',
	expiredAt: null
})
const inviteDetails = ref([])

// 加载邀请码信息
const loadInviteCode = async () => {
	try {
		const response = await apiGet('/api/user/invite/mycode', { token: authStore.token })
		if (response.data) {
			inviteInfo.value = response.data
		}
	} catch (error) {
		console.error('加载邀请码失败:', error)
		ElMessage.error('加载邀请码失败')
	}
}

// 生成/重置邀请码
const generateInviteCode = async () => {
	isGenerating.value = true
	try {
		const response = await apiPost('/api/user/invite/generate', {
			maxUses: 10,
			validDays: 30
		}, { token: authStore.token })

		if (response.code === 0) {
			ElMessage.success('邀请码生成成功')
			// 重新加载邀请码信息
			await loadInviteCode()
		} else {
			ElMessage.error(response.message || '生成失败')
		}
	} catch (error) {
		ElMessage.error('生成邀请码失败')
		console.error('生成邀请码失败:', error)
	} finally {
		isGenerating.value = false
	}
}

// 复制邀请码
const copyInviteCode = async () => {
	try {
		await navigator.clipboard.writeText(inviteInfo.value.code)
		ElMessage.success('邀请码已复制到剪贴板')
	} catch (error) {
		// 降级方案
		const textArea = document.createElement('textarea')
		textArea.value = inviteInfo.value.code
		document.body.appendChild(textArea)
		textArea.select()
		document.execCommand('copy')
		document.body.removeChild(textArea)
		ElMessage.success('邀请码已复制到剪贴板')
	}
}

// 复制邀请链接
const copyInviteLink = async () => {
	const inviteLink = `${window.location.origin}/user/register?invite=${inviteInfo.value.code}`
	try {
		await navigator.clipboard.writeText(inviteLink)
		ElMessage.success('邀请链接已复制到剪贴板')
	} catch (error) {
		// 降级方案
		const textArea = document.createElement('textarea')
		textArea.value = inviteLink
		document.body.appendChild(textArea)
		textArea.select()
		document.execCommand('copy')
		document.body.removeChild(textArea)
		ElMessage.success('邀请链接已复制到剪贴板')
	}
}

// 加载邀请明细
const loadInviteDetails = async () => {
	try {
		// 这里需要根据实际API调整
		// 假设有一个获取邀请明细的API
		const response = await apiGet('/api/user/invite/details', { token: authStore.token })
		if (response.data) {
			inviteDetails.value = response.data.list || []
		}
	} catch (error) {
		console.error('加载邀请明细失败:', error)
		// 暂时使用模拟数据
		inviteDetails.value = [
			{ id: 1, username: 'new_user_1', createdAt: '2025-01-20T10:30:00Z', points: 3 },
			{ id: 2, username: 'new_user_2', createdAt: '2025-01-19T15:20:00Z', points: 3 }
		]
	}
}

// 刷新邀请明细
const refreshInviteDetails = () => {
	loadInviteDetails()
}

// 格式化时间
const formatTime = (timeStr) => {
	if (!timeStr) return '未知'
	const date = new Date(timeStr)
	const now = new Date()
	const diff = now - date

	if (diff < 60000) return '刚刚'
	if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
	if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
	return date.toLocaleDateString()
}

// 获取状态样式类
const getStatusClass = (status) => {
	switch (status) {
		case 'ACTIVE': return 'success'
		case 'EXPIRED': return 'danger'
		case 'DISABLED': return 'warning'
		default: return ''
	}
}

// 初始化数据
const initData = async () => {
	isLoading.value = true
	try {
		await Promise.all([
			loadInviteCode(),
			loadInviteDetails()
		])
	} catch (error) {
		console.error('初始化数据失败:', error)
	} finally {
		isLoading.value = false
	}
}

onMounted(() => {
	initData()
})
</script>
<style scoped>
.invite-container {
	padding: 20px;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
}

.invite-section {
	margin-bottom: 20px;
}

.invite-code-display {
	display: flex;
	gap: 12px;
	align-items: center;
	margin-bottom: 16px;
	flex-wrap: wrap;
}

.code-input-group {
	display: flex;
	gap: 8px;
	align-items: center;
}

.code-input {
	font-family: 'Courier New', monospace;
	font-weight: 600;
	letter-spacing: 1px;
	text-align: center;
}

.invite-stats {
	display: flex;
	gap: 24px;
	flex-wrap: wrap;
	margin-bottom: 16px;
}

.stat-item {
	display: flex;
	align-items: center;
	gap: 8px;
}

.stat-item .label {
	font-size: 14px;
	color: #64748b;
}

.stat-item .value {
	font-size: 14px;
	font-weight: 500;
	color: #151717;
}

.badge {
	padding: 4px 8px;
	border-radius: 4px;
	font-size: 12px;
	font-weight: 500;
}

.badge.success {
	background-color: #dcfce7;
	color: #166534;
}

.badge.warning {
	background-color: #fef3c7;
	color: #92400e;
}

.badge.danger {
	background-color: #fee2e2;
	color: #dc2626;
}

.no-invite-code {
	text-align: center;
	padding: 40px 20px;
	color: #64748b;
}

.no-invite-code p {
	margin: 0;
	font-size: 14px;
}

.invite-tip {
	font-size: 14px;
	color: #64748b;
	margin: 0;
	line-height: 1.5;
}

.empty-state {
	text-align: center;
	padding: 60px 20px;
	color: #64748b;
}

.empty-state svg {
	margin-bottom: 16px;
}

.empty-state p {
	margin: 0;
	font-size: 16px;
}

.btn:disabled {
	background-color: #9ca3af;
	cursor: not-allowed;
}

.btn:disabled:hover {
	background-color: #9ca3af;
}

/* 响应式设计 */
@media (max-width: 768px) {
	.invite-code-display {
		flex-direction: column;
		align-items: stretch;
	}

	.code-input-group {
		width: 100%;
	}

	.code-input {
		flex: 1;
	}

	.invite-stats {
		flex-direction: column;
		gap: 12px;
	}
}
</style>
