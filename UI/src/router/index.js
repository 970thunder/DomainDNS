import { createRouter, createWebHistory } from 'vue-router'

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

export default router
