<template>
  <div class="my-view-page">
    <UserPageSection
      eyebrow="History"
      title="浏览足迹"
      description="回看最近浏览过的商品，快速接续你上次的选择。"
    >
      <template #actions>
        <el-button plain icon="el-icon-delete" @click="clearView"
          >清空足迹</el-button
        >
      </template>

      <UserProductGrid
        :items="productList"
        empty-text="最近还没有浏览记录"
        @select="route"
      >
        <template #summary="{ item }">
          <div class="summary-row">
            <span>继续查看商品详情</span>
            <span class="summary-divider"></span>
            <span>库存 {{ item.inventory || 0 }}</span>
          </div>
        </template>
      </UserProductGrid>
    </UserPageSection>
  </div>
</template>

<script>
import UserPageSection from "@/components/user/UserPageSection.vue";
import UserProductGrid from "@/components/user/UserProductGrid.vue";

export default {
  name: "MyView",
  components: {
    UserPageSection,
    UserProductGrid
  },
  data() {
    return {
      productList: []
    };
  },
  created() {
    this.fetchProduct();
  },
  methods: {
    clearView() {
      this.$axios
        .post("/interaction/batchDeleteView")
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            this.$notify({
              duration: 1000,
              title: "足迹已清空",
              message: data.msg,
              type: "success"
            });
            this.fetchProduct();
          }
        })
        .catch(error => {
          console.log("浏览足迹清空异常：", error);
        });
    },
    route(product) {
      this.$router.push({ path: "/product/detail", query: { id: product.id } });
    },
    fetchProduct() {
      this.$axios
        .post("/interaction/myView")
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            this.productList = data.data || [];
          } else {
            this.productList = [];
          }
        })
        .catch(error => {
          console.log("浏览足迹查询异常：", error);
          this.productList = [];
        });
    }
  }
};
</script>

<style scoped lang="scss">
.summary-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.summary-divider {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #cbd5e1;
}
</style>
