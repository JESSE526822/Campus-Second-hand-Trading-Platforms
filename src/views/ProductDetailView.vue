<template>
  <div class="product-detail">
    <div v-if="loading" class="loading">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
    </div>
    <div v-else-if="!product" class="error">
      <div class="error-content">
        <div class="error-icon">📦</div>
        <h2>商品不存在</h2>
        <p>抱歉，您查看的商品可能已被删除或不存在</p>
        <button @click="goBack" class="action-button primary">返回首页</button>
      </div>
    </div>
    <div v-else class="product-content">
      <div class="container">
        <div class="product-header">
          <button @click="goBack" class="back-button" :class="{ 'button-hover': true }">
            <span class="back-icon">←</span>
            <span>返回</span>
          </button>
        </div>
        
        <div class="product-main">
          <div class="product-images">
            <div class="image-container" :class="{ 'image-hover': true }">
              <img :src="product.imageUrl" :alt="product.title" class="main-image">
              <div class="image-overlay">
                <span class="zoom-icon">🔍</span>
              </div>
            </div>
          </div>
          <div class="product-info">
            <div class="product-status-badge" :class="product.status === 'sold' ? 'status-sold' : 'status-active'">
              {{ product.status === 'for_sale' ? '在售' : product.status === 'sold' ? '已售' : '拍卖中' }}
            </div>
            <h1 class="product-title">{{ product.title }}</h1>
            <div class="product-price-section">
              <p class="product-price">¥{{ product.price }}</p>
            </div>
            <div class="product-meta">
              <div class="meta-grid">
                <div class="meta-item">
                  <span class="meta-label">分类</span>
                  <span class="meta-value">{{ product.category }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">成色</span>
                  <span class="meta-value">{{ product.condition }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">发货方式</span>
                  <span class="meta-value">{{ product.shippingMethod }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">售后支持</span>
                  <span class="meta-value">{{ product.afterSales }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">发布时间</span>
                  <span class="meta-value">{{ formatDate(product.createdAt) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">物主</span>
                  <span class="meta-value">{{ product.ownerName || '未知' }}</span>
                </div>
              </div>
            </div>
            <div class="product-actions">
                <button @click="addToCart" class="action-button primary" :disabled="product.status === 'sold' || isAddedToCart">
                  <span class="button-icon">{{ isAddedToCart ? '✓' : '🛒' }}</span>
                  <span>{{ isAddedToCart ? '已加入购物车' : '加入购物车' }}</span>
                </button>
                <button @click="contactSeller" class="action-button secondary" :disabled="product.status === 'sold'">
                  <span class="button-icon">💬</span>
                  <span>联系卖家</span>
                </button>
              </div>
          </div>
        </div>
        
        <div class="product-reviews">
          <div class="section-header">
            <h2>用户评价</h2>
            <p>查看其他买家对这款商品的评价</p>
          </div>
          
          <!-- Add review form -->
          <div v-if="isLoggedIn" class="add-review-form">
            <div class="form-header">
              <h3>发表评价</h3>
              <p>分享你的购物体验，帮助其他买家做出选择</p>
            </div>
            <div class="form-content">
              <div class="form-group">
                <label class="form-label">评分</label>
                <div class="rating-input">
                  <span 
                    v-for="i in 5" 
                    :key="i" 
                    class="star-input"
                    :class="{ active: reviewRating >= i }"
                    @click="reviewRating = i"
                  >
                    ★
                  </span>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">评论内容</label>
                <textarea 
                  v-model="reviewComment" 
                  placeholder="请详细描述你的购物体验，商品质量、卖家服务等方面..." 
                  class="form-textarea"
                  rows="4"
                ></textarea>
              </div>
              <button @click="submitReview" class="submit-review-button primary" :class="{ 'button-loading': submittingReview }">
                <span v-if="!submittingReview">提交评价</span>
                <span v-else>
                  <span class="spinner small"></span>
                  提交中...
                </span>
              </button>
            </div>
          </div>
          
          <div v-else class="login-prompt">
            <div class="prompt-content">
              <div class="prompt-icon">🔐</div>
              <h3>登录后即可发表评价</h3>
              <p>登录你的账号，分享你的购物体验</p>
              <router-link to="/login" class="login-link">去登录</router-link>
            </div>
          </div>
          
          <div v-if="reviews.length === 0" class="no-reviews">
            <div class="no-reviews-content">
              <div class="no-reviews-icon">⭐</div>
              <h3>暂无评价</h3>
              <p>成为第一个评价这款商品的用户吧！</p>
            </div>
          </div>
          <div v-else class="review-list">
            <div 
              v-for="review in reviews" 
              :key="review.id" 
              class="review-item"
              :class="{ 'review-animation': true }"
            >
              <div class="review-header">
                <div class="review-rating">
                  <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= review.rating }">
                    ★
                  </span>
                </div>
                <div class="review-actions">
                  <span class="review-date">{{ formatDate(review.createdAt) }}</span>
                  <button 
                    v-if="isLoggedIn && userId === review.buyerId" 
                    @click="deleteReview(review.id)" 
                    class="delete-review-button"
                    :class="{ 'button-hover': true }"
                  >
                    <span class="delete-icon">🗑️</span>
                    <span>删除</span>
                  </button>
                </div>
              </div>
              <div class="review-content">
                {{ review.comment }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { inject } from 'vue'

const route = useRoute()
const router = useRouter()
const $api = inject('$api')
const product = ref(null)
const reviews = ref([])
const loading = ref(true)
const userId = ref(1) // 模拟当前用户ID，实际应从登录状态获取
const isLoggedIn = ref(false) // 登录状态
const reviewRating = ref(5) // 默认评分为5星
const reviewComment = ref('') // 评论内容
const isAddedToCart = ref(false) // 是否已加入购物车

// No mock data - only fetch from database API

const fetchProduct = async () => {
  loading.value = true
  try {
    // Get product ID from route params
    const productId = parseInt(route.params.id)
    console.log('Fetching product with ID:', productId)
    
    // Fetch from API
    console.log('Fetching from API...')
    const response = await $api.get(`/product/info/${route.params.id}`)
    console.log('API response received:', response.data.success)
    if (response.data.success) {
      console.log('Setting product from API data')
    product.value = response.data.product
    checkCartStatus()
    } else {
      console.log('API returned success: false')
      product.value = null
    }
  } catch (error) {
    console.error('Error fetching product from API:', error)
    product.value = null
  } finally {
    loading.value = false
    console.log('Final product value:', product.value ? product.value.title : 'null')
  }
}

const fetchReviews = async () => {
  // Run in background without blocking UI
  try {
    // 获取商品的评价列表
    const response = await $api.get(`/product/review/product/${route.params.id}`)
    if (response.data.success) {
      reviews.value = response.data.reviews
    }
  } catch (error) {
    console.error('Error fetching reviews:', error)
    // Use empty array if API fails
    reviews.value = []
  }
}



const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

const checkCartStatus = async () => {
  // Check if user is logged in
  if (!localStorage.getItem('user')) {
    return
  }
  
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    const response = await $api.get(`/product/cart/user/${user.id}`)
    if (response.data.success) {
      const cartItems = response.data.cartItems
      const isInCart = cartItems.some(item => item.productId === product.value.id)
      isAddedToCart.value = isInCart
      console.log('Cart status checked:', isAddedToCart.value)
    }
  } catch (error) {
    console.error('Error checking cart status:', error)
    // Default to false if API fails
    isAddedToCart.value = false
  }
}

const addToCart = async () => {
  // Check if user is logged in
  if (!localStorage.getItem('user')) {
    router.push('/login')
    return
  }
  
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    const response = await $api.post('/product/cart/add', {
      userId: user.id,
      productId: product.value.id,
      quantity: 1
    })
    
    if (response.data.success) {
      // 标记为已加入购物车
      isAddedToCart.value = true
      console.log('已加入购物车')
    } else {
      console.error('加入购物车失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('Error adding to cart:', error)
  }
}

const contactSeller = () => {
  // Check if user is logged in
  if (!localStorage.getItem('user')) {
    router.push('/login')
    return
  }
  
  // Check if current user is the owner
  const user = JSON.parse(localStorage.getItem('user'))
  if (user && user.id === product.value?.userId) {
    alert('不能联系自己')
    return
  }
  
  // Implement contact seller functionality
  router.push(`/chat/${product.value?.userId || 1}`)
}

const goBack = () => {
  router.push('/')
}

const checkLoginStatus = () => {
  const user = localStorage.getItem('user')
  isLoggedIn.value = !!user
  if (user) {
    try {
      const parsedUser = JSON.parse(user)
      userId.value = parsedUser.id || 1
    } catch (error) {
      console.error('Error parsing user data from localStorage:', error)
      // If parsing fails, use default user ID
      userId.value = 1
    }
  }
}

const submitReview = async () => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  
  if (reviewRating.value === 0) {
    alert('请选择评分')
    return
  }
  
  if (!reviewComment.value.trim()) {
    alert('请输入评论内容')
    return
  }
  
  try {
    const response = await $api.post('/product/review/create', {
      productId: product.value.id,
      buyerId: userId.value,
      sellerId: product.value.userId,
      rating: reviewRating.value,
      comment: reviewComment.value
    })
    
    if (response.data.success) {
      alert('评价提交成功')
      // Reset form
      reviewRating.value = 5
      reviewComment.value = ''
      // Refresh reviews
      fetchReviews()
    }
  } catch (error) {
    console.error('Error submitting review:', error)
    alert('评价提交失败，请稍后重试')
  }
}

const deleteReview = async (reviewId) => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  
  try {
    const response = await $api.delete(`/api/product/review/delete/${reviewId}`)
    if (response.data.success) {
      alert('评价删除成功')
      // Refresh reviews
      fetchReviews()
    }
  } catch (error) {
    console.error('Error deleting review:', error)
    alert('评价删除失败，请稍后重试')
  }
}

onMounted(() => {
  checkLoginStatus()
  // Fetch product first for immediate display
  fetchProduct()
  // Fetch reviews in parallel
  fetchReviews()
})
</script>

<style scoped>
.product-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  padding: 2rem 0;
}

.loading,
.error {
  text-align: center;
  padding: 6rem 2rem;
  color: #666;
  background-color: white;
  border-radius: 20px;
  max-width: 800px;
  margin: 0 auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.8s ease-out;
}

.error button {
  margin-top: 20px;
}

.product-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-header {
  margin-bottom: 30px;
  animation: slideIn 0.8s ease-out;
}

.back-button {
  padding: 12px 24px;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  border: none;
  border-radius: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 1rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.back-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.back-icon {
  font-size: 1.2rem;
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  margin-bottom: 3rem;
  background-color: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: slideUp 1s ease-out;
}

.product-images {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.image-container {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-container:hover {
  transform: scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.main-image {
  width: 100%;
  max-height: 500px;
  object-fit: cover;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-hover {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 2;
}

.image-container:hover .image-overlay {
  opacity: 1;
}

.zoom-icon {
  font-size: 2rem;
  color: white;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 16px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.image-container:hover .zoom-icon {
  transform: scale(1.2);
  background: rgba(255, 255, 255, 0.3);
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.product-status-badge {
  align-self: flex-start;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.status-active {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
}

.status-sold {
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  color: white;
}

.product-title {
  font-size: 2rem;
  color: #333;
  margin: 0;
  line-height: 1.4;
  font-weight: 700;
  transition: color 0.3s ease;
}

.product-price-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.product-price {
  font-size: 2.5rem;
  font-weight: bold;
  color: #ff4d4f;
  margin: 0;
  transition: color 0.3s ease;
}

.favorite-button {
  background: white;
  border: 2px solid #e0e0e0;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 8px;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.favorite-button:hover {
  transform: scale(1.2);
  border-color: #ffc107;
  box-shadow: 0 4px 16px rgba(255, 193, 7, 0.2);
}

.favorite-button.favorited {
  animation: pulse 0.6s ease-in-out;
  border-color: #ffc107;
  background: rgba(255, 193, 7, 0.1);
}

.favorite-icon {
  color: #666;
  transition: all 0.3s ease;
}

.favorite-button:hover .favorite-icon {
  color: #ffc107;
}

.favorite-button.favorited .favorite-icon {
  color: #ffc107;
}

.product-meta {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 24px;
  border-radius: 16px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: all 0.3s ease;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
}

.meta-item:hover {
  background: rgba(76, 175, 80, 0.05);
  transform: translateY(-2px);
}

.meta-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-value {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
}

.product-actions {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.action-button {
  flex: 1;
  padding: 16px 24px;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 150px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-button.primary {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
}

.action-button.primary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.action-button.secondary {
  background: white;
  color: #4CAF50;
  border: 2px solid #4CAF50;
}

.action-button.secondary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.2);
  background: rgba(76, 175, 80, 0.05);
}

.action-button:disabled {
  background: #c0c4cc;
  color: #666;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  border-color: #c0c4cc;
}

.button-icon {
  font-size: 1.2rem;
}

.product-reviews {
  margin-top: 3rem;
  background-color: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: slideUp 1s ease-out 0.2s both;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.section-header h2 {
  font-size: 2rem;
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

.add-review-form {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 30px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.8s ease-out;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-header h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-header p {
  font-size: 1rem;
  color: #666;
  margin: 0;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  position: relative;
}

.form-label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.rating-input {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.star-input {
  font-size: 1.8rem;
  color: #dcdfe6;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 4px;
}

.star-input:hover, .star-input.active {
  color: #ff9800;
  transform: scale(1.2);
}

.form-textarea {
  width: 100%;
  min-height: 150px;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  resize: vertical;
  font-family: inherit;
  background: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
  transform: translateY(-2px);
}

.submit-review-button {
  padding: 16px 32px;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  align-self: center;
  min-width: 200px;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.submit-review-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.submit-review-button:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-prompt {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 30px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.8s ease-out;
}

.prompt-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.prompt-icon {
  font-size: 3rem;
  animation: bounce 2s infinite;
}

.prompt-content h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.prompt-content p {
  font-size: 1rem;
  color: #666;
  margin: 0;
  max-width: 400px;
}

.login-link {
  display: inline-block;
  margin-top: 12px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.login-link:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.no-reviews {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  animation: fadeIn 0.8s ease-out;
}

.no-reviews-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.no-reviews-icon {
  font-size: 4rem;
  animation: bounce 2s infinite;
}

.no-reviews-content h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.no-reviews-content p {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
  max-width: 400px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.review-item {
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: reviewAnimation 0.8s ease-out;
}

.review-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.review-rating {
  color: #ff9800;
  font-size: 1.5rem;
  display: flex;
  gap: 4px;
}

.star {
  transition: all 0.3s ease;
}

.star.active {
  animation: pulse 0.6s ease-in-out;
}

.review-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.review-date {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.delete-review-button {
  padding: 8px 16px;
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.delete-review-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(245, 108, 108, 0.4);
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
}

.delete-icon {
  font-size: 1rem;
}

.review-content {
  line-height: 1.6;
  color: #333;
  font-size: 1rem;
  background: white;
  padding: 16px;
  border-radius: 12px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* Animations */
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
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
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

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
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

@keyframes reviewAnimation {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Responsive styles */
@media (max-width: 768px) {
  .product-main {
    grid-template-columns: 1fr;
    gap: 2rem;
    padding: 24px;
  }
  
  .product-actions {
    flex-direction: column;
  }
  
  .product-title {
    font-size: 1.8rem;
  }
  
  .product-price {
    font-size: 2.2rem;
  }
  
  .meta-grid {
    grid-template-columns: 1fr;
  }
  
  .product-reviews {
    padding: 24px;
  }
  
  .add-review-form {
    padding: 24px;
  }
  
  .login-prompt {
    padding: 24px;
  }
  
  .section-header h2 {
    font-size: 1.8rem;
  }
}

@media (max-width: 480px) {
  .product-main {
    padding: 16px;
  }
  
  .product-reviews {
    padding: 16px;
  }
  
  .add-review-form {
    padding: 16px;
  }
  
  .login-prompt {
    padding: 16px;
  }
  
  .product-title {
    font-size: 1.5rem;
  }
  
  .product-price {
    font-size: 1.8rem;
  }
  
  .action-button {
    padding: 12px 16px;
    font-size: 1rem;
  }
  
  .submit-review-button {
    padding: 12px 24px;
    font-size: 1rem;
  }
}
</style>
