<script setup lang="ts">
const panels = [
  { title: '审核流', desc: '校园认证审核、商品审核、举报处理是最适合答辩展示的后台亮点。', to: '/goods-review' },
  { title: '交易闭环', desc: '订单管理、聊天抽查、公告管理可形成后台治理能力。', to: '/orders' },
  { title: '统计展示', desc: '仪表盘和统计报表适合接入 ECharts，强化“系统完整度”。', to: '/stats' }
]

const milestones = [
  '先接 dashboard / user / product / orders / operation-log 现有接口',
  '再补 campus_auth / report / notice 后台新模块',
  '最后接图表、导出、审核记录和管理员日志'
]
</script>

<template>
  <section class="dashboard-view panel">
    <div>
      <h2>管理后台首页</h2>
      <p class="muted">当前页面作为新后台工程的仪表盘入口，后续适合接统计卡片、待审核数量、商品趋势图与快捷操作。</p>
    </div>

    <div class="dashboard-grid">
      <RouterLink v-for="item in panels" :key="item.title" :to="item.to" class="dashboard-card">
        <h3>{{ item.title }}</h3>
        <p class="muted">{{ item.desc }}</p>
      </RouterLink>
    </div>

    <div class="panel inner-panel">
      <h3>推荐实施顺序</h3>
      <ol>
        <li v-for="item in milestones" :key="item">{{ item }}</li>
      </ol>
    </div>
  </section>
</template>

<style scoped>
.dashboard-view {
  display: grid;
  gap: 18px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.dashboard-card {
  display: block;
  border-radius: 18px;
  padding: 18px;
  background: linear-gradient(180deg, #ecfeff 0%, #ffffff 100%);
  border: 1px solid #ccfbf1;
}

.dashboard-card h3 {
  margin: 0 0 10px;
}

.inner-panel {
  background: #f8fafc;
}

.inner-panel ol {
  margin: 0;
  padding-left: 20px;
  display: grid;
  gap: 10px;
}
</style>
