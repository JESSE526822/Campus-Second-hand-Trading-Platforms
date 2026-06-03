<template>
  <div class="my-messages">
    <h3>消息中心</h3>
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="messages.length === 0" class="empty">
      <p>暂无消息</p>
    </div>
    <div v-else class="messages-list">
      <div v-for="message in messages" :key="message.id" class="message-item" :class="{ 'unread': !message.isRead }">
          <div class="message-header">
            <span class="sender">发件人: {{ message.senderName || message.senderId }}</span>
            <span class="message-date">{{ formatDate(message.createdAt) }}</span>
          </div>
          <div class="message-content">
            {{ message.content }}
          </div>
          <div class="message-actions">
            <button @click="markAsRead(message.id)" class="action-button" v-if="!message.isRead">标记为已读</button>
            <button @click="openChat(message)" class="action-button primary">回复</button>
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
const messages = ref([])
const loading = ref(true)

const fetchMessages = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    if (!user) {
      router.push('/login')
      return
    }
    
    const response = await $api.get(`/message/user/${user.id}`)
    if (response.data.success) {
      messages.value = response.data.messages
    }
  } catch (error) {
    console.error('Error fetching messages:', error)
    messages.value = []
  } finally {
    loading.value = false
  }
}

const markAsRead = async (messageId) => {
  try {
    const response = await $api.post(`/message/mark-read/${messageId}`)
    if (response.data.success) {
      // 更新本地消息状态
      const message = messages.value.find(m => m.id === messageId)
      if (message) {
        message.isRead = true
      }
    }
  } catch (error) {
    console.error('Error marking message as read:', error)
    alert('标记消息失败，请稍后重试')
  }
}

const openChat = (message) => {
  // 跳转到聊天窗口，使用与联系卖家相同的聊天框
  router.push(`/chat/${message.senderId}`)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>
.my-messages {
  padding: 1rem 0;
}

.my-messages h3 {
  margin-bottom: 1.5rem;
  color: #333;
}

.loading,
.empty {
  text-align: center;
  padding: 4rem 0;
  color: #666;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-item {
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #409eff;
  transition: all 0.3s;
}

.message-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.message-item.unread {
  border-left-color: #f56c6c;
  background-color: #f9f0f0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.sender {
  font-weight: bold;
  color: #333;
}

.message-date {
  font-size: 0.9rem;
  color: #909399;
}

.message-content {
  line-height: 1.6;
  color: #606266;
  margin-bottom: 1rem;
  min-height: 60px;
}

.message-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.action-button {
  padding: 0.5rem 1rem;
  border: 1px solid #dcdfe6;
  background-color: white;
  color: #606266;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-button:hover {
  border-color: #409eff;
  color: #409eff;
}

.action-button.primary {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.action-button.primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

/* 回复对话框 */
.reply-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog-content h4 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.dialog-body {
  margin-bottom: 1.5rem;
}

.dialog-body p {
  margin: 0 0 0.5rem 0;
  color: #606266;
}

.reply-textarea {
  width: 100%;
  min-height: 120px;
  padding: 0.75rem;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  font-size: 1rem;
}

.dialog-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

/* Responsive styles */
@media (max-width: 768px) {
  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .message-actions {
    flex-direction: column;
  }
  
  .action-button {
    width: 100%;
  }
  
  .dialog-content {
    width: 95%;
    padding: 1.5rem;
  }
}
</style>