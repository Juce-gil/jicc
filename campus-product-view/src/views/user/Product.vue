<template>
  <div class="user-product-page">
    <UserPageSection
      eyebrow="Marketplace"
      title="逛商品"
      description="按分类、议价状态与关键词快速筛选，找到更适合你的校园好物。"
    >
      <template #actions>
        <div class="page-stat">
          <span>当前结果</span>
          <strong>{{ total || productList.length }}</strong>
        </div>
      </template>

      <div class="filter-panel">
        <el-row :gutter="12" class="filter-row">
          <el-col :xs="24" :sm="12" :md="6">
            <el-select
              v-model="query.isBargain"
              placeholder="议价方式"
              clearable
              class="full-width"
            >
              <el-option label="全部" :value="null"></el-option>
              <el-option label="可议价" :value="true"></el-option>
              <el-option label="一口价" :value="false"></el-option>
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-select
              v-model="query.categoryId"
              placeholder="商品分类"
              clearable
              class="full-width"
            >
              <el-option
                v-for="item in categoryList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <el-input
              v-model="query.name"
              placeholder="搜索商品名称"
              clearable
              class="full-width"
              @keyup.enter.native="handleFilter"
            />
          </el-col>
          <el-col :xs="24" :sm="24" :md="4">
            <div class="filter-actions">
              <el-button
                type="primary"
                icon="el-icon-search"
                @click="handleFilter"
                >查询</el-button
              >
              <el-button @click="handleFilterClear">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <div v-if="loading" class="loading-wrap">
        <i class="el-icon-loading"></i>
        <span>正在加载商品列表...</span>
      </div>

      <UserProductGrid
        v-else
        :items="productList"
        empty-text="暂时没有符合条件的商品"
        :show-publisher="true"
        @select="goDetail"
      >
        <template #summary="{ item }">
          <div class="summary-row">
            <span>{{ getCategoryName(item.categoryId) }}</span>
            <span class="summary-divider"></span>
            <span>新旧 {{ item.oldLevel || "-" }}/9</span>
          </div>
        </template>
      </UserProductGrid>

      <el-pagination
        v-if="total > 0"
        class="pagination"
        :current-page="currentPage"
        :page-sizes="[12, 24, 48]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </UserPageSection>
  </div>
</template>

<script>
import UserPageSection from "@/components/user/UserPageSection.vue";
import UserProductGrid from "@/components/user/UserProductGrid.vue";

export default {
  name: "Product",
  components: {
    UserPageSection,
    UserProductGrid
  },
  data() {
    return {
      query: {
        name: "",
        categoryId: null,
        isBargain: null
      },
      productList: [],
      categoryList: [],
      currentPage: 1,
      pageSize: 12,
      total: 0,
      loading: false
    };
  },
  created() {
    this.fetchCategoryList();
    this.fetchProductList();
  },
  methods: {
    getCategoryName(categoryId) {
      if (categoryId == null) return "校园闲置";
      const item = this.categoryList.find(c => c.id === categoryId);
      return item ? item.name : String(categoryId);
    },
    fetchCategoryList() {
      this.$axios
        .post("/category/query", { size: 200 })
        .then(res => {
          const { data } = res;
          if (data && data.code === 200 && Array.isArray(data.data)) {
            this.categoryList = (data.data || []).filter(
              item => item.isUse === true || item.isUse === 1
            );
          } else {
            this.categoryList = [];
          }
        })
        .catch(() => {
          this.categoryList = [];
        });
    },
    fetchProductList() {
      this.loading = true;
      const body = {
        name: this.query.name || undefined,
        current: this.currentPage,
        size: this.pageSize
      };

      if (this.query.categoryId != null && this.query.categoryId !== "") {
        body.categoryId = Number(this.query.categoryId);
      }

      if (this.query.isBargain != null && this.query.isBargain !== "") {
        body.isBargain =
          this.query.isBargain === true || this.query.isBargain === 1 ? 1 : 0;
      }

      this.$axios
        .post("/product/query", body)
        .then(res => {
          const { data } = res;
          if (data && data.code === 200) {
            this.productList = data.data || [];
            this.total =
              data.total != null ? data.total : this.productList.length;
          } else {
            this.productList = [];
            this.total = 0;
          }
        })
        .catch(() => {
          this.productList = [];
          this.total = 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleFilter() {
      this.currentPage = 1;
      this.fetchProductList();
    },
    handleFilterClear() {
      this.query.name = "";
      this.query.categoryId = null;
      this.query.isBargain = null;
      this.currentPage = 1;
      this.fetchProductList();
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.fetchProductList();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.fetchProductList();
    },
    goDetail(item) {
      if (item && item.id != null) {
        this.$router.push({ path: "/product/detail", query: { id: item.id } });
      }
    }
  }
};
</script>

<style scoped lang="scss">
.filter-panel {
  padding: 18px;
  margin-bottom: 22px;
  border-radius: 22px;
  background: linear-gradient(135deg, #f8fbff, #f8fffb);
  border: 1px solid rgba(226, 232, 240, 0.85);
}

.filter-row {
  display: flex;
  align-items: center;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.full-width {
  width: 100%;
}

.page-stat {
  min-width: 110px;
  padding: 10px 14px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff6ff, #f0fdf4);
  color: #475569;
  text-align: right;
}

.page-stat span {
  display: block;
  font-size: 12px;
}

.page-stat strong {
  display: block;
  margin-top: 4px;
  color: #0f172a;
  font-size: 22px;
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

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 220px;
  color: #64748b;
}

.pagination {
  margin-top: 14px;
  text-align: right;
}

@media (max-width: 768px) {
  .filter-panel {
    padding: 16px;
  }

  .filter-actions {
    justify-content: flex-start;
    margin-top: 8px;
  }

  .pagination {
    text-align: left;
  }
}
</style>
