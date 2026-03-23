<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['统计报表页已预留', '适合接入 ECharts 图表', '可展示用户、商品、订单、审核趋势']
const apis = ['GET /dashboard/staticCount', 'GET /dashboard/productShelvesInfo/{day}']
const nextSteps = ['先接统计卡片', '再接折线图/柱状图', '补热门分类与成交趋势']
</script>

<template>
  <FeaturePlaceholder
    title="统计报表"
    description="仪表盘与报表页适合提升项目完整度，也是毕业设计展示中最直观的页面之一。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
