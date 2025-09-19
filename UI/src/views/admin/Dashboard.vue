<template>
	<div class="dashboard">
		<!-- 统计卡片 -->
		<div class="stats-grid">
			<el-card class="stat-card" v-for="stat in stats" :key="stat.key">
				<div class="stat-content">
					<div class="stat-info">
						<div class="stat-label">{{ stat.label }}</div>
						<div class="stat-value">{{ stat.value }}</div>
					</div>
					<span class="stat-badge" :class="stat.badgeClass" v-if="stat.badge">
						{{ stat.badge }}
					</span>
				</div>
			</el-card>
		</div>

		<!-- 图表区域 -->
		<div class="charts-grid">
			<el-card class="chart-card">
				<h3>最近 7 天用户注册</h3>
				<div class="chart-placeholder"></div>
			</el-card>
			<el-card class="chart-card">
				<h3>最近 7 天 DNS 变更</h3>
				<div class="chart-placeholder"></div>
			</el-card>
		</div>

		<!-- 详细信息区域 -->
		<div class="info-grid">
			<el-card class="info-card">
				<h3>Top Zones</h3>
				<el-table :data="topZones" class="zones-table">
					<el-table-column prop="name" label="域名" />
					<el-table-column prop="count" label="记录数" />
					<el-table-column prop="status" label="状态">
						<template #default="{ row }">
							<span class="badge" :class="row.status === '启用' ? 'badge-success' : 'badge-default'">
								{{ row.status }}
							</span>
						</template>
					</el-table-column>
				</el-table>
			</el-card>

			<el-card class="info-card">
				<h3>同步任务与队列</h3>
				<div class="sync-status">
					<div class="sync-tags">
						<span class="badge badge-default">运行中任务 {{ syncStatus.running }}</span>
						<span class="badge badge-warn">24h 失败 {{ syncStatus.failed }}</span>
						<span class="badge badge-default">队列长度 {{ syncStatus.queued }}</span>
					</div>
					<div class="sync-actions">
						<el-button class="btn outline" @click="$router.push('/admin/zones')">进入同步</el-button>
						<el-button class="btn primary" @click="triggerFullSync"
							:loading="syncLoading">手动全量同步</el-button>
					</div>
				</div>
			</el-card>

			<el-card class="info-card">
				<h3>最近操作</h3>
				<el-table :data="recentActions" class="actions-table">
					<el-table-column prop="user" label="用户" />
					<el-table-column prop="action" label="动作" />
					<el-table-column prop="detail" label="说明" />
					<el-table-column prop="time" label="时间" />
				</el-table>
			</el-card>
		</div>
	</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 统计数据
const stats = ref([
	{
		key: 'users',
		label: '总用户数',
		value: '1,024',
		badge: '本周 +34',
		badgeClass: 'badge-default'
	},
	{
		key: 'zones',
		label: '启用分发域名',
		value: '30',
		badge: '总域名 45',
		badgeClass: 'badge-success'
	},
	{
		key: 'records',
		label: 'DNS 记录总数',
		value: '2,345',
		badge: '今日新增 12',
		badgeClass: 'badge-default'
	},
	{
		key: 'points',
		label: '系统总积分',
		value: '5,120',
		badge: '活跃用户 120',
		badgeClass: 'badge-default'
	}
])

// Top Zones 数据
const topZones = ref([
	{ name: 'example.com', count: 400, status: '启用' },
	{ name: 'example.net', count: 210, status: '禁用' },
	{ name: 'test.org', count: 150, status: '启用' },
	{ name: 'demo.io', count: 89, status: '启用' }
])

// 同步状态
const syncStatus = ref({
	running: 2,
	failed: 1,
	queued: 3
})

const syncLoading = ref(false)

// 最近操作
const recentActions = ref([
	{ user: 'admin', action: 'sync_zone', detail: 'example.com', time: '刚刚' },
	{ user: 'alice', action: 'apply_domain', detail: 'a.example.com', time: '5 分钟前' },
	{ user: 'bob', action: 'delete_domain', detail: 'b.example.net', time: '10 分钟前' },
	{ user: 'admin', action: 'update_settings', detail: '系统设置', time: '1 小时前' }
])

// 获取动作类型
const getActionType = (action) => {
	const typeMap = {
		'sync_zone': 'primary',
		'apply_domain': 'success',
		'delete_domain': 'danger',
		'update_settings': 'warning'
	}
	return typeMap[action] || 'info'
}

// 获取动作标签
const getActionLabel = (action) => {
	const labelMap = {
		'sync_zone': '同步域名',
		'apply_domain': '申请域名',
		'delete_domain': '删除域名',
		'update_settings': '更新设置'
	}
	return labelMap[action] || action
}

