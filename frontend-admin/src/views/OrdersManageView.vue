<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['订单列表已预留', '后续可区分预约中/已确认/已完成/已取消', '适合联动商品状态与订单日志']
const apis = ['POST /orders/query']
const nextSteps = ['把旧支付式订单改造成预约式订单文案', '增加预约地点与时间', '补订单日志抽屉']
</script>

<template>
  <FeaturePlaceholder
    title="订单管理"
    description="后台统一查看订单流转，是把“预约面交”业务闭环讲清楚的重要页面。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
