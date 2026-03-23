<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = ['用户分页列表已预留', '可继续补封禁/解封与信用分管理', '适合对接管理员编辑用户信息']
const apis = ['POST /user/query', 'POST /user/insert', 'PUT /user/backUpdate']
const nextSteps = ['对接用户列表', '增加用户状态操作', '补信用分与认证状态列']
</script>

<template>
  <FeaturePlaceholder
    title="用户管理"
    description="用于查看平台用户、封禁异常账号、调整基础资料与角色。"
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
