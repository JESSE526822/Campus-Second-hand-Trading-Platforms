<template>
  <div class="my-reviews">
    <h3>我的评论</h3>
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="reviews.length === 0" class="empty">
      <p>暂无评论</p>
    </div>
    <div v-else class="reviews-list">
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <div class="review-product">
            <h4 class="product-title">{{ review.product.title }}</h4>
            <p class="product-price">¥{{ review.product.price }}</p>
          </div>
          <div class="review-rating">
            <span v-for="i in 5" :key="i" class="star">
              {{ i <= review.rating ? '★' : '☆' }}
            </span>
          </div>
        </div>
        <div class="review-content">
          {{ review.comment }}
        </div>
        <div class="review-footer">
          <span class="review-date">{{ formatDate(review.createdAt) }}</span>
          <button @click="deleteReview(review.id)" class="delete-button">删除</button>
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

const fetchMyReviews = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    const response = await $api.get(`/api/product/review/user/${user.id}`)
    if (response.data.success) {
      reviews.value = response.data.reviews
    }
  } catch (error) {
    console.error('Error fetching reviews:', error)
    reviews.value = []
  } finally {
    loading.value = false
  }
}

const deleteReview = async (reviewId) => {
  if (confirm('确定要删除这条评论吗？')) {
    try {
      const response = await $api.delete(`/api/product/review/delete/${reviewId}`)
      if (response.data.success) {
        alert('评论删除成功')
        // Refresh reviews
        fetchMyReviews()
      }
    } catch (error) {
      console.error('Error deleting review:', error)
      alert('评论删除失败，请稍后重试')
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

onMounted(() => {
  fetchMyReviews()
})
</script>

<style scoped>
.my-reviews {
  padding: 1rem 0;
}

.my-reviews h3 {
  margin-bottom: 1.5rem;
  color: #333;
}

.loading,
.empty {
  text-align: center;
  padding: 4rem 0;
  color: #666;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.review-item {
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #409eff;
  transition: all 0.3s;
}

.review-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.review-product {
  flex: 1;
}

.product-title {
  margin: 0 0 0.25rem 0;
  color: #333;
  font-size: 1.1rem;
}

.product-price {
  margin: 0;
  font-weight: bold;
  color: #4CAF50;
  font-size: 1rem;
}

.review-rating {
  color: #ff9800;
  font-size: 1.2rem;
}

.review-content {
  line-height: 1.6;
  color: #606266;
  margin-bottom: 1rem;
  min-height: 80px;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 1rem;
}

.review-date {
  font-size: 0.9rem;
  color: #909399;
}

.delete-button {
  padding: 0.5rem 1rem;
  border: 1px solid #f56c6c;
  background-color: white;
  color: #f56c6c;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.delete-button:hover {
  background-color: #f56c6c;
  color: white;
}

/* Responsive styles */
@media (max-width: 768px) {
  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .review-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .delete-button {
    align-self: flex-end;
  }
}
</style>