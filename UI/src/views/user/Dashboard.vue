<template>
	<div class="grid cols-3">
		<el-card class="card"><div style="font-size:12px;color:#64748b;">我的积分</div><div style="font-weight:700;font-size:24px;">8</div></el-card>
		<el-card class="card"><div style="font-size:12px;color:#64748b;">已申请域名</div><div style="font-weight:700;font-size:24px;">2</div></el-card>
		<el-card class="card"><div style="font-size:12px;color:#64748b;">我的邀请码</div><div style="font-weight:700;font-size:24px;">ABCD1234</div></el-card>
	</div>
	<el-row :gutter="16" style="margin-top:16px;">
		<el-col :span="12">
			<el-card class="card">
				<template #header>快速申请</template>
				<el-space wrap>
					<el-select placeholder="选择域名（已启用）" style="width:220px;"><el-option label="example.com" value="example.com" /></el-select>
					<el-input placeholder="前缀，如：app" style="width:200px;" />
					<el-input placeholder="记录值，如：1.2.3.4" style="width:240px;" />
					<el-button type="primary">申请（-5积分）</el-button>
				</el-space>
			</el-card>
		</el-col>
		<el-col :span="12">
			<el-card class="card">
				<template #header>最近申请</template>
				<el-table :data="[{domain:'example.com',record:'a.example.com → 1.2.3.4',time:'刚刚',status:'ACTIVE'}]">
					<el-table-column prop="domain" label="域名" />
					<el-table-column prop="record" label="记录" />
					<el-table-column prop="time" label="时间" />
					<el-table-column prop="status" label="状态" />
				</el-table>
			</el-card>
		</el-col>
	</el-row>
</template>
<script setup>
</script>
<style scoped>
</style>
