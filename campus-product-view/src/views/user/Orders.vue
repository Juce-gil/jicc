<template>
  <div class="orders-page">
    <UserPageSection
      eyebrow="Orders"
      title="订单中心"
      description="查看买入订单和卖出订单，跟进预约、线下交易与双方确认流程。"
    >
      <div class="toolbar">
        <div class="mode-tabs">
          <button
            v-for="mode in modeOptions"
            :key="mode.value"
            type="button"
            class="mode-tab"
            :class="{ active: activeMode === mode.value }"
            @click="switchMode(mode.value)"
          >
            {{ mode.label }}
          </button>
        </div>

        <div class="toolbar-right">
          <div class="status-tabs">
            <button
              v-for="item in statusOptions"
              :key="String(item.value)"
              type="button"
              class="status-tab"
              :class="{ active: selectedStatus === item.value }"
              @click="changeStatus(item.value)"
            >
              {{ item.label }}
            </button>
          </div>

          <el-input
            v-model.trim="searchCode"
            class="order-search"
            placeholder="搜索订单号"
            clearable
            @clear="fetchOrders"
            @keyup.enter.native="fetchOrders"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              @click="fetchOrders"
            ></el-button>
          </el-input>
        </div>
      </div>

      <div v-if="loading" class="loading-wrap">
        <i class="el-icon-loading"></i>
        <span>正在加载订单数据...</span>
      </div>

      <div v-else-if="!ordersList.length" class="empty-wrap">
        <el-empty
          :description="
            activeMode === 'buyer' ? '暂无买入订单' : '暂无卖出订单'
          "
        ></el-empty>
      </div>

      <div v-else class="order-list">
        <article
          v-for="order in ordersList"
          :key="`${activeMode}-${order.id}`"
          class="order-card"
        >
          <div class="order-top">
            <div>
              <div class="order-code">
                订单号：{{ order.code || `#${order.id}` }}
              </div>
              <div class="order-time">
                创建时间：{{ order.createTime || "暂无记录" }}
              </div>
            </div>
            <div class="order-top-right">
              <span
                class="status-badge"
                :class="statusClass(order.tradeStatus)"
              >
                {{ statusText(order) }}
              </span>
              <span v-if="legacyOrder(order)" class="legacy-tip">历史数据</span>
            </div>
          </div>

          <div class="order-main">
            <img :src="coverUrl(order)" alt="cover" class="order-cover" />

            <div class="order-content">
              <div class="title-row">
                <div class="order-title">
                  {{ order.productTitle || "未命名商品" }}
                </div>
                <span class="order-count">x{{ order.buyNumber || 1 }}</span>
              </div>

              <div class="partner-row">
                <span class="partner-label">{{
                  activeMode === "buyer" ? "卖家" : "买家"
                }}</span>
                <span class="partner-name">{{ partnerName(order) }}</span>
              </div>

              <div class="order-detail">
                备注：{{ order.detail || "暂无备注" }}
              </div>

              <div class="order-price-row">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ totalPrice(order) }}</span>
              </div>

              <div class="order-progress">
                {{ progressText(order) }}
              </div>

              <div class="confirm-row">
                <span v-if="order.sellerConfirmTime"
                  >卖家接单：{{ order.sellerConfirmTime }}</span
                >
                <span v-if="order.buyerConfirmTime"
                  >买家确认：{{ order.buyerConfirmTime }}</span
                >
                <span v-if="order.sellerFinishTime"
                  >卖家确认完成：{{ order.sellerFinishTime }}</span
                >
                <span v-if="order.cancelTime"
                  >取消时间：{{ order.cancelTime }}</span
                >
              </div>

              <div v-if="actionList(order).length" class="action-row">
                <button
                  v-for="action in actionList(order)"
                  :key="action.key"
                  type="button"
                  class="action-btn"
                  :class="action.className"
                  @click="handleAction(action.key, order)"
                >
                  {{ action.label }}
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </UserPageSection>
  </div>
</template>

<script>
import UserPageSection from "@/components/user/UserPageSection.vue";
import { toFullImageUrl } from "@/utils/imageUrl";

const MODE_BUYER = "buyer";
const MODE_SELLER = "seller";

