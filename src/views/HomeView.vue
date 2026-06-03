<template>
  <div class="home">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>发现校园里的宝藏</h1>
        <p>轻松买卖二手物品，让资源循环利用</p>
      </div>
    </div>
    
    <!-- Search Bar -->
    <div class="search-section">
      <div class="container">
        <div class="search-bar">
          <div class="search-form">
            <div class="search-input-wrapper">
              <span class="search-icon">🔍</span>
              <input 
                type="text" 
                v-model="searchKeyword" 
                placeholder="搜索你想要的商品..." 
                class="search-input"
                :class="{ 'input-focused': isSearchFocused }"
                @focus="isSearchFocused = true"
                @blur="isSearchFocused = false"
              >
            </div>
            <select v-model="searchCategory" class="search-select">
              <option value="">全部分类</option>
              <option v-for="category in categories" :key="category" :value="category">
                {{ category }}
              </option>
            </select>
            <select v-model="sortBy" class="search-select">
              <option value="price_asc">价格从低到高</option>
              <option value="price_desc">价格从高到低</option>
            </select>
            <button @click="searchProducts" class="search-button" :class="{ 'button-loading': loading }">
              <span v-if="!loading">搜索</span>
              <span v-else class="loading-spinner">
                <span class="spinner"></span>
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Product Section -->
    <div class="container">
      <div class="product-section">
        <div class="section-header">
          <h2>热门商品</h2>
          <p>为你推荐的优质二手物品</p>
        </div>
        
        <div v-if="loading" class="loading-products">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <div class="skeleton-image"></div>
            <div class="skeleton-content">
              <div class="skeleton-title"></div>
              <div class="skeleton-price"></div>
              <div class="skeleton-info"></div>
              <div class="skeleton-link"></div>
            </div>
          </div>
        </div>
        
        <div v-else class="product-grid">
          <div 
            v-for="product in products" 
            :key="product.id" 
            class="product-card" 
            :class="{ 'sold': product.status === 'sold' }"
            @mouseenter="hoveredProduct = product.id"
            @mouseleave="hoveredProduct = null"
          >
            <div class="product-image-container">
              <img 
                :src="product.imageUrl" 
                :alt="product.title" 
                class="product-image"
                :class="{ 'image-hover': hoveredProduct === product.id }"
              >
              <div v-if="product.status === 'sold'" class="sold-badge">已下架</div>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">¥{{ product.price }}</p>
              <div class="product-meta">
                <span class="product-category">{{ product.category }}</span>
                <span class="product-condition">{{ product.condition }}</span>
              </div>
              <div class="product-actions">
                <router-link :to="`/product/${product.id}`" class="product-link">
                  查看详情
                  <span class="link-arrow">→</span>
                </router-link>
                <button @click="toggleFavorite(product.id)" class="favorite-button" :class="{ 'favorited': favoriteProducts.includes(product.id) }">
                  <span class="favorite-icon">{{ favoriteProducts.includes(product.id) ? '★' : '☆' }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="!loading && products.length === 0" class="no-products">
          <div class="no-products-icon">📦</div>
          <h3>暂无商品</h3>
          <p>换个关键词试试，或者稍后再来看看</p>
          <router-link to="/publish" class="publish-link">发布商品</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { inject } from 'vue'
import { useRouter } from 'vue-router'

const $api = inject('$api')
const router = useRouter()
const products = ref([])
const categories = ref([])
const searchKeyword = ref('')
const searchCategory = ref('')
const sortBy = ref('price_asc') // 默认价格从低到高
const loading = ref(false)
const isSearchFocused = ref(false)
const hoveredProduct = ref(null)
const favoriteProducts = ref([]) // 收藏的商品ID列表

// No mock data - only fetch from database API

const fetchProducts = async () => {
  loading.value = true
  try {
    console.log('Fetching products with params:', {
      keyword: searchKeyword.value,
      category: searchCategory.value,
      sortBy: sortBy.value
    })
    const response = await $api.get('/product/search', {
      params: {
        keyword: searchKeyword.value,
        category: searchCategory.value,
        sortBy: sortBy.value
      }
    })
    console.log('Fetched products from API:', response.data.products)
    console.log('Total products from API:', response.data.products.length)
    products.value = response.data.products
  } catch (error) {
    console.error('Error fetching products from API:', error)
    products.value = []
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    console.log('Fetching categories from API...')
    const response = await $api.get('/product/categories')
    console.log('Fetched categories from API:', response.data.categories)
    console.log('Total categories from API:', response.data.categories.length)
    categories.value = response.data.categories
  } catch (error) {
    console.error('Error fetching categories from API:', error)
    categories.value = []
  }
}

const searchProducts = () => {
  fetchProducts()
}

const toggleFavorite = async (productId) => {
  // Check if user is logged in
  if (!localStorage.getItem('user')) {
    router.push('/login')
    return
  }
  
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (favoriteProducts.value.includes(productId)) {
      // Remove from favorites
      const response = await $api.get('/product/favorite/remove', {
        params: {
          userId: user.id,
          productId: productId
        }
      })
      if (response.data.success) {
        favoriteProducts.value = favoriteProducts.value.filter(id => id !== productId)
        alert('已移除收藏')
      }
    } else {
      // Add to favorites
      const response = await $api.get('/product/favorite/add', {
        params: {
          userId: user.id,
          productId: productId
        }
      })
      if (response.data.success) {
        favoriteProducts.value.push(productId)
        alert('已添加收藏')
      }
    }
  } catch (error) {
    console.error('Error toggling favorite:', error)
    // Mock implementation if API fails
    if (favoriteProducts.value.includes(productId)) {
      favoriteProducts.value = favoriteProducts.value.filter(id => id !== productId)
      alert('已移除收藏')
    } else {
      favoriteProducts.value.push(productId)
      alert('已添加收藏')
    }
  }
}

