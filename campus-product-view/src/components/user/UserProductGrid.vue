<template>
  <div class="user-product-grid">
    <div v-if="!items.length" class="empty-state">
      <el-empty :description="emptyText"></el-empty>
    </div>

    <el-row v-else :gutter="18" class="grid-row">
      <el-col
        v-for="(item, index) in items"
        :key="item.id != null ? item.id : index"
        :xs="24"
        :sm="12"
        :md="12"
        :lg="8"
        :xl="6"
        class="grid-col"
      >
        <article
          class="product-card"
          :class="{ clickable }"
          @click="handleSelect(item)"
        >
          <div class="card-cover">
            <el-image
              v-if="coverSrc(item)"
              :src="coverSrc(item)"
              fit="cover"
              class="cover-image"
            >
              <div slot="error" class="cover-placeholder">暂无图片</div>
            </el-image>
            <div v-else class="cover-placeholder">暂无图片</div>

            <span class="status-chip" :class="{ bargain: isBargain(item) }">
              {{ isBargain(item) ? "可议价" : "一口价" }}
            </span>
          </div>

          <div class="card-body">
            <div class="card-title" :title="item.name">
              {{ item.name || "未命名商品" }}
            </div>

            <div class="card-summary">
              <slot name="summary" :item="item">
                <span>{{ defaultSummary(item) }}</span>
              </slot>
            </div>

            <div class="price-row">
              <div class="price-main">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{
                  item.price != null ? item.price : "-"
                }}</span>
              </div>
              <span v-if="item.likeNumber != null" class="wish-count">
                {{ item.likeNumber }} 人想要
              </span>
            </div>

            <div class="card-footer">
              <slot name="footer" :item="item">
                <div v-if="showPublisher" class="publisher">
                  <el-avatar
                    :size="28"
                    :src="publisherAvatar(item)"
                    class="publisher-avatar"
                  >
                    <i class="el-icon-user-solid"></i>
                  </el-avatar>
                  <span class="publisher-name">{{ publisherName(item) }}</span>
                </div>
              </slot>
            </div>

            <div v-if="$scopedSlots.actions" class="card-actions" @click.stop>
              <slot name="actions" :item="item"></slot>
            </div>
          </div>
        </article>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { toFullImageUrl } from "@/utils/imageUrl";

export default {
  name: "UserProductGrid",
  props: {
    items: {
      type: Array,
      default: () => []
    },
    emptyText: {
      type: String,
      default: "暂无内容"
    },
    showPublisher: {
      type: Boolean,
      default: false
    },
    clickable: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    handleSelect(item) {
      if (!this.clickable) return;
      this.$emit("select", item);
    },
    firstCover(item) {
      if (!item) return "";
      const cover = item.coverList || item.cover || "";
      if (!cover) return "";
      return String(cover)
        .split(",")
        .map(entry => entry && String(entry).trim())
        .find(Boolean);
    },
    coverSrc(item) {
      return toFullImageUrl(this.firstCover(item) || "");
    },
    isBargain(item) {
      return item && (item.isBargain === true || item.isBargain === 1);
    },
    defaultSummary(item) {
      if (!item) return "校园闲置";
      const detailText = item.detail || item.categoryName || "";
      if (!detailText) return "校园闲置";
      return String(detailText).slice(0, 26);
    },
    publisherName(item) {
      if (!item) return "匿名用户";
      return item.userName || item.userNickName || "匿名用户";
    },
    publisherAvatar(item) {
      return toFullImageUrl((item && item.userAvatar) || "");
    }
  }
};
</script>

<style scoped lang="scss">
.grid-col {
  margin-bottom: 18px;
}

.product-card {
  height: 100%;
  overflow: hidden;
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.88);
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
  transition: transform 0.22s ease, box-shadow 0.22s ease,
    border-color 0.22s ease;
}

.product-card.clickable {
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  border-color: rgba(59, 130, 246, 0.18);
  box-shadow: 0 24px 46px rgba(15, 23, 42, 0.1);
}

.card-cover {
  position: relative;
  padding-top: 88%;
  background: linear-gradient(135deg, #f1f5f9, #eff6ff);
}

.cover-image,
.cover-placeholder {
  position: absolute;
  inset: 0;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-image ::v-deep .el-image__inner {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
}

.status-chip {
  position: absolute;
  top: 14px;
  left: 14px;
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.74);
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.status-chip.bargain {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.card-body {
  display: flex;
  flex-direction: column;
  padding: 18px 18px 16px;
}

.card-title {
  min-height: 50px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.45;
  word-break: break-word;
}

.card-summary {
  min-height: 22px;
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.price-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
  margin-top: 16px;
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-symbol {
  color: #f97316;
  font-size: 14px;
  font-weight: 800;
}

.price-value {
  color: #f97316;
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
}

.wish-count {
  color: #94a3b8;
  font-size: 12px;
  white-space: nowrap;
}

.card-footer {
  min-height: 28px;
  margin-top: 16px;
}

.publisher {
  display: flex;
  align-items: center;
  gap: 10px;
}

.publisher-name {
  color: #475569;
  font-size: 13px;
  font-weight: 600;
}

.card-actions {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid rgba(226, 232, 240, 0.85);
}

.empty-state {
  padding: 16px 0 6px;
}

@media (max-width: 768px) {
  .card-body {
    padding: 16px;
  }

  .price-value {
    font-size: 24px;
  }
}
</style>
