<template>
	<el-card class="card">
		<template #header>申请二级域名</template>
		<el-form label-width="140px">
			<el-form-item label="选择主域名（可分发）">
				<el-select><el-option label="example.com" value="example.com" /><el-option label="example.net" value="example.net" /></el-select>
			</el-form-item>
			<el-form-item label="子域名前缀"><el-input placeholder="例如：app，将生成 app.example.com" /></el-form-item>
			<el-row :gutter="12">
				<el-col :span="8"><el-form-item label="记录类型"><el-select><el-option label="A" value="A" /><el-option label="AAAA" value="AAAA" /><el-option label="CNAME" value="CNAME" /></el-select></el-form-item></el-col>
				<el-col :span="8"><el-form-item label="记录值"><el-input placeholder="例如：1.2.3.4 或 目标域名" /></el-form-item></el-col>
				<el-col :span="8"><el-form-item label="TTL"><el-input placeholder="默认 120" /></el-form-item></el-col>
			</el-row>
			<el-form-item label="备注"><el-input type="textarea" placeholder="用途说明，方便管理" /></el-form-item>
			<el-form-item>
				<el-button type="primary">提交申请（扣除 5 积分）</el-button>
				<el-button @click="$router.push('/user/dashboard')">返回</el-button>
			</el-form-item>
		</el-form>
	</el-card>
</template>
<script setup>
</script>
<style scoped>
</style>
