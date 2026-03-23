<template>
  <div class="user-home">
    <header class="home-header">
      <div class="header-shell">
        <div class="brand-area" @click="handleRouteSelect('/product')">
          <Logo name="校园交易" :bag="'#ffffff'" />
          <div class="brand-copy">
            <strong>Campus Market</strong>
            <span>让闲置在校园里重新流动</span>
          </div>
        </div>

        <nav class="nav-links">
          <button
            v-for="item in visibleNavItems"
            :key="item.path"
            type="button"
            class="nav-pill"
            :class="{ active: isNavActive(item) }"
            @click="handleRouteSelect(item.path)"
          >
            {{ item.label }}
          </button>
        </nav>

        <div class="header-tools">
          <div class="search-box">
            <i class="el-icon-search"></i>
            <input
              v-model.trim="key"
              type="text"
              placeholder="搜索商品、关键词、分类"
              @keyup.enter="fetch"
            />
            <button type="button" @click="fetch">搜索</button>
          </div>

          <template v-if="loginStatus">
            <button
              v-for="item in quickActions"
              :key="item.path"
              type="button"
              class="quick-action"
              :class="{ active: isPathActive(item.aliases) }"
              @click="handleRouteSelect(item.path)"
            >
              <i :class="item.icon"></i>
              <span>{{ item.label }}</span>
            </button>

            <button
              type="button"
              class="avatar-entry"
              @click="handleRouteSelect('/myself')"
            >
              <img :src="avatarUrl" alt="avatar" class="avatar" />
            </button>
          </template>

          <button
            v-else
            type="button"
            class="login-entry"
            @click="loginOperation"
          >
            登录 / 注册
          </button>
        </div>
      </div>

      <div class="hero-shell">
        <div class="hero-card">
          <div class="hero-main">
            <span class="hero-badge">{{
              loginStatus ? "已登录 · 用户空间" : "欢迎来到校园交易"
            }}</span>
            <h1>
              {{
                loginStatus
                  ? `你好，${displayName}`
                  : "让每一件闲置，在校园里更快找到新主人"
              }}
            </h1>
            <p>
              {{
                loginStatus
                  ? "继续浏览商品、管理收藏与订单，也可以发布新的闲置物品，让交易更顺手。"
                  : "聚合发布、搜索、沟通与下单流程，让校园二手交易更简单、更清晰。"
              }}
            </p>

            <div class="hero-actions">
              <button
                type="button"
                class="hero-primary"
                @click="handleRouteSelect('/product')"
              >
                立即逛商品
              </button>
              <button
                v-if="loginStatus"
                type="button"
                class="hero-secondary"
                @click="handleRouteSelect('/post-product')"
              >
                发布闲置
              </button>
              <button
                v-else
                type="button"
                class="hero-secondary"
                @click="loginOperation"
              >
                去登录
              </button>
            </div>
          </div>

          <div class="hero-side">
            <template v-if="loginStatus">
              <div class="profile-card">
                <img :src="avatarUrl" alt="avatar" class="profile-avatar" />
                <div class="profile-name">{{ displayName }}</div>
                <div class="profile-meta">
                  注册时间：{{ userInfo.createTime || "暂无记录" }}
                </div>
                <div class="profile-tags">
                  <span>可发布商品</span>
                  <span>可管理订单</span>
                  <span>可查看消息</span>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="guest-panel">
                <div class="guest-item">
                  <i class="el-icon-shopping-bag-1"></i>
                  <div>
                    <strong>快速浏览</strong>
                    <span>按分类筛选喜欢的商品</span>
                  </div>
                </div>
                <div class="guest-item">
                  <i class="el-icon-chat-dot-round"></i>
                  <div>
                    <strong>便捷沟通</strong>
                    <span>与卖家快速建立联系</span>
                  </div>
                </div>
                <div class="guest-item">
                  <i class="el-icon-s-promotion"></i>
                  <div>
                    <strong>轻松发布</strong>
                    <span>登录后即可发布自己的闲置</span>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </header>

    <main class="content-shell">
      <router-view></router-view>
    </main>
  </div>
