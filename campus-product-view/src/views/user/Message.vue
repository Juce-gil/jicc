<template>
  <div class="message-page">
    <UserPageSection
      eyebrow="Messages"
      title="消息中心"
      description="查看系统通知与交易提醒，及时跟进你的订单与互动。"
    >
      <template #actions>
        <el-button plain icon="el-icon-check" @click="messageIsRead"
          >全部标记已读</el-button
        >
      </template>

      <div v-if="!messageList.length" class="empty-wrap">
        <el-empty description="暂时没有消息"></el-empty>
      </div>

      <div v-else class="message-list">
        <article
          v-for="(message, index) in messageList"
          :key="index"
          class="message-card"
          :class="{ unread: !message.isRead }"
        >
          <div class="message-badge">
            <i class="el-icon-message-solid"></i>
          </div>

          <div class="message-content">
            <div class="message-top">
              <span class="message-status">{{
                message.isRead ? "已读消息" : "未读消息"
              }}</span>
              <span class="message-time">{{ message.createTime }}</span>
            </div>
            <div class="message-text">{{ message.content }}</div>
          </div>
        </article>
      </div>
    </UserPageSection>
  </div>
</template>

<script>
import UserPageSection from "@/components/user/UserPageSection.vue";

export default {
  name: "Message",
  components: {
    UserPageSection
  },
  data() {
    return {
      messageList: []
    };
  },
  created() {
    this.fetchMessage();
  },
  methods: {
    messageIsRead() {
      this.$axios
        .post("/message/setRead")
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            this.$notify({
              duration: 1000,
              title: "消息状态已更新",
              message: data.msg,
              type: "success"
            });
            this.fetchMessage();
          }
        })
        .catch(error => {
          console.log("消息已读设置异常：", error);
        });
    },
    fetchMessage() {
      this.$axios
        .post("/message/queryUser", {})
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            this.messageList = data.data || [];
          } else {
            this.messageList = [];
          }
        })
        .catch(error => {
          console.log("查询消息异常：", error);
          this.messageList = [];
        });
    }
  }
};
</script>

<style scoped lang="scss">
.message-list {
  display: grid;
  gap: 16px;
}

.message-card {
  display: flex;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(226, 232, 240, 0.82);
  background: linear-gradient(135deg, #ffffff, #f8fbff);
  transition: all 0.2s ease;
}

.message-card.unread {
  border-color: rgba(59, 130, 246, 0.28);
  box-shadow: 0 16px 32px rgba(59, 130, 246, 0.08);
}

.message-badge {
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #ffffff;
  font-size: 20px;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.message-status {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.message-time {
  color: #94a3b8;
  font-size: 12px;
}

.message-text {
  margin-top: 10px;
  color: #475569;
  font-size: 15px;
  line-height: 1.9;
  word-break: break-word;
}

.empty-wrap {
  padding: 12px 0;
}
</style>
