<template>
  <div class="register-container">
    <div class="register-left">
      <div class="register-left-content">
        <h1>加入校园二手物品交易平台</h1>
        <p>开启你的环保校园生活，轻松交易闲置物品</p>
        <div class="register-features">
          <div class="feature-item">
            <div class="feature-icon">🎯</div>
            <span>快速注册</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔒</div>
            <span>安全交易</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🌱</div>
            <span>环保生活</span>
          </div>
        </div>
      </div>
    </div>
    <div class="register-right">
      <div class="register-form-container">
        <div class="register-form-header">
          <h2>用户注册</h2>
          <p>创建你的账号，开始体验便捷交易</p>
        </div>
        <div class="register-form">
          <div class="form-group">
            <label for="username" class="form-label">用户名</label>
            <div class="form-input-wrapper">
              <span class="input-icon">👤</span>
              <input 
                type="text" 
                id="username" 
                v-model="registerForm.username" 
                class="form-input"
                placeholder="请输入用户名"
                :class="{ 'input-focus': isFocused.username }"
                @focus="isFocused.username = true"
                @blur="isFocused.username = false"
              >
            </div>
          </div>
          <div class="form-group">
            <label for="password" class="form-label">密码</label>
            <div class="form-input-wrapper">
              <span class="input-icon">🔒</span>
              <input 
                type="password" 
                id="password" 
                v-model="registerForm.password" 
                class="form-input"
                placeholder="请输入密码"
                :class="{ 'input-focus': isFocused.password }"
                @focus="isFocused.password = true"
                @blur="isFocused.password = false"
              >
            </div>
          </div>
          <div class="form-group">
            <label for="confirmPassword" class="form-label">确认密码</label>
            <div class="form-input-wrapper">
              <span class="input-icon">✓</span>
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="registerForm.confirmPassword" 
                class="form-input"
                placeholder="请再次输入密码"
                :class="{ 'input-focus': isFocused.confirmPassword }"
                @focus="isFocused.confirmPassword = true"
                @blur="isFocused.confirmPassword = false"
              >
            </div>
          </div>
          <div class="form-group">
            <button 
              @click="register" 
              class="form-button" 
              :class="{ 'button-loading': loading }"
              :disabled="loading"
            >
              <span v-if="!loading">注册</span>
              <span v-else class="loading-spinner">
                <span class="spinner"></span>
                注册中...
              </span>
            </button>
          </div>
          <div class="form-footer">
            <p>已有账号？<router-link to="/login" class="login-link">立即登录</router-link></p>
          </div>
          <div v-if="error" class="error-message">
            <div class="error-icon">⚠️</div>
            <span>{{ error }}</span>
          </div>
          <div v-if="success" class="success-message">
            <div class="success-icon">✅</div>
            <span>{{ success }}</span>
          </div>
        </div>
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
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})
const error = ref('')
const success = ref('')
const loading = ref(false)
const isFocused = ref({
  username: false,
  password: false,
  confirmPassword: false
})

