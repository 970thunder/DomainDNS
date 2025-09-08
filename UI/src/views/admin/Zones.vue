<template>
	<el-card class="card">
		<template #header>Zones 列表（选择启用分发）</template>
		<div class="container" style="padding-left:0;padding-right:0;">
			<el-space>
				<el-button>刷新列表</el-button>
				<el-button type="primary">手动同步所有 Zones</el-button>
				<el-input placeholder="搜索域名..." style="width:260px;" />
			</el-space>
			<el-table style="margin-top:12px;" :data="rows">
				<el-table-column prop="name" label="域名" />
				<el-table-column prop="zoneId" label="Zone ID" />
				<el-table-column prop="status" label="状态" />
				<el-table-column prop="syncedAt" label="同步时间" />
				<el-table-column label="操作">
					<template #default>
						<el-button>启用/禁用</el-button>
						<el-button @click="$router.push('/admin/dns-records')">查看记录</el-button>
						<el-button type="primary">同步记录</el-button>
					</template>
				</el-table-column>
			</el-table>
		</div>
	</el-card>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ name:'example.com', zoneId:'abcd1234', status:'启用', syncedAt:'2025-09-01 12:30' },
	{ name:'example.net', zoneId:'efgh5678', status:'禁用', syncedAt:'2025-08-31 20:10' },
])
</script>
<style scoped>
</style>
