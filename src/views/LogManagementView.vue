<template>
  <div class="admin-dashboard">
    <!-- Admin Sidebar -->
    <div class="admin-sidebar">
      <div class="sidebar-nav">
        <div 
          class="nav-item"
          @click="navigateTo('/admin/dashboard')"
        >
          <span class="nav-icon">📊</span>
          <span class="nav-text">仪表盘</span>
        </div>
        <div 
          class="nav-item"
          @click="navigateTo('/admin/users')"
        >
          <span class="nav-icon">👥</span>
          <span class="nav-text">用户管理</span>
        </div>
        <div 
          class="nav-item"
          @click="navigateTo('/admin/products')"
        >
          <span class="nav-icon">🛍️</span>
          <span class="nav-text">商品管理</span>
        </div>
        <div 
          class="nav-item active"
          @click="navigateTo('/admin/logs')"
        >
          <span class="nav-icon">📋</span>
          <span class="nav-text">日志管理</span>
        </div>
        <div 
          class="nav-item"
          @click="navigateTo('/admin/analytics')"
        >
          <span class="nav-icon">📈</span>
          <span class="nav-text">数据统计</span>
        </div>
        <div 
          class="nav-item logout-item"
          @click="logout"
        >
          <span class="nav-icon">🚪</span>
          <span class="nav-text">退出登录</span>
        </div>
      </div>
    </div>

    <!-- Admin Main Content -->
    <div class="admin-content">
      <div class="content-header">
        <h2>日志管理</h2>
        <p>查看系统操作日志，了解系统运行状态和用户活动</p>
      </div>

      <!-- Logs Table -->
      <div class="logs-table-container">
        <div class="logs-header">
          <div class="logs-info">
            <span class="logs-count">{{ logs.length }} 条日志记录</span>
          </div>
          <div class="logs-actions">
            <button @click="refreshLogs" class="refresh-button">
              <span class="refresh-icon">🔄</span>
              刷新
            </button>
          </div>
        </div>
        <table class="logs-table">
          <thead>
            <tr>
              <th>时间</th>
              <th>操作</th>
              <th>用户</th>
              <th>IP地址</th>
              <th>详细信息</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(log, index) in logs" :key="index">
              <td>{{ formatDate(log.time) }}</td>
              <td>
                <span class="action-badge" :class="getActionType(log.action)">
                  {{ log.action }}
                </span>
              </td>
              <td>{{ log.user }}</td>
              <td>{{ log.ip }}</td>
              <td>
                <button @click="viewLogDetails(log)" class="details-button">
                  <span class="details-icon">📄</span>
                  查看
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="logs.length === 0" class="empty-state">
          <span class="empty-icon">📋</span>
          <p>暂无日志数据</p>
        </div>
      </div>

      <!-- Log Details Dialog -->
      <div v-if="showLogDetails" class="dialog-overlay" @click="closeLogDetails">
        <div class="dialog-content log-details-dialog" @click.stop>
          <div class="dialog-header">
            <h3>日志详情</h3>
            <button @click="closeLogDetails" class="close-button">×</button>
          </div>
          <div class="dialog-body">
            <div class="log-details">
              <div class="log-detail-item">
                <span class="detail-label">操作时间:</span>
                <span class="detail-value">{{ formatDate(selectedLog?.time) }}</span>
              </div>
              <div class="log-detail-item">
                <span class="detail-label">操作类型:</span>
                <span class="action-badge" :class="getActionType(selectedLog?.action)">
                  {{ selectedLog?.action }}
                </span>
              </div>
              <div class="log-detail-item">
                <span class="detail-label">操作用户:</span>
                <span class="detail-value">{{ selectedLog?.user }}</span>
              </div>
              <div class="log-detail-item">
                <span class="detail-label">IP地址:</span>
                <span class="detail-value">{{ selectedLog?.ip }}</span>
              </div>
              <div class="log-detail-item">
                <span class="detail-label">详细信息:</span>
                <div class="detail-content">
                  {{ selectedLog?.details || '无详细信息' }}
                </div>
              </div>
            </div>
          </div>
          <div class="dialog-footer">
            <button @click="closeLogDetails" class="close-details-button">关闭</button>
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
const username = ref('admin')
const logs = ref([])
const showLogDetails = ref(false)
const selectedLog = ref(null)

const loadLogs = async () => {
  try {
    const response = await $api.get('/admin/logs')
    if (response.data.success) {
      // 过滤只保留登录和登出相关的记录
      logs.value = response.data.logs.filter(log => {
        const action = log.action?.toLowerCase() || ''
        return action.includes('登录') || action.includes('登出')
      })
    }
  } catch (error) {
    console.error('Failed to load logs:', error)
    // 使用模拟数据
    logs.value = getMockLogs()
  }
}

const refreshLogs = () => {
  loadLogs()
}

const viewLogDetails = (log) => {
  selectedLog.value = log
  showLogDetails.value = true
}