const register = async () => {
  try {
    error.value = ''
    success.value = ''
    loading.value = true
    
    // Validate form
    if (registerForm.value.password !== registerForm.value.confirmPassword) {
      error.value = '两次输入的密码不一致'
      return
    }
    
    const response = await $api.post('/user/register', {
      username: registerForm.value.username,
      password: registerForm.value.password
    })
    
    if (response.data.success) {
      success.value = '注册成功，请登录'
      // Redirect to login page after 2 seconds
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = err.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
}

.register-left {
  flex: 1;
  background: linear-gradient(135deg, #45a049 0%, #4CAF50 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  position: relative;
  overflow: hidden;
  animation: gradientShift 15s ease infinite;
  background-size: 200% 200%;
}

.register-left::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: float 20s linear infinite reverse;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  100% {
    transform: translate(-50px, -50px) rotate(360deg);
  }
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

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

@keyframes bounceIn {
  0%, 20%, 40%, 60%, 80%, 100% {
    transform: translateY(0);
  }
  10% {
    transform: translateY(-30px);
  }
  30% {
    transform: translateY(-15px);
  }
  50% {
    transform: translateY(-7px);
  }
  70% {
    transform: translateY(-3px);
  }
  90% {
    transform: translateY(-1px);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes gradientFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes growFromCenter {
  from {
    width: 0;
  }
  to {
    width: 80px;
  }
}

@keyframes buttonPopIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulseBorder {
  0%, 100% {
    opacity: 0;
  }
  50% {
    opacity: 0.05;
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.register-left-content {
  position: relative;
  z-index: 1;
  color: white;
  max-width: 400px;
  text-align: center;
  animation: slideInLeft 1s ease-out;
}

.register-left-content h1 {
  font-size: 2.5rem;
  margin-bottom: 1.5rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 1px;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.register-left-content p {
  font-size: 1.1rem;
  margin-bottom: 3rem;
  opacity: 0.9;
  line-height: 1.6;
  animation: fadeInUp 1s ease-out 0.4s both;
}

.register-features {
  display: flex;
  justify-content: space-around;
  gap: 2rem;
  flex-wrap: wrap;
  animation: fadeInUp 1s ease-out 0.6s both;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 100px;
  animation: bounceIn 1s ease-out;
}

.feature-item:nth-child(1) {
  animation-delay: 0.7s;
}

.feature-item:nth-child(2) {
  animation-delay: 0.8s;
}

.feature-item:nth-child(3) {
  animation-delay: 0.9s;
}

.feature-item:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  position: relative;
  overflow: hidden;
}

.register-right::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(76, 175, 80, 0.05) 1px, transparent 1px);
  background-size: 30px 30px;
  animation: floatReverse 25s linear infinite;
}

@keyframes floatReverse {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  100% {
    transform: translate(50px, 50px) rotate(-360deg);
  }
}

.register-form-container {
  background: white;
  border-radius: 20px;
  padding: 3rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 450px;
  width: 100%;
  position: relative;
  overflow: hidden;
  animation: slideInRight 1s ease-out;
  backdrop-filter: blur(10px);
}

.register-form-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #45a049, #4CAF50);
  animation: gradientFlow 3s ease infinite;
  background-size: 200% 200%;
}

.register-form-header {
  text-align: center;
  margin-bottom: 3rem;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.register-form-header h2 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
  position: relative;
  display: inline-block;
}

.register-form-header h2::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #45a049, #4CAF50);
  border-radius: 2px;
  animation: growFromCenter 1s ease-out 0.4s both;
}

.register-form-header p {
  color: #666;
  font-size: 1rem;
  animation: fadeInUp 1s ease-out 0.4s both;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  animation: fadeInUp 1s ease-out 0.6s both;
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
  transition: all 0.3s ease;
}

.form-input-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.form-input-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #45a049 0%, #4CAF50 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
  animation: pulseBorder 2s infinite;
}

.input-focus .form-input-wrapper::before {
  opacity: 0.1;
  animation: none;
}

.input-icon {
  position: absolute;
  left: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.1rem;
  color: #666;
  z-index: 2;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-focus .input-icon {
  color: #45a049;
  transform: translateY(-50%) scale(1.1);
}

.form-input {
  width: 100%;
  padding: 1.25rem 1.25rem 1.25rem 3.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8f9fa;
  position: relative;
  z-index: 2;
}

.form-input:focus {
  outline: none;
  border-color: #45a049;
  box-shadow: 0 0 0 4px rgba(69, 160, 73, 0.1);
  background: white;
  transform: translateY(-2px);
}

.form-input::placeholder {
  color: #999;
  transition: all 0.3s ease;
  transform: translateX(0);
}

.form-input:focus::placeholder {
  color: #ccc;
  transform: translateX(10px) scale(0.95);
}

.form-input:focus + .form-label,
.input-focus .form-label {
  color: #45a049;
  transform: translateY(-4px) scale(0.9);
  font-weight: 700;
}

.form-button {
  width: 100%;
  padding: 1.25rem;
  background: linear-gradient(135deg, #45a049 0%, #4CAF50 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  margin-top: 1rem;
  box-shadow: 0 4px 12px rgba(69, 160, 73, 0.3);
  animation: buttonPopIn 1s ease-out 0.8s both;
}

.form-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

.form-button:hover::before {
  width: 300px;
  height: 300px;
}

.form-button:hover:not(:disabled) {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 16px 32px rgba(69, 160, 73, 0.4);
  background: linear-gradient(135deg, #388E3C 0%, #45a049 100%);
}

.form-button:active:not(:disabled) {
  transform: translateY(-2px) scale(0.98);
  box-shadow: 0 8px 16px rgba(69, 160, 73, 0.3);
}

.form-button:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.form-footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.95rem;
  color: #666;
  animation: fadeInUp 1s ease-out 1s both;
}

.login-link {
  color: #45a049;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: inline-block;
  overflow: hidden;
}

.login-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #45a049;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(-100%);
}

.login-link:hover::after {
  width: 100%;
  transform: translateX(0);
}

.login-link:hover {
  color: #388E3C;
  transform: translateY(-2px);
  text-shadow: 0 2px 4px rgba(69, 160, 73, 0.2);
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
  margin-top: 1rem;
  animation: shake 0.8s ease-in-out, fadeInUp 0.5s ease-out;
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.2);
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

.error-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
  animation: bounce 0.8s ease-in-out;
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
  margin-top: 1rem;
  animation: fadeInUp 0.5s ease-out;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

.success-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
  animation: bounce 0.8s ease-in-out;
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  
  .register-left {
    min-height: 50vh;
    padding: 3rem 2rem;
  }
  
  .register-left-content h1 {
    font-size: 2rem;
  }
  
  .register-right {
    min-height: 50vh;
    padding: 2rem;
  }
  
  .register-form-container {
    padding: 2rem;
    margin: 2rem 0;
  }
  
  .register-features {
    gap: 1rem;
  }
  
  .feature-item {
    padding: 1rem;
    min-width: 80px;
  }
  
  .feature-icon {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .register-left-content h1 {
    font-size: 1.8rem;
  }
  
  .register-form-container {
    padding: 1.5rem;
  }
  
  .form-input {
    padding: 1rem 1rem 1rem 3rem;
  }
  
  .form-button {
    padding: 1rem;
  }
}
</style>
