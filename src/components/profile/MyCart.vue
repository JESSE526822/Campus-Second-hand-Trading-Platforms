<template>
  <div class="my-cart">
    <h3>我的购物车</h3>
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="cartItems.length === 0" class="empty">
      <p>购物车为空</p>
      <router-link to="/" class="browse-link">去浏览商品</router-link>
    </div>
    <div v-else class="cart-list">
      <div v-for="item in cartItems" :key="item.id" class="cart-item">
        <img :src="item.product.imageUrl" :alt="item.product.title" class="product-image">
        <div class="product-info">
          <h4 class="product-title">{{ item.product.title }}</h4>
          <p class="product-price">¥{{ item.product.price }}</p>
          <div class="product-quantity">
            <span class="quantity-label">数量：</span>
            <span class="quantity">1</span>
          </div>
        </div>
        <div class="item-actions">
          <p class="item-total">¥{{ (item.product.price * item.quantity).toFixed(2) }}</p>
          <button @click="removeFromCart(item.id)" class="remove-button">删除</button>
        </div>
      </div>
      <div class="cart-summary">
        <div class="summary-info">
          <p>商品总数: {{ totalQuantity }}</p>
          <p>总价: ¥{{ totalPrice.toFixed(2) }}</p>
        </div>
        <button @click="checkout" class="checkout-button" :disabled="!selectAllChecked">结算</button>
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
const cartItems = ref([])
const loading = ref(true)
const selectedItems = ref([])

const totalQuantity = computed(() => {
  return cartItems.value.reduce((total, item) => total + item.quantity, 0)
})

const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => total + (item.product.price * item.quantity), 0)
})

const selectAllChecked = computed(() => {
  return selectedItems.value.length > 0
})

const fetchCartItems = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    const response = await $api.get(`/product/cart/user/${user.id}`)
    if (response.data.success) {
      cartItems.value = response.data.cartItems
      // 默认全选
      selectedItems.value = cartItems.value.map(item => item.id)
    }
  } catch (error) {
    console.error('Error fetching cart items:', error)
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

const decreaseQuantity = async (cartId, currentQuantity) => {
  if (currentQuantity <= 1) return
  
  try {
    const response = await $api.post(`/product/cart/update/${cartId}`, {
      quantity: currentQuantity - 1
    })
    if (response.data.success) {
      // Refresh cart items
      fetchCartItems()
    }
  } catch (error) {
    console.error('Error decreasing quantity:', error)
    alert('更新数量失败，请稍后重试')
  }
}

const increaseQuantity = async (cartId) => {
  try {
    const cartItem = cartItems.value.find(item => item.id === cartId)
    if (!cartItem) return
    
    const response = await $api.post(`/product/cart/update/${cartId}`, {
      quantity: cartItem.quantity + 1
    })
    if (response.data.success) {
      // Refresh cart items
      fetchCartItems()
    }
  } catch (error) {
    console.error('Error increasing quantity:', error)
    alert('更新数量失败，请稍后重试')
  }
}

const removeFromCart = async (cartId) => {
  try {
    const response = await $api.delete(`/product/cart/remove/${cartId}`)
    if (response.data.success) {
      // Refresh cart items
      fetchCartItems()
    }
  } catch (error) {
    console.error('Error removing item from cart:', error)
  }
}

const checkout = async () => {
  if (selectedItems.value.length === 0) {
    alert('请选择要购买的商品')
    return
  }
  
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    console.log('结算购物车:', {
      userId: user.id,
      cartIds: selectedItems.value
    })
    
    // 确保userId是数字
    const userIdNum = parseInt(user.id)
    if (isNaN(userIdNum)) {
      alert('用户ID无效')
      return
    }
    
    // 确保cartIds都是数字
    const cartIdsNum = selectedItems.value.map(id => parseInt(id)).filter(id => !isNaN(id))
    if (cartIdsNum.length === 0) {
      alert('购物车商品ID无效')
      return
    }
    
    const response = await $api.post('/product/cart/checkout', {
      userId: userIdNum,
      cartIds: cartIdsNum
    })
    
    console.log('结算响应:', response.data)
    
    if (response.data.success) {
      alert('购物车结算成功')
      // Refresh cart items
      fetchCartItems()
    } else {
      alert('结算失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('Error checking out:', error)
    alert('结算失败，请稍后重试\n错误: ' + error.message)
  }
}

onMounted(() => {
  fetchCartItems()
})
</script>

<style scoped>
.my-cart {
  padding: 1rem 0;
}

.my-cart h3 {
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

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.product-title {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
}

.product-price {
  margin: 0;
  font-weight: bold;
  color: #4CAF50;
}

.product-quantity {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quantity-label {
  font-size: 0.9rem;
  color: #666;
}

.quantity {
  font-weight: bold;
  color: #333;
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
}

.item-total {
  margin: 0;
  font-weight: bold;
  color: #333;
  font-size: 1.1rem;
}

.remove-button {
  padding: 0.5rem 1rem;
  border: 1px solid #f44336;
  background-color: white;
  color: #f44336;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.remove-button:hover {
  background-color: #f44336;
  color: white;
}

.cart-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-top: 1rem;
}

.summary-info {
  display: flex;
  gap: 2rem;
}

.summary-info p {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.checkout-button {
  padding: 1rem 2rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.checkout-button:hover:not(:disabled) {
  background-color: #45a049;
}

.checkout-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* Responsive styles */
@media (max-width: 768px) {
  .cart-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .product-image {
    width: 100%;
    max-width: 200px;
    height: auto;
  }
  
  .item-actions {
    align-self: flex-end;
    margin-top: 1rem;
  }
  
  .cart-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .summary-info {
    width: 100%;
    justify-content: space-between;
  }
  
  .checkout-button {
    align-self: stretch;
  }
}
</style>