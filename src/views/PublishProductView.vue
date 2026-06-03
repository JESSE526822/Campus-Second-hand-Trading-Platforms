<template>
  <div class="publish-product">
    <div class="container">
      <div class="publish-header">
        <h1>发布商品</h1>
        <p>填写以下信息，让你的商品更快找到合适的买家</p>
      </div>
      <div class="publish-form-container">
        <form class="publish-form">
          <div class="form-grid">
            <div class="form-group">
              <label for="title" class="form-label">商品标题</label>
              <div class="form-input-wrapper">
                <input 
                  type="text" 
                  id="title" 
                  v-model="productForm.title" 
                  class="form-input"
                  placeholder="请输入商品标题"
                  :class="{ 'input-focused': isFocused.title }"
                  @focus="isFocused.title = true"
                  @blur="isFocused.title = false"
                >
              </div>
            </div>
            <div class="form-group">
              <label for="category" class="form-label">商品分类</label>
              <div class="form-input-wrapper">
                <select 
                  id="category" 
                  v-model="productForm.category" 
                  class="form-input"
                  :class="{ 'input-focused': isFocused.category }"
                  @focus="isFocused.category = true"
                  @blur="isFocused.category = false"
                >
                  <option value="">请选择分类</option>
                  <option value="手机数码">手机数码</option>
                  <option value="音频乐器">音频乐器</option>
                  <option value="家具厨具">家具厨具</option>
                  <option value="汽车摩托">汽车摩托</option>
                  <option value="生活用品">生活用品</option>
                  <option value="学习用品">学习用品</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label for="price" class="form-label">价格</label>
              <div class="form-input-wrapper">
                <span class="input-prefix">¥</span>
                <input 
                  type="number" 
                  id="price" 
                  v-model.number="productForm.price" 
                  class="form-input"
                  placeholder="0.00"
                  step="0.01"
                  min="0"
                  :class="{ 'input-focused': isFocused.price }"
                  @focus="isFocused.price = true"
                  @blur="isFocused.price = false"
                >
              </div>
            </div>
            <div class="form-group">
              <label for="imageUrl" class="form-label">图片链接</label>
              <div class="form-input-wrapper">
                <input 
                  type="text" 
                  id="imageUrl" 
                  v-model="productForm.imageUrl" 
                  class="form-input"
                  placeholder="请输入图片链接"
                  :class="{ 'input-focused': isFocused.imageUrl }"
                  @focus="isFocused.imageUrl = true"
                  @blur="isFocused.imageUrl = false"
                >
              </div>
            </div>
            <div class="form-group">
              <label for="condition" class="form-label">成色外观</label>
              <div class="form-input-wrapper">
                <select 
                  id="condition" 
                  v-model="productForm.condition" 
                  class="form-input"
                  :class="{ 'input-focused': isFocused.condition }"
                  @focus="isFocused.condition = true"
                  @blur="isFocused.condition = false"
                >
                  <option value="">请选择成色</option>
                  <option value="九九成新（几乎少用）">九九成新（几乎少用）</option>
                  <option value="八九成新（轻微使用）">八九成新（轻微使用）</option>
                  <option value="七八成新（正常使用）">七八成新（正常使用）</option>
                  <option value="六成新以下（明显使用痕迹）">六成新以下（明显使用痕迹）</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label for="shippingMethod" class="form-label">发货方式</label>
              <div class="form-input-wrapper">
                <select 
                  id="shippingMethod" 
                  v-model="productForm.shippingMethod" 
                  class="form-input"
                  :class="{ 'input-focused': isFocused.shippingMethod }"
                  @focus="isFocused.shippingMethod = true"
                  @blur="isFocused.shippingMethod = false"
                >
                  <option value="">请选择发货方式</option>
                  <option value="固定邮费">固定邮费</option>
                  <option value="运费到付">运费到付</option>
                  <option value="同城自提">同城自提</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label for="afterSales" class="form-label">售后支持</label>
              <div class="form-input-wrapper">
                <select 
                  id="afterSales" 
                  v-model="productForm.afterSales" 
                  class="form-input"
                  :class="{ 'input-focused': isFocused.afterSales }"
                  @focus="isFocused.afterSales = true"
                  @blur="isFocused.afterSales = false"
                >
                  <option value="">请选择售后支持</option>
                  <option value="支持七天无理由退货">支持七天无理由退货</option>
                  <option value="三天售后服务">三天售后服务</option>
                  <option value="无售后">无售后</option>
                </select>
              </div>
            </div>
            <div class="form-group form-group-full">
              <label for="description" class="form-label">商品描述</label>
              <div class="form-input-wrapper">
                <textarea 
                  id="description" 
                  v-model="productForm.description" 
                  class="form-input form-textarea"
                  placeholder="请详细描述商品的成色、使用情况、优缺点等信息，让买家更了解商品"
                  rows="5"
                  :class="{ 'input-focused': isFocused.description }"
                  @focus="isFocused.description = true"
                  @blur="isFocused.description = false"
                ></textarea>
              </div>
            </div>
          </div>
          
          <div class="form-actions">
            <button 
              type="button" 
              @click="resetForm" 
              class="form-button secondary"
              :disabled="loading"
            >
              重置
            </button>
            <button 
              type="button" 
              @click="publish" 
              class="form-button primary"
              :disabled="loading"
              :class="{ 'button-loading': loading }"
            >
              <span v-if="!loading">发布商品</span>
              <span v-else class="loading-spinner">
                <span class="spinner"></span>
                发布中...
              </span>
            </button>
          </div>
          
          <div v-if="error" class="error-message">
            <div class="error-icon">⚠️</div>
            <span>{{ error }}</span>
          </div>
          <div v-if="success" class="success-message">
            <div class="success-icon">✅</div>
            <span>{{ success }}</span>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'

