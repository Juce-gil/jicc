<template>
  <div class="my-save-page">
    <UserPageSection
      eyebrow="Favorites"
      title="我的收藏"
      description="把感兴趣的商品先收藏起来，方便之后继续比较和下单。"
    >
      <UserProductGrid
        :items="productList"
        empty-text="还没有收藏商品"
        @select="route"
      >
        <template #summary="{ item }">
          <div class="summary-row">
            <span>收藏后可随时回看</span>
            <span class="summary-divider"></span>
            <span>价格 ¥ {{ item.price != null ? item.price : "-" }}</span>
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
  name: "MySave",
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
    route(product) {
      this.$router.push({ path: "/product/detail", query: { id: product.id } });
    },
    fetchProduct() {
      this.$axios
        .post("/interaction/queryUser")
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            this.productList = data.data || [];
          } else {
            this.productList = [];
          }
        })
        .catch(error => {
          console.log("收藏商品查询异常：", error);
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
