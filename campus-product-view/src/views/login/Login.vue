<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="hero-panel">
        <div class="brand-row">
          <Logo :name="copy.brandShort" :bag="'#0f172a'" />
          <span class="brand-tag">{{ copy.brandTag }}</span>
        </div>

        <div class="hero-copy">
          <p class="eyebrow">{{ copy.welcome }}</p>
          <h1>{{ copy.title }}</h1>
          <p class="hero-desc">{{ copy.subtitle }}</p>
        </div>

        <div class="feature-list">
          <div
            v-for="item in highlights"
            :key="item.title"
            class="feature-card"
          >
            <div class="feature-icon">
              <i :class="item.icon"></i>
            </div>
            <div class="feature-text">
              <div class="feature-title">{{ item.title }}</div>
              <div class="feature-desc">{{ item.desc }}</div>
            </div>
          </div>
        </div>

        <div class="hero-illustration">
          <div class="glow glow-1"></div>
          <div class="glow glow-2"></div>
          <img src="bag.png" :alt="copy.illustrationAlt" />
        </div>
      </section>

      <section class="form-panel">
        <div class="form-header">
          <p class="form-kicker">{{ copy.formKicker }}</p>
          <h2>{{ copy.formTitle }}</h2>
          <p>{{ copy.formDesc }}</p>
        </div>

        <div class="form-item">
          <label>{{ copy.accountLabel }}</label>
          <div class="input-wrapper" :class="{ focused: focusField === 'act' }">
            <i class="el-icon-user-solid"></i>
            <input
              v-model.trim="act"
              :placeholder="copy.accountPlaceholder"
              autocomplete="username"
              @focus="focusField = 'act'"
              @blur="focusField = ''"
              @keyup.enter="login"
            />
          </div>
        </div>

        <div class="form-item">
          <label>{{ copy.passwordLabel }}</label>
          <div class="input-wrapper" :class="{ focused: focusField === 'pwd' }">
            <i class="el-icon-lock"></i>
            <input
              v-model="pwd"
              type="password"
              :placeholder="copy.passwordPlaceholder"
              autocomplete="current-password"
              @focus="focusField = 'pwd'"
              @blur="focusField = ''"
              @keyup.enter="login"
            />
          </div>
        </div>

        <button
          type="button"
          class="login-btn"
          :disabled="!canSubmit || isSubmitting"
          @click="login"
        >
          {{ isSubmitting ? copy.loadingText : copy.loginText }}
        </button>

        <div class="register-entry">
          <span>{{ copy.noAccount }}</span>
          <button type="button" class="link-btn" @click="toDoRegister">
            {{ copy.registerText }}
          </button>
        </div>

        <div class="tips-panel">
          <div class="tip-item">
            <i class="el-icon-lock"></i>
            <span>{{ copy.safeTip }}</span>
          </div>
          <div class="tip-item">
            <i class="el-icon-chat-dot-round"></i>
            <span>{{ copy.serviceTip }}</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import md5 from "js-md5";
import request from "@/utils/request.js";
import { setToken } from "@/utils/storage.js";
import Logo from "@/components/Logo.vue";

const DELAY_TIME = 1300;