const router = useRouter()
const $api = inject('$api')
const productForm = ref({
  title: '',
  category: '',
  price: 0,
  imageUrl: '',
  condition: '',
  shippingMethod: '',
  afterSales: '',
  description: ''
})
const error = ref('')
const success = ref('')
const loading = ref(false)
const isFocused = ref({
  title: false,
  category: false,
  price: false,
  imageUrl: false,
  condition: false,
  shippingMethod: false,
  afterSales: false,
  description: false
})

const publish = async () => {
  try {
    error.value = ''
    success.value = ''
    loading.value = true
    
    // Validate form
    if (!productForm.value.title || !productForm.value.category || !productForm.value.price || !productForm.value.imageUrl) {
      error.value = '请填写必填字段'
      return
    }
    
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    // Create product
    const productData = {
      userId: user.id,
      title: productForm.value.title,
      category: productForm.value.category,
      price: productForm.value.price,
      imageUrl: productForm.value.imageUrl,
      condition: productForm.value.condition,
      shippingMethod: productForm.value.shippingMethod,
      afterSales: productForm.value.afterSales,
      description: productForm.value.description
    }
    
    const response = await $api.post('/product/create', productData)
    
    if (response.data.success) {
      success.value = '商品发布成功'
      // Reset form
      productForm.value = {
        title: '',
        category: '',
        price: 0,
        imageUrl: '',
        condition: '',
        shippingMethod: '',
        afterSales: '',
        description: ''
      }
      // Reset focus states
      Object.keys(isFocused.value).forEach(key => {
        isFocused.value[key] = false
      })
      // Redirect to home page after 2 seconds
      setTimeout(() => {
        router.push('/')
      }, 2000)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = err.response?.data?.message || '发布失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  productForm.value = {
    title: '',
    category: '',
    price: 0,
    imageUrl: '',
    condition: '',
    shippingMethod: '',
    afterSales: '',
    description: ''
  }
  error.value = ''
  success.value = ''
  // Reset focus states
  Object.keys(isFocused.value).forEach(key => {
    isFocused.value[key] = false
  })
}
</script>

<style scoped>
.publish-product {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  padding: 3rem 0;
}

.publish-header {
  text-align: center;
  margin-bottom: 3rem;
  animation: fadeInUp 0.8s ease-out;
}

.publish-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1rem;
  position: relative;
  display: inline-block;
}

.publish-header h1::after {
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

.publish-header p {
  font-size: 1.1rem;
  color: #666;
  margin-top: 1.5rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.publish-form-container {
  background: white;
  border-radius: 20px;
  padding: 3rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: slideUp 1s ease-out;
}

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
}

.form-group {
  position: relative;
}

.form-group-full {
  grid-column: 1 / -1;
}

.form-label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.form-input-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-input-wrapper::before {
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

.input-focused .form-input-wrapper::before {
  opacity: 0.05;
}

.input-prefix {
  position: absolute;
  left: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1rem;
  color: #666;
  z-index: 2;
  font-weight: 600;
  transition: all 0.3s ease;
}

.input-focused .input-prefix {
  color: #4CAF50;
}

.form-input {
  width: 100%;
  padding: 1.25rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8f9fa;
  position: relative;
  z-index: 2;
}

.form-input[type="number"] {
  padding-left: 3rem;
}

.form-input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
  background: white;
  transform: translateY(-2px);
}

.form-input::placeholder {
  color: #999;
  transition: all 0.3s ease;
}

.form-input:focus::placeholder {
  color: #ccc;
  transform: translateX(5px);
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
  max-height: 300px;
  font-family: inherit;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  gap: 1.5rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.form-button {
  padding: 1.25rem 2.5rem;
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

.form-button.primary {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.form-button.primary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.form-button.secondary {
  background: #f8f9fa;
  color: #333;
  border: 2px solid #e0e0e0;
}

.form-button.secondary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #4CAF50;
  color: #4CAF50;
}

.form-button:disabled {
  background: #c0c4cc;
  color: #666;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  border-color: #c0c4cc;
}

.button-loading {
  cursor: not-allowed;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem;
  background: rgba(244, 67, 54, 0.1);
  border: 2px solid rgba(244, 67, 54, 0.2);
  border-radius: 12px;
  color: #d32f2f;
  font-size: 0.95rem;
  animation: shake 0.5s ease-in-out;
}

.error-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.success-message {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem;
  background: rgba(76, 175, 80, 0.1);
  border: 2px solid rgba(76, 175, 80, 0.2);
  border-radius: 12px;
  color: #2e7d32;
  font-size: 0.95rem;
  animation: slideIn 0.5s ease-out;
}

.success-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
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

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .publish-product {
    padding: 2rem 0;
  }
  
  .publish-header h1 {
    font-size: 2rem;
  }
  
  .publish-form-container {
    padding: 2rem;
    margin: 0 1rem;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-button {
    width: 100%;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .publish-header h1 {
    font-size: 1.8rem;
  }
  
  .publish-form-container {
    padding: 1.5rem;
  }
  
  .form-input {
    padding: 1rem;
  }
  
  .form-input[type="number"] {
    padding-left: 2.5rem;
  }
  
  .form-button {
    padding: 1rem 1.5rem;
  }
}
</style>
