import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// Create axios instance
import axios from 'axios'
const api = axios.create({
  baseURL: 'http://localhost:8083/api',
  timeout: 60000, // 增加到60秒，适应AI助手的响应时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add request interceptor
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

const app = createApp(App)

// Provide axios instance to all components
app.provide('$api', api)

app.use(router)
app.use(ElementPlus)
app.mount('#app')
