<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  '分类导航位已预留',
  '可继续对接分类商品列表',
  '后续可增加按校区/价格/成色筛选'
]

const apis = ['POST /category/query', 'POST /product/query']
const nextSteps = ['接入分类接口', '按分类拉取商品列表', '增加分类页骨架屏']
</script>

<template>
  <FeaturePlaceholder
    title="分类页"
    description="对应毕业设计中的商品分类浏览页面，适合继续拆成一级分类、推荐商品和筛选区。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
