<template>
	<div class="card">
		<h3>申请二级域名</h3>
		<div class="form">
			<div class="input-row">
				<label class="label">选择主域名（可分发）</label>
				<select class="select">
					<option>example.com</option>
					<option>example.net</option>
				</select>
			</div>
			<div class="input-row">
				<label class="label">子域名前缀</label>
				<input class="input" placeholder="例如：app，将生成 app.example.com">
			</div>
			<div class="grid cols-3">
				<div class="input-row">
					<label class="label">记录类型</label>
					<select class="select">
						<option>A</option>
						<option>AAAA</option>
						<option>CNAME</option>
					</select>
				</div>
				<div class="input-row">
					<label class="label">记录值</label>
					<input class="input" placeholder="例如：1.2.3.4 或 目标域名">
				</div>
				<div class="input-row">
					<label class="label">TTL</label>
					<input class="input" placeholder="默认 120">
				</div>
			</div>
			<div class="input-row">
				<label class="label">备注</label>
				<textarea class="textarea" placeholder="用途说明，方便管理"></textarea>
			</div>
			<div class="row">
				<button class="btn primary">提交申请（扣除 5 积分）</button>
				<router-link class="btn outline" to="/user/dashboard">返回</router-link>
			</div>
		</div>
	</div>
</template>
<script setup>
</script>
<style scoped></style>
