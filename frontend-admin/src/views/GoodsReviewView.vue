<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['商品审核列表已预留', '可补审核状态筛选', '可对接通过/驳回与原因填写']
const apis = ['POST /product/query', 'PUT /product/update']
const nextSteps = ['补商品审核状态字段', '后台增加通过/驳回操作', '接入商品详情抽屉']
</script>

<template>
  <FeaturePlaceholder
    title="商品审核"
    description="负责商品上架前审核，是校园二手交易平台后台的核心治理能力。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
