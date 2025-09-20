<template>
	<main class="main container">
		<div class="grid cols-2">
			<div class="card">
				<div class="card-header">
					<h3>积分策略</h3>
					<button class="btn outline" @click="loadSettings" :disabled="isLoading">
						{{ isLoading ? '加载中...' : '刷新' }}
					</button>
				</div>
				<div class="form">
					<div class="grid cols-2">
						<div class="input-row">
							<label class="label">注册赠送积分</label>
							<input class="input" v-model.number="settings.initial_register_points" type="number"
								min="0" />
						</div>
						<div class="input-row">
							<label class="label">邀请人奖励</label>
							<input class="input" v-model.number="settings.inviter_points" type="number" min="0" />
						</div>
						<div class="input-row">
							<label class="label">被邀请人奖励</label>
							<input class="input" v-model.number="settings.invitee_points" type="number" min="0" />
						</div>
						<div class="input-row">
							<label class="label">申请域名消耗</label>
							<input class="input" v-model.number="settings.domain_cost_points" type="number" min="1" />
						</div>
						<div class="input-row">
							<label class="label">单用户域名上限</label>
							<input class="input" v-model.number="settings.max_domains_per_user" type="number" min="1" />
						</div>
					</div>
					<div class="row">
						<button class="btn primary" @click="saveSettings" :disabled="isLoading">
							{{ isLoading ? '保存中...' : '保存' }}
						</button>
						<button class="btn outline" @click="resetSettings" :disabled="isLoading">
							重置
						</button>
					</div>
				</div>
			</div>

			<div class="card">
				<div class="card-header">
					<h3>同步与任务</h3>
					<button class="btn primary" @click="triggerSync" :disabled="isLoading">
						{{ isLoading ? '同步中...' : '立即触发同步' }}
					</button>
				</div>
				<div class="form">
					<div class="input-row">
						<label class="label">同步 Cron 表达式</label>
						<input class="input" v-model="settings.sync_cron_expression" placeholder="0 */5 * * * *" />
						<div class="form-tip">
							格式：秒 分 时 日 月 周，如 "0 */5 * * * *" 表示每5分钟执行一次
						</div>
					</div>
					<div class="input-row">
						<label class="label">默认 TTL</label>
						<input class="input" v-model.number="settings.default_ttl" type="number" min="1" max="86400" />
						<div class="form-tip">
							DNS 记录的默认生存时间（秒），范围：1-86400
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="card" style="margin-top:16px;">
			<h3>系统信息</h3>
			<div class="form">
				<div class="grid cols-2">
					<div class="info-item">
						<label class="label">系统版本</label>
						<div class="info-value">v1.0.0</div>
					</div>
					<div class="info-item">
						<label class="label">最后同步时间</label>
						<div class="info-value">{{ lastSyncTime || '未同步' }}</div>
					</div>
					<div class="info-item">
						<label class="label">Cloudflare 账户数</label>
						<div class="info-value">{{ cfAccountCount }}</div>
					</div>
					<div class="info-item">
						<label class="label">启用 Zone 数</label>
						<div class="info-value">{{ enabledZoneCount }}</div>
					</div>
				</div>
			</div>
		</div>
	</main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiGet, apiPut, apiPost } from '@/utils/api.js'
import { useAuthStore } from '@/stores/auth.js'

// 认证store
const authStore = useAuthStore()

// 响应式数据
const isLoading = ref(false)
const lastSyncTime = ref('')
const cfAccountCount = ref(0)
const enabledZoneCount = ref(0)

// 系统设置
const settings = reactive({
	initial_register_points: 5,
	inviter_points: 3,
	invitee_points: 3,
	domain_cost_points: 10,
	max_domains_per_user: 5,
	default_ttl: 120,
	sync_cron_expression: '0 */5 * * * *'
})

// 默认设置（用于重置）
const defaultSettings = {
	initial_register_points: 5,
	inviter_points: 3,
	invitee_points: 3,
	domain_cost_points: 10,
	max_domains_per_user: 5,
	default_ttl: 120,
	sync_cron_expression: '0 */5 * * * *'
}

