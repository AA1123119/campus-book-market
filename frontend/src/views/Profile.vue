<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { auth } from '../store/auth'

const router = useRouter()
const profile = ref(null)
const myBooks = ref([])

const statusText = { ON_SALE: '在售', LOCKED: '已预订', SOLD: '已售出', OFF_SHELF: '已下架' }

async function load() {
  profile.value = await api.profile(auth.user.userId)
  myBooks.value = await api.myBooks()
}

async function offShelf(book) {
  await api.offShelf(book.id)
  ElMessage.success('已下架')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page" v-if="profile">
    <el-card shadow="never" class="profile-card">
      <div class="profile-head">
        <el-avatar :size="64">{{ profile.user.nickname?.slice(0, 1) }}</el-avatar>
        <div class="profile-info">
          <h3>
            {{ profile.user.nickname }}
            <el-tag size="small" effect="plain">{{ profile.user.campus }}</el-tag>
          </h3>
          <p class="muted">账号 {{ profile.user.username }} · 注册于 {{ profile.user.createdAt?.slice(0, 10) }}</p>
        </div>
        <div class="score-box">
          <div>
            <strong>{{ profile.user.creditScore }}</strong>
            <span>信用分</span>
          </div>
          <div>
            <strong>{{ profile.avgRating }}</strong>
            <span>平均评分</span>
          </div>
          <div>
            <strong>{{ profile.reviews.length }}</strong>
            <span>收到评价</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="profile-card">
      <p class="card-title">我的书架 ({{ myBooks.length }})</p>
      <div v-if="!myBooks.length" class="empty-box">还没有发布过书籍</div>
      <div class="book-list">
        <div v-for="b in myBooks" :key="b.id" class="book-row">
          <img class="mini-cover" :src="b.coverUrl" alt="" />
          <div class="book-row-main">
            <p class="book-name" @click="router.push(`/book/${b.id}`)">{{ b.title }}</p>
            <p class="muted">{{ b.category }} · {{ b.viewCount }} 次浏览 · {{ b.createdAt?.slice(0, 10) }}</p>
          </div>
          <span class="price">¥{{ b.price }}</span>
          <el-tag :type="b.status === 'ON_SALE' ? 'success' : 'info'">{{ statusText[b.status] }}</el-tag>
          <el-button size="small" :disabled="b.status !== 'ON_SALE'" @click="offShelf(b)">下架</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="profile-card">
      <p class="card-title">收到的评价 ({{ profile.reviews.length }})</p>
      <div v-if="!profile.reviews.length" class="empty-box">暂无评价</div>
      <div v-for="r in profile.reviews" :key="r.id" class="review-row">
        <el-rate :model-value="r.rating" disabled size="small" />
        <p class="review-text">{{ r.content }}</p>
        <p class="muted">{{ r.fromUserName }} · {{ r.createdAt }}</p>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.profile-card {
  border-radius: 14px;
  margin-bottom: 18px;
}
.profile-head {
  display: flex;
  align-items: center;
  gap: 20px;
}
.profile-info {
  flex: 1;
}
.profile-info h3 {
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.score-box {
  display: flex;
  gap: 32px;
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px 26px;
}
.score-box div {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.score-box strong {
  font-size: 22px;
  color: #3a7afe;
}
.score-box span {
  font-size: 12px;
  color: #8a94a6;
}
.book-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px solid #eef1f6;
}
.book-row:last-child {
  border-bottom: none;
}
.mini-cover {
  width: 44px;
  height: 58px;
  object-fit: cover;
  border-radius: 4px;
}
.book-row-main {
  flex: 1;
}
.book-name {
  margin: 0 0 4px;
  font-weight: 600;
  cursor: pointer;
}
.review-row {
  padding: 12px 0;
  border-bottom: 1px dashed #eef1f6;
}
.review-text {
  margin: 8px 0 4px;
  color: #4a5768;
}
</style>
