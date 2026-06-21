import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user.js'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/user/home',
    meta: { title: '工作台' },
    children: [
      {
        path: 'user/home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '我的工作台', role: 'user' }
      },
      {
        path: 'user/question',
        name: 'UserQuestion',
        component: () => import('@/views/user/Question.vue'),
        meta: { title: '我的提问', role: 'user' }
      },
      {
        path: 'user/order',
        name: 'UserOrder',
        component: () => import('@/views/user/Order.vue'),
        meta: { title: '我的订阅', role: 'user' }
      },
      {
        path: 'user/bill',
        name: 'UserBill',
        component: () => import('@/views/user/Bill.vue'),
        meta: { title: '我的账单', role: 'user' }
      },
      {
        path: 'user/tenant',
        name: 'UserTenant',
        component: () => import('@/views/user/Tenant.vue'),
        meta: { title: '租户信息', role: 'user' }
      },
      {
        path: 'admin/home',
        name: 'AdminHome',
        component: () => import('@/views/admin/Home.vue'),
        meta: { title: '运营仪表盘', role: 'admin' }
      },
      {
        path: 'admin/tenant',
        name: 'AdminTenant',
        component: () => import('@/views/admin/Tenant.vue'),
        meta: { title: '租户管理', role: 'admin' }
      },
      {
        path: 'admin/plan',
        name: 'AdminPlan',
        component: () => import('@/views/admin/Plan.vue'),
        meta: { title: '套餐配置', role: 'admin' }
      },
      {
        path: 'admin/order/sub',
        name: 'AdminSubOrder',
        component: () => import('@/views/admin/SubOrder.vue'),
        meta: { title: '订阅订单', role: 'admin' }
      },
      {
        path: 'admin/order/question',
        name: 'AdminQuestionOrder',
        component: () => import('@/views/admin/QuestionOrder.vue'),
        meta: { title: '提问订单', role: 'admin' }
      },
      {
        path: 'admin/bill',
        name: 'AdminBill',
        component: () => import('@/views/admin/Bill.vue'),
        meta: { title: '账单记录', role: 'admin' }
      },
      {
        path: 'admin/question',
        name: 'AdminQuestion',
        component: () => import('@/views/admin/Question.vue'),
        meta: { title: '问题列表', role: 'admin' }
      },
      {
        path: 'admin/audit',
        name: 'AdminAudit',
        component: () => import('@/views/admin/AuditLog.vue'),
        meta: { title: '审计日志', role: 'admin' }
      },
      {
        path: 'admin/quota',
        name: 'AdminQuota',
        component: () => import('@/views/admin/Quota.vue'),
        meta: { title: '额度管理', role: 'admin' }
      },
      {
        path: 'admin/knowledge',
        name: 'AdminKnowledge',
        component: () => import('@/views/admin/Knowledge.vue'),
        meta: { title: '知识库管理', role: 'admin' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/user/home'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫：认证 + 角色鉴权
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta?.public) {
    next()
    return
  }
  if (!userStore.isLogged) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  const needRole = to.meta?.role
  if (needRole === 'admin' && !userStore.isAdmin) {
    next('/user/home')
    return
  }
  next()
})

router.afterEach((to) => {
  document.title = (to.meta?.title || 'Stock RBAC SaaS') + ' | Stock RBAC SaaS 平台'
})

export default router
