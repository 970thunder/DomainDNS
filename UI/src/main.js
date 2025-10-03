import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/styles.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth.js'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 初始化 auth store
const authStore = useAuthStore()
authStore.init()

// 监听token过期事件
window.addEventListener('token-expired', () => {
    // 调试日志已移除

    // 判断当前是用户还是管理员页面
    if (window.location.pathname.startsWith('/user')) {
        authStore.handleTokenExpired()
    } else if (window.location.pathname.startsWith('/admin')) {
        authStore.handleAdminTokenExpired()
    }
})

app.mount('#app')
