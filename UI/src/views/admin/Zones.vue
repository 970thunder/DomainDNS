<template>
	<main class="main container">
		<div class="card">
			<div class="card-header">
				<h3>Zones 列表（选择启用分发）</h3>
				<div class="header-actions">
					<button class="btn outline" @click="loadZones" :disabled="isLoading">
						{{ isLoading ? '加载中...' : '刷新列表' }}
					</button>
					<button class="btn primary" @click="syncAll" :disabled="isLoading">
						{{ isLoading ? '同步中...' : '手动同步所有 Zones' }}
					</button>
				</div>
			</div>

			<div class="filters" style="margin-bottom:12px;">
				<div class="filter-group">
					<select class="input" v-model="filters.cfAccountId" @change="loadZones">
						<option value="">所有账户</option>
						<option v-for="account in cfAccounts" :key="account.id" :value="account.id">
							{{ account.name }} ({{ account.email }})
						</option>
					</select>
					<select class="input" v-model="filters.enabled" @change="loadZones">
						<option value="">所有状态</option>
						<option value="true">已启用</option>
						<option value="false">已禁用</option>
					</select>
					<input class="input" style="max-width:260px;" placeholder="搜索域名..." v-model="filters.name"
						@input="onSearchInput" />
				</div>
			</div>

			<div class="table-container" v-loading="isLoading">
				<table class="table">
					<thead>
						<tr>
							<th>域名</th>
							<th>Zone ID</th>
							<th>Cloudflare 账户</th>
							<th>状态</th>
							<th>同步时间</th>
							<th>操作</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="zone in filteredZones" :key="zone.id">
							<td>{{ zone.name }}</td>
							<td class="zone-id">{{ zone.zoneId }}</td>
							<td>{{ getCfAccountName(zone.cfAccountId) }}</td>
							<td>
								<span class="badge" :class="zone.enabled ? 'success' : ''">
									{{ zone.enabled ? '启用' : '禁用' }}
								</span>
							</td>
							<td>{{ formatDate(zone.syncedAt) }}</td>
							<td class="row">
								<button class="btn outline" @click="toggleZone(zone)" :disabled="zone.loading">
									{{ zone.loading ? '处理中...' : (zone.enabled ? '禁用' : '启用') }}
								</button>
								<button class="btn outline" @click="viewRecords(zone)">
									查看记录
								</button>
								<button class="btn primary" @click="syncZoneRecords(zone)" :disabled="zone.syncing">
									{{ zone.syncing ? '同步中...' : '同步记录' }}
								</button>
							</td>
						</tr>
					</tbody>
				</table>

				<div v-if="!isLoading && filteredZones.length === 0" class="empty-state">
					<p>暂无 Zones 数据</p>
					<button class="btn primary" @click="syncAll">立即同步</button>
				</div>
			</div>
		</div>
	</main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiGet, apiPost } from '@/utils/api.js'
import { useAuthStore } from '@/stores/auth.js'

const router = useRouter()

// 认证store
const authStore = useAuthStore()

// 响应式数据
const isLoading = ref(false)
const zones = ref([])
const cfAccounts = ref([])

// 过滤器
const filters = ref({
	name: '',
	enabled: '',
	cfAccountId: ''
})

// 搜索防抖
let searchTimeout = null

// 计算属性
const filteredZones = computed(() => {
	let result = zones.value

	// 按名称搜索
	if (filters.value.name) {
		const keyword = filters.value.name.toLowerCase()
		result = result.filter(zone =>
			zone.name.toLowerCase().includes(keyword)
		)
	}

	// 按状态过滤
	if (filters.value.enabled !== '') {
		const enabled = filters.value.enabled === 'true'
		result = result.filter(zone => zone.enabled === enabled)
	}

	// 按 Cloudflare 账户过滤
	if (filters.value.cfAccountId) {
		result = result.filter(zone => zone.cfAccountId == filters.value.cfAccountId)
	}

	return result
})

// 加载 Cloudflare 账户列表
const loadCfAccounts = async () => {
	try {
		const response = await apiGet('/api/admin/cf-accounts', { token: authStore.adminToken })
		cfAccounts.value = response.data || []
	} catch (error) {
		console.error('加载 Cloudflare 账户失败:', error)
	}
}

