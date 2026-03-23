<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  '站内消息列表已预留',
  '可继续拆会话列表与消息明细',
  '后续可接入 WebSocket 和未读数'
]

const apis = ['POST /message/queryUser', 'POST /message/setRead']
const nextSteps = ['先接旧消息接口', '再改为会话模型', '最后切换 WebSocket']
</script>

<template>
  <FeaturePlaceholder
    title="聊天页"
    description="当前先保留消息列表与会话入口骨架，后续再替换为 WebSocket 实时聊天。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
