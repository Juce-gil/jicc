<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  '收藏页结构已预留',
  '可继续增加浏览历史区块',
  '可支持一键取消收藏'
]

const apis = ['POST /interaction/queryUser', 'POST /interaction/myView', 'POST /interaction/saveOperation/{id}']
const nextSteps = ['接入收藏列表', '增加“浏览记录”标签页', '支持取消收藏']
</script>

<template>
  <FeaturePlaceholder
    title="我的收藏"
    description="展示用户收藏的商品，并可继续扩展浏览记录。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
