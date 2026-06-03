<template>
  <div class="admin-dashboard">
    <!-- Admin Sidebar -->
    <div class="admin-sidebar">
      <div class="sidebar-nav">
        <div 
          class="nav-item"
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
          class="nav-item active"
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
        <h2>数据统计</h2>
        <p>查看系统数据统计和可视化分析</p>
      </div>

      <!-- Product Category Chart -->
      <div class="chart-section">
        <div class="chart-card">
          <div class="chart-header">
            <h3>商品分类数量统计</h3>
          </div>
          <div class="chart-container" ref="categoryChartRef"></div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'
import * as echarts from 'echarts'

const router = useRouter()
const $api = inject('$api')
const username = ref('admin')
const categoryChartRef = ref(null)
let categoryChart = null

const loadProductCategories = async () => {
  try {
    const response = await $api.get('/admin/product-categories')
    if (response.data.success) {
      const categories = response.data.categories || []
      renderCategoryChart(categories)
    }
  } catch (error) {
    console.error('Failed to load product categories:', error)
    // 显示空图表
    renderCategoryChart([])
  }
}

const renderCategoryChart = (categories) => {
  if (!categoryChartRef.value) return

  // 销毁现有图表
  if (categoryChart) {
    categoryChart.dispose()
  }

  // 创建新图表
  categoryChart = echarts.init(categoryChartRef.value)

  // 准备数据
  const categoryNames = categories.map(item => item.category || '未分类')
  const categoryCounts = categories.map(item => item.count || 0)

  // 图表配置
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categoryNames,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '商品数量'
    },
    series: [
      {
        name: '商品数量',
        type: 'bar',
        data: categoryCounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4CAF50' },
            { offset: 1, color: '#81C784' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#45a049' },
              { offset: 1, color: '#66BB6A' }
            ])
          }
        }
      }
    ]
  }

  // 设置图表配置
  categoryChart.setOption(option)

  // 响应式调整
  window.addEventListener('resize', () => {
    categoryChart.resize()
  })
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
  loadProductCategories()
  const user = localStorage.getItem('user')
  if (user) {
    username.value = JSON.parse(user).username
  }
})

onUnmounted(() => {
  if (categoryChart) {
    categoryChart.dispose()
  }
  window.removeEventListener('resize', () => {
    categoryChart?.resize()
  })
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

/* Main Content */
.admin-content {
  flex: 1;
  padding: 2rem;
  animation: fadeInRight 0.8s ease-out;
}

.content-header {
  margin-bottom: 2rem;
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

/* Chart Section */
.chart-section {
  margin-bottom: 2rem;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.chart-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}

.chart-header {
  margin-bottom: 1.5rem;
}

.chart-header h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.chart-container {
  height: 400px;
  width: 100%;
}

/* Animations */
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
  .admin-content {
    padding: 1rem;
  }

  .chart-container {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .content-header h2 {
    font-size: 1.5rem;
  }

  .chart-card {
    padding: 1.5rem;
  }

  .chart-container {
    height: 250px;
  }
}
</style>