import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'

const routes = [
    { path: '/', redirect: '/user/login' },
    // User
    { path: '/user/login', component: () => import('../views/user/Login.vue') },
    { path: '/user/register', component: () => import('../views/user/Register.vue') },
    {
        path: '/user', component: () => import('../views/user/Layout.vue'), children: [
            { path: 'dashboard', component: () => import('../views/user/Dashboard.vue') },
            { path: 'apply', component: () => import('../views/user/ApplyDomain.vue') },
            { path: 'domains', component: () => import('../views/user/MyDomains.vue') },
            { path: 'invite', component: () => import('../views/user/Invite.vue') },
            { path: 'recharge', component: () => import('../views/user/Recharge.vue') },
        ]
    },
    // Admin login (separate)
    { path: '/admin/login', component: () => import('../views/admin/AdminLogin.vue') },
    // Admin
    {
        path: '/admin', component: () => import('../views/admin/Layout.vue'), children: [
            { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
            { path: 'cf-accounts', component: () => import('../views/admin/CfAccounts.vue') },
            { path: 'zones', component: () => import('../views/admin/Zones.vue') },
            { path: 'dns-records', component: () => import('../views/admin/DnsRecords.vue') },
            { path: 'users', component: () => import('../views/admin/Users.vue') },
            { path: 'invites', component: () => import('../views/admin/Invites.vue') },
            { path: 'cards', component: () => import('../views/admin/Cards.vue') },
            { path: 'settings', component: () => import('../views/admin/Settings.vue') },
        ]
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    // 只在首次访问或从登录页跳转时加载状态
    if (from.path === '/admin/login' || from.path === '/user/login' || !authStore.isAdminLoggedIn) {
        authStore.loadFromStorage()
    }

    console.log('路由守卫检查:', {
        path: to.path,
        from: from.path,
        isAdminLoggedIn: authStore.isAdminLoggedIn,
        adminToken: authStore.adminToken ? '已设置' : '未设置'
    })

    // 管理员路由需要认证
    if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
        if (!authStore.isAdminLoggedIn || !authStore.adminToken) {
            console.log('管理员未登录，重定向到登录页')
            next('/admin/login')
            return
        }
    }

    // 用户路由需要认证
    if (to.path.startsWith('/user') && to.path !== '/user/login' && to.path !== '/user/register') {
        if (!authStore.isLoggedIn || !authStore.token) {
            console.log('用户未登录，重定向到登录页')
            next('/user/login')
            return
        }
    }

    // 已登录用户访问登录页，重定向到对应首页
    if (to.path === '/admin/login' && authStore.isAdminLoggedIn) {
        next('/admin/dashboard')
        return
    }

    if (to.path === '/user/login' && authStore.isLoggedIn) {
        next('/user/dashboard')
        return
    }

    next()
})

export default router
