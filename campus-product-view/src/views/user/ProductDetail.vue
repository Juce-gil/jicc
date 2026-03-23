<template>
  <div class="product-detail">
    <el-card v-loading="loading" shadow="never" class="detail-card">
      <div v-if="!loading && !product.id" class="empty">商品不存在或已下架</div>

      <template v-else-if="product.id">
        <el-row :gutter="32" class="detail-row">
          <el-col :span="12">
            <div class="image-area">
              <el-image
                v-if="coverUrl"
                :src="coverUrl"
                fit="contain"
                class="main-image"
              >
                <div slot="error" class="image-error">加载失败</div>
              </el-image>

              <div v-else class="image-placeholder">暂无图片</div>

              <span v-if="isBargain" class="tag-bargain">支持议价</span>
              <span class="tag-status" :class="saleStatusClass">
                {{ saleStatusText }}
              </span>
            </div>
          </el-col>

          <el-col :span="12">
            <div class="info-area">
              <div class="info-header">
                <h1 class="product-name">{{ product.name }}</h1>
                <el-button
                  :type="collected ? 'warning' : 'default'"
                  :icon="collected ? 'el-icon-star-on' : 'el-icon-star-off'"
                  class="btn-favorite"
                  @click="toggleFavorite"
                >
                  {{ collected ? "取消收藏" : "收藏" }}
                </el-button>
              </div>

              <div class="block block-publisher">
                <span class="section-label">发布者</span>
                <span class="publisher-name">{{ publisherName }}</span>
              </div>

              <div class="block block-price">
                <span class="section-label section-label-lg">价格</span>
                <span class="price">¥ {{ displayPrice }}</span>
              </div>

              <div class="block block-info">
                <span class="section-label section-label-lg">基础信息</span>

                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-item-label">分类</span>
                    <span>{{ categoryName }}</span>
                  </div>

                  <div class="info-item">
                    <span class="info-item-label">新旧程度</span>
                    <span>{{
                      product.oldLevel != null ? `${product.oldLevel} / 9` : "-"
                    }}</span>
                  </div>

                  <div class="info-item">
                    <span class="info-item-label">库存</span>
                    <span>{{ inventoryText }}</span>
                  </div>

                  <div class="info-item">
                    <span class="info-item-label">状态</span>
                    <span>{{ saleStatusText }}</span>
                  </div>

                  <div class="info-item info-item-bargain">
                    <span class="info-item-label">是否支持议价</span>
                    <span :class="{ 'bargain-yellow': isBargain }">{{
                      isBargain ? "支持" : "不支持"
                    }}</span>
                  </div>
                </div>
              </div>

              <div v-if="product.detail" class="block block-desc">
                <span class="section-label section-label-lg">商品描述</span>
                <p class="detail-text">{{ product.detail }}</p>
              </div>

              <div class="actions">
                <el-button
                  type="primary"
                  size="medium"
                  icon="el-icon-chat-dot-round"
                  @click="handleIWant"
                >
                  我想要
                </el-button>

                <el-button
                  type="danger"
                  size="medium"
                  icon="el-icon-goods"
                  :disabled="!canReserve"
                  @click="openOrderDialog"
                >
                  立即预约
                </el-button>
              </div>

              <p v-if="!canReserve" class="action-tip">
                {{ reserveDisabledReason }}
              </p>
            </div>
          </el-col>
        </el-row>
      </template>
    </el-card>

    <el-dialog
      title="预约商品"
      :visible.sync="orderDialogVisible"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="order-dialog-body">
        <div class="order-summary">
          <div class="order-title">{{ product.name }}</div>
          <div class="order-meta">
            <span class="order-price-label">单价：</span>
            <span class="order-price">¥ {{ displayPrice }}</span>
            <span class="order-stock">库存：{{ inventoryText }}</span>
          </div>
        </div>

        <div class="order-field">
          <span class="order-label">预约数量</span>
          <el-input-number
            v-model="orderQuantity"
            :min="1"
            :max="1"
            :disabled="true"
            label="预约数量"
          />
          <p class="order-tip">当前交易流程仅支持单件预约。</p>
        </div>

        <div class="order-field">
          <span class="order-label">备注信息</span>
          <el-input
            v-model.trim="orderRemark"
            type="textarea"
            :rows="3"
            placeholder="可以补充与卖家约时间、地点等信息（可选）"
          />
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="orderDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitOrder">确认预约</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { toFullImageUrl } from "@/utils/imageUrl";
import { getToken, getUserInfo } from "@/utils/storage";

