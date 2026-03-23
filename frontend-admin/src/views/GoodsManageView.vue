<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['商品管理列表已预留', '可继续支持编辑/下架/删除', '可联动分类管理与举报处理']
const apis = ['POST /product/query', 'PUT /product/update', 'POST /product/batchDelete', 'POST /category/query']
const nextSteps = ['补商品状态标签', '支持一键下架', '联动举报处理结果']
</script>

<template>
  <FeaturePlaceholder
    title="商品管理"
    description="面向后台管理员的全量商品管理入口，可配合审核、举报、上下架操作展示系统完整度。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
