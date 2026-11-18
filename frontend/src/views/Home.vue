<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { auth } from '../store/auth'

const router = useRouter()
const books = ref([])
const recommend = ref([])
const meta = reactive({ categories: [], campuses: [] })
const loading = ref(false)

const query = reactive({
  keyword: '',
  category: '',
  campus: '',
  minPrice: null,
  maxPrice: null,
  sort: ''
})

const conditionText = ['', '1 成新', '2 成新', '3 成新', '4 成新', '近乎全新']

async function loadBooks() {
  loading.value = true
  try {
    books.value = await api.books({
      keyword: query.keyword || undefined,
      category: query.category || undefined,
      campus: query.campus || undefined,
      minPrice: query.minPrice ?? undefined,
      maxPrice: query.maxPrice ?? undefined,
      sort: query.sort || undefined
    })
  } finally {
    loading.value = false
  }
}

async function loadRecommend() {
  try {
    recommend.value = await api.recommend()
  } catch (e) {
    recommend.value = []
  }
}

function reset() {
  Object.assign(query, { keyword: '', category: '', campus: '', minPrice: null, maxPrice: null, sort: '' })
  loadBooks()
}

onMounted(async () => {
  const m = await api.bookMeta()
  meta.categories = m.categories
  meta.campuses = m.campuses
  await loadBooks()
  if (auth.logged) loadRecommend()
})
</script>

<template>
  <div class="page">
    <section class="hero">
      <div>
        <h1>把闲置教材，交给下一位同学</h1>
        <p>AI 智能定价 · 同校当面交易 · 信用分保障，一本书平均再省 62%</p>
        <el-input
          v-model="query.keyword"
          size="large"
          class="hero-search"
          placeholder="搜书名 / 作者 / ISBN，如：数据结构"
          clearable
          @keyup.enter="loadBooks"
        >
          <template #append>
            <el-button type="primary" @click="loadBooks">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="hero-stat">
        <div><strong>2,318</strong><span>本教材在流转</span></div>
        <div><strong>96.4%</strong><span>交易满意度</span></div>
        <div><strong>¥28</strong><span>平均成交价</span></div>
      </div>
    </section>

    <el-card v-if="recommend.length" shadow="never" class="block">
      <div class="block-head">
        <p class="card-title">为你推荐</p>
        <span class="muted">基于你的浏览与购买记录，冷启动用户展示全站热销</span>
      </div>
      <div class="rec-row">
        <div v-for="item in recommend" :key="item.book.id" class="rec-item" @click="router.push(`/book/${item.book.id}`)">
          <img class="rec-cover" :src="item.book.coverUrl" alt="" />
          <div class="rec-info">
            <p class="rec-title">{{ item.book.title }}</p>
            <p class="muted">{{ item.reason }}</p>
            <p class="price">¥{{ item.book.price }}</p>
          </div>
          <el-tag size="small" type="success" effect="light">匹配度 {{ item.score }}</el-tag>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="block">
      <div class="filters">
        <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 140px" @change="loadBooks">
          <el-option v-for="c in meta.categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="query.campus" placeholder="全部校区" clearable style="width: 130px" @change="loadBooks">
          <el-option v-for="c in meta.campuses" :key="c" :label="c" :value="c" />
        </el-select>
        <el-input-number v-model="query.minPrice" :min="0" placeholder="最低价" controls-position="right" style="width: 120px" />
        <span class="muted">—</span>
        <el-input-number v-model="query.maxPrice" :min="0" placeholder="最高价" controls-position="right" style="width: 120px" />
        <el-select v-model="query.sort" placeholder="综合排序" clearable style="width: 130px" @change="loadBooks">
          <el-option label="最新发布" value="" />
          <el-option label="价格从低到高" value="priceAsc" />
          <el-option label="价格从高到低" value="priceDesc" />
          <el-option label="最多浏览" value="hot" />
        </el-select>
        <el-button @click="loadBooks">筛选</el-button>
        <el-button text @click="reset">重置</el-button>
        <span class="muted" style="margin-left: auto">共 {{ books.length }} 本在售</span>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated />
      <div v-else-if="books.length" class="grid">
        <el-card v-for="b in books" :key="b.id" shadow="hover" class="book-card" @click="router.push(`/book/${b.id}`)">
          <img class="book-cover" :src="b.coverUrl" alt="" />
          <div class="book-body">
            <p class="book-title">{{ b.title }}</p>
            <p class="muted">{{ b.author }} · {{ b.category }}</p>
            <div class="book-foot">
              <span class="price">¥{{ b.price }}</span>
              <span class="muted">原价 ¥{{ b.originalPrice }}</span>
            </div>
            <div class="book-tags">
              <el-tag size="small" effect="plain">{{ conditionText[b.conditionLevel] || '成色良好' }}</el-tag>
              <el-tag size="small" type="info" effect="plain">{{ b.campus }}</el-tag>
            </div>
          </div>
        </el-card>
      </div>
      <div v-else class="empty-box">没有符合条件的书籍，换个关键词试试～</div>
    </el-card>
  </div>
</template>

<style scoped>
.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
  background: linear-gradient(120deg, #2d5bff, #6f4ef2 60%, #9a5cf5);
  border-radius: 18px;
  padding: 40px;
  color: #fff;
  margin-bottom: 20px;
}
.hero h1 {
  margin: 0 0 10px;
  font-size: 30px;
}
.hero p {
  margin: 0 0 20px;
  opacity: 0.88;
}
.hero-search {
  max-width: 520px;
}
.hero-stat {
  display: flex;
  gap: 28px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  padding: 20px 26px;
  backdrop-filter: blur(4px);
}
.hero-stat div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hero-stat strong {
  font-size: 24px;
}
.hero-stat span {
  font-size: 12px;
  opacity: 0.85;
}
.block {
  margin-bottom: 20px;
  border-radius: 14px;
}
.block-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}
.rec-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}
.rec-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  background: #f8fafc;
  cursor: pointer;
  transition: 0.2s;
}
.rec-item:hover {
  background: #eef3ff;
}
.rec-cover {
  width: 52px;
  height: 70px;
  object-fit: cover;
  border-radius: 4px;
}
.rec-info {
  flex: 1;
  min-width: 0;
}
.rec-title {
  margin: 0 0 4px;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rec-info .muted {
  margin: 0 0 4px;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rec-info .price {
  margin: 0;
}
.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}
.book-card {
  border-radius: 12px;
  cursor: pointer;
}
.book-body {
  padding-top: 10px;
}
.book-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.book-body .muted {
  margin: 0 0 8px;
}
.book-foot {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.book-foot .price {
  font-size: 18px;
}
.book-tags {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}
</style>
