<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { auth } from '../store/auth'

const stats = ref({})
const reports = ref([])
const status = ref('PENDING')

const statusText = { PENDING: '待处理', APPROVED: '举报成立', REJECTED: '已驳回' }

async function load() {
  stats.value = await api.dashboard()
  reports.value = await api.reports(status.value || undefined)
}

async function handle(row, result) {
  await api.handleReport(row.id, {
    status: result,
    remark: result === 'APPROVED' ? '内容违规，已下架并扣减信用分' : '经核实未违规，驳回举报'
  })
  ElMessage.success('已处理')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-alert v-if="!auth.isAdmin" type="warning" :closable="false" show-icon
              title="当前账号不是管理员，处理举报会被后端拒绝（403），可用 admin/123456 登录演示" />

    <div class="stat-grid">
      <el-card shadow="never"><div class="stat"><span>在售书籍</span><strong>{{ stats.onSaleBooks }}</strong></div></el-card>
      <el-card shadow="never"><div class="stat"><span>已售出</span><strong>{{ stats.soldBooks }}</strong></div></el-card>
      <el-card shadow="never"><div class="stat"><span>订单总数</span><strong>{{ stats.totalOrders }}</strong></div></el-card>
      <el-card shadow="never"><div class="stat"><span>待处理举报</span><strong class="warn">{{ stats.pendingReports }}</strong></div></el-card>
      <el-card shadow="never"><div class="stat"><span>注册用户</span><strong>{{ stats.totalUsers }}</strong></div></el-card>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="table-head">
        <p class="card-title" style="margin: 0">举报处理</p>
        <el-radio-group v-model="status" size="small" @change="load">
          <el-radio-button label="PENDING">待处理</el-radio-button>
          <el-radio-button label="APPROVED">已成立</el-radio-button>
          <el-radio-button label="REJECTED">已驳回</el-radio-button>
          <el-radio-button label="">全部</el-radio-button>
        </el-radio-group>
        <el-button size="small" @click="load">刷新</el-button>
      </div>

      <el-table :data="reports" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="targetType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.targetType === 'BOOK' ? 'primary' : 'danger'">
              {{ row.targetType === 'BOOK' ? '书籍' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetTitle" label="举报对象" min-width="160" />
        <el-table-column prop="reason" label="举报原因" min-width="220" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="举报人" width="100" />
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'info'">
              {{ statusText[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="danger" @click="handle(row, 'APPROVED')">成立</el-button>
              <el-button size="small" @click="handle(row, 'REJECTED')">驳回</el-button>
            </template>
            <span v-else class="muted">{{ row.handleRemark }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 18px;
}
.stat {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat span {
  font-size: 13px;
  color: #8a94a6;
}
.stat strong {
  font-size: 26px;
  color: #1f2d3d;
}
.stat strong.warn {
  color: #e6502d;
}
.table-card {
  border-radius: 14px;
}
.table-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.table-head .card-title {
  margin-right: auto;
}
</style>
