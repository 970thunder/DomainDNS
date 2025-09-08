<template>
	<el-row :gutter="16">
		<el-col :span="12">
			<el-card class="card">
				<template #header>积分策略</template>
				<el-form label-width="140px">
					<el-row :gutter="12">
						<el-col :span="12"><el-form-item label="注册赠送积分"><el-input model-value="1" /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="邀请人奖励"><el-input model-value="3" /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="被邀请人奖励"><el-input model-value="3" /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="申请域名消耗"><el-input model-value="5" /></el-form-item></el-col>
						<el-col :span="12"><el-form-item label="单用户域名上限"><el-input model-value="5" /></el-form-item></el-col>
					</el-row>
					<el-form-item><el-button type="primary">保存</el-button><el-button>重置</el-button></el-form-item>
				</el-form>
			</el-card>
		</el-col>
		<el-col :span="12">
			<el-card class="card">
				<template #header>同步与任务</template>
				<el-form label-width="140px">
					<el-form-item label="同步 Cron 表达式"><el-input placeholder="0 */5 * * * *" /></el-form-item>
					<el-form-item label="默认 TTL"><el-input model-value="120" /></el-form-item>
					<el-form-item><el-button type="primary">保存</el-button><el-button>立即触发同步</el-button></el-form-item>
				</el-form>
			</el-card>
		</el-col>
	</el-row>
	<el-card class="card" style="margin-top:16px;">
		<template #header>安全与速率限制</template>
		<el-form label-width="180px">
			<el-row :gutter="12">
				<el-col :span="8"><el-form-item label="注册 IP 限制（/日）"><el-input model-value="5" /></el-form-item></el-col>
				<el-col :span="8"><el-form-item label="申请频率（次/分钟）"><el-input model-value="10" /></el-form-item></el-col>
				<el-col :span="8"><el-form-item label="启用邮箱验证"><el-select model-value="是"><el-option label="是" value="true" /><el-option label="否" value="false" /></el-select></el-form-item></el-col>
			</el-row>
			<el-form-item><el-button type="primary">保存</el-button></el-form-item>
		</el-form>
	</el-card>
</template>
<script setup>
</script>
<style scoped>
</style>
