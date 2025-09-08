import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
        token: null,
        role: null
    }),
    actions: {
        loginAsUser() {
            this.user = { username: 'demo' }
            this.role = 'USER'
            this.token = 'mock-token'
        },
        loginAsAdmin() {
            this.user = { username: 'admin' }
            this.role = 'ADMIN'
            this.token = 'mock-token'
        },
        logout() {
            this.user = null
            this.role = null
            this.token = null
        }
    }
})
