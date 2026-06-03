<template>
  <div class="my-transactions">
    <h3>交易管理</h3>
    <div class="transaction-tabs">
      <button @click="activeTab = 'buying'" :class="['tab-button', { active: activeTab === 'buying' }]">我买到的</button>
      <button @click="activeTab = 'selling'" :class="['tab-button', { active: activeTab === 'selling' }]">我卖出的</button>
    </div>
    
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="activeTab === 'buying' && buyingTransactions.length === 0" class="empty">
      <p>暂无买入交易</p>
    </div>
    <div v-else-if="activeTab === 'selling' && sellingTransactions.length === 0" class="empty">
      <p>暂无卖出交易</p>
    </div>
    <div v-else class="transaction-list">
      <div v-for="transaction in activeTransactions" :key="transaction.id" class="transaction-item">
        <div class="transaction-info">
          <h4 class="transaction-product">{{ getProductTitle(transaction.productId) }}</h4>
          <p class="transaction-price">¥{{ transaction.price }}</p>
          <p class="transaction-status">{{ getStatusText(transaction.status) }}</p>
          <p class="transaction-date">{{ formatDate(transaction.createdAt) }}</p>
        </div>
        <div class="transaction-actions">
          <router-link :to="`/product/${transaction.productId}`" class="action-button">查看商品</router-link>
          <button @click="updateStatus(transaction.id)" class="action-button">更新状态</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'

const router = useRouter()
const $api = inject('$api')
const activeTab = ref('buying')
const buyingTransactions = ref([])
const sellingTransactions = ref([])
const loading = ref(true)
const products = ref({})

const activeTransactions = computed(() => {
  return activeTab.value === 'buying' ? buyingTransactions.value : sellingTransactions.value
})

const fetchTransactions = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    // Fetch buying transactions
    const buyingResponse = await $api.get(`/transaction/buyer/${user.id}`)
    if (buyingResponse.data.success) {
      buyingTransactions.value = buyingResponse.data.transactions
      // Fetch product details for buying transactions
      for (const transaction of buyingTransactions.value) {
        await fetchProductDetails(transaction.productId)
      }
    }
    
    // Fetch selling transactions
    const sellingResponse = await $api.get(`/transaction/seller/${user.id}`)
    if (sellingResponse.data.success) {
      sellingTransactions.value = sellingResponse.data.transactions
      // Fetch product details for selling transactions
      for (const transaction of sellingTransactions.value) {
        await fetchProductDetails(transaction.productId)
      }
    }
  } catch (error) {
    console.error('Error fetching transactions:', error)
  } finally {
    loading.value = false
  }
}

const fetchProductDetails = async (productId) => {
  try {
    const response = await $api.get(`/product/info/${productId}`)
    if (response.data.success) {
      products.value[productId] = response.data.product
    }
  } catch (error) {
    console.error('Error fetching product details:', error)
  }
}

const getProductTitle = (productId) => {
  return products.value[productId]?.title || '商品信息加载中...'
}

const getStatusText = (status) => {
  const statusMap = {
    'pending_payment': '待付款',
    'pending_shipping': '待发货',
    'pending_receipt': '待收货',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return statusMap[status] || status
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString()
}

const updateStatus = (transactionId) => {
  // Implement update status functionality
  alert('更新状态功能开发中')
}

onMounted(() => {
  fetchTransactions()
})

watch(activeTab, () => {
  // Fetch data if needed when tab changes
})
</script>

<style scoped>
.my-transactions {
  padding: 1rem 0;
}

.my-transactions h3 {
  margin-bottom: 1.5rem;
  color: #333;
}

.transaction-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.tab-button {
  padding: 0.75rem 1.5rem;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-button:hover {
  border-color: #4CAF50;
  color: #4CAF50;
}

.tab-button.active {
  background-color: #4CAF50;
  color: white;
  border-color: #4CAF50;
}

.loading,
.empty {
  text-align: center;
  padding: 4rem 0;
  color: #666;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.transaction-item {
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.transaction-info {
  flex: 1;
}

.transaction-product {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.transaction-price {
  margin: 0 0 0.5rem 0;
  font-weight: bold;
  color: #4CAF50;
}

.transaction-status {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  color: #666;
}

.transaction-date {
  margin: 0;
  font-size: 0.8rem;
  color: #999;
}

.transaction-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
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
  text-align: center;
}

.action-button:hover {
  background-color: #f0f9f0;
}

/* Responsive styles */
@media (max-width: 768px) {
  .transaction-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .transaction-actions {
    flex-direction: row;
    justify-content: flex-end;
  }
}
</style>