export default {
  name: "Login",
  components: { Logo },
  data() {
    return {
      act: "",
      pwd: "",
      isSubmitting: false,
      focusField: "",
      copy: {
        brandShort: "\u6821\u56ed\u4ea4\u6613",
        brandTag: "\u5b89\u5168\u3001\u9ad8\u6548\u3001\u7701\u5fc3",
        welcome: "\u6b22\u8fce\u56de\u6765",
        title:
          "\u8ba9\u95f2\u7f6e\u5728\u6821\u56ed\u91cc\u5feb\u901f\u6d41\u8f6c",
        subtitle:
          "\u5728\u8fd9\u91cc\u53d1\u5e03\u3001\u6d4f\u89c8\u548c\u6c9f\u901a\uff0c\u8ba9\u6bcf\u4e00\u4ef6\u95f2\u7f6e\u90fd\u80fd\u627e\u5230\u5408\u9002\u7684\u65b0\u4e3b\u4eba",
        illustrationAlt: "\u6821\u56ed\u4ea4\u6613\u63d2\u56fe",
        formKicker: "\u8d26\u53f7\u767b\u5f55",
        formTitle: "\u5f00\u59cb\u4f60\u7684\u6821\u56ed\u4ea4\u6613",
        formDesc:
          "\u767b\u5f55\u540e\u53ef\u67e5\u770b\u5546\u54c1\u3001\u8ddf\u8fdb\u8ba2\u5355\uff0c\u4e5f\u53ef\u4ee5\u7acb\u5373\u53d1\u5e03\u4f60\u7684\u95f2\u7f6e\u597d\u7269",
        accountLabel: "\u8d26\u53f7",
        accountPlaceholder: "\u8bf7\u8f93\u5165\u8d26\u53f7",
        passwordLabel: "\u5bc6\u7801",
        passwordPlaceholder: "\u8bf7\u8f93\u5165\u5bc6\u7801",
        loginText: "\u767b\u5f55",
        loadingText: "\u767b\u5f55\u4e2d...",
        noAccount: "\u8fd8\u6ca1\u6709\u8d26\u53f7\uff1f",
        registerText: "\u53bb\u6ce8\u518c",
        safeTip:
          "\u6211\u4eec\u4f1a\u5bf9\u767b\u5f55\u4fe1\u606f\u8fdb\u884c\u5b89\u5168\u6821\u9a8c\uff0c\u8bf7\u4fdd\u7ba1\u597d\u4f60\u7684\u8d26\u53f7\u5bc6\u7801",
        serviceTip:
          "\u5982\u679c\u662f\u7b2c\u4e00\u6b21\u4f7f\u7528\uff0c\u53ef\u4ee5\u5148\u6ce8\u518c\u8d26\u53f7\uff0c\u518d\u5f00\u59cb\u6d4f\u89c8\u548c\u53d1\u5e03",
        emptyTitle: "\u586b\u5199\u6821\u9a8c",
        emptyText: "\u8d26\u53f7\u6216\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a",
        errorTitle: "\u767b\u5f55\u5931\u8d25",
        networkError:
          "\u7f51\u7edc\u8bf7\u6c42\u51fa\u9519\uff0c\u8bf7\u68c0\u67e5\u540e\u7aef\u670d\u52a1\u662f\u5426\u6b63\u5e38\u8fd0\u884c",
        successTitle: "\u767b\u5f55\u6210\u529f",
        successText: "\u5373\u5c06\u8df3\u8f6c\u5230\u5bf9\u5e94\u9875\u9762"
      },
      highlights: [
        {
          icon: "el-icon-shopping-bag-1",
          title: "\u95f2\u7f6e\u597d\u7269\u66f4\u597d\u5356",
          desc:
            "\u6e05\u6670\u5c55\u793a\u3001\u5feb\u901f\u53d1\u5e03\uff0c\u8ba9\u4f60\u7684\u7269\u54c1\u66f4\u5bb9\u6613\u88ab\u770b\u89c1"
        },
        {
          icon: "el-icon-chat-dot-round",
          title: "\u6c9f\u901a\u6548\u7387\u66f4\u9ad8",
          desc:
            "\u5356\u5bb6\u4e0e\u4e70\u5bb6\u53ef\u4ee5\u5feb\u901f\u6c9f\u901a\uff0c\u964d\u4f4e\u4ea4\u6613\u6210\u672c"
        },
        {
          icon: "el-icon-lock",
          title: "\u6821\u56ed\u4ea4\u6613\u66f4\u653e\u5fc3",
          desc:
            "\u56f4\u7ed5\u6821\u56ed\u95f2\u7f6e\u573a\u666f\u8bbe\u8ba1\uff0c\u8ba9\u4ea4\u6613\u66f4\u76f4\u89c2\u3001\u66f4\u5b89\u5fc3"
        }
      ]
    };
  },
  computed: {
    canSubmit() {
      return Boolean(this.act && this.pwd);
    }
  },
  methods: {
    toDoRegister() {
      this.$router.push("/register");
    },
    getSafeRedirectPath() {
      const redirectPath =
        typeof this.$route.query.redirect === "string"
          ? this.$route.query.redirect
          : "";
      return redirectPath.startsWith("/") ? redirectPath : "";
    },
    async login() {
      if (this.isSubmitting) {
        return;
      }

      if (!this.act || !this.pwd) {
        this.$swal.fire({
          title: this.copy.emptyTitle,
          text: this.copy.emptyText,
          icon: "error",
          showConfirmButton: false,
          timer: DELAY_TIME
        });
        return;
      }

      this.isSubmitting = true;
      const hashedPwd = md5(md5(this.pwd));
      const paramDTO = { userAccount: this.act, userPwd: hashedPwd };

      try {
        const { data } = await request.post("user/login", paramDTO);

        if (data.code !== 200) {
          this.$swal.fire({
            title: this.copy.errorTitle,
            text: data.msg,
            icon: "error",
            showConfirmButton: false,
            timer: DELAY_TIME
          });
          return;
        }

        setToken(data.data.token);
        const { role } = data.data;
        sessionStorage.setItem("role", role);

        this.$swal.fire({
          title: this.copy.successTitle,
          text: this.copy.successText,
          icon: "success",
          showConfirmButton: false,
          timer: DELAY_TIME
        });

        setTimeout(() => {
          this.navigateToRole(role);
        }, DELAY_TIME);
      } catch (error) {
        console.error("login request error:", error);
        let errorMsg = this.copy.networkError;

        if (error.response && error.response.data && error.response.data.msg) {
          errorMsg = error.response.data.msg;
        } else if (error.message) {
          errorMsg = error.message;
        }

        this.$swal.fire({
          title: this.copy.errorTitle,
          text: errorMsg,
          icon: "error",
          showConfirmButton: false,
          timer: DELAY_TIME
        });
      } finally {
        this.isSubmitting = false;
      }
    },
    navigateToRole(role) {
      const redirectPath = this.getSafeRedirectPath();
      if (redirectPath) {
        this.$router.push(redirectPath);
        return;
      }

      switch (role) {
        case 1:
          this.$router.push("/admin");
          break;
        case 2:
          this.$router.push("/user");
          break;
        default:
          console.warn("unknown role:", role);
          break;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  padding: 28px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: radial-gradient(
      circle at top left,
      rgba(34, 197, 94, 0.18),
      transparent 28%
    ),
    radial-gradient(
      circle at bottom right,
      rgba(59, 130, 246, 0.18),
      transparent 30%
    ),
    linear-gradient(135deg, #effaf3 0%, #f8fbff 52%, #eef3ff 100%);
}

.login-page::before,
.login-page::after {
  content: "";
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.login-page::before {
  width: 320px;
  height: 320px;
  left: -100px;
  top: -80px;
  background: rgba(34, 197, 94, 0.14);
  filter: blur(8px);
}

.login-page::after {
  width: 380px;
  height: 380px;
  right: -140px;
  bottom: -140px;
  background: rgba(59, 130, 246, 0.12);
  filter: blur(12px);
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1100px, 100%);
  min-height: 700px;
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  border-radius: 30px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 28px 80px rgba(15, 23, 42, 0.16);
  backdrop-filter: blur(18px);
}

.hero-panel,
.form-panel {
  box-sizing: border-box;
}

.hero-panel {
  position: relative;
  padding: 48px 54px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.55),
      rgba(255, 255, 255, 0.24)
    ),
    linear-gradient(135deg, rgba(34, 197, 94, 0.08), rgba(59, 130, 246, 0.04));
}

.brand-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.brand-tag {
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #166534;
  background: rgba(220, 252, 231, 0.78);
  border: 1px solid rgba(34, 197, 94, 0.12);
}

.hero-copy {
  margin-top: 24px;
}

.eyebrow {
  margin: 0 0 12px;
  color: #16a34a;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  max-width: 520px;
  color: #0f172a;
  font-size: 44px;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.hero-desc {
  margin: 18px 0 0;
  max-width: 520px;
  color: #475569;
  font-size: 16px;
  line-height: 1.8;
}

.feature-list {
  margin: 34px 0 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.feature-card {
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.06);
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  color: #16a34a;
  background: rgba(220, 252, 231, 0.92);
  font-size: 20px;
}

.feature-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.45;
}

.feature-desc {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.hero-illustration {
  position: relative;
  margin-top: 40px;
  padding: 28px;
  min-height: 280px;
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(
    135deg,
    rgba(15, 23, 42, 0.92),
    rgba(30, 41, 59, 0.86)
  );
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-illustration img {
  position: relative;
  z-index: 1;
  width: min(100%, 340px);
  object-fit: contain;
  filter: drop-shadow(0 18px 40px rgba(15, 23, 42, 0.3));
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(4px);
}

.glow-1 {
  width: 180px;
  height: 180px;
  left: 20px;
  top: 24px;
  background: rgba(34, 197, 94, 0.35);
}

.glow-2 {
  width: 220px;
  height: 220px;
  right: 18px;
  bottom: -50px;
  background: rgba(59, 130, 246, 0.28);
}

.form-panel {
  padding: 56px 52px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
}

.form-header {
  margin-bottom: 28px;
}

.form-kicker {
  margin: 0 0 10px;
  color: #16a34a;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.form-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 32px;
  line-height: 1.28;
}

.form-header p:last-child {
  margin: 12px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}

.form-item + .form-item {
  margin-top: 18px;
}

.form-item label {
  display: inline-block;
  margin-bottom: 10px;
  color: #334155;
  font-size: 14px;
  font-weight: 600;
}

.input-wrapper {
  height: 58px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  box-sizing: border-box;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.25s ease;
}

.input-wrapper:hover {
  border-color: #cbd5e1;
  background: #ffffff;
}

.input-wrapper.focused {
  border-color: rgba(34, 197, 94, 0.55);
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.1);
}

.input-wrapper i {
  color: #94a3b8;
  font-size: 18px;
}

.input-wrapper input {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  color: #0f172a;
  font-size: 15px;
}

.input-wrapper input::placeholder {
  color: #94a3b8;
}

.login-btn {
  margin-top: 26px;
  height: 58px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 16px 28px rgba(34, 197, 94, 0.24);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 20px 34px rgba(34, 197, 94, 0.28);
}

.login-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  box-shadow: none;
}

.register-entry {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-size: 14px;
}

.link-btn {
  padding: 0;
  border: none;
  background: transparent;
  color: #16a34a;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.link-btn:hover {
  color: #15803d;
}

.tips-panel {
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid #e5e7eb;
  display: grid;
  gap: 14px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.tip-item i {
  margin-top: 2px;
  color: #16a34a;
  font-size: 15px;
}

@media (max-width: 1080px) {
  .feature-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .login-page {
    padding: 18px;
  }

  .login-shell {
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .hero-panel {
    padding: 36px 28px 0;
  }

  .form-panel {
    padding: 36px 28px 32px;
  }

  .hero-illustration {
    margin-top: 28px;
    min-height: 220px;
  }
}

@media (max-width: 640px) {
  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-desc {
    font-size: 14px;
  }

  .brand-row {
    align-items: flex-start;
  }

  .brand-tag {
    font-size: 11px;
  }

  .hero-illustration {
    display: none;
  }

  .form-header h2 {
    font-size: 26px;
  }
}
</style>
