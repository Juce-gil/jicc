<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['聊天抽查与敏感词管理页已预留', '后续可接消息抽样、敏感词规则与会话查看', '适合作为后台扩展亮点']
const apis = ['POST /message/query', '待补：敏感词管理接口']
const nextSteps = ['先接旧消息查询', '再切到会话模型', '补敏感词规则与拦截日志']
</script>

<template>
  <FeaturePlaceholder
    title="聊天抽查 / 敏感词管理"
    description="当前先预留后台治理入口，后续配合 WebSocket 聊天模块一起补齐。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
