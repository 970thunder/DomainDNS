<template>
	<div class="card">
		<h3>积分充值</h3>
		<div class="form">
			<div class="input-row">
				<label class="label">充值金额（¥）</label>
				<input class="input" placeholder="例如：10">
			</div>
			<div class="input-row">
				<label class="label">预计获得积分</label>
				<input class="input" value="10" disabled>
			</div>
			<div class="row">
				<button class="btn primary">创建订单</button>
				<button class="btn outline">查看历史订单</button>
			</div>
		</div>
	</div>
</template>
<script setup>
</script>
<style scoped></style>
