<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  '支持关键词搜索设计',
  '可增加综合/最新/价格排序',
  '可承接标签筛选与历史搜索'
]

const apis = ['POST /product/query']
const nextSteps = ['接入搜索条件表单', '对接分页查询', '保留最近搜索记录']
</script>

<template>
  <FeaturePlaceholder
    title="搜索结果页"
    description="用于关键词搜索、价格筛选、排序和结果分页。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
