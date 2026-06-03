import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/product/:id',
      name: 'product',
      component: () => import('../views/ProductDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/publish',
      name: 'publish',
      component: () => import('../views/PublishProductView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true },
      children: [
        {
              path: 'products',
              name: 'profile-products',
              component: () => import('../components/profile/MyProducts.vue')
            },
            {
              path: 'favorites',
              name: 'profile-favorites',
              component: () => import('../components/profile/MyFavorites.vue')
            },
            {
              path: 'cart',
              name: 'profile-cart',
              component: () => import('../components/profile/MyCart.vue')
            },
            {
              path: 'reviews',
              name: 'profile-reviews',
              component: () => import('../components/profile/MyReviews.vue')
            }

      ]
    },
    {
      path: '/chat/:userId',
      name: 'chat',
      component: () => import('../views/ChatView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/ReviewCommunityView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../components/profile/MyCart.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/ai-assistant',
      name: 'ai-assistant',
      component: () => import('../views/AIAssistantView.vue'),
      meta: { requiresAuth: true }
    },
    // Admin routes
    {
      path: '/admin/dashboard',
      name: 'adminDashboard',
      component: () => import('../views/AdminDashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/users',
      name: 'userManagement',
      component: () => import('../views/UserManagementView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/products',
      name: 'productManagement',
      component: () => import('../views/ProductManagementView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/logs',
      name: 'logManagement',
      component: () => import('../views/LogManagementView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/analytics',
      name: 'dataVisualization',
      component: () => import('../views/DataVisualizationView.vue'),
      meta: { requiresAuth: true }
    }

  ]
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  // 检查路由是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  
  // 检查用户是否已登录
  const isLoggedIn = !!localStorage.getItem('token') && !!localStorage.getItem('user')
  
  // 检查是否是管理员路由
  const isAdminRoute = to.path.startsWith('/admin/')
  
  if (requiresAuth && !isLoggedIn) {
    // 未登录，重定向到登录页
    next('/login')
  } else if (isAdminRoute) {
    // 是管理员路由，检查用户角色
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      if (user.role === 'admin') {
        // 是管理员，允许访问
        next()
      } else {
        // 不是管理员，重定向到首页
        next('/')
      }
    } else {
      // 未登录，重定向到登录页
      next('/login')
    }
  } else {
    // 已登录或不需要认证，继续访问
    next()
  }
})

export default router
