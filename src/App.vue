<template>
  <div class="app">
    <!-- 登录页和注册页不显示导航栏 -->
    <header v-if="!isAuthPage" class="header" :class="{ 'header-scrolled': isScrolled }">
      <div class="container">
        <div class="header-content">
          <div class="header-center">
            <router-link to="/" class="logo">校园二手物品交易平台</router-link>
            <div class="header-nav">
              <router-link to="/" class="nav-item">首页</router-link>
              <router-link to="/publish" class="nav-item">发布商品</router-link>
              <router-link to="/ai-assistant" class="nav-item">AI助手</router-link>
              <router-link to="/community" class="nav-item">评论社区</router-link>
              <div v-if="isLoggedIn" class="nav-user">
                <router-link to="/cart" class="nav-item">购物车</router-link>
                <router-link to="/profile" class="nav-item">个人中心</router-link>
                <button @click="logout" class="nav-item">退出</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
    
    <main class="main">
      <div :class="['container', { 'container-ai-assistant': route.path === '/ai-assistant' }]">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
    
    <!-- 登录页和注册页不显示页脚 -->
    <footer v-if="!isAuthPage" class="footer">
      <div class="container">
        <div class="footer-content">
          <p>© 2026 校园二手物品拍卖管理系统</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isLoggedIn = ref(false)
const isScrolled = ref(false)

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const user = localStorage.getItem('user')
  isLoggedIn.value = !!token && !!user
}

const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register' || route.path === '/ai-assistant' || route.path.startsWith('/admin/')
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  isLoggedIn.value = false
  router.push('/login')
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

onMounted(() => {
  checkLoginStatus()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由变化，更新登录状态
watch(() => route.path, () => {
  checkLoginStatus()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  line-height: 1.6;
  color: #333;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  padding: 1.5rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header-scrolled {
  padding: 1rem 0;
  background: linear-gradient(135deg, #388E3C 0%, #2E7D32 100%);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

.header-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.logo {
  font-size: 1.8rem;
  font-weight: 700;
  color: white;
  text-decoration: none;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  letter-spacing: 1px;
}

.logo:hover {
  transform: translateY(-2px);
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
  justify-content: center;
}

.nav-item {
  color: white;
  text-decoration: none;
  padding: 0.75rem 1.25rem;
  border-radius: 25px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-item:hover::before {
  width: 300px;
  height: 300px;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.nav-item:active {
  transform: translateY(0);
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-left: 1rem;
  padding-left: 1rem;
  border-left: 1px solid rgba(255, 255, 255, 0.2);
}

@media (max-width: 768px) {
  .header {
    padding: 1rem 0;
  }
  
  .header-center {
    gap: 0.5rem;
  }
  
  .logo {
    font-size: 1.4rem;
  }
  
  .header-nav {
    gap: 0.75rem;
  }
  
  .nav-item {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
  
  .nav-user {
    border-left: none;
    margin-left: 0;
    padding-left: 0;
    margin-top: 0.5rem;
  }
}

.main {
  min-height: 80vh;
  padding: 2rem 0;
}

/* AI Assistant Page */
.container-ai-assistant {
  padding: 0 !important;
  margin: 0 !important;
  max-width: 100% !important;
  width: 100% !important;
}

.router-view-ai-assistant {
  padding: 0 !important;
  margin: 0 !important;
}

.footer {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 3rem 0;
  text-align: center;
  margin-top: 4rem;
  border-top: 1px solid #dee2e6;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
}

.footer-content {
  position: relative;
  z-index: 1;
}

.footer-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #45a049);
  border-radius: 2px;
  z-index: -1;
}

.footer-content p {
  margin: 0;
  font-size: 1rem;
  color: #6c757d;
  font-weight: 500;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .footer {
    padding: 2rem 0;
    margin-top: 2rem;
  }
  
  .footer-content p {
    font-size: 0.9rem;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

button {
  cursor: pointer;
  border: none;
  background: none;
  font-size: 1rem;
}

/* Form styles */
.form {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-button {
  display: block;
  width: 100%;
  padding: 0.75rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.form-button:hover {
  background-color: #45a049;
}

/* Product card styles */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.product-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 1rem;
}

.product-title {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.product-price {
  font-size: 1.2rem;
  font-weight: bold;
  color: #4CAF50;
  margin-bottom: 0.5rem;
}

.product-category {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1rem;
}

.product-link {
  display: inline-block;
  padding: 0.5rem 1rem;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.product-link:hover {
  background-color: #45a049;
}

/* Responsive styles */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .header-nav {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}
</style>
