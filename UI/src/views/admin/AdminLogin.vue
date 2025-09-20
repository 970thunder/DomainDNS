<template>
	<div class="container" style="display:grid;place-items:center;min-height:100vh;">
		<form class="form" @submit.prevent="onSubmit">
			<div class="flex-column">
				<label>管理员账号（Email）</label>
			</div>
			<div class="inputForm">
				<svg xmlns="http://www.w3.org/2000/svg" width="20" viewBox="0 0 32 32" height="20">
					<g data-name="Layer 3" id="Layer_3">
						<path
							d="m30.853 13.87a15 15 0 0 0 -29.729 4.082 15.1 15.1 0 0 0 12.876 12.918 15.6 15.6 0 0 0 2.016.13 14.85 14.85 0 0 0 7.715-2.145 1 1 0 1 0 -1.031-1.711 13.007 13.007 0 1 1 5.458-6.529 2.149 2.149 0 0 1 -4.158-.759v-10.856a1 1 0 0 0 -2 0v1.726a8 8 0 1 0 .2 10.325 4.135 4.135 0 0 0 7.83.274 15.2 15.2 0 0 0 .823-7.455zm-14.853 8.13a6 6 0 1 1 6-6 6.006 6.006 0 0 1 -6 6z">
						</path>
					</g>
				</svg>
				<input v-model="email" placeholder="输入账号" class="input" type="text">
			</div>

			<div class="flex-column">
				<label>密码</label>
			</div>
			<div class="inputForm">
				<svg xmlns="http://www.w3.org/2000/svg" width="20" viewBox="-64 0 512 512" height="20">
					<path
						d="m336 512h-288c-26.453125 0-48-21.523438-48-48v-224c0-26.476562 21.546875-48 48-48h288c26.453125 0 48 21.523438 48 48v224c0 26.476562-21.546875 48-48 48zm-288-288c-8.8125 0-16 7.167969-16 16v224c0 8.832031 7.1875 16 16 16h288c8.8125 0 16-7.167969 16-16v-224c0-8.832031-7.1875-16-16-16zm0 0">
					</path>
					<path
						d="m304 224c-8.832031 0-16-7.167969-16-16v-80c0-52.929688-43.070312-96-96-96s-96 43.070312-96 96v80c0 8.832031-7.167969 16-16 16s-16-7.167969-16-16v-80c0-70.59375 57.40625-128 128-128s128 57.40625 128 128v80c0 8.832031-7.167969 16-16 16zm0 0">
					</path>
				</svg>
				<input v-model="password" placeholder="输入密码" class="input" type="password">
			</div>

			<div class="flex-row">
				<div class="remember-me">
					<input type="checkbox" v-model="remember" id="remember-me">
					<label for="remember-me">记住我</label>
				</div>
				<span class="span" @click.prevent="goForgot">忘记密码？</span>
			</div>

			<div class="error-message" v-if="errorMessage">
				{{ errorMessage }}
			</div>


			<button class="button-submit" type="submit" :disabled="isLoading">
				{{ isLoading ? '登录中...' : '登录管理端' }}
			</button>

			<div class="login-actions">
				<p class="p"><span class="span" @click.prevent="goUserLogin">去用户登录</span></p>
				<!-- 调试按钮 -->
				<button type="button" @click="testRememberMe" class="test-btn">测试记住我功能</button>
			</div>
		</form>
	</div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const remember = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

// 已删除设置页面相关功能

// 管理员登录
const onSubmit = async () => {
	if (!email.value.trim() || !password.value.trim()) {
		errorMessage.value = '请输入账号和密码'
		return
	}

	isLoading.value = true
	errorMessage.value = ''

	try {
		const result = await authStore.loginAdmin({
			username: email.value,
			password: password.value
		})

		console.log('登录结果:', result)
		console.log('登录后管理员token:', authStore.adminToken)
		console.log('登录后管理员状态:', authStore.isAdminLoggedIn)

		if (result.success) {
			// 设置记住我
			authStore.setRememberMe(remember.value)
			console.log('设置记住我状态:', remember.value)
			
			// 如果选择了记住我，保存用户名
			if (remember.value) {
				authStore.setRememberedUsername(email.value)
				console.log('已保存用户名:', email.value)
				console.log('localStorage中的记住用户名:', localStorage.getItem('remembered_username'))
			} else {
				authStore.clearRememberedUsername()
				console.log('已清除记住的用户名')
			}

			// 跳转到管理后台
			router.push('/admin/dashboard')
		} else {
			errorMessage.value = result.message || '登录失败'
		}
	} catch (error) {
		errorMessage.value = error.message || '登录失败'
		console.error('登录错误:', error)
	} finally {
		isLoading.value = false
	}
}

const goUserLogin = () => router.push('/user/login')
const goForgot = () => { }

