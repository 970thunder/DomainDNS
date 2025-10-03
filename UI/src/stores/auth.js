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
        },

        // 获取管理员认证头
        adminAuthHeaders: (state) => {
            return state.adminToken ? { Authorization: `Bearer ${state.adminToken}` } : {}
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
                console.log('loadFromStorage 开始加载本地存储')

                // 加载用户信息
                const userToken = localStorage.getItem(STORAGE_CONFIG.USER_TOKEN_KEY)
                const userInfo = localStorage.getItem(STORAGE_CONFIG.USER_INFO_KEY)

                if (userToken && userInfo) {
                    this.token = userToken
                    this.user = JSON.parse(userInfo)
                    this.role = 'USER'
                    this.isLoggedIn = true
                    console.log('用户信息已从localStorage加载:', {
                        user: this.user,
                        token: this.token ? this.token.substring(0, 20) + '...' : null,
                        isLoggedIn: this.isLoggedIn
                    })
                } else {
                    console.log('用户信息加载条件不满足:', {
                        hasUserToken: !!userToken,
                        hasUserInfo: !!userInfo
                    })
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
                    console.log('管理员信息已从localStorage加载')
                } else {
                    console.log('管理员信息加载条件不满足:', {
                        hasAdminToken: !!adminToken,
                        hasAdminRole: !!adminRole,
                        hasAdminUsername: !!adminUsername
                    })
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
                    console.log('用户登录成功:', response.data)

                    this.user = response.data.user
                    this.token = response.data.token
                    this.role = 'USER'
                    this.isLoggedIn = true

                    // 保存到本地存储
                    this.saveToStorage()

                    console.log('用户登录后状态:', {
                        user: this.user,
                        token: this.token ? this.token.substring(0, 20) + '...' : null,
                        isLoggedIn: this.isLoggedIn
                    })

                    // 确保状态立即生效，避免路由守卫读取到旧状态
                    console.log('用户登录完成，状态已保存')

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
                    console.log('设置管理员状态前:', {
                        adminToken: this.adminToken,
                        admin: this.admin,
                        isAdminLoggedIn: this.isAdminLoggedIn
                    })

                    // 分步设置，每步都检查
                    this.admin = { username: credentials.username }
                    console.log('设置admin后:', { adminToken: this.adminToken, admin: this.admin })

                    this.adminToken = response.data.token
                    console.log('设置adminToken后:', { adminToken: this.adminToken, admin: this.admin })

                    this.adminRole = response.data.role
                    console.log('设置adminRole后:', { adminToken: this.adminToken, admin: this.admin, adminRole: this.adminRole })

                    this.isAdminLoggedIn = true
                    console.log('设置isAdminLoggedIn后:', { adminToken: this.adminToken, admin: this.admin, adminRole: this.adminRole, isAdminLoggedIn: this.isAdminLoggedIn })

                    // 保存到本地存储
                    this.saveToStorage()

                    console.log('保存到本地存储后:', {
                        adminToken: this.adminToken,
                        admin: this.admin,
                        isAdminLoggedIn: this.isAdminLoggedIn
                    })

                    // 立即检查localStorage
                    const savedToken = localStorage.getItem(STORAGE_CONFIG.TOKEN_KEY)
                    console.log('localStorage中的token:', savedToken ? savedToken.substring(0, 20) + '...' : 'null')

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
                    // 立即返回成功，不等待邮件发送完成
                    return { success: true, message: '验证码发送中，请稍后查收邮件' }
                } else {
                    return { success: false, message: response.message }
                }
            } catch (error) {
                console.error('发送验证码失败:', error)
                // 即使网络错误也返回成功，让用户进入验证码输入界面
                return { success: true, message: '验证码发送中，请稍后查收邮件' }
            }
        },

        // 已删除创建管理员账号功能

        // 已删除 checkAdminExists 方法，不再自动检测管理员状态

        // 保存到本地存储
        saveToStorage() {
            try {
                console.log('saveToStorage 开始:', {
                    user: this.user,
                    token: this.token ? this.token.substring(0, 20) + '...' : null,
                    adminToken: this.adminToken,
                    admin: this.admin,
                    adminRole: this.adminRole
                })

                // 保存用户信息
                if (this.token && this.user) {
                    localStorage.setItem(STORAGE_CONFIG.USER_TOKEN_KEY, this.token)
                    localStorage.setItem(STORAGE_CONFIG.USER_INFO_KEY, JSON.stringify(this.user))
                    console.log('用户信息已保存到localStorage')
                } else {
                    console.log('用户信息保存条件不满足:', {
                        hasToken: !!this.token,
                        hasUser: !!this.user
                    })
                }

                // 保存管理员信息
                if (this.adminToken && this.admin) {
                    localStorage.setItem(STORAGE_CONFIG.TOKEN_KEY, this.adminToken)
                    localStorage.setItem(STORAGE_CONFIG.ADMIN_ROLE_KEY, this.adminRole)
                    localStorage.setItem(STORAGE_CONFIG.ADMIN_USERNAME_KEY, this.admin.username)
                    console.log('管理员信息已保存到localStorage')
                } else {
                    console.log('管理员信息保存条件不满足:', {
                        hasAdminToken: !!this.adminToken,
                        hasAdmin: !!this.admin
                    })
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

        // 用户登出
        logout() {
            // 只清除用户相关状态，保留管理员状态
            this.user = null
            this.token = null
            this.role = null
            this.isLoggedIn = false

            // 只清除用户相关的存储
            localStorage.removeItem(STORAGE_CONFIG.USER_TOKEN_KEY)
            localStorage.removeItem(STORAGE_CONFIG.USER_INFO_KEY)

            // 注意：不清除记住的用户名，因为"记住我"是为了方便下次登录
        },

        // 管理员登出
        async logoutAdmin() {
            try {
                // 调用后端注销接口
                if (this.adminToken) {
                    await apiPost('/api/auth/logout')
                }
            } catch (error) {
                console.error('调用注销接口失败:', error)
                // 即使后端调用失败，也要清理本地状态
            }

            // 清理本地状态
            this.admin = null
            this.adminToken = null
            this.adminRole = null
            this.isAdminLoggedIn = false

            // 只清除管理员相关的存储
            localStorage.removeItem(STORAGE_CONFIG.TOKEN_KEY)
            localStorage.removeItem(STORAGE_CONFIG.ADMIN_ROLE_KEY)
            localStorage.removeItem(STORAGE_CONFIG.ADMIN_USERNAME_KEY)

            // 注意：不清除记住的用户名，因为"记住我"是为了方便下次登录
        },

        // 设置记住我
        setRememberMe(remember) {
            this.rememberMe = remember
            localStorage.setItem(STORAGE_CONFIG.REMEMBER_ME_KEY, remember.toString())
        },

        // 设置记住的用户名
        setRememberedUsername(username) {
            localStorage.setItem(STORAGE_CONFIG.REMEMBERED_USERNAME_KEY, username)
        },

        // 获取记住的用户名
        getRememberedUsername() {
            return localStorage.getItem(STORAGE_CONFIG.REMEMBERED_USERNAME_KEY)
        },

        // 清除记住的用户名
        clearRememberedUsername() {
            localStorage.removeItem(STORAGE_CONFIG.REMEMBERED_USERNAME_KEY)
        },

        // 处理token过期
        handleTokenExpired() {
            console.log('处理token过期，清除用户状态')

            // 清除用户状态
            this.user = null
            this.token = null
            this.role = null
            this.isLoggedIn = false

            // 清除用户相关的存储
            localStorage.removeItem(STORAGE_CONFIG.USER_TOKEN_KEY)
            localStorage.removeItem(STORAGE_CONFIG.USER_INFO_KEY)

            // 跳转到登录页
            if (window.location.pathname.startsWith('/user')) {
                window.location.href = '/user/login'
            }
        },

        // 处理管理员token过期
        handleAdminTokenExpired() {
            console.log('处理管理员token过期，清除管理员状态')

            // 清除管理员状态
            this.admin = null
            this.adminToken = null
            this.adminRole = null
            this.isAdminLoggedIn = false

            // 清除管理员相关的存储
            localStorage.removeItem(STORAGE_CONFIG.TOKEN_KEY)
            localStorage.removeItem(STORAGE_CONFIG.ADMIN_ROLE_KEY)
            localStorage.removeItem(STORAGE_CONFIG.ADMIN_USERNAME_KEY)

            // 跳转到管理员登录页
            if (window.location.pathname.startsWith('/admin')) {
                window.location.href = '/admin/login'
            }
        }
    }
})