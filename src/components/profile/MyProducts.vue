<template>
  <div class="my-products">
    <h3>我的商品</h3>
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="products.length === 0" class="empty">
      <p>暂无商品</p>
      <router-link to="/publish" class="publish-link">发布商品</router-link>
    </div>
    <div v-else class="product-list">
      <div v-for="product in products" :key="product.id" class="product-item">
        <img :src="product.imageUrl" :alt="product.title" class="product-image">
        <div class="product-info">
          <h4 class="product-title">{{ product.title }}</h4>
          <p class="product-price">¥{{ product.price }}</p>
          <p class="product-status">{{ getStatusText(product.status) }}</p>
          <div class="product-actions">
            <router-link :to="`/product/${product.id}`" class="action-button">查看详情</router-link>
            <button v-if="product.status === 'sold'" @click="putOnSale(product.id)" class="action-button">上架</button>
            <button v-if="product.status === 'for_sale'" @click="takeOffSale(product.id)" class="action-button">下架</button>
            <button v-if="product.status === 'for_sale'" disabled class="action-button disabled">上架</button>
            <button v-if="product.status === 'sold'" disabled class="action-button disabled">下架</button>
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
const products = ref([])
const loading = ref(true)

const fetchMyProducts = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    const response = await $api.get(`/product/user/${user.id}`)
    if (response.data.success) {
      products.value = response.data.products
    }
  } catch (error) {
    console.error('Error fetching products:', error)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'for_sale': '在售',
    'sold': '已下架',
    'auctioning': '拍卖中'
  }
  return statusMap[status] || status
}

const takeOffSale = async (productId) => {
  try {
    const response = await $api.post(`/product/update-status/${productId}`, {
      status: 'sold'
    })
    if (response.data.success) {
      // Refresh product list
      fetchMyProducts()
    }
  } catch (error) {
    console.error('Error updating product status:', error)
  }
}

const putOnSale = async (productId) => {
  try {
    const response = await $api.post(`/product/update-status/${productId}`, {
      status: 'for_sale'
    })
    if (response.data.success) {
      // Refresh product list
      fetchMyProducts()
    }
  } catch (error) {
    console.error('Error updating product status:', error)
  }
}



onMounted(() => {
  fetchMyProducts()
})
</script>

<style scoped>
.my-products {
  padding: 1rem 0;
}

.my-products h3 {
  margin-bottom: 1.5rem;
  color: #333;
}

.loading,
.empty {
  text-align: center;
  padding: 4rem 0;
  color: #666;
}

.publish-link {
  display: inline-block;
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.publish-link:hover {
  background-color: #45a049;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.product-item {
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

.action-button.disabled {
  border-color: #dcdfe6;
  color: #c0c4cc;
  cursor: not-allowed;
}

.action-button.disabled:hover {
  background-color: white;
  border-color: #dcdfe6;
  color: #c0c4cc;
}

/* Responsive styles */
@media (max-width: 768px) {
  .product-item {
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
