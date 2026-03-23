<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  '我的商品列表已预留',
  '可对接商品状态标签',
  '可继续增加“查看订单”聚合入口'
]

const apis = ['POST /product/queryUser', 'PUT /product/update', 'POST /product/batchDelete']
const nextSteps = ['对接我的商品列表', '支持编辑商品', '支持按状态筛选']
</script>

<template>
  <FeaturePlaceholder
    title="我的发布"
    description="展示当前用户发布的商品，并提供编辑、下架、查看订单等入口。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
