<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  '已预留认证材料区',
  '已预留审核状态区',
  '适合作为毕业设计亮点模块优先实现'
]

const apis = ['待补：校园认证提交接口', '待补：校园认证查询接口']
const nextSteps = ['先补后端 campus_auth 模块', '再完成前端上传表单', '联调审核通过/驳回状态']
</script>

<template>
  <FeaturePlaceholder
    title="校园认证页"
    description="这是计划中新增的重要模块，用于提交校园身份材料并等待管理员审核。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
