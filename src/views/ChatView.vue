<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2>聊天窗口</h2>
      <p>与 {{ recipientName }} 聊天</p>
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="loading" class="loading">
        <p>加载中...</p>
      </div>
      <div v-else-if="messages.length === 0" class="no-messages">
        <p>暂无消息</p>
      </div>
      <div v-else v-for="message in messages" :key="message.id" 
           :class="['message-item', message.senderId === userId ? 'sent' : 'received']">
        <div class="message-avatar">
          <span class="avatar">👤</span>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-sender">{{ message.senderId === userId ? '我' : recipientName }}</span>
          </div>
          <p>{{ message.content }}</p>
          <span class="message-time">{{ formatDate(message.createdAt) }}</span>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <textarea
        v-model="messageInput"
        rows="3"
        placeholder="输入消息..."
        @keyup.enter.exact="sendMessage"
        class="message-textarea"
      />
      <button @click="sendMessage" class="send-button">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { inject } from 'vue'

const route = useRoute()
const $api = inject('$api')
const recipientId = ref(parseInt(route.params.userId))
const recipientName = ref('加载中...')
const userId = ref(1)
const messages = ref([])
const messageInput = ref('')
const loading = ref(true)
const messagesContainer = ref(null)

// 从localStorage获取当前用户信息
const loadUserInfo = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userId.value = user.id
  }
}

// 获取聊天对象用户名
const loadRecipientName = async () => {
  try {
    const response = await $api.get(`/user/info/${recipientId.value}`)
    if (response.data.success) {
      recipientName.value = response.data.user.username
    }
  } catch (error) {
    console.error('Error loading recipient name:', error)
    recipientName.value = '未知用户'
  }
}

// 加载聊天记录（自动标记为已读）
const loadMessages = async () => {
  loading.value = true
  try {
    const response = await $api.get(`/message/conversation/${userId.value}/${recipientId.value}`)
    if (response.data.success) {
      messages.value = response.data.messages
    }
  } catch (error) {
    console.error('Error loading messages:', error)
    messages.value = []
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageInput.value.trim()) {
    alert('请输入消息内容')
    return
  }
  
  try {
    console.log('发送消息:', {
      senderId: userId.value,
      receiverId: recipientId.value,
      content: messageInput.value
    })
    
    // 确保userId和recipientId都是数字
    const senderIdNum = parseInt(userId.value)
    const receiverIdNum = parseInt(recipientId.value)
    
    if (isNaN(senderIdNum) || isNaN(receiverIdNum)) {
      alert('用户ID无效')
      return
    }
    
    const response = await $api.post('/message/create', {
      senderId: senderIdNum,
      receiverId: receiverIdNum,
      content: messageInput.value
    })
    
    console.log('消息发送响应:', response.data)
    
    if (response.data.success) {
      // 重新加载消息
      await loadMessages()
      messageInput.value = ''
    } else {
      alert('发送消息失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('Error sending message:', error)
    alert('发送消息失败，请稍后重试\n错误: ' + error.message)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    const container = messagesContainer.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }, 100)
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 初始化
onMounted(() => {
  loadUserInfo()
  loadRecipientName()
  loadMessages()
})
</script>

<style scoped>
.chat-container {
  max-width: 800px;
  margin: 0 auto;
  height: 80vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chat-header {
  padding: 20px;
  background-color: #409eff;
  color: white;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

.chat-header h2 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.chat-header p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  max-width: 70%;
  padding: 10px;
  border-radius: 18px;
  position: relative;
  display: flex;
  gap: 10px;
}

.message-item.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.sent .message-content {
  background-color: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.received {
  align-self: flex-start;
}

.message-item.received .message-content {
  background-color: white;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-avatar {
  display: flex;
  align-items: flex-start;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #e6e6e6;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.message-item.sent .avatar {
  background-color: #409eff;
  color: white;
}

.message-content {
  flex: 1;
  padding: 10px 15px;
  border-radius: 18px;
}

.message-header {
  margin-bottom: 5px;
  font-size: 12px;
  opacity: 0.8;
}

.message-sender {
  font-weight: bold;
}

.message-content p {
  margin: 0 0 5px 0;
  line-height: 1.4;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  display: block;
  text-align: right;
}

.chat-input {
  padding: 20px;
  background-color: white;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.message-textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: none;
  font-size: 14px;
  font-family: inherit;
  height: 100px;
}

.message-textarea:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.send-button {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  height: 100px;
  min-width: 80px;
}

.send-button:hover {
  background-color: #66b1ff;
}

.send-button:active {
  background-color: #3a8ee6;
}

.loading,
.no-messages {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>