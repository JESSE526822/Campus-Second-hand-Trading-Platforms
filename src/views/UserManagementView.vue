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
          class="nav-item active"
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
          class="nav-item"
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
        <h2>用户管理</h2>
        <p>管理系统用户账号，包括查看、添加和删除用户</p>
      </div>

      <!-- Add User Button -->
      <div class="content-actions">
        <button @click="showAddUserDialog = true" class="add-user-button">
          <span class="add-icon">➕</span>
          新增用户
        </button>
      </div>

      <!-- Users Table -->
      <div class="users-table-container">
        <table class="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>角色</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>最后登录时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>
                <span class="role-badge" :class="user.role">
                  {{ user.role === 'admin' ? '管理员' : '普通用户' }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="user.status">
                  {{ user.status }}
                </span>
              </td>
              <td>{{ formatDate(user.created_at) }}</td>
              <td>{{ formatDate(user.last_login_at) || '从未登录' }}</td>
              <td>
                <div class="user-actions">
                  <button 
                    v-if="user.role !== 'admin'"
                    @click="confirmDeleteUser(user)"
                    class="delete-button"
                  >
                    <span class="delete-icon">🗑️</span>
                    删除
                  </button>
                  <span v-else class="admin-lock">
                    <span class="lock-icon">🔒</span>
                    不可删除
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="users.length === 0" class="empty-state">
          <span class="empty-icon">👥</span>
          <p>暂无用户数据</p>
        </div>
      </div>

      <!-- Add User Dialog -->
      <div v-if="showAddUserDialog" class="dialog-overlay" @click="closeAddUserDialog">
        <div class="dialog-content" @click.stop>
          <div class="dialog-header">
            <h3>新增用户</h3>
            <button @click="closeAddUserDialog" class="close-button">×</button>
          </div>
          <div class="dialog-body">
            <div class="form-group">
              <label for="newUsername">用户名</label>
              <input
                type="text"
                id="newUsername"
                v-model="newUser.username"
                class="form-input"
                placeholder="请输入用户名"
              >
            </div>
            <div class="form-group">
              <label for="newPassword">密码</label>
              <input
                type="password"
                id="newPassword"
                v-model="newUser.password"
                class="form-input"
                placeholder="请输入密码"
              >
            </div>
            <div class="form-group">
              <label for="newRole">角色</label>
              <select
                id="newRole"
                v-model="newUser.role"
                class="form-select"
              >
                <option value="user">普通用户</option>
                <option value="admin">管理员</option>
              </select>
            </div>
          </div>
          <div class="dialog-footer">
            <button @click="closeAddUserDialog" class="cancel-button">取消</button>
            <button @click="addUser" class="confirm-button" :disabled="!isFormValid">确认添加</button>
          </div>
        </div>
      </div>

      <!-- Delete Confirmation Dialog -->
      <div v-if="showDeleteDialog" class="dialog-overlay" @click="closeDeleteDialog">
        <div class="dialog-content" @click.stop>
          <div class="dialog-header">
            <h3>确认删除</h3>
            <button @click="closeDeleteDialog" class="close-button">×</button>
          </div>
          <div class="dialog-body">
            <p>确定要删除用户 <strong>{{ userToDelete?.username }}</strong> 吗？</p>
            <p class="delete-warning">此操作不可撤销，删除后用户数据将被永久移除。</p>
          </div>
          <div class="dialog-footer">
            <button @click="closeDeleteDialog" class="cancel-button">取消</button>
            <button @click="deleteUser" class="delete-confirm-button">确认删除</button>
          </div>
        </div>
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
const username = ref('admin')
const users = ref([])
const showAddUserDialog = ref(false)
const showDeleteDialog = ref(false)
const userToDelete = ref(null)
const newUser = ref({
  username: '',
  password: '',
  role: 'user'
})

const isFormValid = computed(() => {
  return newUser.value.username && newUser.value.password
})

const loadUsers = async () => {
  try {
    const response = await $api.get('/admin/users')
    if (response.data.success) {
      users.value = response.data.users
    }
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

const addUser = async () => {
  try {
    const response = await $api.post('/admin/users', newUser.value)
    if (response.data.success) {
      closeAddUserDialog()
      loadUsers()
    }
  } catch (error) {
    console.error('Failed to add user:', error)
  }
}

const confirmDeleteUser = (user) => {
  userToDelete.value = user
  showDeleteDialog.value = true
}

const deleteUser = async () => {
  if (!userToDelete.value) return
  
  try {
    const response = await $api.delete(`/admin/users/${userToDelete.value.id}`)
    if (response.data.success) {
      closeDeleteDialog()
      loadUsers()
    }
  } catch (error) {
    console.error('Failed to delete user:', error)
  }
}

const closeAddUserDialog = () => {
  showAddUserDialog.value = false
  newUser.value = {
    username: '',
    password: '',
    role: 'user'
  }
}

const closeDeleteDialog = () => {
  showDeleteDialog.value = false
  userToDelete.value = null
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
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
  loadUsers()
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

/* Content Actions */
.content-actions {
  margin-bottom: 2rem;
}

.add-user-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.add-user-button:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(76, 175, 80, 0.4);
  background: linear-gradient(135deg, #45a049 0%, #388E3C 100%);
}

.add-icon {
  font-size: 1.2rem;
}

/* Users Table */
.users-table-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 1.5rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.users-table th {
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

.users-table tr:hover {
  background: #f8fff8;
}

.users-table tr:last-child td {
  border-bottom: none;
}

/* Role and Status Badges */
.role-badge {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-badge.admin {
  background: #e3f2fd;
  color: #1565c0;
}

.role-badge.user {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.normal {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge.disabled {
  background: #ffebee;
  color: #c62828;
}

/* User Actions */
.user-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.delete-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.delete-button:hover {
  transform: translateY(-2px);
  background: #d32f2f;
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}

.delete-icon {
  font-size: 1rem;
}

.admin-lock {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #f5f5f5;
  color: #666;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
}

.lock-icon {
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

/* Dialogs */
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
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: slideIn 0.3s ease-out;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.dialog-header h3 {
  font-size: 1.3rem;
  font-weight: 600;
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

.dialog-body {
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.form-input,
.form-select {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
}

.delete-warning {
  color: #f44336;
  font-size: 0.9rem;
  margin-top: 1rem;
}

.dialog-footer {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.cancel-button {
  padding: 0.75rem 1.5rem;
  background: #f5f5f5;
  color: #333;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background: #e0e0e0;
}

.confirm-button {
  padding: 0.75rem 1.5rem;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-button:hover:not(:disabled) {
  background: #45a049;
  transform: translateY(-2px);
}

.confirm-button:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.delete-confirm-button {
  padding: 0.75rem 1.5rem;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.delete-confirm-button:hover {
  background: #d32f2f;
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

  .users-table {
    font-size: 0.875rem;
  }

  .users-table th,
  .users-table td {
    padding: 1rem;
  }

  .user-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
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

  .add-user-button {
    width: 100%;
    justify-content: center;
  }

  .users-table-container {
    overflow-x: auto;
  }
}
</style>