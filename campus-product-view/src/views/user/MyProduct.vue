<template>
  <div class="my-product-page">
    <UserPageSection
      eyebrow="My goods"
      title="我的商品"
      description="集中管理你发布的校园闲置，支持快速查看、编辑与删除。"
    >
      <template #actions>
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="$router.push('/post-product')"
          >发布商品</el-button
        >
      </template>

      <UserProductGrid
        :items="productList"
        empty-text="你还没有发布商品"
        @select="route"
      >
        <template #summary="{ item }">
          <div class="summary-row">
            <span>{{ (item.likeNumber || 0) + " 人想要" }}</span>
            <span class="summary-divider"></span>
            <span>库存 {{ item.inventory || 0 }}</span>
          </div>
        </template>

        <template #actions="{ item }">
          <div class="action-row">
            <button
              type="button"
              class="action-btn primary"
              @click.stop="handleEdit(item)"
            >
              编辑
            </button>
            <button
              type="button"
              class="action-btn danger"
              @click.stop="handleDelete(item)"
            >
              删除
            </button>
          </div>
        </template>
      </UserProductGrid>
    </UserPageSection>
  </div>
</template>

<script>
import UserPageSection from "@/components/user/UserPageSection.vue";
import UserProductGrid from "@/components/user/UserProductGrid.vue";
import { setProductInfo } from "@/utils/storage";

export default {
  name: "MyProduct",
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
    handleEdit(product) {
      setProductInfo(product);
      this.$router.push("/edit-product");
    },
    async handleDelete(product) {
      const confirmed = await this.$swalConfirm({
        title: `删除「${product.name || "商品"}」`,
        text: "删除后不可恢复，确定继续吗？",
        icon: "warning"
      });

      if (!confirmed) return;

      try {
        const response = await this.$axios.post("/product/batchDelete", [
          product.id
        ]);
        if (response.data.code === 200) {
          this.$notify({
            duration: 1200,
            title: "删除成功",
            message: response.data.msg || "商品已删除",
            type: "success"
          });
          this.fetchProduct();
        }
      } catch (error) {
        this.$notify({
          duration: 2000,
          title: "删除失败",
          message: error,
          type: "error"
        });
        console.error("商品删除异常：", error);
      }
    },
    fetchProduct() {
      this.$axios
        .post("/product/queryUser", {})
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            this.productList = data.data || [];
          } else {
            this.productList = [];
          }
        })
        .catch(error => {
          console.log("商品查询异常：", error);
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

.action-row {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  min-height: 40px;
  border: none;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.primary {
  background: #eff6ff;
  color: #2563eb;
}

.action-btn.primary:hover {
  background: #dbeafe;
}

.action-btn.danger {
  background: #fef2f2;
  color: #dc2626;
}

.action-btn.danger:hover {
  background: #fee2e2;
}
</style>
