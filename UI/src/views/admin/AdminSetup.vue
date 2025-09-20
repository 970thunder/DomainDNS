<template>
    <div class="container" style="display:grid;place-items:center;min-height:100vh;">
        <div class="setup-container">
            <!-- 欢迎界面 -->
            <div class="welcome-section" v-if="!showSetupForm">
                <div class="welcome-icon">
                    <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 2L2 7l10 5 10-5-10-5z" />
                        <path d="M2 17l10 5 10-5" />
                        <path d="M2 12l10 5 10-5" />
                    </svg>
                </div>
                <h1 class="welcome-title">欢迎使用 DomainDNS</h1>
                <p class="welcome-subtitle">系统首次启动，需要创建管理员账号</p>
                <p class="welcome-description">
                    请设置您的管理员账号信息，包括用户名、邮箱和密码。<br>
                    设置完成后，您将拥有系统的完全管理权限。
                </p>
                <button class="button-primary" @click="startSetup">
                    开始设置
                </button>
            </div>

            <!-- 设置表单 -->
            <form class="form" @submit.prevent="onSubmit" v-if="showSetupForm">
                <div class="form-header">
                    <h2 class="form-title">创建管理员账号</h2>
                    <p class="form-subtitle">请填写以下信息完成系统初始化</p>
                </div>

                <div class="flex-column">
                    <label>用户名</label>
                </div>
                <div class="inputForm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" viewBox="0 0 24 24" height="20">
                        <path
                            d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
                            fill="currentColor" />
                    </svg>
                    <input v-model="formData.username" placeholder="输入管理员用户名" class="input" type="text" required>
                </div>

                <div class="flex-column">
                    <label>邮箱</label>
                </div>
                <div class="inputForm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" viewBox="0 0 24 24" height="20">
                        <path
                            d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"
                            fill="currentColor" />
                    </svg>
                    <input v-model="formData.email" placeholder="admin@example.com" class="input" type="email" required>
                </div>

                <div class="flex-column">
                    <label>密码</label>
                </div>
                <div class="inputForm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" viewBox="0 0 24 24" height="20">
                        <path
                            d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
                            fill="currentColor" />
                    </svg>
                    <input v-model="formData.password" placeholder="设置管理员密码" class="input" type="password" required>
                </div>

                <div class="flex-column">
                    <label>确认密码</label>
                </div>
                <div class="inputForm">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" viewBox="0 0 24 24" height="20">
                        <path
                            d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
                            fill="currentColor" />
                    </svg>
                    <input v-model="formData.confirmPassword" placeholder="再次输入密码" class="input" type="password"
                        required>
                </div>

                <!-- 密码强度提示 -->
                <div class="password-tips" v-if="formData.password">
                    <div class="tip-item" :class="{ valid: hasLength }">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2">
                            <path d="M20 6L9 17l-5-5" />
                        </svg>
                        至少8个字符
                    </div>
                    <div class="tip-item" :class="{ valid: hasUppercase }">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2">
                            <path d="M20 6L9 17l-5-5" />
                        </svg>
                        包含大写字母
                    </div>
                    <div class="tip-item" :class="{ valid: hasLowercase }">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2">
                            <path d="M20 6L9 17l-5-5" />
                        </svg>
                        包含小写字母
                    </div>
                    <div class="tip-item" :class="{ valid: hasNumber }">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2">
                            <path d="M20 6L9 17l-5-5" />
                        </svg>
                        包含数字
                    </div>
                </div>

                <div class="error-message" v-if="errorMessage">
                    {{ errorMessage }}
                </div>

                <div class="form-actions">
                    <button class="button-primary" type="submit" :disabled="isLoading || !isFormValid">
                        {{ isLoading ? '创建中...' : '创建管理员账号' }}
                    </button>
                    <button class="button-outline" type="button" @click="goBack" :disabled="isLoading">
                        返回
                    </button>
                </div>
            </form>

            <!-- 成功界面 -->
            <div class="success-section" v-if="showSuccess">
                <div class="success-icon">
                    <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                        <polyline points="22,4 12,14.01 9,11.01" />
                    </svg>
                </div>
                <h1 class="success-title">设置完成！</h1>
                <p class="success-subtitle">管理员账号创建成功</p>
                <p class="success-description">
                    您的管理员账号已成功创建，现在可以使用该账号登录管理后台。<br>
                    请妥善保管您的登录信息。
                </p>
                <button class="button-primary" @click="goToLogin">
                    前往登录
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 状态管理
const showSetupForm = ref(false)
const showSuccess = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

// 表单数据
const formData = ref({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
})

// 密码强度检查
const hasLength = computed(() => formData.value.password.length >= 8)
const hasUppercase = computed(() => /[A-Z]/.test(formData.value.password))
const hasLowercase = computed(() => /[a-z]/.test(formData.value.password))
const hasNumber = computed(() => /\d/.test(formData.value.password))

// 表单验证
const isFormValid = computed(() => {
    return formData.value.username.trim() &&
        formData.value.email.trim() &&
        formData.value.password &&
        formData.value.confirmPassword &&
        formData.value.password === formData.value.confirmPassword &&
        hasLength.value &&
        hasUppercase.value &&
        hasLowercase.value &&
        hasNumber.value
})

// 开始设置
const startSetup = () => {
    showSetupForm.value = true
}

// 返回欢迎界面
const goBack = () => {
    showSetupForm.value = false
    errorMessage.value = ''
}

