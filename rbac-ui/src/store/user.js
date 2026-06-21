import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout } from '@/api/auth.js'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: {}
  }),
  getters: {
    isLogged: (state) => !!state.token,
    isAdmin: (state) => state.userInfo.userType === 'SUPER_ADMIN',
    userGuid: (state) => state.userInfo.userGuid || '',
    tenantId: (state) => state.userInfo.tenantId || ''
  },
  actions: {
    setToken(token) { this.token = token },
    setUserInfo(info) { this.userInfo = info || {} },
    async login(form) {
      const res = await apiLogin(form)
      if (res.code === 200) {
        this.setToken(res.data.token || '')
        this.setUserInfo(res.data)
      }
      return res
    },
    async logout() {
      try { await apiLogout() } catch {}
      this.token = ''
      this.userInfo = {}
    }
  },
  persist: {
    key: 'rbac-user',
    storage: localStorage,
    paths: ['token', 'userInfo']
  }
})