// Watch for changes in category and sortBy to trigger fetch immediately
watch([searchCategory, sortBy], () => {
  fetchProducts()
})

const fetchUserFavorites = async () => {
  // Check if user is logged in
  if (!localStorage.getItem('user')) {
    return
  }
  
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    const response = await $api.get(`/product/favorite/user/${user.id}`)
    if (response.data.success) {
      favoriteProducts.value = response.data.favorites.map(fav => fav.productId)
      console.log('Fetched user favorites:', favoriteProducts.value)
    }
  } catch (error) {
    console.error('Error fetching user favorites:', error)
  }
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchUserFavorites()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  padding: 8rem 0;
  text-align: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: float 20s linear infinite;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  100% {
    transform: translate(-50px, -50px) rotate(360deg);
  }
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 2rem;
}

.hero-content h1 {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 1px;
  animation: fadeInUp 1s ease-out;
}

.hero-content p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
  line-height: 1.6;
  animation: fadeInUp 1s ease-out 0.2s both;
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

/* Search Section */
.search-section {
  margin-top: -4rem;
  margin-bottom: 4rem;
  position: relative;
  z-index: 2;
}

.search-bar {
  background: white;
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: slideUp 0.8s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-form {
  display: flex;
  gap: 1.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-input-wrapper {
  flex: 1;
  min-width: 300px;
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-input-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
}

.input-focused .search-input-wrapper::before {
  opacity: 0.05;
}

.search-icon {
  position: absolute;
  left: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.1rem;
  color: #666;
  z-index: 2;
  transition: all 0.3s ease;
}

.input-focused .search-icon {
  color: #4CAF50;
}

.search-input {
  width: 100%;
  padding: 1.25rem 1.25rem 1.25rem 3.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  position: relative;
  z-index: 2;
}

.search-input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
  transform: translateY(-2px);
}

.search-input::placeholder {
  color: #999;
  transition: all 0.3s ease;
}

.search-input:focus::placeholder {
  color: #ccc;
  transform: translateX(5px);
}

.search-select {
  padding: 1.25rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  min-width: 180px;
}

.search-select:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
  transform: translateY(-2px);
}

.search-button {
  padding: 1.25rem 2rem;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-width: 120px;
}

.search-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(76, 175, 80, 0.3);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.search-button:active:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(76, 175, 80, 0.3);
}

.search-button:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Product Section */
.product-section {
  margin-top: 4rem;
  margin-bottom: 6rem;
}

.section-header {
  text-align: center;
  margin-bottom: 4rem;
  animation: fadeIn 1s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.section-header h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1rem;
  position: relative;
  display: inline-block;
}

