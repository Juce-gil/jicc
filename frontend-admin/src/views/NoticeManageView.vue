<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['公告管理页已预留', '可补发布/下线/草稿三态', '适合承接系统消息与首页公告']
const apis = ['待补：公告列表接口', '待补：公告新增/发布接口']
const nextSteps = ['先补 sys_notice 后端模块', '再接公告富文本与状态切换', '联动学生端首页公告展示']
</script>

<template>
  <FeaturePlaceholder
    title="公告管理"
    description="用于发布系统通知、维护活动公告与站内提醒，是答辩中很加分的后台模块。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
