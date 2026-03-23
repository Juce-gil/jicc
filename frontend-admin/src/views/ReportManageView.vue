<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['举报处理模块已预留', '可补举报详情、证据查看、处理结果', '适合体现平台风控能力']
const apis = ['待补：举报列表接口', '待补：举报处理接口']
const nextSteps = ['先补 goods_report 后端模块', '再完成处理流程', '联动商品下架与用户处罚']
</script>

<template>
  <FeaturePlaceholder
    title="举报处理"
    description="用于处理违规商品与异常行为，是管理后台的重要治理模块。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