// 加载 Zones 列表
const loadZones = async () => {
	try {
		isLoading.value = true

		const params = new URLSearchParams()
		if (filters.value.enabled !== '') {
			params.append('enabled', filters.value.enabled)
		}
		if (filters.value.name) {
			params.append('name', filters.value.name)
		}
		if (filters.value.cfAccountId) {
			params.append('cfAccountId', filters.value.cfAccountId)
		}

		const response = await apiGet(`/api/admin/zones?${params.toString()}`, { token: authStore.adminToken })
		zones.value = response.data || []
	} catch (error) {
		ElMessage.error('加载 Zones 列表失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 同步所有 Zones
const syncAll = async () => {
	try {
		isLoading.value = true

		// 检查是否有可用的 Cloudflare 账户
		if (cfAccounts.value.length === 0) {
			ElMessage.warning('请先添加 Cloudflare 账户')
			return
		}

		await apiPost('/api/admin/zones/sync', {}, { token: authStore.adminToken })
		ElMessage.success('同步所有 Zones 成功')
		await loadZones()
	} catch (error) {
		ElMessage.error('同步失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 同步指定账户的 Zones
const syncByAccount = async (cfAccountId) => {
	try {
		isLoading.value = true

		await apiPost('/api/admin/zones/sync', { cfAccountId }, { token: authStore.adminToken })
		ElMessage.success('同步指定账户 Zones 成功')
		await loadZones()
	} catch (error) {
		ElMessage.error('同步失败: ' + error.message)
	} finally {
		isLoading.value = false
	}
}

// 切换 Zone 状态
const toggleZone = async (zone) => {
	try {
		zone.loading = true

		const action = zone.enabled ? 'disable' : 'enable'
		await apiPost(`/api/admin/zones/${zone.id}/${action}`, {}, { token: authStore.adminToken })

		zone.enabled = !zone.enabled
		ElMessage.success(`Zone "${zone.name}" 已${zone.enabled ? '启用' : '禁用'}`)
	} catch (error) {
		ElMessage.error('操作失败: ' + error.message)
	} finally {
		zone.loading = false
	}
}

// 同步 Zone 的 DNS 记录
const syncZoneRecords = async (zone) => {
	try {
		zone.syncing = true

		await apiPost(`/api/admin/zones/${zone.id}/sync-records`, {}, { token: authStore.adminToken })
		ElMessage.success(`Zone "${zone.name}" 记录同步成功`)

		// 更新同步时间
		zone.syncedAt = new Date().toISOString()
	} catch (error) {
		ElMessage.error('同步记录失败: ' + error.message)
	} finally {
		zone.syncing = false
	}
}

// 查看 DNS 记录
const viewRecords = (zone) => {
	router.push(`/admin/dns-records?zoneId=${zone.id}&zoneName=${encodeURIComponent(zone.name)}`)
}

// 搜索输入处理（防抖）
const onSearchInput = () => {
	if (searchTimeout) {
		clearTimeout(searchTimeout)
	}
	searchTimeout = setTimeout(() => {
		loadZones()
	}, 500)
}

// 获取 Cloudflare 账户名称
const getCfAccountName = (cfAccountId) => {
	const account = cfAccounts.value.find(acc => acc.id === cfAccountId)
	return account ? `${account.name} (${account.email})` : '未知账户'
}

// 格式化日期
const formatDate = (dateString) => {
	if (!dateString) return '-'
	const date = new Date(dateString)
	return date.toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(async () => {
	await loadCfAccounts()
	await loadZones()
})
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

.filter-group select {
	min-width: 150px;
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
	margin: 0 0 16px 0;
	font-size: 14px;
}

.zone-id {
	font-family: monospace;
	font-size: 12px;
	color: #64748b;
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

	.card-header {
		flex-direction: column;
		align-items: stretch;
		gap: 12px;
	}

	.header-actions {
		justify-content: stretch;
	}

	.header-actions .btn {
		flex: 1;
	}

	.filter-group {
		flex-direction: column;
		align-items: stretch;
	}

	.filter-group select,
	.filter-group input {
		width: 100%;
		min-width: auto;
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