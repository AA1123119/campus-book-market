<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'

const router = useRouter()
const tab = ref('bought')
const bought = ref([])
const sold = ref([])
const reviewVisible = ref(false)
const reviewForm = reactive({ orderId: null, rating: 5, content: '' })

const statusText = {
  PENDING: '待卖家确认',
  CONFIRMED: '已确认，待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}
const statusType = { PENDING: 'warning', CONFIRMED: 'primary', COMPLETED: 'success', CANCELLED: 'info' }

async function load() {
  bought.value = await api.boughtOrders()
  sold.value = await api.soldOrders()
}

async function act(fn, order, text) {
  await ElMessageBox.confirm(text, '提示', { type: 'warning' })
  await fn(order.id)
  ElMessage.success('操作成功')
  await load()
}

function openReview(order) {
  reviewForm.orderId = order.id
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

async function submitReview() {
  await api.review({ ...reviewForm })
  reviewVisible.value = false
  ElMessage.success('评价成功，感谢你的反馈')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never" class="orders-card">
      <el-tabs v-model="tab">
        <el-tab-pane :label="`我买到的 (${bought.length})`" name="bought">
          <div v-if="!bought.length" class="empty-box">还没有买过书，去首页逛逛吧</div>
          <div v-for="o in bought" :key="o.id" class="order-row">
            <div class="order-main">
              <p class="order-title" @click="router.push(`/book/${o.bookId}`)">{{ o.bookTitle }}</p>
              <p class="muted">
                订单号 {{ o.orderNo }} · 卖家 {{ o.sellerName }} ·
                {{ o.tradeType === 'FACE_TO_FACE' ? '校内面交' : '驿站自提' }}：{{ o.tradePlace }}
              </p>
              <p class="muted">{{ o.createdAt }}</p>
            </div>
            <div class="order-side">
              <span class="price">¥{{ o.amount }}</span>
              <el-tag :type="statusType[o.status]">{{ statusText[o.status] }}</el-tag>
              <div class="order-actions">
                <el-button v-if="o.status === 'CONFIRMED'" size="small" type="primary"
                           @click="act(api.completeOrder, o, '确认已收到书籍？确认后订单完成')">确认收货</el-button>
                <el-button v-if="['PENDING', 'CONFIRMED'].includes(o.status)" size="small"
                           @click="act(api.cancelOrder, o, '确定取消该订单？')">取消</el-button>
                <el-button v-if="o.status === 'COMPLETED'" size="small" type="success" plain
                           @click="openReview(o)">评价</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`我卖出的 (${sold.length})`" name="sold">
          <div v-if="!sold.length" class="empty-box">还没有卖出记录，去发布一本书吧</div>
          <div v-for="o in sold" :key="o.id" class="order-row">
            <div class="order-main">
              <p class="order-title" @click="router.push(`/book/${o.bookId}`)">{{ o.bookTitle }}</p>
              <p class="muted">订单号 {{ o.orderNo }} · 买家 {{ o.buyerName }} · {{ o.tradePlace }}</p>
              <p class="muted">{{ o.createdAt }}</p>
            </div>
            <div class="order-side">
              <span class="price">¥{{ o.amount }}</span>
              <el-tag :type="statusType[o.status]">{{ statusText[o.status] }}</el-tag>
              <div class="order-actions">
                <el-button v-if="o.status === 'PENDING'" size="small" type="primary"
                           @click="act(api.confirmOrder, o, '确认接受该订单并与买家约定面交？')">确认订单</el-button>
                <el-button v-if="['PENDING', 'CONFIRMED'].includes(o.status)" size="small"
                           @click="act(api.cancelOrder, o, '确定取消该订单？')">取消</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="reviewVisible" title="评价本次交易" width="440px">
      <el-form label-width="70px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" show-text />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="reviewForm.content" type="textarea" :rows="3"
                    placeholder="书籍成色、卖家沟通、面交是否准时…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.orders-card {
  border-radius: 14px;
}
.order-row {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 16px 6px;
  border-bottom: 1px solid #eef1f6;
}
.order-row:last-child {
  border-bottom: none;
}
.order-title {
  margin: 0 0 6px;
  font-weight: 600;
  cursor: pointer;
}
.order-title:hover {
  color: #3a7afe;
}
.order-main .muted {
  margin: 4px 0;
}
.order-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  min-width: 180px;
}
.order-actions {
  display: flex;
  gap: 8px;
}
</style>