</template>

<script>
import Logo from "@/components/Logo";
import { getToken, setSearchKey, setUserInfo } from "@/utils/storage";
import { toFullImageUrl } from "@/utils/imageUrl";

export default {
  name: "Home",
  components: {
    Logo
  },
  data() {
    return {
      key: "",
      loginStatus: false,
      userInfo: {},
      searchPath: "/search",
      navItems: [
        {
          label: "逛商品",
          path: "/product",
          aliases: ["/product", "/search", "/product/detail", "/product-detail"]
        },
        {
          label: "我的商品",
          path: "/myProduct",
          aliases: ["/myProduct", "/edit-product"]
        },
        {
          label: "我的收藏",
          path: "/mySave",
          aliases: ["/mySave"]
        },
        {
          label: "浏览足迹",
          path: "/myView",
          aliases: ["/myView"]
        }
      ],
      quickActions: [
        {
          label: "订单",
          path: "/orders",
          icon: "el-icon-document",
          aliases: ["/orders"]
        },
        {
          label: "消息",
          path: "/message",
          icon: "el-icon-bell",
          aliases: ["/message"]
        },
        {
          label: "发布",
          path: "/post-product",
          icon: "el-icon-plus",
          aliases: ["/post-product"]
        }
      ]
    };
  },
  computed: {
    visibleNavItems() {
      return this.loginStatus
        ? this.navItems
        : this.navItems.filter(item => item.path === "/product");
    },
    avatarUrl() {
      return toFullImageUrl(this.userInfo.userAvatar || "");
    },
    displayName() {
      return this.userInfo.userName || this.userInfo.userAccount || "同学";
    }
  },
  created() {
    this.loadLoginStatus();
    const currentPath = this.$route.path;
    if ((currentPath === "/" || currentPath === "/user") && getToken()) {
      this.handleRouteSelect("/product");
    }
  },
  methods: {
    loginOperation() {
      this.$router.push("/login");
    },
    fetch() {
      setSearchKey(this.key);
      this.handleRouteSelect(this.searchPath);
    },
    handleRouteSelect(path) {
      if (this.$router.currentRoute.fullPath !== path) {
        this.$router.push(path);
      }
    },
    isPathActive(aliases) {
      return aliases.includes(this.$route.path);
    },
    isNavActive(item) {
      if (!this.loginStatus && item.path !== "/product") {
        return false;
      }
      return this.isPathActive(item.aliases);
    },
    loadLoginStatus() {
      const token = getToken();
      if (!token) {
        this.loginStatus = false;
        return;
      }
      this.auth();
    },
    auth() {
      this.$axios
        .get("/user/auth")
        .then(res => {
          const { data } = res;
          if (data.code === 200) {
            setUserInfo(data.data);
            this.userInfo = data.data;
          }
          this.loginStatus = data.code === 200;
        })
        .catch(error => {
          this.loginStatus = false;
          this.userInfo = {};
          console.log("token 校验异常：", error);
        });
    }
  }
};
</script>

<style scoped lang="scss">
.user-home {
  min-height: 100vh;
  background: radial-gradient(
      circle at top left,
      rgba(34, 197, 94, 0.16),
      transparent 28%
    ),
    radial-gradient(
      circle at top right,
      rgba(59, 130, 246, 0.14),
      transparent 28%
    ),
    linear-gradient(
      180deg,
      #0f172a 0,
      #132238 420px,
      #f3f7fb 420px,
      #f3f7fb 100%
    );
}

.home-header {
  padding: 22px 28px 0;
}

.header-shell,
.hero-shell,
.content-shell {
  width: min(1280px, 100%);
  margin: 0 auto;
}

.header-shell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 24px;
  border-radius: 26px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 20px 50px rgba(2, 6, 23, 0.22);
  backdrop-filter: blur(14px);
}

