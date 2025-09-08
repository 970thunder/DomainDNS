<template>
	<el-card class="card">
		<template #header>积分充值</template>
		<el-form label-width="120px">
			<el-form-item label="充值金额（¥）"><el-input placeholder="例如：10" /></el-form-item>
			<el-form-item label="预计获得积分"><el-input model-value="10" disabled /></el-form-item>
			<el-form-item>
				<el-button type="primary">创建订单</el-button>
				<el-button>查看历史订单</el-button>
			</el-form-item>
		</el-form>
	</el-card>
</template>
<script setup>
</script>
<style scoped>
</style>