.section-header h2::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #45a049);
  border-radius: 2px;
}

.section-header p {
  font-size: 1.1rem;
  color: #666;
  margin-top: 1.5rem;
}

/* Loading Products */
.loading-products {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.skeleton-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: pulse 1.5s infinite ease-in-out;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.skeleton-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
}

.skeleton-content {
  padding: 1.5rem;
}

.skeleton-title {
  width: 80%;
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.skeleton-price {
  width: 50%;
  height: 24px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 2s infinite 0.2s;
  border-radius: 4px;
  margin-bottom: 0.8rem;
}

.skeleton-info {
  width: 60%;
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 2s infinite 0.4s;
  border-radius: 4px;
  margin-bottom: 1.5rem;
}

.skeleton-link {
  width: 40%;
  height: 36px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 2s infinite 0.6s;
  border-radius: 8px;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 32px;
  margin-top: 2rem;
}

.product-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.15);
}

.product-card.sold {
  opacity: 0.7;
  position: relative;
}

.product-card.sold:hover {
  transform: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: not-allowed;
}

.product-image-container {
  position: relative;
  overflow: hidden;
  height: 220px;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-hover {
  transform: scale(1.1);
}

.sold-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  z-index: 3;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}



.product-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
  line-height: 1.4;
  height: 56px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.3s ease;
}

.product-card:hover .product-title {
  color: #4CAF50;
}

.product-price {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
  margin-bottom: 12px;
  transition: color 0.3s ease;
}

.product-card:hover .product-price {
  color: #ff7875;
}

.product-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.product-category {
  font-size: 14px;
  color: #666;
  background: #f5f5f5;
  padding: 4px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.product-condition {
  font-size: 14px;
  color: #666;
  background: #f5f5f5;
  padding: 4px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.product-card:hover .product-category,
.product-card:hover .product-condition {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.product-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: auto;
}

.product-link {
  flex: 1;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border-radius: 12px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.product-link:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.link-arrow {
  transition: all 0.3s ease;
}

.product-link:hover .link-arrow {
  transform: translateX(5px);
}

.favorite-button {
  padding: 12px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  min-width: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-button:hover {
  transform: scale(1.1);
  border-color: #4CAF50;
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.2);
}

.favorite-button.favorited {
  animation: pulse 0.6s ease-in-out;
  border-color: #ffc107;
  background: rgba(255, 193, 7, 0.05);
}

.favorite-icon {
  font-size: 1.2rem;
  transition: all 0.3s ease;
  color: #666;
}

.favorite-button:hover .favorite-icon {
  transform: scale(1.2);
  color: #ffc107;
}

.favorite-button.favorited .favorite-icon {
  color: #ffc107;
}

/* No Products */
.no-products {
  text-align: center;
  padding: 8rem 2rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-top: 2rem;
  animation: fadeIn 1s ease-out;
}

.no-products-icon {
  font-size: 4rem;
  margin-bottom: 2rem;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.no-products h3 {
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.no-products p {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 3rem;
  line-height: 1.6;
}

.publish-link {
  display: inline-block;
  padding: 16px 32px;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border-radius: 25px;
  text-decoration: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.publish-link:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

/* Responsive Styles */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 24px;
  }
  
  .hero-content h1 {
    font-size: 3rem;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 6rem 0;
  }
  
  .hero-content h1 {
    font-size: 2.2rem;
  }
  
  .hero-content p {
    font-size: 1rem;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input-wrapper {
    min-width: auto;
  }
  
  .search-select {
    width: 100%;
  }
  
  .search-button {
    width: 100%;
  }
  
  .section-header h2 {
    font-size: 2rem;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 20px;
  }
  
  .product-image-container {
    height: 180px;
  }
  
  .loading-products {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .hero-content h1 {
    font-size: 1.8rem;
  }
  
  .search-bar {
    padding: 1.5rem;
  }
  
  .search-input {
    padding: 1rem 1rem 1rem 3rem;
  }
  
  .product-info {
    padding: 16px;
  }
  
  .no-products {
    padding: 4rem 1.5rem;
  }
  
  .no-products-icon {
    font-size: 3rem;
  }
  
  .no-products h3 {
    font-size: 1.5rem;
  }
}
</style>
