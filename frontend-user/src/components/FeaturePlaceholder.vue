<script setup lang="ts">
interface Props {
  title: string
  description: string
  status?: string
  features?: string[]
  apis?: string[]
  nextSteps?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  status: '页面骨架已创建，等待接入真实接口与业务数据。',
  features: () => [],
  apis: () => [],
  nextSteps: () => []
})
</script>

<template>
  <section class="feature-page panel">
    <div class="feature-header">
      <div>
        <h2>{{ props.title }}</h2>
        <p class="muted">{{ props.description }}</p>
      </div>
      <span class="feature-status">{{ props.status }}</span>
    </div>

    <div class="feature-grid">
      <div class="feature-block">
        <h3>页面规划点</h3>
        <ul>
          <li v-for="item in props.features" :key="item">{{ item }}</li>
        </ul>
      </div>

      <div class="feature-block">
        <h3>已预留接口</h3>
        <ul>
          <li v-for="item in props.apis" :key="item">{{ item }}</li>
        </ul>
      </div>

      <div class="feature-block">
        <h3>下一步联调</h3>
        <ul>
          <li v-for="item in props.nextSteps" :key="item">{{ item }}</li>
        </ul>
      </div>
    </div>
  </section>
</template>

<style scoped>
.feature-page {
  display: grid;
  gap: 20px;
}

.feature-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.feature-header h2 {
  margin: 0 0 8px;
}

.feature-status {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: #dcfce7;
  color: #166534;
  font-size: 13px;
  white-space: nowrap;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.feature-block {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  background: #f8fafc;
}

.feature-block h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.feature-block ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  color: #475569;
}

@media (max-width: 768px) {
  .feature-header {
    flex-direction: column;
  }
}
</style>
