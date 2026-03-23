<script setup lang="ts">
import FeaturePlaceholder from '@/components/FeaturePlaceholder.vue'

const features = [
  'Product detail module is planned',
  'Favorite / browse / want interactions are reserved',
  'Ready for reservation launch and meetup guidance'
]

const apis = [
  'POST /product/query',
  'POST /interaction/view/{id}',
  'POST /interaction/saveOperation/{id}',
  'POST /product/buyProduct'
]

const nextSteps = ['Load detail by product id', 'Record a view on detail entry', 'Add a reservation request modal']
</script>

<template>
  <FeaturePlaceholder
    title="Product Detail"
    description="Focuses on gallery, seller info, favorite actions, reservation entry and chat entry."
    :features="features"
    :apis="apis"
    :next-steps="nextSteps"
  />
</template>
