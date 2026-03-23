<template>
  <div class="legacy-compat">
    <el-empty description="旧版买入订单页已升级，正在跳转到新的订单中心..." />
  </div>
</template>

<script>
export default {
  name: "LegacyBuyOrders",
  created() {
    this.$router
      .replace({
        path: "/orders",
        query: {
          ...this.$route.query,
          mode: "buyer"
        }
      })
      .catch(() => {});
  }
};
</script>

<style scoped>
.legacy-compat {
  padding: 32px 0;
}
</style>