// 刷新同步状态
const refreshSyncStatus = async () => {
	try {
		// 这里应该调用API获取同步状态
		ElMessage.success('同步状态已刷新')
	} catch (error) {
		ElMessage.error('刷新失败')
	}
}

// 触发全量同步
const triggerFullSync = async () => {
	syncLoading.value = true
	try {
		// 这里应该调用API触发全量同步
		await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟API调用
		ElMessage.success('全量同步已启动')
	} catch (error) {
		ElMessage.error('同步启动失败')
	} finally {
		syncLoading.value = false
	}
}

// 刷新最近操作
const refreshRecentActions = async () => {
	try {
		// 这里应该调用API获取最近操作
		ElMessage.success('最近操作已刷新')
	} catch (error) {
		ElMessage.error('刷新失败')
	}
}

// 加载仪表板数据
const loadDashboardData = async () => {
	try {
		// 这里应该调用API获取仪表板数据
		console.log('加载仪表板数据...')
	} catch (error) {
		ElMessage.error('数据加载失败')
	}
}

onMounted(() => {
	loadDashboardData()
})
</script>

<style scoped>
.dashboard {
	display: flex;
	flex-direction: column;
	gap: 16px;
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

/* 统计卡片样式 - 参照原型图 */
.stats-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 16px;
}

.stat-card {
	border-radius: 12px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
	border: 1px solid #e2e8f0;
}

.stat-content {
	display: flex;
	justify-content: space-between;
	align-items: baseline;
}

.stat-info {
	display: flex;
	flex-direction: column;
	gap: 0;
}

.stat-label {
	font-size: 12px;
	color: #64748b;
	font-weight: 500;
	margin-bottom: 4px;
}

.stat-value {
	font-size: 28px;
	font-weight: 700;
	color: #0f172a;
	line-height: 1;
}

.stat-badge {
	display: inline-flex;
	align-items: center;
	padding: 4px 8px;
	border-radius: 999px;
	font-size: 12px;
	border: 1px solid #e2e8f0;
	color: #334155;
	background: #f8fafc;
}

.stat-badge.badge-success {
	background: #ecfdf5;
	color: #065f46;
	border-color: #a7f3d0;
}

/* 图表区域样式 */
.charts-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16px;
}

.chart-card {
	border-radius: 12px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
	border: 1px solid #e2e8f0;
}

.chart-card h3 {
	margin: 0 0 16px 0;
	font-size: 16px;
	color: #0f172a;
	font-weight: 600;
}

.chart-placeholder {
	height: 180px;
	background: repeating-linear-gradient(45deg, #f1f5f9, #f1f5f9 10px, #e2e8f0 10px, #e2e8f0 20px);
	border-radius: 12px;
	border: 1px dashed #cbd5e1;
}

/* 详细信息区域样式 */
.info-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 16px;
}

.info-card {
	border-radius: 12px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
	border: 1px solid #e2e8f0;
}

.info-card h3 {
	margin: 0 0 16px 0;
	font-size: 16px;
	color: #0f172a;
	font-weight: 600;
}

/* 表格样式 */
.zones-table,
.actions-table {
	width: 100%;
	border-collapse: collapse;
	border: 1px solid #e2e8f0;
	border-radius: 12px;
	overflow: hidden;
}

.zones-table th,
.zones-table td,
.actions-table th,
.actions-table td {
	text-align: left;
	padding: 12px 10px;
	border-bottom: 1px solid #e2e8f0;
	font-size: 14px;
}

.zones-table th,
.actions-table th {
	background: #f1f5f9;
	color: #0f172a;
}

.zones-table tr:hover td,
.actions-table tr:hover td {
	background: #f8fafc;
}

/* Badge 样式 */
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

.badge.badge-success {
	background: #ecfdf5;
	color: #065f46;
	border-color: #a7f3d0;
}

.badge.badge-warn {
	background: #fff7ed;
	color: #9a3412;
	border-color: #fed7aa;
}

/* 同步状态样式 */
.sync-status {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.sync-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
}

.sync-actions {
	display: flex;
	gap: 8px;
	margin-top: 12px;
}

/* 响应式设计 */
@media (max-width: 960px) {
	.stats-grid {
		grid-template-columns: repeat(2, 1fr);
	}
}

@media (max-width: 720px) {
	.stats-grid {
		grid-template-columns: 1fr;
	}

	.charts-grid {
		grid-template-columns: 1fr;
	}

	.info-grid {
		grid-template-columns: 1fr;
	}

	.sync-tags {
		flex-direction: column;
		align-items: flex-start;
	}

	.sync-actions {
		flex-direction: column;
	}
}
</style>
