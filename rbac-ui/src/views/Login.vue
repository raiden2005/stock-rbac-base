<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">用户登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="userAccount">
          <el-input v-model="form.userAccount" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="userPwd">
          <el-input v-model="form.userPwd" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user.js'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  userAccount: '',
  userPwd: ''
})

const rules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userPwd: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(form)
        ElMessage.success('登录成功')
        // 使用 Pinia getter isAdmin 判断角色并跳转
        if (userStore.isAdmin) {
          router.replace('/admin/home')
        } else {
          router.replace('/user/home')
        }
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-btn {
  width: 100%;
}
</style>
