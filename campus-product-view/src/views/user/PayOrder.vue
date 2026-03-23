<template>
  <div class="legacy-compat">
    <el-empty description="旧版下单页已升级，正在跳转到新的商品预约页..." />
  </div>
</template>

<script>
import { getProductInfo } from "@/utils/storage";

export default {
  name: "LegacyPayOrder",
  created() {
    const productInfo = getProductInfo() || {};
    const productId =
      this.$route.query.productId ||
      this.$route.query.id ||
      productInfo.id ||
      productInfo.productId;

    if (!productId) {
      this.$router.replace("/product").catch(() => {});
      return;
    }

    this.$router
      .replace({
        path: "/product/detail",
        query: {
          ...this.$route.query,
          id: productId,
          openOrder: 1
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
