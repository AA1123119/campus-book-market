import { reactive } from 'vue'

export const auth = reactive({
  user: JSON.parse(localStorage.getItem('cbm_user') || 'null'),
  get logged() {
    return !!this.user
  },
  get isAdmin() {
    return this.user?.role === 'ADMIN'
  },
  setSession(loginRes) {
    this.user = loginRes
    localStorage.setItem('cbm_token', loginRes.token)
    localStorage.setItem('cbm_user', JSON.stringify(loginRes))
  },
  logout() {
    this.user = null
    localStorage.removeItem('cbm_token')
    localStorage.removeItem('cbm_user')
  }
})
