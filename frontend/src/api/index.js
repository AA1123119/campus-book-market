import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/api', timeout: 15000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('cbm_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code !== 0) {
      if (body.code === 401) {
        localStorage.removeItem('cbm_token')
        localStorage.removeItem('cbm_user')
        if (location.hash !== '#/login') location.hash = '#/login'
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body.data
  },
  (err) => {
    ElMessage.error(err.message || '网络异常')
    return Promise.reject(err)
  }
)

export const api = {
  register: (data) => http.post('/auth/register', data),
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/auth/me'),

  books: (params) => http.get('/books', { params }),
  bookMeta: () => http.get('/books/meta'),
  bookDetail: (id) => http.get(`/books/${id}`),
  publishBook: (data) => http.post('/books', data),
  updateBook: (id, data) => http.put(`/books/${id}`, data),
  offShelf: (id) => http.delete(`/books/${id}`),
  myBooks: () => http.get('/books/mine'),
  recommend: () => http.get('/books/recommend'),

  priceSuggestion: (data) => http.post('/ai/price-suggestion', data),
  isbnLookup: (isbn) => http.get(`/ai/isbn/${isbn}`),
  genDescription: (data) => http.post('/ai/description', data),

  createOrder: (data) => http.post('/orders', data),
  confirmOrder: (id) => http.post(`/orders/${id}/confirm`),
  completeOrder: (id) => http.post(`/orders/${id}/complete`),
  cancelOrder: (id) => http.post(`/orders/${id}/cancel`),
  boughtOrders: () => http.get('/orders/bought'),
  soldOrders: () => http.get('/orders/sold'),

  review: (data) => http.post('/reviews', data),
  profile: (id) => http.get(`/users/${id}/profile`),
  sendMessage: (data) => http.post('/messages', data),
  conversation: (peerId) => http.get(`/messages/${peerId}`),
  unread: () => http.get('/messages/unread'),
  report: (data) => http.post('/reports', data),
  reports: (status) => http.get('/admin/reports', { params: { status } }),
  handleReport: (id, data) => http.post(`/admin/reports/${id}/handle`, data),
  dashboard: () => http.get('/admin/dashboard')
}