const STATUS_PENDING = 1;
const STATUS_RESERVED = 2;
const STATUS_PARTIAL = 3;
const STATUS_COMPLETED = 4;
const STATUS_CANCELLED = 5;

export default {
  name: "Orders",
  components: {
    UserPageSection
  },
  data() {
    return {
      loading: false,
      activeMode: MODE_BUYER,
      searchCode: "",
      selectedStatus: null,
      buyerOrders: [],
      sellerOrders: [],
      modeOptions: [
        { value: MODE_BUYER, label: "我买到的" },
        { value: MODE_SELLER, label: "我卖出的" }
      ],
      statusOptions: [
        { value: null, label: "全部" },
        { value: STATUS_PENDING, label: "待确认" },
        { value: STATUS_RESERVED, label: "待交易" },
        { value: STATUS_PARTIAL, label: "待另一方确认" },
        { value: STATUS_COMPLETED, label: "已完成" },
        { value: STATUS_CANCELLED, label: "已取消" },
        { value: 0, label: "历史订单" }
      ]
    };
  },
  computed: {
    ordersList() {
      return this.activeMode === MODE_BUYER
        ? this.buyerOrders
        : this.sellerOrders;
    }
  },
  watch: {
    "$route.query.mode"(mode) {
      const normalizedMode = this.normalizeMode(mode);
      if (normalizedMode !== this.activeMode) {
        this.activeMode = normalizedMode;
      }
    }
  },
  created() {
    this.activeMode = this.normalizeMode(this.$route.query.mode);
    this.fetchOrders();
  },
  methods: {
    normalizeMode(mode) {
      return mode === MODE_SELLER ? MODE_SELLER : MODE_BUYER;
    },
    switchMode(mode) {
      const normalizedMode = this.normalizeMode(mode);
      this.activeMode = normalizedMode;
      if (this.$route.query.mode === normalizedMode) {
        return;
      }
      this.$router
        .replace({
          path: this.$route.path,
          query: {
            ...this.$route.query,
            mode: normalizedMode
          }
        })
        .catch(() => {});
    },
    changeStatus(status) {
      this.selectedStatus = status;
      this.fetchOrders();
    },
    buildQuery() {
      const query = {};
      if (this.searchCode) {
        query.code = this.searchCode;
      }
      if (this.selectedStatus !== null && this.selectedStatus !== undefined) {
        query.tradeStatus = this.selectedStatus;
      }
      return query;
    },
    normalizeOrders(list) {
      if (!Array.isArray(list)) return [];
      return list.map(order => ({
        ...order,
        buyNumber: order.buyNumber || 1,
        buyPrice: order.buyPrice || 0
      }));
    },
    async fetchOrders() {
      this.loading = true;
      const query = this.buildQuery();
      try {
        const [buyerRes, sellerRes] = await Promise.all([
          this.$axios.post("/orders/queryUser", query),
          this.$axios.post("/orders/queryOrdersList", query)
        ]);
        const buyerData = buyerRes && buyerRes.data ? buyerRes.data : {};
        const sellerData = sellerRes && sellerRes.data ? sellerRes.data : {};
        this.buyerOrders =
          buyerData.code === 200 ? this.normalizeOrders(buyerData.data) : [];
        this.sellerOrders =
          sellerData.code === 200 ? this.normalizeOrders(sellerData.data) : [];
      } catch (error) {
        console.error("订单查询异常：", error);
        this.buyerOrders = [];
        this.sellerOrders = [];
      } finally {
        this.loading = false;
      }
    },
    coverUrl(order) {
      const raw = order && order.productCover ? String(order.productCover) : "";
      const first = raw ? raw.split(",")[0] : "";
      return toFullImageUrl(first || "") || "";
    },
    totalPrice(order) {
      const total = Number(order.buyNumber || 0) * Number(order.buyPrice || 0);
      return total.toFixed(2);
    },
    legacyOrder(order) {
      return ![
        STATUS_PENDING,
        STATUS_RESERVED,
        STATUS_PARTIAL,
        STATUS_COMPLETED,
        STATUS_CANCELLED
      ].includes(order.tradeStatus);
    },
    isBuyerConfirmed(order) {
      return order.isConfirm === true || order.isConfirm === 1;
    },
    isSellerConfirmed(order) {
      return order.isRefundConfirm === true || order.isRefundConfirm === 1;
    },
    partnerName(order) {
      if (this.activeMode === MODE_BUYER) {
        return order.sellerName || `用户 #${order.sellerId || "-"}`;
      }
      return order.userName || `用户 #${order.userId || "-"}`;
    },
    statusText(order) {
      switch (order.tradeStatus) {
        case STATUS_PENDING:
          return this.activeMode === MODE_BUYER ? "待卖家确认" : "待处理预约";
        case STATUS_RESERVED:
          return "已预约待交易";
        case STATUS_PARTIAL:
          return "待另一方确认";
        case STATUS_COMPLETED:
          return "交易已完成";
        case STATUS_CANCELLED:
          return "订单已取消";
        default:
          return "历史订单";
      }
    },
    statusClass(tradeStatus) {
      if (tradeStatus === STATUS_COMPLETED) return "success";
      if (tradeStatus === STATUS_CANCELLED) return "danger";
      if (tradeStatus === STATUS_PENDING || tradeStatus === STATUS_RESERVED)
        return "warning";
      if (tradeStatus === STATUS_PARTIAL) return "primary";
      return "legacy";
    },
    progressText(order) {
      if (this.legacyOrder(order)) {
        return "该订单来自旧版流程，仅支持查看，不建议继续操作。";
      }
      switch (order.tradeStatus) {
        case STATUS_PENDING:
          return this.activeMode === MODE_BUYER
            ? "你已提交预约请求，等待卖家确认。"
            : "有新的预约请求，请及时确认或取消。";
        case STATUS_RESERVED:
          return "卖家已确认预约，请线下完成交易，随后双方分别确认。";
        case STATUS_PARTIAL:
          if (this.isBuyerConfirmed(order) && !this.isSellerConfirmed(order)) {
            return "买家已确认交易完成，等待卖家确认。";
          }
          if (!this.isBuyerConfirmed(order) && this.isSellerConfirmed(order)) {
            return "卖家已确认交易完成，等待买家确认。";
          }
          return "已有一方确认交易完成，等待另一方确认。";
        case STATUS_COMPLETED:
          return "双方都已确认，本次交易已完成。";
        case STATUS_CANCELLED:
          return order.cancelReason || "订单已取消。";
        default:
          return "当前订单状态暂不支持自动操作。";
      }
    },
    actionList(order) {
      if (this.legacyOrder(order)) {
        return [];
      }

      if (this.activeMode === MODE_BUYER) {
        if (order.tradeStatus === STATUS_PENDING) {
          return [{ key: "cancel", label: "取消预约", className: "danger" }];
        }
        if (order.tradeStatus === STATUS_RESERVED) {
          return [
            {
              key: "buyerConfirm",
              label: "我已完成交易",
              className: "primary"
            },
            { key: "cancel", label: "取消预约", className: "secondary" }
          ];
        }
        if (
          order.tradeStatus === STATUS_PARTIAL &&
          !this.isBuyerConfirmed(order)
        ) {
          return [
            {
              key: "buyerConfirm",
              label: "我已完成交易",
              className: "primary"
            }
          ];
        }
        return [];
      }

      if (order.tradeStatus === STATUS_PENDING) {
        return [
          {
            key: "sellerReserve",
            label: "确认预约",
            className: "primary"
          },
          { key: "cancel", label: "取消预约", className: "secondary" }
        ];
      }
      if (order.tradeStatus === STATUS_RESERVED) {
        return [
          {
            key: "sellerComplete",
            label: "卖家确认交易完成",
            className: "primary"
          },
          { key: "cancel", label: "取消预约", className: "secondary" }
        ];
      }
      if (
        order.tradeStatus === STATUS_PARTIAL &&
        !this.isSellerConfirmed(order)
      ) {
        return [
          {
            key: "sellerComplete",
            label: "卖家确认交易完成",
            className: "primary"
          }
        ];
      }
      return [];
    },
    async handleAction(actionKey, order) {
      const configMap = {
        sellerReserve: {
          title: "确认预约",
          text: "确认后，商品会进入已预约状态，等待线下交易。",
          request: () => this.$axios.post(`/product/placeAnOrder/${order.id}`)
        },
        buyerConfirm: {
          title: "买家确认完成",
          text: "确认后会记录买家已完成线下交易，等待卖家最终确认。",
          request: () => this.$axios.post(`/product/getGoods/${order.id}`)
        },
        sellerComplete: {
          title: "卖家确认完成",
          text: "确认后会记录卖家已完成线下交易，如买家已确认则订单直接完成。",
          request: () =>
            this.$axios.post(`/product/confirmTradeBySeller/${order.id}`)
        },
        cancel: {
          title: "取消预约",
          text: "取消后订单将结束，商品会在可恢复的情况下重新上架。",
          request: () => this.$axios.post(`/product/refund/${order.id}`)
        }
      };

      const config = configMap[actionKey];
      if (!config) {
        return;
      }

      const confirmed = await this.$swalConfirm({
        title: config.title,
        text: config.text,
        icon: "warning"
      });
      if (!confirmed) {
        return;
      }

      try {
        const res = await config.request();
        const { data } = res;
        if (data && data.code === 200) {
          this.$notify({
            duration: 1200,
            title: config.title,
            message: data.msg || "操作成功",
            type: "success"
          });
          this.fetchOrders();
        } else {
          this.$message.info((data && data.msg) || "操作失败");
        }
      } catch (error) {
        console.error(`${config.title}异常：`, error);
        this.$message.error(`${config.title}异常`);
      }
    }
  }
};
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.mode-tabs,
.status-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  flex-wrap: wrap;
  flex: 1;
}

