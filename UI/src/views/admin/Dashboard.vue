<template>
	<div class="grid cols-4">
		<el-card class="card">
			<div class="grid">
				<div style="font-size:12px;color:#64748b;">总用户数</div>
				<div style="font-weight:700;font-size:24px;">1,024</div>
			</div>
		</el-card>
		<el-card class="card">
			<div class="grid">
				<div style="font-size:12px;color:#64748b;">启用分发域名</div>
				<div style="font-weight:700;font-size:24px;">30</div>
			</div>
		</el-card>
		<el-card class="card">
			<div class="grid">
				<div style="font-size:12px;color:#64748b;">DNS 记录总数</div>
				<div style="font-weight:700;font-size:24px;">2,345</div>
			</div>
		</el-card>
		<el-card class="card">
			<div class="grid">
				<div style="font-size:12px;color:#64748b;">系统总积分</div>
				<div style="font-weight:700;font-size:24px;">5,120</div>
			</div>
		</el-card>
	</div>
	<div class="grid cols-2" style="margin-top:16px;">
		<el-card class="card"><template #header>最近 7 天用户注册</template><div style="height:180px;border:1px dashed #cbd5e1;border-radius:12px;"></div></el-card>
		<el-card class="card"><template #header>最近 7 天 DNS 变更</template><div style="height:180px;border:1px dashed #cbd5e1;border-radius:12px;"></div></el-card>
	</div>
	<div class="grid cols-3" style="margin-top:16px;">
		<el-card class="card">
			<template #header>Top Zones</template>
			<el-table :data="[{name:'example.com',count:400,status:'启用'},{name:'example.net',count:210,status:'禁用'}]">
				<el-table-column prop="name" label="域名" />
				<el-table-column prop="count" label="记录数" />
				<el-table-column prop="status" label="状态" />
			</el-table>
		</el-card>
		<el-card class="card">
			<template #header>同步任务与队列</template>
			<el-space>
				<el-tag>运行中任务 2</el-tag>
				<el-tag type="warning">24h 失败 1</el-tag>
				<el-tag>队列长度 3</el-tag>
			</el-space>
			<div style="margin-top:12px;">
				<el-button @click="$router.push('/admin/zones')">进入同步</el-button>
				<el-button type="primary">手动全量同步</el-button>
			</div>
		</el-card>
		<el-card class="card">
			<template #header>最近操作</template>
			<el-table :data="[{user:'admin',action:'sync_zone',detail:'example.com',time:'刚刚'},{user:'alice',action:'apply_domain',detail:'a.example.com',time:'5 分钟前'}]">
				<el-table-column prop="user" label="用户" />
				<el-table-column prop="action" label="动作" />
				<el-table-column prop="detail" label="说明" />
				<el-table-column prop="time" label="时间" />
			</el-table>
		</el-card>
	</div>
</template>
<script setup>
</script>
<style scoped>
</style>
