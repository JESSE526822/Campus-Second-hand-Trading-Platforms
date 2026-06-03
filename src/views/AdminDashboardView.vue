<template>
  <div class="admin-dashboard">
    <!-- Admin Sidebar -->
    <div class="admin-sidebar">
      <div class="sidebar-nav">
        <div 
          class="nav-item active"
          @click="navigateTo('/admin/dashboard')"
        >
          <span class="nav-icon">📊</span>
          <span class="nav-text">仪表盘</span>
        </div>
        <div 
          class="nav-item"
          @click="navigateTo('/admin/users')"
        >
          <span class="nav-icon">👥</span>
          <span class="nav-text">用户管理</span>
        </div>
        <div 
          class="nav-item"
          @click="navigateTo('/admin/products')"
        >
          <span class="nav-icon">🛍️</span>
          <span class="nav-text">商品管理</span>
        </div>
        <div 
          class="nav-item"
          @click="navigateTo('/admin/logs')"
        >
          <span class="nav-icon">📋</span>
          <span class="nav-text">日志管理</span>
        </div>
        <div 
          class="nav-item"
          @click="navigateTo('/admin/analytics')"
        >
          <span class="nav-icon">📈</span>
          <span class="nav-text">数据统计</span>
        </div>
        <div 
          class="nav-item logout-item"
          @click="logout"
        >
          <span class="nav-icon">🚪</span>
          <span class="nav-text">退出登录</span>
        </div>
      </div>
    </div>

    <!-- Admin Main Content -->
    <div class="admin-content">
      <div class="content-header">
        <h2>系统概览</h2>
        <p>实时监控系统运行状态和关键指标</p>
      </div>

      <!-- Stats Cards -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon user-icon">👥</div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboardData.userCount || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon product-icon">🛍️</div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboardData.productCount || 0 }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon transaction-icon">💳</div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboardData.transactionCount || 0 }}</div>
            <div class="stat-label">交易总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon revenue-icon">💰</div>
          <div class="stat-content">
            <div class="stat-value">¥{{ (dashboardData.totalAmount || 0).toFixed(2) }}</div>
            <div class="stat-label">总成交额</div>
          </div>
        </div>
      </div>




    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'

const router = useRouter()
const $api = inject('$api')
const username = ref('admin')
const dashboardData = ref({})

const loadDashboardData = async () => {
  try {
    const response = await $api.get('/admin/dashboard')
    if (response.data.success) {
      dashboardData.value = response.data.dashboardData
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

const navigateTo = (path) => {
  router.push(path)
}

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(() => {
  loadDashboardData()
  const user = localStorage.getItem('user')
  if (user) {
    username.value = JSON.parse(user).username
  }
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: row;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* Admin Sidebar */
.admin-sidebar {
  width: 250px;
  background: white;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  border-right: 2px solid #f0f0f0;
  animation: slideInLeft 0.8s ease-out;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 2rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 2rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 4px solid transparent;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(76, 175, 80, 0.05));
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  z-index: 0;
}

.nav-item:hover::before,
.nav-item.active::before {
  transform: translateX(0);
}

.nav-item:hover {
  background: #f8fff8;
  transform: translateX(8px);
  border-left-color: #4CAF50;
}

.nav-item.active {
  background: #f0fff0;
  border-left-color: #4CAF50;
  font-weight: 600;
}

.nav-item.active .nav-text {
  color: #4CAF50;
}

.nav-icon {
  font-size: 1.3rem;
  flex-shrink: 0;
  z-index: 1;
}

.nav-text {
  font-size: 1rem;
  color: #333;
  z-index: 1;
  transition: color 0.3s ease;
}

.nav-item.active .nav-text {
  color: #4CAF50;
}

/* Main Content Layout */
.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 2rem;
  gap: 2rem;
  animation: fadeInRight 0.8s ease-out;
}

/* Animations */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Content Header */
.content-header {
  animation: fadeInUp 1s ease-out;
}

.content-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.content-header p {
  font-size: 1.1rem;
  color: #666;
  margin: 0.5rem 0 0 0;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 3rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  height: 200px;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, #4CAF50, #45a049);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 3rem;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.stat-label {
  font-size: 1rem;
  color: #666;
  margin: 0.25rem 0 0 0;
}





/* Animations */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .admin-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .admin-content {
    padding: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .activity-table {
    overflow-x: auto;
  }
}

@media (max-width: 480px) {
  .admin-header-left h1 {
    font-size: 1.5rem;
  }

  .content-header h2 {
    font-size: 1.5rem;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 1.5rem;
  }

  .stat-icon {
    font-size: 2.5rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }
}
</style>