.brand-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-copy strong {
  color: #ffffff;
  font-size: 15px;
}

.brand-copy span {
  color: rgba(255, 255, 255, 0.62);
  font-size: 12px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.nav-pill,
.quick-action,
.login-entry,
.avatar-entry {
  border: none;
  cursor: pointer;
  transition: all 0.22s ease;
}

.nav-pill {
  min-height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  background: transparent;
  color: rgba(255, 255, 255, 0.74);
  font-size: 14px;
  font-weight: 600;
}

.nav-pill:hover,
.nav-pill.active {
  color: #0f172a;
  background: linear-gradient(135deg, #ffffff, #dcfce7);
}

.header-tools {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 300px;
  height: 46px;
  padding: 0 12px 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
}

.search-box i {
  color: #94a3b8;
  font-size: 16px;
}

.search-box input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #0f172a;
  font-size: 14px;
}

.search-box button {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.quick-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.84);
  font-size: 13px;
  font-weight: 600;
}

.quick-action:hover,
.quick-action.active {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
}

.login-entry {
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
}

.avatar-entry {
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.6);
}

.hero-shell {
  padding: 24px 0 0;
}

.hero-card {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 20px;
  padding: 28px;
  border-radius: 30px;
  background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.96),
      rgba(248, 250, 252, 0.9)
    ),
    linear-gradient(135deg, rgba(34, 197, 94, 0.12), rgba(59, 130, 246, 0.08));
  box-shadow: 0 26px 60px rgba(15, 23, 42, 0.12);
}

.hero-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(220, 252, 231, 0.92);
  color: #15803d;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-main h1 {
  margin: 18px 0 0;
  color: #0f172a;
  font-size: 38px;
  line-height: 1.22;
}

.hero-main p {
  margin: 16px 0 0;
  max-width: 720px;
  color: #475569;
  font-size: 15px;
  line-height: 1.9;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 24px;
}

.hero-primary,
.hero-secondary {
  min-height: 46px;
  padding: 0 18px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.22s ease;
}

.hero-primary {
  border: none;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #ffffff;
  box-shadow: 0 16px 28px rgba(34, 197, 94, 0.22);
}

.hero-secondary {
  border: 1px solid #dbeafe;
  background: #ffffff;
  color: #2563eb;
}

.hero-side {
  display: flex;
  align-items: stretch;
}

.profile-card,
.guest-panel {
  width: 100%;
  border-radius: 26px;
  padding: 24px;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  color: #ffffff;
}

.profile-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.profile-avatar {
  width: 88px;
  height: 88px;
  border-radius: 24px;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.2);
}

.profile-name {
  margin-top: 18px;
  font-size: 24px;
  font-weight: 700;
}

.profile-meta {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.profile-tags span {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 12px;
}

.guest-panel {
  display: grid;
  gap: 14px;
}

.guest-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
}

.guest-item i {
  color: #86efac;
  font-size: 22px;
  margin-top: 2px;
}

.guest-item strong {
  display: block;
  font-size: 15px;
}

.guest-item span {
  display: block;
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  line-height: 1.7;
}

.content-shell {
  padding: 24px 0 36px;
}

@media (max-width: 1200px) {
  .header-shell {
    flex-direction: column;
    align-items: stretch;
  }

  .nav-links {
    justify-content: center;
  }

  .header-tools {
    justify-content: center;
  }

  .hero-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .user-home {
    background: linear-gradient(
      180deg,
      #0f172a 0,
      #132238 500px,
      #f3f7fb 500px,
      #f3f7fb 100%
    );
  }

  .home-header {
    padding: 16px 14px 0;
  }

  .header-shell {
    padding: 16px;
    border-radius: 20px;
  }

  .search-box {
    min-width: 100%;
  }

  .hero-shell {
    padding-top: 16px;
  }

  .hero-card {
    padding: 20px;
    border-radius: 24px;
  }

  .hero-main h1 {
    font-size: 30px;
  }
}
</style>
