<template>
  <div class="myself-page">
    <UserPageSection
      eyebrow="Profile"
      title="个人中心"
      description="在这里维护个人资料、修改密码，或安全退出当前账号。"
    >
      <div class="tab-list">
        <button
          v-for="(tagItem, index) in tabs"
          :key="index"
          type="button"
          class="tab-btn"
          :class="{ active: tabSelected === tagItem.key }"
          @click="condition(tagItem.key)"
        >
          {{ tagItem.label }}
        </button>
      </div>

      <div class="content-panel">
        <Self v-if="tabSelected === 'profile'" />

        <ResetPwd v-else-if="tabSelected === 'password'" />

        <div v-else class="logout-panel">
          <el-result
            icon="warning"
            title="确认退出登录"
            sub-title="退出后需要重新登录才能继续查看个人数据与订单。"
          >
            <template slot="extra">
              <button type="button" class="logout-btn" @click="loginout">
                我已确认，退出登录
              </button>
            </template>
          </el-result>
        </div>
      </div>
    </UserPageSection>
  </div>
</template>

<script>
import UserPageSection from "@/components/user/UserPageSection.vue";
import ResetPwd from "@/views/user/ResetPwd";
import Self from "@/views/user/Self";
import { clearToken } from "@/utils/storage";

export default {
  name: "Myself",
  components: {
    UserPageSection,
    ResetPwd,
    Self
  },
  data() {
    return {
      tabs: [
        { key: "profile", label: "修改资料" },
        { key: "password", label: "修改密码" },
        { key: "logout", label: "退出登录" }
      ],
      tabSelected: "profile"
    };
  },
  methods: {
    loginout() {
      clearToken();
      this.$router.push("/login");
    },
    condition(tag) {
      this.tabSelected = tag;
    }
  }
};
</script>

<style scoped lang="scss">
.tab-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}

.tab-btn {
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  background: #ffffff;
  color: #475569;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn.active,
.tab-btn:hover {
  background: linear-gradient(135deg, #eff6ff, #dcfce7);
  color: #0f172a;
}

.content-panel {
  min-height: 320px;
}

.logout-panel {
  padding: 12px 0;
}

.logout-btn {
  min-height: 44px;
  padding: 0 20px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}
</style>
