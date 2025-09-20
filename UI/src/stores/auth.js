import { defineStore } from 'pinia'
import { STORAGE_CONFIG } from '@/config/env.js'
import { apiPost } from '@/utils/api.js'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        // 用户信息
        user: null,
        token: null,
        role: null,

        // 管理员信息
        admin: null,
        adminToken: null,
        adminRole: null,

        // 登录状态
        isLoggedIn: false,
        isAdminLoggedIn: false,

        // 记住我
        rememberMe: false,
    }),

    getters: {
        // 获取当前用户信息
        currentUser: (state) => state.user,

        // 获取当前管理员信息
        currentAdmin: (state) => state.admin,

        // 检查是否为管理员
        isAdmin: (state) => state.role === 'ADMIN' || state.adminRole === 'ADMIN',

        // 检查是否为普通用户
        isUser: (state) => state.role === 'USER',

        // 获取认证头
        authHeaders: (state) => {
            const token = state.token || state.adminToken
            return token ? { Authorization: `Bearer ${token}` } : {}
        }
    },

    actions: {
        // 初始化 - 从本地存储恢复状态
        init() {
            this.loadFromStorage()
        },

        // 从本地存储加载状态
        loadFromStorage() {
            try {
                // 加载用户信息
                const userToken = localStorage.getItem(STORAGE_CONFIG.USER_TOKEN_KEY)
                const userInfo = localStorage.getItem(STORAGE_CONFIG.USER_INFO_KEY)

                if (userToken && userInfo) {
                    this.token = userToken
                    this.user = JSON.parse(userInfo)
                    this.role = 'USER'
                    this.isLoggedIn = true
                }

                // 加载管理员信息
                const adminToken = localStorage.getItem(STORAGE_CONFIG.TOKEN_KEY)
                const adminRole = localStorage.getItem(STORAGE_CONFIG.ADMIN_ROLE_KEY)
                const adminUsername = localStorage.getItem(STORAGE_CONFIG.ADMIN_USERNAME_KEY)

                if (adminToken && adminRole && adminUsername) {
                    this.adminToken = adminToken
                    this.adminRole = adminRole
                    this.admin = { username: adminUsername }
                    this.isAdminLoggedIn = true
                }

                // 加载记住我设置
                const rememberMe = localStorage.getItem(STORAGE_CONFIG.REMEMBER_ME_KEY)
                this.rememberMe = rememberMe === 'true'

            } catch (error) {
                console.error('加载本地存储失败:', error)
                this.clearStorage()
            }
        },

        // 用户登录
        async loginUser(credentials) {
            try {
                const response = await apiPost('/api/auth/login', credentials)

                if (response.code === 0) {
                    this.user = response.data.user
                    this.token = response.data.token
                    this.role = 'USER'
                    this.isLoggedIn = true

                    // 保存到本地存储
                    this.saveToStorage()

                    return { success: true, data: response.data }
                } else {
                    return { success: false, message: response.message }
                }
            } catch (error) {
                console.error('用户登录失败:', error)
                return { success: false, message: error.message || '登录失败' }
            }
        },

        // 管理员登录
        async loginAdmin(credentials) {
            try {
                const response = await apiPost('/api/auth/admin/login', credentials)

                if (response.code === 0) {
                    this.admin = { username: credentials.username }
                    this.adminToken = response.data.token
                    this.adminRole = response.data.role
                    this.isAdminLoggedIn = true

                    // 保存到本地存储
                    this.saveToStorage()

                    return { success: true, data: response.data }
                } else {
                    return { success: false, message: response.message }
                }
            } catch (error) {
                console.error('管理员登录失败:', error)
                return { success: false, message: error.message || '登录失败' }
            }
        },

        // 用户注册
        async registerUser(userData) {
            try {
                const response = await apiPost('/api/auth/register', userData)

                if (response.code === 0) {
                    return { success: true, message: '注册成功' }
                } else {
                    return { success: false, message: response.message }
                }
            } catch (error) {
                console.error('用户注册失败:', error)
                return { success: false, message: error.message || '注册失败' }
            }
        },

        // 发送注册验证码
        async sendRegisterCode(email) {
            try {
                const response = await apiPost('/api/auth/register/send-code', { email })

                if (response.code === 0) {
                    return { success: true, message: '验证码已发送' }
                } else {
                    return { success: false, message: response.message }
                }
            } catch (error) {
                console.error('发送验证码失败:', error)
                return { success: false, message: error.message || '发送验证码失败' }
            }
        },

        // 已删除创建管理员账号功能

        // 已删除 checkAdminExists 方法，不再自动检测管理员状态

        // 保存到本地存储
        saveToStorage() {
            try {
                // 保存用户信息
                if (this.token && this.user) {
                    localStorage.setItem(STORAGE_CONFIG.USER_TOKEN_KEY, this.token)
                    localStorage.setItem(STORAGE_CONFIG.USER_INFO_KEY, JSON.stringify(this.user))
                }

                // 保存管理员信息
                if (this.adminToken && this.admin) {
                    localStorage.setItem(STORAGE_CONFIG.TOKEN_KEY, this.adminToken)
                    localStorage.setItem(STORAGE_CONFIG.ADMIN_ROLE_KEY, this.adminRole)
                    localStorage.setItem(STORAGE_CONFIG.ADMIN_USERNAME_KEY, this.admin.username)
                }

                // 保存记住我设置
                localStorage.setItem(STORAGE_CONFIG.REMEMBER_ME_KEY, this.rememberMe.toString())

            } catch (error) {
                console.error('保存到本地存储失败:', error)
            }
        },

        // 清除本地存储
        clearStorage() {
            try {
                localStorage.removeItem(STORAGE_CONFIG.USER_TOKEN_KEY)
                localStorage.removeItem(STORAGE_CONFIG.USER_INFO_KEY)
                localStorage.removeItem(STORAGE_CONFIG.TOKEN_KEY)
                localStorage.removeItem(STORAGE_CONFIG.ADMIN_ROLE_KEY)
                localStorage.removeItem(STORAGE_CONFIG.ADMIN_USERNAME_KEY)
                localStorage.removeItem(STORAGE_CONFIG.REMEMBER_ME_KEY)
            } catch (error) {
                console.error('清除本地存储失败:', error)
            }
        },

        // 登出
        logout() {
            this.user = null
            this.token = null
            this.role = null
            this.admin = null
            this.adminToken = null
            this.adminRole = null
            this.isLoggedIn = false
            this.isAdminLoggedIn = false
            this.rememberMe = false

            this.clearStorage()
        },

        // 管理员登出
        logoutAdmin() {
            this.admin = null
            this.adminToken = null
            this.adminRole = null
            this.isAdminLoggedIn = false

            // 只清除管理员相关的存储
            localStorage.removeItem(STORAGE_CONFIG.TOKEN_KEY)
            localStorage.removeItem(STORAGE_CONFIG.ADMIN_ROLE_KEY)
            localStorage.removeItem(STORAGE_CONFIG.ADMIN_USERNAME_KEY)
        },

        // 设置记住我
        setRememberMe(remember) {
            this.rememberMe = remember
            localStorage.setItem(STORAGE_CONFIG.REMEMBER_ME_KEY, remember.toString())
        }
    }
})