.mode-tab,
.status-tab {
  min-height: 40px;
  padding: 0 16px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #ffffff;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-tab.active,
.mode-tab:hover,
.status-tab.active,
.status-tab:hover {
  background: linear-gradient(135deg, #eff6ff, #dcfce7);
  color: #0f172a;
}

.order-search {
  width: 320px;
}

.loading-wrap,
.empty-wrap {
  padding: 16px 0;
}

.loading-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #64748b;
}

.order-list {
  display: grid;
  gap: 18px;
}

.order-card {
  padding: 20px;
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.82);
  background: linear-gradient(135deg, #ffffff, #f8fbff);
}

.order-top {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.order-top-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.order-code {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.order-time,
.confirm-row,
.order-detail,
.partner-row,
.order-progress {
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}

.order-time {
  margin-top: 6px;
}

.partner-row,
.confirm-row {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.partner-label {
  color: #94a3b8;
}

.partner-name {
  color: #0f172a;
  font-weight: 600;
}

.status-badge,
.legacy-tip {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-badge.success {
  background: #dcfce7;
  color: #15803d;
}

.status-badge.warning {
  background: #fef3c7;
  color: #b45309;
}

.status-badge.primary {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-badge.danger {
  background: #fee2e2;
  color: #dc2626;
}

.status-badge.legacy,
.legacy-tip {
  background: #f1f5f9;
  color: #64748b;
}

.order-main {
  display: flex;
  gap: 18px;
}

.order-cover {
  width: 132px;
  height: 132px;
  border-radius: 18px;
  object-fit: cover;
  background: #f1f5f9;
}

.order-content {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.order-title {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
}

.order-count {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.order-price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-top: 14px;
}

.price-symbol,
.price-value {
  color: #f97316;
  font-weight: 800;
}

.price-symbol {
  font-size: 14px;
}

.price-value {
  font-size: 28px;
}

.action-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 18px;
}

.action-btn {
  min-height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.action-btn.primary {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #ffffff;
}

.action-btn.secondary {
  background: #eff6ff;
  color: #2563eb;
}

.action-btn.danger {
  background: #fee2e2;
  color: #dc2626;
}

@media (max-width: 960px) {
  .toolbar-right {
    width: 100%;
    justify-content: flex-start;
  }

  .order-search {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .order-main {
    flex-direction: column;
  }

  .order-cover {
    width: 100%;
    height: 220px;
  }

  .order-top {
    flex-direction: column;
  }
}
</style>
