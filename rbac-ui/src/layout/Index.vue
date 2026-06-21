<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <el-icon :size="26"><TrendCharts /></el-icon>
        <span>Stock RBAC SaaS</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :default-openeds="['user', 'admin']"
        class="aside-menu"
        background-color="#1f2937"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        router
        unique-opened
      >
        <el-menu-item index="/user/home">
          <el-icon><ChatDotRound /></el-icon>
          <span>我的工作台</span>
        </el-menu-item>

        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户中心</span>
          </template>
          <el-menu-item index="/user/question">我的问题</el-menu-item>
          <el-menu-item index="/user/order">我的订阅</el-menu-item>
          <el-menu-item index="/user/bill">我的账单</el-menu-item>
          <el-menu-item index="/user/tenant">租户信息</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>运营管理</span>
          </template>
          <el-menu-item index="/admin/home">运营仪表盘</el-menu-item>
          <el-menu-item index="/admin/tenant">租户管理</el-menu-item>
          <el-menu-item index="/admin/plan">套餐配置</el-menu-item>
          <el-menu-item index="/admin/order/sub">订阅订单</el-menu-item>
          <el-menu-item index="/admin/order/question">提问订单</el-menu-item>
          <el-menu-item index="/admin/bill">账单记录</el-menu-item>
          <el-menu-item index="/admin/question">问题列表</el-menu-item>
          <el-menu-item index="/admin/audit">审计日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon :size="20"><Menu /></el-icon>
          <span class="title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-tag :type="userStore.isAdmin ? 'danger' : 'success'" effect="plain" round>
            {{ userStore.isAdmin ? '管理员' : '普通用户' }}
          </el-tag>
          <el-dropdown>
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo.userName || userStore.userInfo.userAccount }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => route.meta?.title || 'Stock RBAC SaaS')

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (e) {}
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #f5f7fa;
}
.layout-aside {
  background: #1f2937;
  box-shadow: 2px 0 8px rgba(0,0,0,0.08);
}
.logo {
  height: 60px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.logo .el-icon { color: #fff; }
.aside-menu {
  border-right: none;
}
.aside-menu :deep(.el-menu-item), .aside-menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
}
.aside-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(102,126,234,0.4), rgba(118,75,162,0.4)) !important;
  border-left: 3px solid #667eea;
}
.aside-menu :deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.05);
}
.layout-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  padding: 0 20px;
  height: 54px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #333;
}
.header-left .title {
  font-size: 16px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}
.layout-main {
  padding: 20px;
  overflow-y: auto;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
