<script setup lang="ts">
const quickCards = [
  { title: '商品浏览', desc: '首页、分类、搜索、商品详情已经按学生端路线拆开。', to: '/search' },
  { title: '交易闭环', desc: '已预留发布商品、我的订单、聊天、校园认证等模块入口。', to: '/my/orders' },
  { title: '兼容旧后端', desc: '登录、注册、商品、收藏、订单接口封装已按当前后端可用接口预留。', to: '/publish' }
]

const milestones = [
  '先接通商品列表 / 商品详情 / 发布商品 / 我的订单',
  '再把支付式下单逻辑改造成预约面交逻辑',
  '最后补校园认证、举报处理、聊天与公告模块'
]
</script>

<template>
  <section class="home-view panel">
    <div>
      <h2>学生端首页骨架</h2>
      <p class="muted">
        当前页面已经从旧的单体 Vue2 工程中剥离出来，成为独立的 Vue3 学生端项目，后续可以直接在这里做真实业务实现。
      </p>
    </div>

    <div class="home-grid">
      <RouterLink v-for="card in quickCards" :key="card.title" :to="card.to" class="home-card">
        <h3>{{ card.title }}</h3>
        <p class="muted">{{ card.desc }}</p>
      </RouterLink>
    </div>

    <div class="panel inner-panel">
      <h3>当前推荐实施顺序</h3>
      <ol>
        <li v-for="item in milestones" :key="item">{{ item }}</li>
      </ol>
    </div>
  </section>
</template>

<style scoped>
.home-view {
  display: grid;
  gap: 18px;
}

.home-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.home-card {
  display: block;
  border-radius: 18px;
  padding: 18px;
  background: linear-gradient(180deg, #eff6ff 0%, #ffffff 100%);
  border: 1px solid #dbeafe;
}

.home-card h3 {
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
