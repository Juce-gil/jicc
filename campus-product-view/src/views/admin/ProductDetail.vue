<template>
  <div class="admin-product-detail">
    <el-card v-loading="loading" shadow="never" class="detail-card">
      <div class="page-head">
        <div>
          <h2>商品详情</h2>
          <p>查看商品展示信息、卖家信息与当前交易状态。</p>
        </div>

        <div class="page-actions">
          <el-button @click="routeBack">返回商品管理</el-button>
          <el-button type="primary" @click="goOrdersCenter">
            前往订单管理
          </el-button>
        </div>
      </div>

      <el-empty
        v-if="!loading && !product.id"
        description="商品不存在、已删除或查询失败"
      />

      <template v-else-if="product.id">
        <div class="detail-grid">
          <section class="media-panel">
            <el-image
              v-if="currentCover"
              :src="currentCover"
              fit="contain"
              class="main-cover"
            >
              <div slot="error" class="main-cover placeholder">加载失败</div>
            </el-image>

            <div v-else class="main-cover placeholder">暂无图片</div>

            <div v-if="coverList.length > 1" class="thumb-list">
              <button
                v-for="(cover, index) in coverList"
                :key="cover + index"
                type="button"
                class="thumb-btn"
                :class="{ active: activeCoverIndex === index }"
                @click="activeCoverIndex = index"
              >
                <img :src="cover" alt="thumb" />
              </button>
            </div>
          </section>

          <section class="info-panel">
            <div class="title-row">
              <div>
                <h1>{{ product.name || `商品 #${product.id}` }}</h1>
                <p class="sub-copy">
                  商品ID：{{ product.id || "-" }} · 发布于
                  {{ product.createTime || "暂无记录" }}
                </p>
              </div>

              <div class="tag-group">
                <el-tag :type="statusTagType" effect="plain">
                  {{ statusText }}
                </el-tag>
                <el-tag type="info" effect="plain">
                  {{ auditStatusText }}
                </el-tag>
              </div>
            </div>

            <div class="price-row">¥ {{ displayPrice }}</div>

            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">分类</span>
                <span>{{ product.categoryName || "-" }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">新旧程度</span>
                <span>{{
                  product.oldLevel != null ? `${product.oldLevel} / 9` : "-"
                }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">库存</span>
                <span>{{
                  product.inventory != null ? product.inventory : "-"
                }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">是否支持议价</span>
                <span>{{ product.isBargain ? "支持" : "不支持" }}</span>
              </div>
            </div>

            <div class="seller-card">
              <div class="card-title">卖家信息</div>
              <div class="seller-body">
                <el-avatar :size="52" :src="avatarUrl(product.userAvatar)" />
                <div>
                  <div class="seller-name">
                    {{ product.userName || `用户 #${product.userId || "-"}` }}
                  </div>
                  <div class="seller-meta">
                    卖家ID：{{ product.userId || "-" }}
                  </div>
                </div>
              </div>
            </div>

            <div class="stats-grid">
              <div class="stat-card">
                <span class="stat-label">想要人数</span>
                <strong>{{ product.likeNumber || 0 }}</strong>
              </div>
              <div class="stat-card">
                <span class="stat-label">收藏人数</span>
                <strong>{{ product.saveNumber || 0 }}</strong>
              </div>
              <div class="stat-card">
                <span class="stat-label">浏览次数</span>
                <strong>{{ product.viewNumber || 0 }}</strong>
              </div>
            </div>

            <div class="desc-card">
              <div class="card-title">商品描述</div>
              <p class="desc-text">{{ product.detail || "暂无描述" }}</p>
            </div>
          </section>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script>
import { toFullImageUrl } from "@/utils/imageUrl";

const PRODUCT_STATUS_ON_SALE = "ON_SALE";
const PRODUCT_STATUS_RESERVED = "RESERVED";
const PRODUCT_STATUS_SOLD = "SOLD";
const PRODUCT_STATUS_OFFLINE = "OFFLINE";
const PRODUCT_AUDIT_APPROVED = "APPROVED";

export default {
  name: "AdminProductDetail",
  data() {
    return {
      product: {},
      loading: false,
      activeCoverIndex: 0
    };
  },
  computed: {
    productId() {
      const id =
        this.$route.query.productId != null &&
        this.$route.query.productId !== ""
          ? this.$route.query.productId
          : this.$route.query.id;
      return id != null && id !== "" ? Number(id) : null;
    },
    coverList() {
      const raw = this.product.coverList ? String(this.product.coverList) : "";
      return raw
        .split(",")
        .map(item => item.trim())
        .filter(Boolean)
        .map(item => toFullImageUrl(item));
    },
    currentCover() {
      return this.coverList[this.activeCoverIndex] || "";
    },
    displayPrice() {
      return this.product.price != null ? this.product.price : "-";
    },
    statusText() {
      switch ((this.product.status || PRODUCT_STATUS_ON_SALE).toUpperCase()) {
        case PRODUCT_STATUS_RESERVED:
          return "已预约";
        case PRODUCT_STATUS_SOLD:
          return "已售出";
        case PRODUCT_STATUS_OFFLINE:
          return "已下架";
        default:
          return "在售中";
      }
    },
    statusTagType() {
      switch ((this.product.status || PRODUCT_STATUS_ON_SALE).toUpperCase()) {
        case PRODUCT_STATUS_RESERVED:
          return "warning";
        case PRODUCT_STATUS_SOLD:
          return "danger";
        case PRODUCT_STATUS_OFFLINE:
          return "info";
        default:
          return "success";
      }
    },
    auditStatusText() {
      const auditStatus = (
        this.product.auditStatus || PRODUCT_AUDIT_APPROVED
      ).toUpperCase();
      return auditStatus === PRODUCT_AUDIT_APPROVED
        ? "审核通过"
        : `审核状态：${auditStatus}`;
    }
  },
  watch: {
    productId() {
      this.fetchDetail();
    },
    coverList() {
      this.activeCoverIndex = 0;
    }
  },
  created() {
    this.fetchDetail();
  },
  methods: {
    avatarUrl(url) {
      return toFullImageUrl(url || "");
    },
    fetchDetail() {
      if (this.productId == null || Number.isNaN(this.productId)) {
        this.product = {};
        this.activeCoverIndex = 0;
        return;
      }

      this.loading = true;
      this.$axios
        .post("/product/query", {
          id: this.productId,
          current: 1,
          size: 1
        })
        .then(res => {
          const { data } = res;
          if (
            data &&
            data.code === 200 &&
            Array.isArray(data.data) &&
            data.data.length
          ) {
            this.product = data.data[0];
            this.activeCoverIndex = 0;
          } else {
            this.product = {};
          }
        })
        .catch(error => {
          console.error("管理端商品详情查询异常：", error);
          this.product = {};
        })
        .finally(() => {
          this.loading = false;
        });
    },
    routeBack() {
      this.$router.push("/ProductManage");
    },
    goOrdersCenter() {
      this.$router.push("/ordersManage");
    }
  }
};
</script>

<style scoped lang="scss">
.admin-product-detail {
  padding: 16px 6px 24px;
}

.detail-card {
  border: 1px solid #ebeef5;
  border-radius: 18px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 22px;
}

.page-head h2 {
  margin: 0;
  color: #111827;
  font-size: 24px;
}

.page-head p {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.page-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 28px;
}

.media-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.main-cover {
  width: 100%;
  height: 420px;
  border-radius: 20px;
  background: #f8fafc;
}

.main-cover.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.thumb-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.thumb-btn {
  width: 84px;
  height: 84px;
  padding: 0;
  overflow: hidden;
  border: 2px solid transparent;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
}

.thumb-btn.active {
  border-color: #22c55e;
}

.thumb-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-panel {
  min-width: 0;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.title-row h1 {
  margin: 0;
  color: #111827;
  font-size: 32px;
  line-height: 1.3;
}

.sub-copy {
  margin: 12px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.tag-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.price-row {
  margin-top: 20px;
  color: #f97316;
  font-size: 38px;
  font-weight: 800;
}

.info-grid,
.stats-grid {
  display: grid;
  gap: 14px;
  margin-top: 22px;
}

.info-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.stats-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.info-item,
.stat-card,
.seller-card,
.desc-card {
  padding: 16px 18px;
  border: 1px solid #ebeef5;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff, #f8fbff);
}

.info-label,
.stat-label {
  display: block;
  color: #94a3b8;
  font-size: 12px;
}

.info-item span:last-child,
.seller-name {
  display: block;
  margin-top: 8px;
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.seller-body {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
}

.seller-meta {
  margin-top: 6px;
  color: #6b7280;
  font-size: 12px;
}

.card-title {
  color: #111827;
  font-size: 15px;
  font-weight: 700;
}

.stat-card strong {
  display: block;
  margin-top: 10px;
  color: #111827;
  font-size: 26px;
  font-weight: 800;
}

.desc-text {
  margin: 12px 0 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 1200px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-product-detail {
    padding-left: 0;
    padding-right: 0;
  }

  .page-head,
  .title-row {
    flex-direction: column;
  }

  .info-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .main-cover {
    height: 320px;
  }
}
</style>
