<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { auth } from '../store/auth'

const route = useRoute()
const router = useRouter()
const book = ref(null)
const orderVisible = ref(false)
const chatVisible = ref(false)
const reportVisible = ref(false)
const messages = ref([])
const chatText = ref('')
const busy = ref(false)

const orderForm = reactive({ tradeType: 'FACE_TO_FACE', tradePlace: '图书馆一楼大厅' })
const reportForm = reactive({ targetType: 'BOOK', targetId: null, reason: '' })

const conditionText = ['', '1 成新', '2 成新', '3 成新', '4 成新', '近乎全新']
const statusText = { ON_SALE: '在售', LOCKED: '已预订', SOLD: '已售出', OFF_SHELF: '已下架' }
const isMine = computed(() => auth.logged && book.value && book.value.sellerId === auth.user.userId)

async function load() {
  book.value = await api.bookDetail(route.params.id)
  reportForm.targetId = book.value.id
}

async function submitOrder() {
  if (!auth.logged) return router.push('/login')
  busy.value = true
  try {
    await api.createOrder({ bookId: book.value.id, ...orderForm })
    ElMessage.success('下单成功，等待卖家确认')
    orderVisible.value = false
    await load()
    router.push('/orders')
  } finally {
    busy.value = false
  }
}

async function openChat() {
  if (!auth.logged) return router.push('/login')
  chatVisible.value = true
  messages.value = await api.conversation(book.value.sellerId)
}

async function send() {
  if (!chatText.value.trim()) return
  await api.sendMessage({ toUserId: book.value.sellerId, bookId: book.value.id, content: chatText.value })
  chatText.value = ''
  messages.value = await api.conversation(book.value.sellerId)
}

async function submitReport() {
  if (!reportForm.reason.trim()) return ElMessage.warning('请填写举报原因')
  await api.report({ ...reportForm })
  reportVisible.value = false
  ElMessage.success('举报已提交，管理员会尽快处理')
}

async function offShelf() {
  await ElMessageBox.confirm('下架后其他同学将无法看到这本书，确定继续？', '提示', { type: 'warning' })
  await api.offShelf(book.value.id)
  ElMessage.success('已下架')
  router.push('/profile')
}

onMounted(load)
</script>

<template>
  <div class="page" v-if="book">
    <el-page-header title="返回" content="书籍详情" @back="router.back()" style="margin-bottom: 16px" />

    <el-card shadow="never" class="detail-card">
      <div class="detail">
        <img class="detail-cover" :src="book.coverUrl" alt="" />
        <div class="detail-info">
          <div class="title-row">
            <h2>{{ book.title }}</h2>
            <el-tag :type="book.status === 'ON_SALE' ? 'success' : 'info'">{{ statusText[book.status] }}</el-tag>
          </div>
          <p class="muted">{{ book.author }} · {{ book.publisher }} · ISBN {{ book.isbn || '—' }}</p>

          <div class="price-box">
            <span class="price big">¥{{ book.price }}</span>
            <span class="muted">原价 ¥{{ book.originalPrice }}</span>
            <el-tag type="warning" effect="light">
              比原价省 {{ book.originalPrice ? Math.round((1 - book.price / book.originalPrice) * 100) : 0 }}%
            </el-tag>
          </div>

          <el-descriptions :column="2" border size="small" style="margin: 16px 0">
            <el-descriptions-item label="成色">{{ conditionText[book.conditionLevel] }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ book.category }}</el-descriptions-item>
            <el-descriptions-item label="校区">{{ book.campus }}</el-descriptions-item>
            <el-descriptions-item label="浏览">{{ book.viewCount }} 次</el-descriptions-item>
            <el-descriptions-item label="卖家">{{ book.sellerName }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ book.createdAt }}</el-descriptions-item>
          </el-descriptions>

          <p class="desc">{{ book.description }}</p>

          <div class="actions">
            <template v-if="isMine">
              <el-button type="primary" disabled>这是我发布的书籍</el-button>
              <el-button @click="router.push('/profile')">去我的书架</el-button>
              <el-button type="danger" plain :disabled="book.status === 'LOCKED'" @click="offShelf">下架</el-button>
            </template>
            <template v-else>
              <el-button type="primary" size="large" :disabled="book.status !== 'ON_SALE'" @click="orderVisible = true">
                立即下单
              </el-button>
              <el-button size="large" @click="openChat">联系卖家</el-button>
              <el-button text type="danger" @click="reportVisible = true">举报</el-button>
            </template>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="orderVisible" title="确认订单" width="440px">
      <el-form label-width="90px">
        <el-form-item label="书籍">{{ book.title }}</el-form-item>
        <el-form-item label="金额">
          <span class="price">¥{{ book.price }}</span>
        </el-form-item>
        <el-form-item label="交易方式">
          <el-radio-group v-model="orderForm.tradeType">
            <el-radio value="FACE_TO_FACE">校内面交</el-radio>
            <el-radio value="SELF_PICKUP">驿站自提</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="交易地点">
          <el-input v-model="orderForm.tradePlace" placeholder="如：图书馆一楼大厅" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderVisible = false">取消</el-button>
        <el-button type="primary" :loading="busy" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="chatVisible" title="与卖家沟通" size="420px">
      <div class="chat-list">
        <div v-for="m in messages" :key="m.id" class="chat-item" :class="{ mine: m.fromUserId === auth.user?.userId }">
          <div class="bubble">{{ m.content }}</div>
        </div>
        <div v-if="!messages.length" class="empty-box">还没有聊天记录，先问问书的成色吧～</div>
      </div>
      <div class="chat-input">
        <el-input v-model="chatText" placeholder="输入消息，回车发送" @keyup.enter="send" />
        <el-button type="primary" @click="send">发送</el-button>
      </div>
    </el-drawer>

    <el-dialog v-model="reportVisible" title="举报违规内容" width="420px">
      <el-input v-model="reportForm.reason" type="textarea" :rows="3" placeholder="请描述违规情况，如盗版、虚假描述等" />
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-card {
  border-radius: 14px;
}
.detail {
  display: flex;
  gap: 32px;
}
.detail-cover {
  width: 280px;
  height: 373px;
  object-fit: cover;
  border-radius: 10px;
  background: #eef1f6;
}
.detail-info {
  flex: 1;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title-row h2 {
  margin: 0;
}
.price-box {
  display: flex;
  align-items: baseline;
  gap: 12px;
  background: #fff8f5;
  border-radius: 10px;
  padding: 14px 18px;
}
.price.big {
  font-size: 30px;
}
.desc {
  background: #f8fafc;
  border-radius: 10px;
  padding: 14px 16px;
  line-height: 1.8;
  color: #4a5768;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
.chat-list {
  height: calc(100vh - 200px);
  overflow-y: auto;
  padding: 4px;
}
.chat-item {
  display: flex;
  margin-bottom: 10px;
}
.chat-item.mine {
  justify-content: flex-end;
}
.bubble {
  max-width: 78%;
  padding: 10px 14px;
  border-radius: 12px;
  background: #f0f2f7;
  line-height: 1.6;
  font-size: 14px;
}
.chat-item.mine .bubble {
  background: #3a7afe;
  color: #fff;
}
.chat-input {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #eef1f6;
}
</style>
