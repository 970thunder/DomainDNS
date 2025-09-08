<template>
	<el-card class="card">
		<template #header>example.com — DNS 记录</template>
		<el-space wrap>
			<el-input placeholder="搜索记录..." style="width:260px;" />
			<el-select placeholder="全部类型" style="width:160px;">
				<el-option label="全部类型" value="" />
				<el-option label="A" value="A" />
				<el-option label="AAAA" value="AAAA" />
				<el-option label="CNAME" value="CNAME" />
				<el-option label="TXT" value="TXT" />
			</el-select>
			<el-button>刷新</el-button>
			<el-button type="primary">从 Cloudflare 同步</el-button>
		</el-space>
		<el-table style="margin-top:12px;" :data="rows">
			<el-table-column prop="name" label="名称" />
			<el-table-column prop="type" label="类型" />
			<el-table-column prop="content" label="内容" />
			<el-table-column prop="ttl" label="TTL" />
			<el-table-column prop="proxied" label="代理" />
			<el-table-column label="操作">
				<template #default>
					<el-button>编辑</el-button>
					<el-button type="danger">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
	</el-card>
</template>
<script setup>
import { ref } from 'vue'
const rows = ref([
	{ name:'a.example.com', type:'A', content:'1.2.3.4', ttl:120, proxied:'否' },
	{ name:'www.example.com', type:'CNAME', content:'app.host.com', ttl:300, proxied:'是' },
])
</script>
<style scoped>
</style>
