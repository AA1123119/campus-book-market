<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const router = useRouter()
const formRef = ref()
const meta = reactive({ categories: [], campuses: [] })
const isbn = ref('')
const suggestion = ref(null)
const aiLoading = ref(false)
const submitting = ref(false)

const form = reactive({
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  category: '数据结构',
  conditionLevel: 4,
  originalPrice: 45,
  price: null,
  description: '',
  coverUrl: ''
})

const rules = {
  title: [{ required: true, message: '请填写书名', trigger: 'blur' }],
  price: [{ required: true, message: '请填写售价', trigger: 'blur' }]
}

const conditionMarks = { 1: '1成新', 2: '2成新', 3: '3成新', 4: '4成新', 5: '全新' }

async function lookup() {
  if (!isbn.value) return ElMessage.warning('请输入 ISBN')
  const r = await api.isbnLookup(isbn.value.trim())
  form.isbn = r.isbn
  form.title = r.title
  form.author = r.author
  form.publisher = r.publisher
  form.category = r.category
  form.originalPrice = r.originalPrice
  form.coverUrl = r.coverUrl
  ElMessage.success('已识别书籍信息，请核对后继续')
  suggest()
}

async function suggest() {
  aiLoading.value = true
  try {
    suggestion.value = await api.priceSuggestion({
      isbn: form.isbn,
      category: form.category,
      originalPrice: form.originalPrice,
      conditionLevel: form.conditionLevel,
      title: form.title
    })
    if (!form.price) form.price = suggestion.value.suggestedPrice
  } finally {
    aiLoading.value = false
  }
}

async function genDesc() {
  const r = await api.genDescription({
    title: form.title || '这本书',
    conditionLevel: String(form.conditionLevel),
    note: '有少量重点标注'
  })
  form.description = r.description
  ElMessage.success('已生成卖点文案，可自行修改')
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const created = await api.publishBook({ ...form })
    ElMessage.success('发布成功')
    router.push(`/book/${created.id}`)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const m = await api.bookMeta()
  meta.categories = m.categories
})
</script>

<template>
  <div class="page publish-page">
    <el-card shadow="never" class="publish-card">
      <template #header>
        <div class="head">
          <span class="card-title" style="margin: 0">发布我的书籍</span>
          <span class="muted">填写越完整，成交越快。AI 会根据成色与行情给出定价建议</span>
        </div>
      </template>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 18px">
        <template #title>
          <div class="isbn-row">
            <span>扫码 / 输入 ISBN 自动填充书籍信息：</span>
            <el-input v-model="isbn" placeholder="如 9787040498258" style="width: 220px" size="small" />
            <el-button size="small" type="primary" plain @click="lookup">AI 识别</el-button>
          </div>
        </template>
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="form.title" placeholder="如：数据结构（C语言版）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者">
              <el-input v-model="form.author" placeholder="如：严蔚敏" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社">
              <el-input v-model="form.publisher" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" style="width: 100%" @change="suggest">
                <el-option v-for="c in meta.categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" @change="suggest" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色">
              <el-slider v-model="form.conditionLevel" :min="1" :max="5" :marks="conditionMarks" show-stops @change="suggest" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面图">
          <el-input v-model="form.coverUrl" placeholder="图片 URL，可留空自动使用识别结果" />
        </el-form-item>

        <el-form-item label="售价" prop="price">
          <div class="price-row">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" style="width: 180px" />
            <el-button type="primary" plain :loading="aiLoading" @click="suggest">
              <el-icon><MagicStick /></el-icon> AI 智能定价
            </el-button>
            <span v-if="suggestion" class="muted">
              建议 ¥{{ suggestion.suggestedPrice }}（区间 ¥{{ suggestion.lowBound }} ~ ¥{{ suggestion.highBound }}，置信度
              {{ Math.round(suggestion.confidence * 100) }}%）
            </span>
          </div>
        </el-form-item>

        <el-form-item v-if="suggestion" label="定价依据">
          <el-alert type="warning" :closable="false">
            <template #title>
              <span style="font-size: 13px; line-height: 1.7">{{ suggestion.reason }}</span>
            </template>
          </el-alert>
        </el-form-item>

        <el-form-item label="书籍描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述成色、笔记情况、是否含赠品等" />
          <el-button text type="primary" style="margin-top: 6px" @click="genDesc">
            <el-icon><MagicStick /></el-icon> AI 生成卖点文案
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="submit">立即发布</el-button>
          <el-button size="large" @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.publish-page {
  max-width: 900px;
}
.publish-card {
  border-radius: 14px;
}
.head {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.isbn-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.price-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