const PRODUCT_STATUS_ON_SALE = "ON_SALE";
const PRODUCT_STATUS_RESERVED = "RESERVED";
const PRODUCT_STATUS_SOLD = "SOLD";
const PRODUCT_STATUS_OFFLINE = "OFFLINE";

export default {
  name: "ProductDetail",
  data() {
    return {
      product: {},
      categoryList: [],
      loading: false,
      collected: false,
      orderDialogVisible: false,
      orderQuantity: 1,
      orderRemark: "",
      viewedProductId: null
    };
  },
  computed: {
    productId() {
      const id =
        this.$route.query.id != null && this.$route.query.id !== ""
          ? this.$route.query.id
          : this.$route.query.productId;
      return id != null && id !== "" ? Number(id) : null;
    },
    currentUser() {
      return getUserInfo() || {};
    },
    coverUrl() {
      const coverList = this.product.coverList;
      const firstCover = coverList ? String(coverList).split(",")[0] : "";
      return toFullImageUrl(firstCover || "");
    },
    categoryName() {
      if (this.product.categoryName) {
        return this.product.categoryName;
      }
      const categoryId = this.product.categoryId;
      if (categoryId == null) {
        return "-";
      }
      const target = this.categoryList.find(
        item => Number(item.id) === Number(categoryId)
      );
      return target ? target.name : String(categoryId);
    },
    publisherName() {
      return (
        this.product.userName ||
        this.product.userNickName ||
        (this.product.userId != null ? String(this.product.userId) : "-")
      );
    },
    isBargain() {
      return this.product.isBargain === true || this.product.isBargain === 1;
    },
    displayPrice() {
      return this.product.price != null ? this.product.price : "-";
    },
    normalizedStatus() {
      return (this.product.status || PRODUCT_STATUS_ON_SALE).toUpperCase();
    },
    saleStatusText() {
      switch (this.normalizedStatus) {
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
    saleStatusClass() {
      switch (this.normalizedStatus) {
        case PRODUCT_STATUS_RESERVED:
          return "warning";
        case PRODUCT_STATUS_SOLD:
          return "danger";
        case PRODUCT_STATUS_OFFLINE:
          return "offline";
        default:
          return "success";
      }
    },
    inventoryText() {
      return this.product.inventory != null ? this.product.inventory : "-";
    },
    isOwner() {
      return (
        this.currentUser &&
        this.currentUser.id != null &&
        this.product.userId != null &&
        Number(this.currentUser.id) === Number(this.product.userId)
      );
    },
    canReserve() {
      return (
        !!this.product.id &&
        !this.isOwner &&
        this.normalizedStatus === PRODUCT_STATUS_ON_SALE &&
        Number(this.product.inventory || 0) > 0
      );
    },
    reserveDisabledReason() {
      if (!this.product.id) {
        return "";
      }
      if (this.isOwner) {
        return "这是你自己发布的商品，不能给自己下单。";
      }
      if (this.normalizedStatus === PRODUCT_STATUS_RESERVED) {
        return "该商品已被其他用户预约，暂时不能下单。";
      }
      if (this.normalizedStatus === PRODUCT_STATUS_SOLD) {
        return "该商品已售出。";
      }
      if (this.normalizedStatus === PRODUCT_STATUS_OFFLINE) {
        return "该商品已下架。";
      }
      if (Number(this.product.inventory || 0) <= 0) {
        return "该商品库存不足。";
      }
      return "";
    }
  },
  watch: {
    productId() {
      this.viewedProductId = null;
      this.fetchDetail();
      this.loadFavoriteStatus();
    },
    "$route.query.openOrder"(value) {
      if ((value === "1" || value === "true") && this.product.id) {
        this.tryOpenOrderDialogFromRoute();
      }
    }
  },
  created() {
    this.fetchCategoryList();
    this.fetchDetail();
    this.loadFavoriteStatus();
  },
  methods: {
    ensureLogin(actionText = "继续操作") {
      if (getToken()) {
        return true;
      }
      this.$message.info(`请先登录后再${actionText}`);
      this.$router.push({
        path: "/login",
        query: { redirect: this.$route.fullPath }
      });
      return false;
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
    fetchDetail() {
      if (this.productId == null || Number.isNaN(this.productId)) {
        this.product = {};
        this.collected = false;
        this.viewedProductId = null;
        return;
      }

      this.loading = true;
      this.$axios
        .post("/product/query", { id: this.productId, current: 1, size: 1 })
        .then(res => {
          const { data } = res;
          if (
            data &&
            data.code === 200 &&
            Array.isArray(data.data) &&
            data.data.length
          ) {
            this.product = data.data[0];
            this.loadFavoriteStatus();
            this.recordView();
            this.tryOpenOrderDialogFromRoute();
          } else {
            this.product = {};
            this.collected = false;
            this.viewedProductId = null;
          }
        })
        .catch(() => {
          this.product = {};
          this.collected = false;
          this.viewedProductId = null;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadFavoriteStatus() {
      if (this.productId == null) {
        this.collected = false;
        return;
      }

      const userInfo = getUserInfo();
      if (!userInfo || userInfo.id == null) {
        this.collected = false;
        return;
      }

      this.$axios
        .post("/interaction/query", {
          userId: userInfo.id,
          productId: this.productId,
          type: 1
        })
        .then(res => {
          const { data } = res;
          if (data && data.code === 200) {
            this.collected = data.total > 0;
          } else {
            this.collected = false;
          }
        })
        .catch(() => {
          this.collected = false;
        });
    },
    recordView() {
      if (!getToken() || this.productId == null) {
        return;
      }

      if (this.viewedProductId === this.productId) {
        return;
      }

      this.viewedProductId = this.productId;
      this.$axios.post(`/interaction/view/${this.productId}`).catch(() => {
        this.viewedProductId = null;
      });
    },
    shouldAutoOpenOrderDialog() {
      const { openOrder } = this.$route.query;
      return openOrder === "1" || openOrder === "true";
    },
    clearOpenOrderQuery() {
      if (!this.shouldAutoOpenOrderDialog()) {
        return;
      }
      const query = { ...this.$route.query };
      delete query.openOrder;
      this.$router
        .replace({
          path: this.$route.path,
          query
        })
        .catch(() => {});
    },
    tryOpenOrderDialogFromRoute() {
      if (!this.shouldAutoOpenOrderDialog() || !this.product.id) {
        return;
      }
      if (!getToken()) {
        this.ensureLogin("预约商品");
        return;
      }
      this.openOrderDialog();
      this.clearOpenOrderQuery();
    },
    toggleFavorite() {
      if (!this.ensureLogin("收藏商品")) {
        return;
      }

      if (this.productId == null) {
        this.$message.warning("商品ID不存在");
        return;
      }

      this.$axios
        .post(`/interaction/saveOperation/${this.productId}`)
        .then(res => {
          const { data } = res;
          if (data && data.code === 200) {
            this.collected = !!data.data;
            this.$notify({
              duration: 1000,
              title: "收藏操作",
              message: data.msg || (this.collected ? "已收藏" : "已取消收藏"),
              type: "success"
            });
          }
        })
        .catch(error => {
          console.error("收藏操作异常：", error);
          this.$message.error("收藏操作异常");
        });
    },
    handleIWant() {
      if (!this.ensureLogin("发送想要通知")) {
        return;
      }

      if (!this.product || !this.product.id) {
        this.$message.warning("商品信息不完整");
        return;
      }

      if (this.isOwner) {
        this.$message.info("这是你自己发布的商品");
        return;
      }

      this.$axios
        .post(`/interaction/likeProduct/${this.product.id}`)
        .then(res => {
          const { data } = res;
          if (data && data.code === 200) {
            this.$notify({
              duration: 1200,
              title: "想要操作通知",
              message: data.msg || "已通知发布者",
              type: "success"
            });
          } else {
            this.$notify({
              duration: 2000,
              title: "想要操作通知",
              message: (data && data.msg) || "操作失败",
              type: "info"
            });
          }
        })
        .catch(error => {
          console.error("商品想要操作异常：", error);
          this.$message.error("想要操作异常");
        });
    },
    openOrderDialog() {
      if (!this.ensureLogin("预约商品")) {
        return;
      }

      if (!this.product || !this.product.id) {
        this.$message.warning("商品信息不完整");
        return;
      }

      if (!this.canReserve) {
        this.$message.info(this.reserveDisabledReason || "当前商品暂不可预约");
        return;
      }

      this.orderQuantity = 1;
      this.orderRemark = "";
      this.orderDialogVisible = true;
    },
    submitOrder() {
      if (!this.ensureLogin("预约商品")) {
        return;
      }

      if (!this.product || !this.product.id) {
        this.$message.warning("商品信息不完整");
        return;
      }

      if (!this.canReserve) {
        this.$message.info(this.reserveDisabledReason || "当前商品暂不可预约");
        this.orderDialogVisible = false;
        return;
      }

      const payload = {
        productId: this.product.id,
        buyNumber: 1,
        detail: this.orderRemark
      };

      this.$axios
        .post("/product/buyProduct", payload)
        .then(res => {
          const { data } = res;
          if (data && data.code === 200) {
            this.$notify({
              duration: 1200,
              title: "预约成功",
              message: data.msg || "预约成功",
              type: "success"
            });
            this.orderDialogVisible = false;
            this.fetchDetail();
          } else {
            this.$notify({
              duration: 2000,
              title: "预约失败",
              message: (data && data.msg) || "预约失败",
              type: "error"
            });
          }
        })
        .catch(error => {
          console.error("商品预约异常：", error);
          this.$message.error("预约异常");
        });
    }
  }
};
</script>

<style scoped lang="scss">
.product-detail {
  padding: 0 0 24px;
}

.detail-card {
  border-radius: 12px;
  min-height: 360px;
}

.empty {
  padding: 60px 20px;
  color: #909399;
  text-align: center;
}

.detail-row {
  align-items: flex-start;
}

.image-area {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  border-radius: 12px;
  background: #f5f7fa;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.main-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.main-image ::v-deep .el-image__inner {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.image-error,
.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
}

.tag-bargain,
.tag-status {
  position: absolute;
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
}

.tag-bargain {
  top: 12px;
  right: 12px;
  background: #e6a23c;
}

.tag-status {
  top: 12px;
  left: 12px;
}

.tag-status.success {
  background: #22c55e;
}

.tag-status.warning {
  background: #f59e0b;
}

.tag-status.danger {
  background: #ef4444;
}

.tag-status.offline {
  background: #64748b;
}

.info-area {
  padding: 0 0 0 8px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.product-name {
  flex: 1;
  margin: 0 0 8px;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.4;
}

.btn-favorite {
  flex-shrink: 0;
  white-space: nowrap;
}

.block {
  margin-bottom: 20px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fafbfc;
}

.block-publisher {
  margin-bottom: 16px;
  padding: 10px 14px;
}

.section-label {
  display: block;
  margin-bottom: 6px;
  color: #909399;
  font-size: 13px;
}

.section-label-lg {
  margin-bottom: 10px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.publisher-name {
  color: #303133;
  font-size: 15px;
  font-weight: 500;
}

.block-price {
  padding: 18px 16px;
}

.block-price .price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: 600;
}

.block-info .info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 24px;
}

.info-item {
  color: #606266;
  font-size: 14px;
}

.info-item-label {
  margin-right: 8px;
  color: #909399;
}

.info-item-bargain span:last-child {
  font-weight: 500;
}

.bargain-yellow {
  padding: 2px 10px;
  border-radius: 6px;
  background: linear-gradient(to bottom, #fdf6ec, #f5dab1);
  color: #b88230;
  font-weight: 600;
}

.block-desc {
  padding-bottom: 18px;
}

.detail-text {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.action-tip {
  margin: 12px 0 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
}

.order-dialog-body {
  padding: 4px 4px 0;
}

.order-summary {
  margin-bottom: 16px;
}

.order-title {
  margin-bottom: 6px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.order-meta {
  color: #606266;
  font-size: 13px;
}

.order-price-label {
  margin-right: 4px;
}

.order-price {
  margin-right: 12px;
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}

.order-stock {
  color: #909399;
}

.order-field {
  margin-bottom: 12px;
}

.order-label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}

.order-tip {
  margin: 8px 0 0;
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .detail-row {
    display: block;
  }

  .detail-row .el-col {
    width: 100%;
  }

  .info-area {
    margin-top: 20px;
    padding-left: 0;
  }

  .actions {
    flex-wrap: wrap;
  }
}
</style>