const closeLogDetails = () => {
  showLogDetails.value = false
  selectedLog.value = null
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getActionType = (action) => {
  if (!action) return 'other'
  const actionLower = action.toLowerCase()
  if (actionLower.includes('登录')) return 'login'
  if (actionLower.includes('删除')) return 'delete'
  if (actionLower.includes('添加') || actionLower.includes('新增')) return 'add'
  if (actionLower.includes('查看') || actionLower.includes('浏览')) return 'view'
  return 'other'
}

const getMockLogs = () => {
  return [
    {
      time: new Date(),
      action: '管理员登录',
      user: 'admin',
      ip: '127.0.0.1',
      details: '管理员通过系统登录界面登录'
    },
    {
      time: new Date(Date.now() - 3600000),
      action: '管理员登出',
      user: 'admin',
      ip: '127.0.0.1',
      details: '管理员退出系统'
    },
    {
      time: new Date(Date.now() - 7200000),
      action: '用户登录',
      user: 'user1',
      ip: '192.168.1.100',
      details: '用户 user1 登录系统'
    },
    {
      time: new Date(Date.now() - 10800000),
      action: '用户登出',
      user: 'user1',
      ip: '192.168.1.100',
      details: '用户 user1 退出系统'
    },
    {
      time: new Date(Date.now() - 14400000),
      action: '用户登录',
      user: 'user2',
      ip: '192.168.1.101',
      details: '用户 user2 登录系统'
    },
    {
      time: new Date(Date.now() - 18000000),
      action: '管理员登录',
      user: 'admin',
      ip: '127.0.0.1',
      details: '管理员通过系统登录界面登录'
    },
    {
      time: new Date(Date.now() - 21600000),
      action: '管理员登出',
      user: 'admin',
      ip: '127.0.0.1',
      details: '管理员退出系统'
    },
    {
      time: new Date(Date.now() - 25200000),
      action: '用户登录',
      user: 'user3',
      ip: '192.168.1.102',
      details: '用户 user3 登录系统'
    }
  ]
}

const navigateTo = (path) => {
  router.push(path)
}

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(() => {
  loadLogs()
  const user = localStorage.getItem('user')
  if (user) {
    username.value = JSON.parse(user).username
  }
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: row;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* Admin Sidebar */
.admin-sidebar {
  width: 250px;
  background: white;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  border-right: 2px solid #f0f0f0;
  animation: slideInLeft 0.8s ease-out;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 2rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 2rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 4px solid transparent;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(76, 175, 80, 0.05));
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  z-index: 0;
}

.nav-item:hover::before,
.nav-item.active::before {
  transform: translateX(0);
}

.nav-item:hover {
  background: #f8fff8;
  transform: translateX(8px);
  border-left-color: #4CAF50;
}

.nav-item.active {
  background: #f0fff0;
  border-left-color: #4CAF50;
  font-weight: 600;
}

.nav-item.active .nav-text {
  color: #4CAF50;
}

.nav-icon {
  font-size: 1.3rem;
  flex-shrink: 0;
  z-index: 1;
}

.nav-text {
  font-size: 1rem;
  color: #333;
  z-index: 1;
  transition: color 0.3s ease;
}

.nav-item.active .nav-text {
  color: #4CAF50;
}

/* Animations */
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

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Main Content */
.admin-content {
  flex: 1;
  padding: 2rem;
  animation: fadeInRight 0.8s ease-out;
}

.content-header {
  margin-bottom: 2rem;
}

.content-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.content-header p {
  font-size: 1.1rem;
  color: #666;
  margin: 0.5rem 0 0 0;
}

/* Logs Table Container */
.logs-table-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: fadeInUp 1s ease-out 0.2s both;
}

/* Logs Header */
.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
  background: #f8f9fa;
}

.logs-info {
  display: flex;
  gap: 1rem;
}

.logs-count {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  background: #e8f5e8;
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.logs-actions {
  display: flex;
  gap: 1rem;
}

.refresh-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.refresh-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.refresh-icon {
  font-size: 1rem;
}

/* Logs Table */
.logs-table {
  width: 100%;
  border-collapse: collapse;
}

.logs-table th,
.logs-table td {
  padding: 1.5rem 2rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.logs-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.logs-table tr:hover {
  background: #f8fff8;
}

.logs-table tr:last-child td {
  border-bottom: none;
}

/* Action Badges */
.action-badge {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.action-badge.login {
  background: #e3f2fd;
  color: #1565c0;
}

.action-badge.delete {
  background: #ffebee;
  color: #c62828;
}

.action-badge.add {
  background: #e8f5e8;
  color: #2e7d32;
}

.action-badge.view {
  background: #fff3e0;
  color: #ef6c00;
}

.action-badge.other {
  background: #f5f5f5;
  color: #616161;
}

/* Details Button */
.details-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.details-button:hover {
  transform: translateY(-2px);
  background: #1976d2;
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.details-icon {
  font-size: 1rem;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6rem 2rem;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

/* Log Details Dialog */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

.dialog-content {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideIn 0.3s ease-out;
}

.log-details-dialog {
  max-width: 700px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.dialog-header h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-button:hover {
  background: #f0f0f0;
  color: #333;
}

.log-details {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.log-detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detail-label {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.detail-value {
  color: #666;
  font-size: 1rem;
}

.detail-content {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  color: #333;
  font-size: 1rem;
  line-height: 1.6;
  white-space: pre-wrap;
}

.dialog-footer {
  margin-top: 2rem;
  display: flex;
  justify-content: flex-end;
}

.close-details-button {
  padding: 0.75rem 2rem;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-details-button:hover {
  background: #45a049;
  transform: translateY(-2px);
}

/* Animations */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .admin-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .admin-content {
    padding: 1rem;
  }

  .logs-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .logs-table {
    font-size: 0.875rem;
  }

  .logs-table th,
  .logs-table td {
    padding: 1rem;
  }

  .dialog-content {
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .admin-header-left h1 {
    font-size: 1.5rem;
  }

  .content-header h2 {
    font-size: 1.5rem;
  }

  .logs-table-container {
    overflow-x: auto;
  }

  .logs-table {
    min-width: 600px;
  }
}
</style>