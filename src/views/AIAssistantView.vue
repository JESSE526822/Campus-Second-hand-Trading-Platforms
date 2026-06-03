<template>
  <div class="ai-assistant-container">
    <div class="ai-chat-wrapper">
      <!-- Back to Home Button -->
      <div class="back-button-container">
        <router-link to="/" class="back-button">
          <svg class="back-icon" viewBox="0 0 24 24">
            <path fill="currentColor" d="M20,11V13H8L13.5,18.5L12.08,19.92L4.16,12L12.08,4.08L13.5,5.5L8,11H20Z"/>
          </svg>
          <span>返回首页</span>
        </router-link>
      </div>

      <!-- Header -->
      <div class="ai-chat-header">
        <div class="header-content">
          <div class="avatar-container">
            <div class="ai-avatar">
              <svg class="ai-icon" viewBox="0 0 24 24">
                <path fill="currentColor" d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7H14A7,7 0 0,1 21,14H22A1,1 0 0,1 23,15V18A1,1 0 0,1 22,19H21V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V19H2A1,1 0 0,1 1,18V15A1,1 0 0,1 2,14H3A7,7 0 0,1 10,7H11V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7.5,13A1.5,1.5 0 0,0 6,14.5A1.5,1.5 0 0,0 7.5,16A1.5,1.5 0 0,0 9,14.5A1.5,1.5 0 0,0 7.5,13M16.5,13A1.5,1.5 0 0,0 15,14.5A1.5,1.5 0 0,0 16.5,16A1.5,1.5 0 0,0 18,14.5A1.5,1.5 0 0,0 16.5,13Z"/>
              </svg>
            </div>
          </div>
          <div class="header-info">
            <h2>AI智能助手</h2>
            <p>专为校园二手平台设计的智能助手</p>
          </div>
          <div class="header-status">
            <div class="status-indicator">
              <div class="status-pulse"></div>
            </div>
            <span class="status-text">在线服务中</span>
          </div>
        </div>
      </div>

      <!-- Chat Messages -->
      <div class="ai-chat-messages" ref="messagesContainer">
        <!-- Welcome Message -->
        <div v-if="!loading && messages.length === 0" class="welcome-message">
          <div class="welcome-container">
            <div class="welcome-avatar">
              <div class="avatar-glow"></div>
              <div class="avatar-content">
                <svg class="welcome-icon" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M12,2C6.48,2 2,6.48 2,12C2,17.52 6.48,22 12,22C17.52,22 22,17.52 22,12C22,6.48 17.52,2 12,2ZM12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20ZM13,7H11V11H7V13H11V17H13V13H17V11H13V7Z"/>
                </svg>
              </div>
            </div>
            <div class="welcome-content">
              <h3>欢迎使用AI助手！</h3>
              <p>我是您的专属智能助手，随时为您解答校园二手平台的各类问题</p>
            </div>
          </div>
        </div>

        <!-- Messages List -->
        <div class="messages-list">
          <div
              v-for="message in messages"
              :key="message.id"
              :class="['message-bubble', message.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <div class="message-wrapper">
              <!-- AI Message -->
              <div v-if="message.role === 'assistant'" class="message-left">
                <div class="message-avatar ai-avatar-message">
                  <div class="avatar-badge"></div>
                  <svg class="message-ai-icon" viewBox="0 0 24 24">
                    <path fill="currentColor" d="M12,2C6.48,2 2,6.48 2,12C2,17.52 6.48,22 12,22C17.52,22 22,17.52 22,12C22,6.48 17.52,2 12,2Z"/>
                  </svg>
                </div>
                <div class="message-content-wrapper">
                  <div class="message-name">AI助手</div>
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-time">{{ formatDate(message.timestamp) }}</div>
                </div>
              </div>

              <!-- User Message -->
              <div v-else class="message-right">
                <div class="message-content-wrapper user">
                  <div class="message-name">您</div>
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-time">{{ formatDate(message.timestamp) }}</div>
                </div>
                <div class="message-avatar user-avatar-message">
                  <svg class="message-user-icon" viewBox="0 0 24 24">
                    <path fill="currentColor" d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- Loading Indicator -->
          <div v-if="loading" class="loading-indicator">
            <div class="loading-dots">
              <div class="dot"></div>
              <div class="dot"></div>
              <div class="dot"></div>
            </div>
            <div class="loading-text">AI正在思考中...</div>
          </div>
        </div>
      </div>

      <!-- Quick Suggestions -->
      <div class="quick-suggestions">
        <div class="suggestions-header">
          <span class="suggestions-icon">💡</span>
          <span class="suggestions-title">快速提问</span>
        </div>
        <div class="suggestions-container" ref="suggestionsContainer">
          <div class="suggestions-scroll">
            <button
                v-for="(suggestion, index) in suggestions"
                :key="index"
                @click="sendQuickQuestion(suggestion)"
                class="suggestion-chip"
                :style="{ animationDelay: `${index * 0.1}s` }"
            >
              <span class="chip-content">{{ suggestion }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="ai-chat-input">
        <div class="input-container">
          <div class="input-wrapper">
            <textarea
                v-model="messageInput"
                rows="1"
                placeholder="输入您的问题..."
                @keyup.enter.exact="sendMessage"
                @keyup.enter.shift="messageInput += '\n'"
                class="message-textarea"
                :disabled="loading"
                @input="adjustTextareaHeight"
                ref="textarea"
            />
            <div class="input-actions">
              <button
                  @click="clearInput"
                  class="clear-btn"
                  v-if="messageInput.trim()"
                  title="清空输入"
              >
                <svg class="clear-icon" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
                </svg>
              </button>
              <button
                  @click="sendMessage"
                  class="send-button"
                  :disabled="loading || !messageInput.trim()"
                  :class="{ 'button-active': messageInput.trim() }"
                  title="发送消息"
              >
                <svg class="send-icon" viewBox="0 0 24 24">
                  <path fill="currentColor" d="M2,21L23,12L2,3V10L17,12L2,14V21Z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { inject } from 'vue'

const $api = inject('$api')
const messages = ref([])
const messageInput = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const textarea = ref(null)
const suggestionsContainer = ref(null)

const suggestions = ref([
  '如何发布二手商品？',
  '如何联系卖家？',
  '商品质量有问题怎么办？',
  '交易流程是怎样的？',
  '如何修改个人信息？',
  '如何搜索特定商品？',
  '交易安全注意事项',
  '运费如何计算？'
])

let messageIdCounter = 0

const sendMessage = async () => {
  if (!messageInput.value.trim() || loading.value) {
    return
  }

  const userMessage = messageInput.value.trim()

  messages.value.push({
    id: ++messageIdCounter,
    role: 'user',
    content: userMessage,
    timestamp: new Date().toISOString()
  })

  messageInput.value = ''
  adjustTextareaHeight()
  scrollToBottom()

  loading.value = true

  try {
    const response = await $api.post('/ai/chat', {
      message: userMessage,
      history: messages.value.slice(0, -1).map(m => ({
        role: m.role,
        content: m.content
      }))
    })

    if (response.data.success) {
      messages.value.push({
        id: ++messageIdCounter,
        role: 'assistant',
        content: response.data.reply,
        timestamp: new Date().toISOString()
      })
    } else {
      throw new Error('API error')
    }
  } catch (error) {
    console.error('Error sending message to AI:', error)
    messages.value.push({
      id: ++messageIdCounter,
      role: 'assistant',
      content: '抱歉，暂时无法处理您的请求。请稍后重试或联系客服。',
      timestamp: new Date().toISOString()
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const sendQuickQuestion = (question) => {
  messageInput.value = question
  sendMessage()
}

const clearInput = () => {
  messageInput.value = ''
  adjustTextareaHeight()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const adjustTextareaHeight = () => {
  nextTick(() => {
    if (textarea.value) {
      textarea.value.style.height = 'auto'
      textarea.value.style.height = Math.min(textarea.value.scrollHeight, 80) + 'px'
    }
  })
}

onMounted(() => {
  adjustTextareaHeight()
  // 移除滚动条
  document.documentElement.style.overflow = 'hidden'
  document.body.style.overflow = 'hidden'
})
</script>

<style scoped>
.ai-assistant-container {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: stretch;
  justify-content: stretch;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 14px;
}

.ai-chat-wrapper {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  transform: scale(0.9);
  transform-origin: center;
}

/* Back to Home Button */
.back-button-container {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1000;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  color: #667eea;
  text-decoration: none;
  font-size: 0.8rem;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.back-button:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.back-icon {
  width: 14px;
  height: 14px;
}

/* Header Styles */
.ai-chat-header {
  padding: 0.8rem 1rem;
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
  margin-top: 48px; /* 为返回按钮留出空间 */
}

.header-content {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  max-width: 100%;
  margin: 0 auto;
  width: 100%;
}

.avatar-container {
  position: relative;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.ai-icon {
  width: 20px;
  height: 20px;
  color: white;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.header-info h2 {
  font-size: 1.1rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 0.1rem 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-info p {
  font-size: 0.75rem;
  color: #666;
  margin: 0;
  opacity: 0.8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.3rem 0.6rem;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  flex-shrink: 0;
}

.status-indicator {
  position: relative;
  width: 8px;
  height: 8px;
}

.status-pulse {
  width: 8px;
  height: 8px;
  background: #4CAF50;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-pulse::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: #4CAF50;
  border-radius: 50%;
  animation: ripple 2s infinite;
}

.status-text {
  font-size: 0.7rem;
  font-weight: 500;
  color: #4CAF50;
}

/* Messages Container */
.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem 1rem;
  position: relative;
  background: #f8f9ff;
  /* 隐藏滚动条 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.ai-chat-messages::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

/* Welcome Message */
.welcome-message {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
  animation: fadeIn 0.8s ease-out;
}

.welcome-container {
  text-align: center;
  max-width: 320px;
  padding: 1rem;
}

.welcome-avatar {
  position: relative;
  margin-bottom: 1.2rem;
}

.avatar-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  filter: blur(20px);
  opacity: 0.3;
  animation: glow 3s ease-in-out infinite;
}

.avatar-content {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  position: relative;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
}

.welcome-icon {
  width: 28px;
  height: 28px;
  color: white;
}

.welcome-content h3 {
  font-size: 1.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 0.6rem 0;
}

.welcome-content p {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.4;
  opacity: 0.8;
  margin: 0;
}

/* Messages List */
.messages-list {
  max-width: 680px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  position: relative;
  z-index: 2;
}

.message-bubble {
  opacity: 0;
  animation: slideUp 0.3s ease-out forwards;
}

.message-wrapper {
  display: flex;
  width: 100%;
}

/* AI Message */
.message-left {
  display: flex;
  gap: 0.6rem;
  align-items: flex-start;
  max-width: 85%;
  margin-right: auto;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar-message {
  position: relative;
}

.avatar-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #4CAF50;
  border-radius: 50%;
  border: 1.5px solid white;
  z-index: 1;
}

.message-ai-icon {
  width: 32px;
  height: 32px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 6px;
  border-radius: 8px;
}

.message-content-wrapper {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 0.8rem 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  border: 1px solid rgba(102, 126, 234, 0.08);
  max-width: 100%;
  overflow-wrap: break-word;
}

.message-content-wrapper::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 14px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 6px 6px 6px 0;
  border-color: transparent white transparent transparent;
}

.message-name {
  font-size: 0.7rem;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 0.3rem;
}

.message-text {
  font-size: 0.85rem;
  line-height: 1.4;
  color: #333;
  margin-bottom: 0.4rem;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-time {
  font-size: 0.65rem;
  color: #888;
  text-align: right;
}

/* User Message */
.message-right {
  display: flex;
  gap: 0.6rem;
  align-items: flex-start;
  max-width: 85%;
  margin-left: auto;
}

.message-content-wrapper.user {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  order: 1;
}

.message-content-wrapper.user::before {
  left: auto;
  right: -6px;
  border-width: 6px 0 6px 6px;
  border-color: transparent transparent transparent #4CAF50;
}

.message-content-wrapper.user .message-name {
  color: rgba(255, 255, 255, 0.9);
}

.message-content-wrapper.user .message-text {
  color: white;
}

.message-content-wrapper.user .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.user-avatar-message {
  order: 2;
}

.message-user-icon {
  width: 32px;
  height: 32px;
  color: #764ba2;
  background: rgba(118, 75, 162, 0.1);
  padding: 6px;
  border-radius: 8px;
}

/* Loading Indicator */
.loading-indicator {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.6rem;
  max-width: 85%;
}

.loading-dots {
  display: flex;
  gap: 0.3rem;
}

.dot {
  width: 6px;
  height: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

.loading-text {
  font-size: 0.8rem;
  color: #667eea;
  font-weight: 500;
}

/* Quick Suggestions */
.quick-suggestions {
  padding: 0.6rem 0.8rem;
  background: rgba(255, 255, 255, 0.95);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
  flex-shrink: 0;
  margin-top: -20px;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.6rem;
  padding: 0 0.2rem;
}

.suggestions-icon {
  font-size: 0.9rem;
}

.suggestions-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: #667eea;
}

.suggestions-container {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  padding-bottom: 2px;
}

.suggestions-container::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
  height: 0;
}

.suggestions-scroll {
  display: flex;
  gap: 0.4rem;
  padding: 0 0.2rem;
  width: max-content;
}

.suggestion-chip {
  padding: 0.4rem 0.8rem;
  background: white;
  border: 1px solid rgba(102, 126, 234, 0.15);
  border-radius: 50px;
  font-size: 0.75rem;
  color: #667eea;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: 0;
  transform: translateY(5px);
  animation: slideUp 0.3s ease-out forwards;
  white-space: nowrap;
  flex-shrink: 0;
  height: 28px;
  display: flex;
  align-items: center;
}

.suggestion-chip:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  border-color: transparent;
}

.chip-content {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Input Area */
.ai-chat-input {
  padding: 0.4rem;
  background: white;
  flex-shrink: 0;
  margin-top: -15px;
}

.input-container {
  max-width: 500px;
  margin: 0 auto;
  width: 90%;
  min-width: 300px;
}

.input-wrapper {
  display: flex;
  gap: 0.4rem;
  align-items: flex-end;
  background: rgba(102, 126, 234, 0.04);
  border: 1px solid rgba(102, 126, 234, 0.15);
  border-radius: 20px;
  padding: 0.6rem 0.8rem;
  transition: all 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
  background: white;
}

.message-textarea {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.85rem;
  line-height: 1.4;
  color: #333;
  resize: none;
  max-height: 80px;
  min-height: 20px;
  padding: 0.2rem 0;
  outline: none;
  font-family: inherit;
  /* 隐藏滚动条 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.message-textarea::-webkit-scrollbar {
  display: none; /* Chrome, Safari and Opera */
}

.message-textarea::placeholder {
  color: #999;
  font-size: 0.85rem;
}

.message-textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-actions {
  display: flex;
  gap: 0.3rem;
  align-items: center;
  flex-shrink: 0;
}

.clear-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: rgba(0, 0, 0, 0.08);
  transform: rotate(90deg);
}

.clear-icon {
  width: 14px;
  height: 14px;
  color: #666;
}

.send-button {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(102, 126, 234, 0.15);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.send-button.button-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 3px 10px rgba(102, 126, 234, 0.2);
}

.send-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-icon {
  width: 16px;
  height: 16px;
  color: white;
}

/* Animations */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, -45%);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes ripple {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(3);
    opacity: 0;
  }
}

@keyframes glow {
  0%, 100% {
    opacity: 0.3;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.4;
    transform: translate(-50%, -50%) scale(1.05);
  }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 移除全局滚动条 */
:global(html) {
  overflow: hidden !important;
}

:global(body) {
  overflow: hidden !important;
}

/* Responsive Design */
@media (max-width: 768px) {
  .ai-chat-header {
    padding: 0.6rem 0.8rem;
    margin-top: 42px;
  }

  .header-content {
    gap: 0.6rem;
  }

  .ai-avatar {
    width: 36px;
    height: 36px;
  }

  .header-info h2 {
    font-size: 1rem;
  }

  .header-info p {
    font-size: 0.7rem;
  }

  .ai-chat-messages {
    padding: 0.6rem;
  }

  .messages-list {
    gap: 0.5rem;
  }

  .message-left,
  .message-right {
    max-width: 90%;
  }

  .message-text {
    font-size: 0.8rem;
  }

  .quick-suggestions {
    padding: 0.5rem 0.6rem;
  }

  .suggestion-chip {
    font-size: 0.7rem;
    padding: 0.3rem 0.6rem;
    height: 26px;
  }

  .ai-chat-input {
    padding: 0.5rem;
  }

  .input-wrapper {
    padding: 0.5rem 0.6rem;
  }

  .send-button {
    width: 32px;
    height: 32px;
  }

  .clear-btn {
    width: 26px;
    height: 26px;
  }
}

@media (max-width: 480px) {
  .header-status {
    display: none;
  }

  .suggestions-scroll {
    gap: 0.3rem;
  }

  .suggestion-chip {
    font-size: 0.65rem;
    padding: 0.25rem 0.5rem;
    height: 24px;
  }

  .chip-content {
    max-width: 150px;
  }

  .send-button {
    width: 30px;
    height: 30px;
  }

  .clear-btn {
    width: 24px;
    height: 24px;
  }

  .back-button {
    padding: 6px 10px;
    font-size: 0.75rem;
  }

  .ai-chat-header {
    margin-top: 38px;
  }
}
</style>