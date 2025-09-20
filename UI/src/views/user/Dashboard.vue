<template>
	<div class="grid cols-3">
		<div class="card stat">
			<div>
				<div class="label">我的积分</div>
				<div class="value">8</div>
			</div>
			<span class="badge">本周 +3</span>
		</div>
		<div class="card stat">
			<div>
				<div class="label">已申请域名</div>
				<div class="value">2</div>
			</div>
			<span class="badge">上限 5</span>
		</div>
		<div class="card stat">
			<div>
				<div class="label">我的邀请码</div>
				<div class="value">ABCD1234</div>
			</div>
			<router-link class="btn outline" to="/user/invite">管理</router-link>
		</div>
	</div>

	<div class="grid cols-2" style="margin-top:16px;">
		<div class="card">
			<h3>快速申请</h3>
			<div class="form">
				<div class="row">
					<select class="select" style="max-width: 260px;">
						<option>选择域名（已启用）</option>
						<option>example.com</option>
					</select>
					<input class="input" style="max-width: 200px;" placeholder="前缀，如：app">
					<input class="input" style="max-width: 240px;" placeholder="记录值，如：1.2.3.4">
					<button class="btn primary">申请（-5积分）</button>
				</div>
			</div>
		</div>
		<div class="card">
			<h3>最近申请</h3>
			<table class="table">
				<thead>
					<tr>
						<th>域名</th>
						<th>记录</th>
						<th>时间</th>
						<th>状态</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td data-label="域名">example.com</td>
						<td data-label="记录">a.example.com → 1.2.3.4</td>
						<td data-label="时间">刚刚</td>
						<td data-label="状态"><span class="badge success">ACTIVE</span></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>
<script setup>
</script>
<style scoped></style>