// 测试记住我功能
const testRememberMe = () => {
	console.log('=== 测试记住我功能 ===')
	console.log('当前用户名:', email.value)
	console.log('当前记住我状态:', remember.value)
	console.log('localStorage中的记住用户名:', localStorage.getItem('remembered_username'))
	console.log('localStorage中的记住我状态:', localStorage.getItem('remember_me'))
	
	// 测试保存
	if (email.value) {
		authStore.setRememberedUsername(email.value)
		console.log('已保存用户名到localStorage')
	}
	
	// 测试获取
	const savedUsername = authStore.getRememberedUsername()
	console.log('从localStorage获取的用户名:', savedUsername)
}

// 组件挂载时恢复记住的用户名
onMounted(() => {
	// 恢复记住的用户名
	const rememberedUsername = authStore.getRememberedUsername()
	console.log('恢复记住的用户名:', rememberedUsername)
	console.log('localStorage中的记住用户名:', localStorage.getItem('remembered_username'))
	console.log('localStorage中的记住我状态:', localStorage.getItem('remember_me'))
	
	if (rememberedUsername) {
		email.value = rememberedUsername
		remember.value = true
		console.log('已恢复用户名:', email.value, '记住我状态:', remember.value)
	} else {
		console.log('没有找到记住的用户名')
	}
})
</script>
<style scoped>
.form {
	display: flex;
	flex-direction: column;
	gap: 10px;
	background-color: #ffffff;
	padding: 30px;
	width: 450px;
	border-radius: 20px;
	box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12), 0 2px 6px rgba(15, 23, 42, 0.06);
	font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

::placeholder {
	font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

.form button {
	align-self: flex-end;
}

.flex-column>label {
	color: #151717;
	font-weight: 600;
}

.inputForm {
	border: 1.5px solid #ecedec;
	border-radius: 10px;
	height: 50px;
	display: flex;
	align-items: center;
	padding-left: 10px;
	transition: 0.2s ease-in-out;
}

.input {
	margin-left: 10px;
	border-radius: 10px;
	border: none;
	width: 100%;
	height: 100%;
}

.input:focus {
	outline: none;
}

.inputForm:focus-within {
	border: 1.5px solid #2d79f3;
}

.flex-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 10px;
	justify-content: space-between;
}

.flex-row>div>label {
	font-size: 14px;
	color: black;
	font-weight: 400;
}

.remember-me {
	display: flex;
	align-items: center;
	gap: 8px;
}

.remember-me input[type="checkbox"] {
	width: 16px;
	height: 16px;
	cursor: pointer;
	accent-color: #2d79f3;
}

.remember-me label {
	cursor: pointer;
	user-select: none;
}

.span {
	font-size: 14px;
	margin-left: 5px;
	color: #2d79f3;
	font-weight: 500;
	cursor: pointer;
}

.button-submit {
	margin: 20px 0 10px 0;
	background-color: #151717;
	border: none;
	color: white;
	font-size: 15px;
	font-weight: 500;
	border-radius: 10px;
	height: 50px;
	width: 100%;
	cursor: pointer;
	transition: background-color 0.2s;
}

.button-submit:hover:not(:disabled) {
	background-color: #252727;
}

.button-submit:disabled {
	background-color: #9ca3af;
	cursor: not-allowed;
}

.error-message {
	color: #ef4444;
	font-size: 14px;
	text-align: center;
	margin: 10px 0;
	padding: 8px 12px;
	background-color: #fef2f2;
	border: 1px solid #fecaca;
	border-radius: 8px;
}

.p {
	text-align: center;
	color: black;
	font-size: 14px;
	margin: 5px 0;
}

.login-actions {
	margin-top: 10px;
}

.setup-link {
	color: #ef4444 !important;
	font-weight: 600;
}


.btn {
	margin-top: 10px;
	width: 100%;
	height: 50px;
	border-radius: 10px;
	display: flex;
	justify-content: center;
	align-items: center;
	font-weight: 500;
	gap: 10px;
	border: 1px solid #ededef;
	background-color: white;
	cursor: pointer;
	transition: 0.2s ease-in-out;
}

.btn:hover {
	border: 1px solid #2d79f3;
}

.test-btn {
	margin-top: 10px;
	padding: 8px 16px;
	background-color: #f3f4f6;
	border: 1px solid #d1d5db;
	border-radius: 6px;
	color: #374151;
	font-size: 12px;
	cursor: pointer;
	transition: all 0.2s;
}

.test-btn:hover {
	background-color: #e5e7eb;
	border-color: #9ca3af;
}

/* 响应式：窄屏表单宽度自适应 */
@media (max-width: 520px) {
	.form {
		width: 100%;
		border-radius: 0;
		padding: 20px;
	}
}
</style>
