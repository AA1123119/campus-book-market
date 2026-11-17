<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auth } from './store/auth'

const route = useRoute()
const router = useRouter()
const active = computed(() => route.path)

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <el-container>
    <el-header class="app-header">
      <div class="header-inner">
        <router-link to="/" class="brand">
          <span class="brand-mark">书</span>
          <span class="brand-text">书适圈<small>校园二手书智能交易</small></span>
        </router-link>

        <el-menu mode="horizontal" :default-active="active" router :ellipsis="false" class="nav">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/publish">我要卖书</el-menu-item>
          <el-menu-item index="/orders">我的订单</el-menu-item>
          <el-menu-item v-if="auth.isAdmin" index="/admin">管理后台</el-menu-item>
        </el-menu>

        <div class="user-area">
          <template v-if="auth.logged">
            <el-dropdown @command="(c) => c === 'logout' ? logout() : router.push('/profile')">
              <span class="user-chip">
                <el-avatar :size="28">{{ auth.user.nickname?.slice(0, 1) }}</el-avatar>
                {{ auth.user.nickname }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <el-button v-else type="primary" round @click="router.push('/login')">登录 / 注册</el-button>
        </div>
      </div>
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>

    <el-footer class="app-footer">
      《高级软件工程》项目实践 · 书适圈 Campus Book Market · 2026
    </el-footer>
  </el-container>
</template>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #e8ecf3;
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 20;
}
.header-inner {
  max-width: 1180px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #1f2d3d;
}
.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #3a7afe, #6f4ef2);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
}
.brand-text {
  font-weight: 700;
  font-size: 17px;
}
.brand-text small {
  display: block;
  font-size: 11px;
  font-weight: 400;
  color: #8a94a6;
}
.nav {
  flex: 1;
  border-bottom: none;
}
.user-area {
  display: flex;
  align-items: center;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}
.app-main {
  padding: 0;
  min-height: calc(100vh - 120px);
}
.app-footer {
  text-align: center;
  color: #98a1b3;
  font-size: 12px;
  line-height: 60px;
  background: #fff;
  border-top: 1px solid #e8ecf3;
}
</style>
