<template>
  <div class="profile">
    <div class="profile-header">
      <h2>个人中心</h2>
      <div class="user-info">
        <div class="user-details">
          <div class="user-avatar">👤</div>
          <h3>{{ user?.username }}</h3>
        </div>
        <div class="notifications" @click="toggleNotifications">
          <span class="notification-icon">🔔</span>
          <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
          <div v-if="showNotifications" class="notification-dropdown">
            <div v-if="notifications.length === 0" class="no-notifications">
              <p>暂无通知</p>
            </div>
            <div v-else class="notification-list">
              <div v-for="notification in notifications" :key="notification.id" 
                   class="notification-item" @click="openChatFromNotification(notification)">
                <div class="notification-content">
                  <p class="notification-message">{{ notification.message }}</p>
                  <span class="notification-time">{{ formatDate(notification.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="profile-nav">
      <router-link to="/profile/products" class="nav-item">我的商品</router-link>
      <router-link to="/profile/favorites" class="nav-item">我的收藏</router-link>
    </div>
    
    <div class="profile-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'

const router = useRouter()
const $api = inject('$api')
const user = ref(null)
const notifications = ref([])
const unreadCount = ref(0)
const showNotifications = ref(false)

const loadUserInfo = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
    loadNotifications()
  } else {
    router.push('/login')
  }
}

const loadNotifications = async () => {
  try {
    if (!user.value) return
    
    const response = await $api.get(`/message/user/${user.value.id}`)
    if (response.data.success) {
      const messages = response.data.messages
      // 转换消息为通知格式
      notifications.value = messages.map(msg => ({
        id: msg.id,
        message: `收到来自 ${msg.senderName || msg.senderId} 的消息: ${msg.content.substring(0, 20)}${msg.content.length > 20 ? '...' : ''}`,
        senderId: msg.senderId,
        createdAt: msg.createdAt,
        isRead: msg.isRead
      }))
      // 计算未读消息数量
      unreadCount.value = messages.filter(msg => !msg.isRead).length
    }
  } catch (error) {
    console.error('Error loading notifications:', error)
  }
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
}

const openChatFromNotification = (notification) => {
  // 跳转到聊天窗口
  router.push(`/chat/${notification.senderId}`)
  // 关闭通知 dropdown
  showNotifications.value = false
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  padding: 2rem 0;
}

.profile-header {
  margin-bottom: 3rem;
  animation: fadeInUp 0.8s ease-out;
}

.profile-header h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 2rem;
  color: #333;
  text-align: center;
  position: relative;
  display: inline-block;
  left: 50%;
  transform: translateX(-50%);
}

.profile-header h2::after {
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

.user-info {
  padding: 2rem;
  background: linear-gradient(135deg, white 0%, #f8f9fa 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: slideUp 1s ease-out;
}

.user-details {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 32px rgba(76, 175, 80, 0.4);
}

.user-details h3 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
  font-weight: 600;
  transition: color 0.3s ease;
}

.user-details h3:hover {
  color: #4CAF50;
}

.notifications {
  position: relative;
  cursor: pointer;
  padding: 1rem;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.notifications:hover {
  background: rgba(76, 175, 80, 0.1);
  transform: scale(1.1);
}

.notification-icon {
  font-size: 1.8rem;
  transition: all 0.3s ease;
}

.notifications:hover .notification-icon {
  transform: scale(1.2);
}

.notification-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 0.9rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  animation: pulse 2s infinite;
}

.notification-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 1rem;
  width: 350px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  max-height: 450px;
  overflow-y: auto;
  animation: fadeInUp 0.5s ease-out;
}

.notification-dropdown::-webkit-scrollbar {
  width: 6px;
}

.notification-dropdown::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.notification-dropdown::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.notification-dropdown::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.no-notifications {
  padding: 3rem 2rem;
  text-align: center;
  color: #909399;
  animation: fadeIn 0.8s ease-out;
}

.no-notifications p {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 500;
}

.notification-list {
  padding: 0.5rem;
}

.notification-item {
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  margin: 0.5rem;
  background: #f8f9fa;
}

.notification-item:hover {
  background: rgba(76, 175, 80, 0.05);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.notification-message {
  margin: 0;
  line-height: 1.5;
  color: #333;
  font-weight: 500;
  font-size: 1rem;
}

.notification-time {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

.profile-nav {
  display: flex;
  gap: 2rem;
  margin-bottom: 3rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e0e0e0;
  animation: slideIn 1s ease-out;
}

.nav-item {
  padding: 1.25rem 2rem;
  text-decoration: none;
  color: #666;
  border-bottom: 3px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 1.1rem;
  font-weight: 600;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(76, 175, 80, 0.1);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-item:hover::before {
  width: 300px;
  height: 300px;
}

.nav-item:hover {
  color: #4CAF50;
  transform: translateY(-2px);
}

.nav-item.router-link-active {
  color: #4CAF50;
  border-bottom-color: #4CAF50;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

.profile-content {
  min-height: 60vh;
  padding: 3rem;
  background: linear-gradient(135deg, white 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: slideUp 1s ease-out 0.2s both;
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

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

/* Responsive styles */
@media (max-width: 768px) {
  .profile-header h2 {
    font-size: 2rem;
  }
  
  .user-info {
    flex-direction: column;
    gap: 1.5rem;
    text-align: center;
    padding: 1.5rem;
  }
  
  .user-details {
    flex-direction: column;
    gap: 1rem;
  }
  
  .profile-nav {
    flex-wrap: wrap;
    justify-content: center;
    gap: 1rem;
  }
  
  .nav-item {
    flex: 1;
    min-width: 140px;
    text-align: center;
    padding: 1rem;
  }
  
  .profile-content {
    padding: 1.5rem;
  }
  
  .notification-dropdown {
    width: 280px;
    right: -20px;
  }
}

@media (max-width: 480px) {
  .profile-header h2 {
    font-size: 1.8rem;
  }
  
  .user-avatar {
    width: 60px;
    height: 60px;
    font-size: 1.5rem;
  }
  
  .user-details h3 {
    font-size: 1.25rem;
  }
  
  .profile-nav {
    flex-direction: column;
    align-items: stretch;
  }
  
  .nav-item {
    width: 100%;
  }
  
  .profile-content {
    padding: 1rem;
  }
  
  .notification-dropdown {
    width: 250px;
    right: -40px;
  }
}
</style>
