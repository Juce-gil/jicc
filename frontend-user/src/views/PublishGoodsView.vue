<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  '发布表单模块已预留',
  '后续可接入图片上传',
  '可在提交后提示“待审核”'
]

const apis = ['POST /product/save', 'PUT /product/update', 'POST /file/upload']
const nextSteps = ['完成表单字段与校验', '接入封面图上传', '发布成功后跳转我的商品']
</script>

<template>
  <FeaturePlaceholder
    title="发布商品页"
    description="承接商品标题、成色、价格、封面、描述等表单，后续再补图片上传与审核状态说明。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
