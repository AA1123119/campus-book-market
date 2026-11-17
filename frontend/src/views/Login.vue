<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { auth } from '../store/auth'

const router = useRouter()
const route = useRoute()
const tab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: 'alice', password: '123456' })
const regForm = reactive({ username: '', password: '', nickname: '', campus: '主校区', phone: '' })

async function doLogin() {
  loading.value = true
  try {
    const res = await api.login(loginForm)
    auth.setSession(res)
    ElMessage.success(`欢迎回来，${res.nickname}`)
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  loading.value = true
  try {
    const res = await api.register(regForm)
    auth.setSession(res)
    ElMessage.success('注册成功，已自动登录')
    router.replace('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="login-brand">
        <div class="login-mark">书</div>
        <h2>书适圈</h2>
        <p class="muted">让每一本教材在校园里多流转一次</p>
      </div>

      <el-tabs v-model="tab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form label-position="top" @submit.prevent="doLogin">
            <el-form-item label="学号 / 用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password @keyup.enter="doLogin" />
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="loading" @click="doLogin">登录</el-button>
            <p class="muted" style="margin-top: 12px; text-align: center">
              演示账号：alice / bob / carol / admin，密码 123456
            </p>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form label-position="top" @submit.prevent="doRegister">
            <el-form-item label="用户名">
              <el-input v-model="regForm.username" placeholder="3-20 位字母或数字" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="regForm.password" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="regForm.nickname" placeholder="同学怎么称呼你" />
            </el-form-item>
            <el-form-item label="校区">
              <el-select v-model="regForm.campus" style="width: 100%">
                <el-option label="主校区" value="主校区" />
                <el-option label="东校区" value="东校区" />
                <el-option label="西校区" value="西校区" />
                <el-option label="南校区" value="南校区" />
              </el-select>
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="loading" @click="doRegister">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.login-wrap {
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 20% 20%, #e8f0ff 0, #f5f7fa 45%);
  padding: 40px 16px;
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 18px 40px rgba(31, 45, 61, 0.08);
}
.login-brand {
  text-align: center;
  margin-bottom: 8px;
}
.login-mark {
  width: 48px;
  height: 48px;
  margin: 0 auto 10px;
  border-radius: 14px;
  background: linear-gradient(135deg, #3a7afe, #6f4ef2);
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  display: grid;
  place-items: center;
}
.login-brand h2 {
  margin: 0 0 6px;
}
</style>
