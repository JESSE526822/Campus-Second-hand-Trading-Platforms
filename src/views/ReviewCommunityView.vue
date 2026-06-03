<template>
  <div class="review-community">
    <div class="community-header">
      <h1>评论社区</h1>
      <p>查看所有用户的商品评价</p>
    </div>
    
    <div class="community-content">
      <div v-if="loading" class="loading">
        <p>加载中...</p>
      </div>
      <div v-else-if="reviews.length === 0" class="no-reviews">
        <div class="no-reviews-icon">⭐</div>
        <p>暂无评论</p>
      </div>
      <div v-else class="review-grid">
        <div v-for="review in reviews" :key="review.id" class="review-card">
          <div class="review-header">
            <div class="review-rating">
              <span v-for="i in 5" :key="i" class="star">
                {{ i <= review.rating ? '★' : '☆' }}
              </span>
            </div>
            <span class="review-date">{{ formatDate(review.createdAt) }}</span>
          </div>
          
          <div class="review-user">
            <span class="user-label">评论者：</span>
            <span class="user-name">{{ review.buyerName || '未知用户' }}</span>
          </div>
          
          <div class="review-content">
            {{ review.comment || '暂无评论内容' }}
          </div>
          
          <div class="review-product">
            <div class="product-info">
              <span class="product-label">商品：</span>
              <span class="product-title">{{ review.productTitle || '未知商品' }}</span>
            </div>
            <button @click="viewProduct(review.productId)" class="view-button">
              查看商品
            </button>
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
const reviews = ref([])
const loading = ref(true)

const fetchReviews = async () => {
  loading.value = true
  try {
    // 获取所有评论
    const response = await $api.get('/product/review/all')
    if (response.data.success) {
      reviews.value = response.data.reviews
    }
  } catch (error) {
    console.error('Error fetching reviews:', error)
    // API失败时返回空数组
    reviews.value = []
  } finally {
    loading.value = false
  }
}

const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.review-community {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  padding: 2rem 0;
}

.community-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, white 0%, #f8f9fa 100%);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border-radius: 20px;
  animation: fadeInUp 0.8s ease-out;
}

.community-header h1 {
  font-size: 2.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1.5rem;
  position: relative;
  display: inline-block;
}

.community-header h1::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #45a049);
  border-radius: 2px;
}

.community-header p {
  font-size: 1.2rem;
  color: #666;
  margin-top: 1.5rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.community-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.loading,
.no-reviews {
  text-align: center;
  padding: 6rem 2rem;
  color: #666;
  background: linear-gradient(135deg, white 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.8s ease-out;
}

.no-reviews {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.no-reviews-icon {
  font-size: 4rem;
  animation: bounce 2s infinite;
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 32px;
  margin-bottom: 30px;
}

.review-card {
  background: linear-gradient(135deg, white 0%, #f8f9fa 100%);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideUp 1s ease-out;
  position: relative;
  overflow: hidden;
}

.review-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #45a049);
  border-radius: 4px 4px 0 0;
}

.review-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.15);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.review-card:hover .star {
  transform: scale(1.1);
}

.review-date {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
  background: rgba(102, 102, 102, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
}

.review-user {
  font-size: 0.95rem;
  color: #666;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: rgba(76, 175, 80, 0.05);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.review-card:hover .review-user {
  background: rgba(76, 175, 80, 0.1);
  transform: translateX(4px);
}

.user-label {
  font-weight: 600;
  color: #333;
}

.user-name {
  color: #4CAF50;
  font-weight: 600;
  background: rgba(76, 175, 80, 0.1);
  padding: 2px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.review-card:hover .user-name {
  background: rgba(76, 175, 80, 0.2);
  transform: scale(1.05);
}

.review-content {
  line-height: 1.8;
  color: #333;
  margin-bottom: 24px;
  min-height: 120px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.review-card:hover .review-content {
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.08);
}

.review-product {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.review-card:hover .product-info {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(4px);
}

.product-label {
  font-weight: 600;
  color: #333;
  background: rgba(102, 126, 234, 0.1);
  padding: 4px 12px;
  border-radius: 8px;
}

.product-title {
  color: #333;
  font-weight: 600;
  flex: 1;
  font-size: 0.95rem;
  line-height: 1.4;
}

.view-button {
  padding: 12px 24px;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  align-self: flex-end;
  font-size: 0.95rem;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.view-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
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

/* Responsive styles */
@media (max-width: 768px) {
  .review-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .community-header h1 {
    font-size: 2.2rem;
  }
  
  .community-header {
    padding: 3rem 1.5rem;
  }
  
  .review-card {
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .community-header {
    padding: 2rem 1rem;
  }
  
  .community-header h1 {
    font-size: 1.8rem;
  }
  
  .community-content {
    padding: 0 10px;
  }
  
  .review-card {
    padding: 16px;
  }
  
  .review-content {
    padding: 16px;
    min-height: 100px;
  }
  
  .view-button {
    padding: 10px 20px;
    font-size: 0.9rem;
  }
}
</style>