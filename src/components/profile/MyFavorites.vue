<template>
  <div class="my-favorites">
    <h3>我的收藏</h3>
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="favorites.length === 0" class="empty">
      <p>暂无收藏</p>
      <router-link to="/" class="browse-link">浏览商品</router-link>
    </div>
    <div v-else class="favorite-list">
      <div v-for="favorite in favorites" :key="favorite.productId" class="favorite-item">
        <img :src="favorite.product.imageUrl" :alt="favorite.product.title" class="product-image">
        <div class="product-info">
          <h4 class="product-title">{{ favorite.product.title }}</h4>
          <p class="product-price">¥{{ favorite.product.price }}</p>
          <p class="product-status">{{ getStatusText(favorite.product.status) }}</p>
          <div class="product-actions">
            <button @click="removeFavorite(favorite.id)" class="action-button danger">取消收藏</button>
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
const favorites = ref([])
const loading = ref(true)

const fetchMyFavorites = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    const response = await $api.get(`/product/favorite/user/${user.id}`)
    if (response.data.success) {
      favorites.value = response.data.favorites
    }
  } catch (error) {
    console.error('Error fetching favorites:', error)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'for_sale': '在售',
    'sold': '已售',
    'auctioning': '拍卖中'
  }
  return statusMap[status] || status
}

const removeFavorite = async (favoriteId) => {
  if (confirm('确定要取消收藏吗？')) {
    try {
      const response = await $api.delete(`/favorite/remove/${favoriteId}`)
      if (response.data.success) {
        // Refresh favorite list
        fetchMyFavorites()
      }
    } catch (error) {
      console.error('Error removing favorite:', error)
    }
  }
}

onMounted(() => {
  fetchMyFavorites()
})
</script>

<style scoped>
.my-favorites {
  padding: 1rem 0;
}

.my-favorites h3 {
  margin-bottom: 1.5rem;
  color: #333;
}

.loading,
.empty {
  text-align: center;
  padding: 4rem 0;
  color: #666;
}

.browse-link {
  display: inline-block;
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.browse-link:hover {
  background-color: #45a049;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.favorite-item {
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-title {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.product-price {
  margin: 0 0 0.5rem 0;
  font-weight: bold;
  color: #4CAF50;
}

.product-status {
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: #666;
}

.product-actions {
  display: flex;
  gap: 1rem;
}

.action-button {
  padding: 0.5rem 1rem;
  border: 1px solid #4CAF50;
  background-color: white;
  color: #4CAF50;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.action-button:hover {
  background-color: #f0f9f0;
}

.action-button.danger {
  border-color: #f44336;
  color: #f44336;
}

.action-button.danger:hover {
  background-color: #ffebee;
}

/* Responsive styles */
@media (max-width: 768px) {
  .favorite-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .product-image {
    width: 100%;
    max-width: 200px;
    height: auto;
  }
  
  .product-actions {
    justify-content: center;
  }
}
</style>