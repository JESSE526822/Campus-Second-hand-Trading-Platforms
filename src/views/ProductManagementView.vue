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
          class="nav-item active"
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
        <h2>商品管理</h2>
        <p>查看系统中所有商品信息，包括商品详情和卖家信息</p>
      </div>

      <!-- Search and Filter -->
      <div class="content-actions">
        <div class="search-filter">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索商品名称或描述"
            class="search-input"
            @input="debounceSearch"
          >
        </div>
      </div>

      <!-- Products Grid -->
      <div class="products-grid">
        <div 
          v-for="product in filteredProducts" 
          :key="product.id"
          class="product-card"
        >
          <div class="product-image">
            <img 
              :src="product.image_url || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=second%20hand%20product%20placeholder&image_size=square'" 
              :alt="product.name"
              @error="handleImageError"
            >
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-description">{{ truncateDescription(product.description) }}</p>
            <div class="product-meta">
              <div class="meta-item">
                <span class="meta-label">价格:</span>
                <span class="meta-value price">¥{{ product.price }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">卖家:</span>
                <span class="meta-value">{{ product.seller_name }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">发布时间:</span>
                <span class="meta-value">{{ formatDate(product.created_at) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">状态:</span>
                <span class="status-badge" :class="product.status">
                  {{ getStatusText(product.status) }}
                </span>
              </div>
            </div>
            <div class="product-actions">
              <button @click="viewProductDetails(product)" class="view-button">
                <span class="view-icon">👁️</span>
                查看详情
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="filteredProducts.length === 0" class="empty-state">
        <span class="empty-icon">🛍️</span>
        <p>暂无商品数据</p>
      </div>

      <!-- Product Details Dialog -->
      <div v-if="showProductDetails" class="dialog-overlay" @click="closeProductDetails">
        <div class="dialog-content product-details-dialog" @click.stop>
          <div class="dialog-header">
            <h3>商品详情</h3>
            <button @click="closeProductDetails" class="close-button">×</button>
          </div>
          <div class="dialog-body">
            <div class="product-details">
              <div class="product-details-image">
                <img 
                  :src="selectedProduct?.image_url || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=second%20hand%20product%20placeholder&image_size=square'" 
                  :alt="selectedProduct?.name"
                >
              </div>
              <div class="product-details-info">
                <h4>{{ selectedProduct?.name }}</h4>
                <p class="product-details-price">¥{{ selectedProduct?.price }}</p>
                <p class="product-details-description">{{ selectedProduct?.description }}</p>
                <div class="product-details-meta">
                  <div class="meta-row">
                    <span class="meta-label">卖家:</span>
                    <span class="meta-value">{{ selectedProduct?.seller_name }}</span>
                  </div>
                  <div class="meta-row">
                    <span class="meta-label">发布时间:</span>
                    <span class="meta-value">{{ formatDate(selectedProduct?.created_at) }}</span>
                  </div>
                  <div class="meta-row">
                    <span class="meta-label">状态:</span>
                    <span class="status-badge" :class="selectedProduct?.status">
                      {{ getStatusText(selectedProduct?.status) }}
                    </span>
                  </div>

                </div>
              </div>
            </div>
          </div>
          <div class="dialog-footer">
            <button @click="closeProductDetails" class="close-details-button">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'

const router = useRouter()
const $api = inject('$api')
const username = ref('admin')
const products = ref([])
const searchKeyword = ref('')
const showProductDetails = ref(false)
const selectedProduct = ref(null)
const searchTimeout = ref(null)

const filteredProducts = computed(() => {
  if (!searchKeyword.value) {
    return products.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return products.value.filter(product => 
    product.name?.toLowerCase().includes(keyword) || 
    product.description?.toLowerCase().includes(keyword)
  )
})

const loadProducts = async () => {
  try {
    const response = await $api.get('/admin/products')
    if (response.data.success) {
      products.value = response.data.products
    }
  } catch (error) {
    console.error('Failed to load products:', error)
  }
}

const debounceSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = setTimeout(() => {
    // 搜索逻辑已在计算属性中处理
  }, 300)
}

const viewProductDetails = (product) => {
  selectedProduct.value = product
  showProductDetails.value = true
}

const closeProductDetails = () => {
  showProductDetails.value = false
  selectedProduct.value = null
}

const truncateDescription = (description) => {
  if (!description) return ''
  return description.length > 80 ? description.substring(0, 80) + '...' : description
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

const handleImageError = (event) => {
  event.target.src = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=second%20hand%20product%20placeholder&image_size=square'
}

const getStatusText = (status) => {
  const statusMap = {
    'active': '在售',
    'for_sale': '在售',
    'sold': '已售',
    'pending': '待审核',
    'inactive': '下架'
  }
  return statusMap[status] || status
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
  loadProducts()
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
  animation: fadeInUp 1s ease-out;
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

/* Search and Filter */
.content-actions {
  margin-bottom: 2rem;
}

.search-filter {
  position: relative;
  max-width: 500px;
}

.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.search-input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
}

.search-input::placeholder {
  color: #999;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.product-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.product-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover .product-image img {
  transform: scale(1.1);
}

.product-info {
  padding: 1.5rem;
}

.product-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 0.75rem 0;
  line-height: 1.4;
}

.product-description {
  font-size: 0.95rem;
  color: #666;
  margin: 0 0 1.25rem 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  margin-bottom: 1.5rem;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.meta-label {
  font-weight: 600;
  color: #333;
}

.meta-value {
  color: #666;
}

.meta-value.price {
  font-size: 1.1rem;
  font-weight: 700;
  color: #f44336;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.active {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge.sold {
  background: #ffebee;
  color: #c62828;
}

.product-actions {
  display: flex;
  gap: 1rem;
}

.view-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.view-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.view-icon {
  font-size: 1rem;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rem 2rem;
  text-align: center;
  animation: fadeInUp 1s ease-out 0.4s both;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
  opacity: 0.5;
}

.empty-state p {
  font-size: 1.2rem;
  color: #666;
  margin: 0;
}

/* Product Details Dialog */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

.dialog-content {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  max-width: 800px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideIn 0.3s ease-out;
}

.product-details-dialog {
  max-width: 900px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.dialog-header h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-button:hover {
  background: #f0f0f0;
  color: #333;
}

.product-details {
  display: flex;
  gap: 2rem;
}

.product-details-image {
  flex: 0 0 300px;
  height: 300px;
  overflow: hidden;
  border-radius: 12px;
  background: #f5f5f5;
}

.product-details-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-details-info {
  flex: 1;
}

.product-details-info h4 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 1rem 0;
}

.product-details-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #f44336;
  margin: 0 0 1.5rem 0;
}

.product-details-description {
  font-size: 1rem;
  color: #666;
  margin: 0 0 2rem 0;
  line-height: 1.6;
}

.product-details-meta {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.meta-row .meta-label {
  font-weight: 600;
  color: #333;
  min-width: 80px;
}

.meta-row .meta-value {
  color: #666;
}

.dialog-footer {
  margin-top: 2rem;
  display: flex;
  justify-content: flex-end;
}

.close-details-button {
  padding: 0.75rem 2rem;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-details-button:hover {
  background: #45a049;
  transform: translateY(-2px);
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

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
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

  .products-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .product-details {
    flex-direction: column;
  }

  .product-details-image {
    flex: 0 0 200px;
    width: 100%;
  }

  .search-filter {
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .admin-header-left h1 {
    font-size: 1.5rem;
  }

  .content-header h2 {
    font-size: 1.5rem;
  }

  .dialog-content {
    padding: 1.5rem;
  }

  .product-info {
    padding: 1.25rem;
  }
}
</style>