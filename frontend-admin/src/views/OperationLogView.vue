<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['管理员操作日志页已预留', '可继续增加按模块/时间筛选', '适合配合审核、封禁、公告发布操作展示审计能力']
const apis = ['POST /operation-log/query']
const nextSteps = ['先接日志列表', '增加模块筛选', '补详情查看与导出']
</script>

<template>
  <FeaturePlaceholder
    title="操作日志"
    description="用于沉淀管理员操作审计记录，提升系统可维护性与可信度。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
