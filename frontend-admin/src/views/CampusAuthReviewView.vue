<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['校园认证审核模块已预留', '可补通过/驳回/查看材料流程', '适合展示实名认证与校园信任机制']
const apis = ['待补：校园认证列表接口', '待补：校园认证审核接口']
const nextSteps = ['先补后端 campus_auth 审核模块', '再增加审核列表', '补材料预览与驳回原因']
</script>

<template>
  <FeaturePlaceholder
    title="校园认证审核"
    description="这是计划里最重要的后台亮点之一，建议优先补成可演示模块。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
