<template>
    <div class="container" style="display:grid;place-items:center;min-height:100vh;">
        <div class="form">
            <h2 style="margin:0 0 10px 0;">找回密码</h2>
            <p class="desc">输入邮箱获取验证码，然后设置新密码。</p>

            <div class="flex-column">
                <label>邮箱</label>
            </div>
            <div class="inputForm">
                <input v-model="email" placeholder="输入注册邮箱" class="input" type="email">
            </div>

            <div class="flex-row" style="gap:8px;">
                <div class="flex-1 inputForm">
                    <input v-model="code" placeholder="邮箱验证码" class="input" type="text">
                </div>
                <button class="btn" style="width:auto;min-width:120px;" @click="sendCode"
                    :disabled="sending || countdown > 0">
                    {{ sending ? '发送中...' : (countdown > 0 ? `${countdown}s 后可重发` : '获取验证码') }}
                </button>
            </div>

            <div class="flex-column">
                <label>新密码</label>
            </div>
            <div class="inputForm">
                <input v-model="newPassword" placeholder="输入新密码" class="input" type="password">
            </div>

            <div class="error-message" v-if="errorMessage">{{ errorMessage }}</div>
            <div class="success-message" v-if="successMessage">{{ successMessage }}</div>

            <button class="button-submit" @click="resetPassword" :disabled="reseting">
                {{ reseting ? '提交中...' : '重置密码' }}
            </button>
            <p class="p"><a class="span" @click.prevent="goLogin">返回登录</a></p>
        </div>
    </div>
</template>
<script setup>
import { ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { apiPost } from '@/utils/api.js'

const router = useRouter()

const email = ref('')
const code = ref('')
const newPassword = ref('')
const sending = ref(false)
const reseting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const countdown = ref(0)
let timer = null

const sendCode = async () => {
    if (!email.value.trim()) {
        errorMessage.value = '请输入邮箱'
        return
    }
    try {
        sending.value = true
        errorMessage.value = ''
        successMessage.value = ''
        await apiPost('/api/auth/forgot', { email: email.value.trim() })
        successMessage.value = '验证码已发送，请检查邮箱（10分钟内有效）'
        // 开始60秒倒计时
        countdown.value = 60
        if (timer) clearInterval(timer)
        timer = setInterval(() => {
            countdown.value = countdown.value - 1
            if (countdown.value <= 0) {
                countdown.value = 0
                clearInterval(timer)
                timer = null
            }
        }, 1000)
    } catch (e) {
        errorMessage.value = e.message || '发送验证码失败'
    } finally {
        sending.value = false
    }
}

const resetPassword = async () => {
    if (!email.value.trim() || !code.value.trim() || !newPassword.value) {
        errorMessage.value = '请完整填写邮箱、验证码和新密码'
        return
    }
    try {
        reseting.value = true
        errorMessage.value = ''
        successMessage.value = ''
        await apiPost('/api/auth/reset', {
            email: email.value.trim(),
            code: code.value.trim(),
            newPassword: newPassword.value
        })
        successMessage.value = '重置成功，正在返回登录...'
        setTimeout(() => router.push('/user/login'), 1000)
    } catch (e) {
        errorMessage.value = e.message || '重置失败'
    } finally {
        reseting.value = false
    }
}

const goLogin = () => router.push('/user/login')

onBeforeUnmount(() => {
    if (timer) clearInterval(timer)
})
</script>
<style scoped>
.form {
    display: flex;
    flex-direction: column;
    gap: 10px;
    background: #fff;
    padding: 30px;
    width: 450px;
    border-radius: 20px;
    box-shadow: 0 12px 28px rgba(15, 23, 42, .12), 0 2px 6px rgba(15, 23, 42, .06);
}

.inputForm {
    border: 1.5px solid #ecedec;
    border-radius: 10px;
    height: 50px;
    display: flex;
    align-items: center;
    padding-left: 10px;
    transition: .2s;
}

.input {
    margin-left: 10px;
    border-radius: 10px;
    border: none;
    width: 85%;
    height: 100%;
}

.input:focus {
    outline: none;
}

.button-submit {
    margin: 10px 0 10px 0;
    background: #151717;
    border: none;
    color: #fff;
    font-size: 15px;
    font-weight: 500;
    border-radius: 10px;
    height: 50px;
    width: 100%;
    cursor: pointer;
}

.btn {
    height: 50px;
    border-radius: 10px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: 500;
    border: 1px solid #ededef;
    background: #fff;
    cursor: pointer;
    transition: .2s;
}

.btn:hover {
    border: 1px solid #2d79f3;
}

.desc {
    color: #6b7280;
    margin: 0 0 6px 0;
}

.p {
    text-align: center;
    color: black;
    font-size: 14px;
    margin: 5px 0;
}

.span {
    color: #2d79f3;
    cursor: pointer;
}

.error-message {
    color: #ef4444;
    font-size: 14px;
    text-align: center;
    margin: 10px 0;
    padding: 8px 12px;
    background: #fef2f2;
    border: 1px solid #fecaca;
    border-radius: 8px;
}

.success-message {
    color: #16a34a;
    font-size: 14px;
    text-align: center;
    margin: 10px 0;
    padding: 8px 12px;
    background: #ecfdf5;
    border: 1px solid #bbf7d0;
    border-radius: 8px;
}

@media (max-width: 520px) {
    .form {
        width: 100%;
        border-radius: 0;
        padding: 20px;
    }
}
</style>
