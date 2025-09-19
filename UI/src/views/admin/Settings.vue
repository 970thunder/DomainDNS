<template>
	<main class="main container">
		<div class="grid cols-2">
			<div class="card">
				<h3>积分策略</h3>
				<div class="form">
					<div class="grid cols-2">
						<div class="input-row"><label class="label">注册赠送积分</label><input class="input"
								v-model.number="points.initial_register_points" /></div>
						<div class="input-row"><label class="label">邀请人奖励</label><input class="input"
								v-model.number="points.inviter_points" /></div>
						<div class="input-row"><label class="label">被邀请人奖励</label><input class="input"
								v-model.number="points.invitee_points" /></div>
						<div class="input-row"><label class="label">申请域名消耗</label><input class="input"
								v-model.number="points.domain_cost_points" /></div>
						<div class="input-row"><label class="label">单用户域名上限</label><input class="input"
								v-model.number="points.max_domains_per_user" /></div>
					</div>
					<div class="row"><button class="btn primary" @click="savePoints">保存</button><button
							class="btn outline" @click="reset">重置</button></div>
				</div>
			</div>
			<div class="card">
				<h3>同步与任务</h3>
				<div class="form">
					<div class="input-row"><label class="label">同步 Cron 表达式</label><input class="input"
							v-model="sync.sync_cron" placeholder="0 */5 * * * *" /></div>
					<div class="input-row"><label class="label">默认 TTL</label><input class="input"
							v-model.number="sync.default_ttl" /></div>
					<div class="row"><button class="btn primary" @click="saveSync">保存</button><button
							class="btn outline" @click="trigger">立即触发同步</button></div>
				</div>
			</div>
		</div>

		<div class="card" style="margin-top:16px;">
			<h3>安全与速率限制</h3>
			<div class="form">
				<div class="grid cols-3">
					<div class="input-row"><label class="label">注册 IP 限制（/日）</label><input class="input"
							v-model.number="sec.register_ip_limit" /></div>
					<div class="input-row"><label class="label">申请频率（次/分钟）</label><input class="input"
							v-model.number="sec.apply_rate_limit" /></div>
					<div class="input-row"><label class="label">启用邮箱验证</label>
						<select class="select" v-model="sec.email_verification">
							<option :value="true">是</option>
							<option :value="false">否</option>
						</select>
					</div>
				</div>
				<div class="row"><button class="btn primary" @click="saveSecurity">保存</button></div>
			</div>
		</div>
	</main>
</template>

<script setup>
import { reactive } from 'vue'

const points = reactive({ initial_register_points: 5, inviter_points: 3, invitee_points: 3, domain_cost_points: 10, max_domains_per_user: 5 })
const sync = reactive({ sync_cron: '0 */5 * * * *', default_ttl: 120 })
const sec = reactive({ register_ip_limit: 5, apply_rate_limit: 10, email_verification: true })

const savePoints = () => { }
const saveSync = () => { }
const trigger = () => { }
const saveSecurity = () => { }
const reset = () => { points.initial_register_points = 5; points.inviter_points = 3; points.invitee_points = 3; points.domain_cost_points = 10; points.max_domains_per_user = 5 }
</script>

<style scoped>
.grid {
	display: grid;
	gap: 16px;
}

.grid.cols-2 {
	grid-template-columns: repeat(2, minmax(0, 1fr));
}

.grid.cols-3 {
	grid-template-columns: repeat(3, minmax(0, 1fr));
}

.card {
	background: #fff;
	border: 1px solid #e2e8f0;
	border-radius: 12px;
	padding: 16px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, .04);
}

.form {
	display: grid;
	gap: 12px;
	max-width: 640px;
}

.input-row {
	display: grid;
	gap: 8px;
}

.label {
	font-size: 12px;
	color: #475569;
}

.input,
.select {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #cbd5e1;
	border-radius: 10px;
}

.input:focus,
.select:focus {
	border-color: #6366f1;
	box-shadow: 0 0 0 3px rgba(99, 102, 241, .15);
	outline: none;
}

.row {
	display: flex;
	gap: 8px;
	align-items: center;
	flex-wrap: wrap;
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

/* 响应式：两列网格在窄屏改为单列 */
@media (max-width: 960px) {
	.grid.cols-2 {
		grid-template-columns: 1fr;
	}
}

@media (max-width: 768px) {
	.card {
		padding: 12px;
	}

	.form {
		max-width: 100%;
	}
}
</style>