// 加载系统设置
const loadSettings = async () => {
	try {
		isLoading.value = true

		const response = await apiGet('/api/admin/settings', { token: authStore.adminToken })
		const data = response.data || {}

		// 更新设置
		Object.assign(settings, {
			initial_register_points: parseInt(data.initial_register_points) || 5,
			inviter_points: parseInt(data.inviter_points) || 3,
			invitee_points: parseInt(data.invitee_points) || 3,
			domain_cost_points: parseInt(data.domain_cost_points) || 10,
			max_domains_per_user: parseInt(data.max_domains_per_user) || 5,
			default_ttl: parseInt(data.default_ttl) || 120,
			sync_cron_expression: data.sync_cron_expression || '0 */5 * * * *'
		})

		// 加载统计信息
		await loadStats()

		ElMessage.success('设置加载成功')
	} catch (error) {
		ElMessage.error('加载设置失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 加载统计信息
const loadStats = async () => {
	try {
		// 加载 Cloudflare 账户数量
		const cfResponse = await apiGet('/api/admin/cf-accounts')
		cfAccountCount.value = cfResponse.data?.length || 0

		// 加载启用 Zone 数量
		const zonesResponse = await apiGet('/api/admin/zones?enabled=true')
		enabledZoneCount.value = zonesResponse.data?.length || 0
	} catch (error) {
		console.error('加载统计信息失败:', error)
	}
}

// 保存系统设置
const saveSettings = async () => {
	try {
		isLoading.value = true

		// 验证设置
		if (settings.domain_cost_points < 1) {
			ElMessage.error('申请域名消耗积分必须大于 0')
			return
		}
		if (settings.max_domains_per_user < 1) {
			ElMessage.error('单用户域名上限必须大于 0')
			return
		}
		if (settings.default_ttl < 1 || settings.default_ttl > 86400) {
			ElMessage.error('默认 TTL 必须在 1-86400 之间')
			return
		}

		// 转换数值为字符串（后端要求）
		const settingsData = {
			initial_register_points: settings.initial_register_points.toString(),
			inviter_points: settings.inviter_points.toString(),
			invitee_points: settings.invitee_points.toString(),
			domain_cost_points: settings.domain_cost_points.toString(),
			max_domains_per_user: settings.max_domains_per_user.toString(),
			default_ttl: settings.default_ttl.toString(),
			sync_cron_expression: settings.sync_cron_expression
		}

		await apiPut('/api/admin/settings', settingsData, { token: authStore.adminToken })
		ElMessage.success('设置保存成功')
	} catch (error) {
		ElMessage.error('保存设置失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 重置设置
const resetSettings = () => {
	Object.assign(settings, defaultSettings)
	ElMessage.info('设置已重置为默认值')
}

// 触发同步
const triggerSync = async () => {
	try {
		isLoading.value = true

		await apiPost('/api/admin/zones/sync', {}, { token: authStore.adminToken })
		lastSyncTime.value = new Date().toLocaleString('zh-CN')
		ElMessage.success('同步任务已触发')

		// 重新加载统计信息
		await loadStats()
	} catch (error) {
		ElMessage.error('触发同步失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 组件挂载时加载数据
onMounted(() => {
	loadSettings()
})
</script>

<style scoped>
.grid {
	display: grid;
	gap: 16px;
}

.grid.cols-2 {
	grid-template-columns: repeat(2, minmax(0, 1fr));
}

.grid.cols-3 {
	grid-template-columns: repeat(3, minmax(0, 1fr));
}

.card {
	background: #fff;
	border: 1px solid #e2e8f0;
	border-radius: 12px;
	padding: 16px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, .04);
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16px;
}

.info-item {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.info-value {
	font-size: 14px;
	color: #0f172a;
	font-weight: 500;
}

.form-tip {
	font-size: 12px;
	color: #64748b;
	margin-top: 4px;
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

/* 响应式：两列网格在窄屏改为单列 */
@media (max-width: 960px) {
	.grid.cols-2 {
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
}
</style>
