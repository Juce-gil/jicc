<template>
  <div class="admin-orders-page">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-head">
        <div>
          <h2>订单管理</h2>
          <p>统一查看预约订单、交易确认进度与异常关闭情况。</p>
        </div>

        <div class="toolbar-actions">
          <el-button icon="el-icon-refresh" @click="fetchOrders"
            >刷新</el-button
          >
        </div>
      </div>

      <div class="toolbar-filters">
        <el-select
          v-model="selectedStatus"
          clearable
          placeholder="全部状态"
          class="filter-item"
          @change="handleFilter"
        >
          <el-option
            v-for="item in statusOptions"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-date-picker
          v-model="searchTime"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="filter-item date-range"
          @change="handleFilter"
        />

        <el-input
          v-model.trim="searchCode"
          clearable
          placeholder="搜索订单号"
          class="filter-item search-input"
          @clear="handleFilter"
          @keyup.enter.native="handleFilter"
        >
          <el-button
            slot="append"
            icon="el-icon-search"
            @click="handleFilter"
          />
        </el-input>
      </div>
    </el-card>

    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">筛选结果</span>
        <strong>{{ totalItems }}</strong>
        <p>当前条件下的订单总数</p>
      </div>
      <div class="stat-card">
        <span class="stat-label">进行中</span>
        <strong>{{ activeCount }}</strong>
        <p>待确认 / 待交易 / 待另一方确认</p>
      </div>
      <div class="stat-card">
        <span class="stat-label">已完成</span>
        <strong>{{ completedCount }}</strong>
        <p>当前页已完成订单</p>
      </div>
      <div class="stat-card">
        <span class="stat-label">已取消 / 历史</span>
        <strong>{{ closedOrLegacyCount }}</strong>
        <p>当前页已关闭或旧数据订单</p>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column label="商品" min-width="280">
          <template slot-scope="scope">
            <div class="product-cell">
              <img
                v-if="coverUrl(scope.row)"
                :src="coverUrl(scope.row)"
                alt="cover"
                class="product-cover"
              />
              <div v-else class="product-cover placeholder">无图</div>

              <div class="product-meta">
                <div class="product-title">
                  {{ scope.row.productTitle || `商品 #${scope.row.productId}` }}
                </div>
                <div class="product-sub">
                  订单号：{{ scope.row.code || `#${scope.row.id}` }}
                </div>
                <div class="product-sub">
                  备注：{{ scope.row.detail || "无" }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="买家" min-width="160">
          <template slot-scope="scope">
            <div class="person-cell">
              <el-avatar :size="32" :src="avatarUrl(scope.row.userAvatar)" />
              <div>
                <div class="person-name">
                  {{ scope.row.userName || `用户 #${scope.row.userId || "-"}` }}
                </div>
                <div class="person-sub">ID：{{ scope.row.userId || "-" }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="卖家" min-width="160">
          <template slot-scope="scope">
            <div class="person-cell">
              <el-avatar :size="32" :src="avatarUrl(scope.row.sellerAvatar)" />
              <div>
                <div class="person-name">
                  {{
                    scope.row.sellerName || `用户 #${scope.row.sellerId || "-"}`
                  }}
                </div>
                <div class="person-sub">
                  ID：{{ scope.row.sellerId || "-" }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="120">
          <template slot-scope="scope">
            <span class="amount-text">¥ {{ totalPrice(scope.row) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="150">
          <template slot-scope="scope">
            <el-tag
              :type="statusTagType(scope.row.tradeStatus)"
              effect="plain"
              size="small"
            >
              {{ statusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" min-width="170" />

        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openDetail(scope.row)">
              详情
            </el-button>

            <el-button
              v-if="canClose(scope.row)"
              type="text"
              @click="closeOrder(scope.row)"
            >
              关闭订单
            </el-button>

            <el-button
              type="text"
              class="danger-text"
              :disabled="!canDelete(scope.row)"
              @click="deleteOrder(scope.row)"
            >
              删除记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-drawer
      title="订单详情"
      :visible.sync="detailVisible"
      direction="rtl"
      size="420px"
    >
      <div v-if="activeOrder.id" class="detail-panel">
        <div class="detail-header">
          <img
            v-if="coverUrl(activeOrder)"
            :src="coverUrl(activeOrder)"
            alt="cover"
            class="detail-cover"
          />
          <div v-else class="detail-cover placeholder">无图</div>

          <div class="detail-header-info">
            <div class="detail-title">
              {{ activeOrder.productTitle || `商品 #${activeOrder.productId}` }}
            </div>
            <el-tag
              :type="statusTagType(activeOrder.tradeStatus)"
              effect="plain"
              size="small"
            >
              {{ statusText(activeOrder) }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-label">订单信息</div>
          <div class="detail-item">订单号：{{ activeOrder.code || "-" }}</div>
          <div class="detail-item">订单ID：{{ activeOrder.id || "-" }}</div>
          <div class="detail-item">金额：¥ {{ totalPrice(activeOrder) }}</div>
          <div class="detail-item">数量：{{ activeOrder.buyNumber || 1 }}</div>
          <div class="detail-item">备注：{{ activeOrder.detail || "无" }}</div>
          <div class="detail-item">
            流程说明：{{ progressText(activeOrder) }}
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-label">买卖双方</div>
          <div class="detail-item">
            买家：{{
              activeOrder.userName || `用户 #${activeOrder.userId || "-"}`
            }}
          </div>
          <div class="detail-item">
            卖家：{{
              activeOrder.sellerName || `用户 #${activeOrder.sellerId || "-"}`
            }}
          </div>
        </div>

        <div class="detail-section">
          <div class="detail-label">时间节点</div>
          <div class="detail-item">
            创建时间：{{ activeOrder.createTime || "暂无记录" }}
          </div>
          <div class="detail-item">
            卖家确认：{{ activeOrder.sellerConfirmTime || "未确认" }}
          </div>
          <div class="detail-item">
            买家确认：{{ activeOrder.buyerConfirmTime || "未确认" }}
          </div>
          <div class="detail-item">
            卖家完成确认：{{ activeOrder.sellerFinishTime || "未确认" }}
          </div>
          <div class="detail-item">
            取消时间：{{ activeOrder.cancelTime || "未取消" }}
          </div>
          <div class="detail-item">
            取消原因：{{ activeOrder.cancelReason || "无" }}
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { toFullImageUrl } from "@/utils/imageUrl";

const STATUS_PENDING = 1;
const STATUS_RESERVED = 2;
const STATUS_PARTIAL = 3;
const STATUS_COMPLETED = 4;
const STATUS_CANCELLED = 5;

export default {
  name: "AdminOrders",
  data() {
    return {
      loading: false,
      tableData: [],
      totalItems: 0,
      currentPage: 1,
      pageSize: 10,
      selectedStatus: null,
      searchCode: "",
      searchTime: [],
      detailVisible: false,
      activeOrder: {},
      statusOptions: [
        { value: null, label: "全部状态" },
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
    activeCount() {
      return this.tableData.filter(item =>
        [STATUS_PENDING, STATUS_RESERVED, STATUS_PARTIAL].includes(
          item.tradeStatus
        )
      ).length;
    },
    completedCount() {
      return this.tableData.filter(
        item => item.tradeStatus === STATUS_COMPLETED
      ).length;
    },
    closedOrLegacyCount() {
      return this.tableData.filter(
        item =>
          item.tradeStatus === STATUS_CANCELLED ||
          this.legacyOrder(item.tradeStatus)
      ).length;
    }
  },
  created() {
    this.fetchOrders();
  },
  methods: {
    buildDateTime(date, endOfDay = false) {
      if (!(date instanceof Date)) {
        return null;
      }
      const year = date.getFullYear();
      const month = `${date.getMonth() + 1}`.padStart(2, "0");
      const day = `${date.getDate()}`.padStart(2, "0");
      const time = endOfDay ? "23:59:59" : "00:00:00";
      return `${year}-${month}-${day}T${time}`;
    },
    buildQuery() {
      const payload = {
        current: this.currentPage,
        size: this.pageSize
      };

      if (this.searchCode) {
        payload.code = this.searchCode;
      }

      if (this.selectedStatus !== null && this.selectedStatus !== undefined) {
        payload.tradeStatus = this.selectedStatus;
      }

      if (Array.isArray(this.searchTime) && this.searchTime.length === 2) {
        payload.startTime = this.buildDateTime(this.searchTime[0], false);
        payload.endTime = this.buildDateTime(this.searchTime[1], true);
      }

      return payload;
    },
    fetchOrders() {
      this.loading = true;
      this.$axios
        .post("/orders/query", this.buildQuery())
        .then(res => {
          const { data } = res;
          if (data && data.code === 200) {
            this.tableData = Array.isArray(data.data) ? data.data : [];
            this.totalItems = Number(data.total || 0);
          } else {
            this.tableData = [];
            this.totalItems = 0;
          }
        })
        .catch(error => {
          console.error("管理端订单查询异常：", error);
          this.tableData = [];
          this.totalItems = 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleFilter() {
      this.currentPage = 1;
      this.fetchOrders();
    },
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
      this.fetchOrders();
    },
    handleCurrentChange(page) {
      this.currentPage = page;
      this.fetchOrders();
    },
    avatarUrl(url) {
      return toFullImageUrl(url || "");
    },
    coverUrl(order) {
      const raw = order && order.productCover ? String(order.productCover) : "";
      const first = raw ? raw.split(",")[0] : "";
      return toFullImageUrl(first || "");
    },
    totalPrice(order) {
      const total = Number(order.buyPrice || 0) * Number(order.buyNumber || 0);
      return total.toFixed(2);
    },
    legacyOrder(tradeStatus) {
      return ![
        STATUS_PENDING,
        STATUS_RESERVED,
        STATUS_PARTIAL,
        STATUS_COMPLETED,
        STATUS_CANCELLED
      ].includes(tradeStatus);
    },
    statusText(order) {
      switch (order.tradeStatus) {
        case STATUS_PENDING:
          return "待卖家确认";
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
    statusTagType(tradeStatus) {
      if (tradeStatus === STATUS_COMPLETED) return "success";
      if (tradeStatus === STATUS_CANCELLED) return "danger";
      if (tradeStatus === STATUS_PENDING || tradeStatus === STATUS_RESERVED)
        return "warning";
      if (tradeStatus === STATUS_PARTIAL) return "";
      return "info";
    },
    progressText(order) {
      if (this.legacyOrder(order.tradeStatus)) {
        return "该订单来自旧版流程，建议仅做查看或数据清理。";
      }
      switch (order.tradeStatus) {
        case STATUS_PENDING:
          return "买家已发起预约，等待卖家确认。";
        case STATUS_RESERVED:
          return "卖家已确认预约，等待双方线下交易。";
        case STATUS_PARTIAL:
          return "已有一方确认交易完成，等待另一方确认。";
        case STATUS_COMPLETED:
          return "双方已确认，交易已完成。";
        case STATUS_CANCELLED:
          return order.cancelReason || "订单已被关闭。";
        default:
          return "当前状态暂无说明。";
      }
    },
    canClose(order) {
      return [STATUS_PENDING, STATUS_RESERVED, STATUS_PARTIAL].includes(
        order.tradeStatus
      );
    },
    canDelete(order) {
      return this.legacyOrder(order.tradeStatus)
        ? true
        : [STATUS_COMPLETED, STATUS_CANCELLED].includes(order.tradeStatus);
    },
    openDetail(order) {
      this.activeOrder = { ...order };
      this.detailVisible = true;
    },
    async closeOrder(order) {
      const confirmed = await this.$swalConfirm({
        title: "关闭订单",
        text: `确认关闭订单【${order.code ||
          order.id}】吗？关闭后会尝试恢复商品可售状态。`,
        icon: "warning"
      });

      if (!confirmed) {
        return;
      }

      try {
        const res = await this.$axios.post(`/orders/returnMoney/${order.id}`);
        const { data } = res;
        if (data && data.code === 200) {
          this.$notify({
            duration: 1200,
            title: "订单关闭",
            message: data.msg || "订单已关闭",
            type: "success"
          });
          this.fetchOrders();
        }
      } catch (error) {
        console.error("管理端关闭订单异常：", error);
        this.$message.error("关闭订单异常");
      }
    },
    async deleteOrder(order) {
      if (!this.canDelete(order)) {
        this.$message.info("请先完成或关闭订单后再删除记录");
        return;
      }

      const confirmed = await this.$swalConfirm({
        title: "删除订单记录",
        text: `删除后不可恢复，确认删除订单【${order.code || order.id}】吗？`,
        icon: "warning"
      });

      if (!confirmed) {
        return;
      }

      try {
        const res = await this.$axios.post("/orders/batchDelete", [order.id]);
        const { data } = res;
        if (data && data.code === 200) {
          this.$notify({
            duration: 1200,
            title: "删除成功",
            message: data.msg || "订单记录已删除",
            type: "success"
          });
          if (this.detailVisible && this.activeOrder.id === order.id) {
            this.detailVisible = false;
            this.activeOrder = {};
          }
          this.fetchOrders();
        }
      } catch (error) {
        console.error("管理端删除订单异常：", error);
        this.$message.error("删除订单异常");
      }
    }
  }
};
</script>

<style scoped lang="scss">
.admin-orders-page {
  padding: 16px 6px 24px;
}

.toolbar-card,
.table-card {
  border-radius: 16px;
  border: 1px solid #ebeef5;
}

.toolbar-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.toolbar-head h2 {
  margin: 0;
  color: #1f2937;
  font-size: 24px;
}

.toolbar-head p {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.toolbar-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.filter-item {
  width: 220px;
}

.date-range {
  width: 280px;
}

.search-input {
  width: 320px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin: 18px 0;
}

.stat-card {
  padding: 18px 20px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff, #f8fbff);
}

.stat-label {
  color: #6b7280;
  font-size: 13px;
}

.stat-card strong {
  display: block;
  margin-top: 12px;
  color: #111827;
  font-size: 30px;
  font-weight: 700;
}

.stat-card p {
  margin: 8px 0 0;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.6;
}

.product-cell,
.person-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-cover,
.detail-cover {
  width: 72px;
  height: 72px;
  border-radius: 14px;
  object-fit: cover;
  background: #f1f5f9;
}

.product-cover.placeholder,
.detail-cover.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 12px;
}

.product-meta,
.detail-header-info {
  min-width: 0;
}

.product-title,
.detail-title,
.person-name {
  color: #111827;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.6;
  word-break: break-word;
}

.product-sub,
.person-sub {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.6;
}

.amount-text {
  color: #f97316;
  font-weight: 700;
}

.danger-text {
  color: #ef4444;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.detail-panel {
  padding: 0 18px 24px;
}

.detail-header {
  display: flex;
  gap: 14px;
  align-items: center;
}

.detail-section {
  margin-top: 22px;
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
}

.detail-label {
  margin-bottom: 10px;
  color: #111827;
  font-size: 14px;
  font-weight: 700;
}

.detail-item {
  color: #475569;
  font-size: 13px;
  line-height: 1.9;
  word-break: break-word;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .admin-orders-page {
    padding-left: 0;
    padding-right: 0;
  }

  .toolbar-head,
  .toolbar-filters {
    flex-direction: column;
  }

  .filter-item,
  .date-range,
  .search-input {
    width: 100%;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
