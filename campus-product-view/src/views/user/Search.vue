<template>
  <div class="search-page">
    <UserPageSection
      eyebrow="Search"
      title="搜索结果"
      :description="searchDescription"
    >
      <template #actions>
        <div class="keyword-badge">
          <span>关键词</span>
          <strong>{{ searchKey || "未输入" }}</strong>
        </div>
      </template>

      <UserProductGrid
        :items="productList"
        :empty-text="searchKey ? '没有找到相关商品' : '请输入关键词开始搜索'"
        :show-publisher="true"
        @select="route"
      >
        <template #summary="{ item }">
          <div class="summary-row">
            <span>搜索命中商品</span>
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
import { getSearchKey } from "@/utils/storage";

export default {
  name: "Search",
  components: {
    UserPageSection,
    UserProductGrid
  },
  data() {
    return {
      searchKey: "",
      keyInterval: null,
      productQueryDto: {},
      productList: []
    };
  },
  computed: {
    searchDescription() {
      if (!this.searchKey) {
        return "在顶部搜索框输入关键词后，这里会展示相关商品。";
      }
      return `当前正在为你展示与「${this.searchKey}」相关的商品结果。`;
    }
  },
  created() {
    this.loadKey();
    this.startKeyLoader();
  },
  beforeDestroy() {
    this.clearKeyLoader();
  },
  methods: {
    route(product) {
      this.$router.push({ path: "/product/detail", query: { id: product.id } });
    },
    fetchProduct() {
      this.productQueryDto.name = this.searchKey || undefined;
      this.$axios
        .post("/product/query", this.productQueryDto)
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            this.productList = data.data || [];
          } else {
            this.productList = [];
          }
        })
        .catch(error => {
          console.log("搜索商品异常：", error);
          this.productList = [];
        });
    },
    loadKey() {
      const key = getSearchKey() || "";
      if (key !== this.searchKey) {
        this.searchKey = key;
        this.fetchProduct();
      }
    },
    startKeyLoader() {
      this.keyInterval = setInterval(() => {
        this.loadKey();
      }, 800);
    },
    clearKeyLoader() {
      if (this.keyInterval) {
        clearInterval(this.keyInterval);
        this.keyInterval = null;
      }
    }
  }
};
</script>

<style scoped lang="scss">
.keyword-badge {
  min-width: 120px;
  padding: 10px 14px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff6ff, #f8fafc);
  text-align: right;
}

.keyword-badge span {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.keyword-badge strong {
  display: block;
  margin-top: 4px;
  color: #0f172a;
  font-size: 16px;
}

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