// 表单验证
const validateForm = () => {
    errorMessage.value = ''

    if (!formData.value.username.trim()) {
        errorMessage.value = '请输入用户名'
        return false
    }

    if (!formData.value.email.trim()) {
        errorMessage.value = '请输入邮箱'
        return false
    }

    // 邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(formData.value.email)) {
        errorMessage.value = '请输入有效的邮箱地址'
        return false
    }

    if (!formData.value.password) {
        errorMessage.value = '请输入密码'
        return false
    }

    if (!isFormValid.value) {
        errorMessage.value = '密码不符合要求或两次输入不一致'
        return false
    }

    return true
}

// 创建管理员账号
const createAdminAccount = async () => {
    try {
        const response = await fetch('/api/auth/admin/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: formData.value.username,
                email: formData.value.email,
                password: formData.value.password
            })
        })

        const result = await response.json()

        if (result.code === 0) {
            showSuccess.value = true
        } else {
            errorMessage.value = result.message || '创建管理员账号失败'
        }
    } catch (error) {
        errorMessage.value = '网络错误，请稍后重试'
    }
}

// 表单提交
const onSubmit = async () => {
    if (!validateForm()) return

    isLoading.value = true
    errorMessage.value = ''

    try {
        await createAdminAccount()
    } catch (error) {
        errorMessage.value = '创建失败，请稍后重试'
    } finally {
        isLoading.value = false
    }
}

// 前往登录页面
const goToLogin = () => {
    router.push('/admin/login')
}
</script>

<style scoped>
.setup-container {
    width: 100%;
    max-width: 500px;
    padding: 20px;
}

/* 欢迎界面样式 */
.welcome-section {
    text-align: center;
    padding: 40px 20px;
}

.welcome-icon {
    color: #6366f1;
    margin-bottom: 24px;
}

.welcome-title {
    font-size: 32px;
    font-weight: 700;
    color: #151717;
    margin: 0 0 16px 0;
}

.welcome-subtitle {
    font-size: 18px;
    color: #64748b;
    margin: 0 0 16px 0;
}

.welcome-description {
    font-size: 14px;
    color: #64748b;
    line-height: 1.6;
    margin: 0 0 32px 0;
}

/* 表单样式 */
.form {
    display: flex;
    flex-direction: column;
    gap: 16px;
    background-color: #ffffff;
    padding: 40px;
    border-radius: 20px;
    box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12), 0 2px 6px rgba(15, 23, 42, 0.06);
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

.form-header {
    text-align: center;
    margin-bottom: 20px;
}

.form-title {
    font-size: 24px;
    font-weight: 700;
    color: #151717;
    margin: 0 0 8px 0;
}

.form-subtitle {
    font-size: 14px;
    color: #64748b;
    margin: 0;
}

.flex-column {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.flex-column>label {
    color: #151717;
    font-weight: 600;
    font-size: 14px;
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

.inputForm:focus-within {
    border: 1.5px solid #6366f1;
}

.input {
    margin-left: 10px;
    border-radius: 10px;
    border: none;
    width: 85%;
    height: 100%;
    font-size: 14px;
}

.input:focus {
    outline: none;
}

/* 密码强度提示 */
.password-tips {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
    margin-top: 8px;
}

.tip-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #9ca3af;
    transition: color 0.2s;
}

.tip-item.valid {
    color: #10b981;
}

.tip-item svg {
    width: 14px;
    height: 14px;
}

/* 按钮样式 */
.button-primary {
    background-color: #6366f1;
    border: none;
    color: white;
    font-size: 15px;
    font-weight: 500;
    border-radius: 10px;
    height: 50px;
    width: 100%;
    cursor: pointer;
    transition: background-color 0.2s;
    margin-bottom: 12px;
}

.button-primary:hover:not(:disabled) {
    background-color: #4f46e5;
}

.button-primary:disabled {
    background-color: #9ca3af;
    cursor: not-allowed;
}

.button-outline {
    background: #ffffff;
    border: 1.5px solid #ecedec;
    color: #151717;
    font-size: 15px;
    font-weight: 500;
    border-radius: 10px;
    height: 50px;
    width: 100%;
    cursor: pointer;
    transition: all 0.2s;
}

.button-outline:hover:not(:disabled) {
    border-color: #6366f1;
    color: #6366f1;
}

.button-outline:disabled {
    background-color: #f3f4f6;
    color: #9ca3af;
    cursor: not-allowed;
}

.form-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 20px;
}

/* 错误消息 */
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

/* 成功界面样式 */
.success-section {
    text-align: center;
    padding: 40px 20px;
}

.success-icon {
    color: #10b981;
    margin-bottom: 24px;
}

.success-title {
    font-size: 32px;
    font-weight: 700;
    color: #151717;
    margin: 0 0 16px 0;
}

.success-subtitle {
    font-size: 18px;
    color: #64748b;
    margin: 0 0 16px 0;
}

.success-description {
    font-size: 14px;
    color: #64748b;
    line-height: 1.6;
    margin: 0 0 32px 0;
}

/* 响应式设计 */
@media (max-width: 520px) {
    .setup-container {
        padding: 10px;
    }

    .form {
        padding: 20px;
        border-radius: 0;
    }

    .welcome-section,
    .success-section {
        padding: 20px 10px;
    }

    .welcome-title,
    .success-title {
        font-size: 24px;
    }

    .password-tips {
        grid-template-columns: 1fr;
    }
}
</style>
