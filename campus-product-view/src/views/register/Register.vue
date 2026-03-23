<template>
  <div class="register-page">
    <div class="register-shell">
      <section class="form-panel">
        <div class="brand-row">
          <Logo :name="copy.brandShort" :bag="'#0f172a'" />
          <button type="button" class="back-home-btn" @click="toDoLogin">
            {{ copy.backToLogin }}
          </button>
        </div>

        <div class="form-header">
          <p class="form-kicker">{{ copy.formKicker }}</p>
          <h1>{{ copy.formTitle }}</h1>
          <p>{{ copy.formDesc }}</p>
        </div>

        <div class="form-grid">
          <div class="form-item">
            <label>{{ copy.accountLabel }}</label>
            <div
              class="input-wrapper"
              :class="{ focused: focusField === 'act' }"
            >
              <i class="el-icon-user-solid"></i>
              <input
                v-model.trim="act"
                :placeholder="copy.accountPlaceholder"
                autocomplete="username"
                @focus="focusField = 'act'"
                @blur="focusField = ''"
              />
            </div>
          </div>

          <div class="form-item">
            <label>{{ copy.nameLabel }}</label>
            <div
              class="input-wrapper"
              :class="{ focused: focusField === 'name' }"
            >
              <i class="el-icon-edit"></i>
              <input
                v-model.trim="name"
                :placeholder="copy.namePlaceholder"
                autocomplete="nickname"
                @focus="focusField = 'name'"
                @blur="focusField = ''"
              />
            </div>
          </div>

          <div class="form-item">
            <label>{{ copy.passwordLabel }}</label>
            <div
              class="input-wrapper"
              :class="{ focused: focusField === 'pwd' }"
            >
              <i class="el-icon-lock"></i>
              <input
                v-model="pwd"
                type="password"
                :placeholder="copy.passwordPlaceholder"
                autocomplete="new-password"
                @focus="focusField = 'pwd'"
                @blur="focusField = ''"
              />
            </div>
          </div>

          <div class="form-item">
            <label>{{ copy.confirmLabel }}</label>
            <div
              class="input-wrapper"
              :class="{ focused: focusField === 'pwdConfirm' }"
            >
              <i class="el-icon-circle-check"></i>
              <input
                v-model="pwdConfirm"
                type="password"
                :placeholder="copy.confirmPlaceholder"
                autocomplete="new-password"
                @focus="focusField = 'pwdConfirm'"
                @blur="focusField = ''"
                @keyup.enter="registerFunc"
              />
            </div>
          </div>
        </div>

        <button
          type="button"
          class="register-btn"
          :disabled="!canSubmit || isSubmitting"
          @click="registerFunc"
        >
          {{ isSubmitting ? copy.loadingText : copy.registerText }}
        </button>

        <div class="login-entry">
          <span>{{ copy.hasAccount }}</span>
          <button type="button" class="link-btn" @click="toDoLogin">
            {{ copy.loginNow }}
          </button>
        </div>

        <div class="tips-panel">
          <div class="tip-item">
            <i class="el-icon-s-check"></i>
            <span>{{ copy.safeTip }}</span>
          </div>
          <div class="tip-item">
            <i class="el-icon-s-promotion"></i>
            <span>{{ copy.publishTip }}</span>
          </div>
        </div>
      </section>

      <section class="hero-panel">
        <div class="hero-copy">
          <span class="hero-badge">{{ copy.heroBadge }}</span>
          <h2>{{ copy.heroTitle }}</h2>
          <p>{{ copy.heroDesc }}</p>
        </div>

        <div class="benefit-list">
          <div
            v-for="item in highlights"
            :key="item.title"
            class="benefit-card"
          >
            <div class="benefit-icon">
              <i :class="item.icon"></i>
            </div>
            <div>
              <div class="benefit-title">{{ item.title }}</div>
              <div class="benefit-desc">{{ item.desc }}</div>
            </div>
          </div>
        </div>

        <div class="status-board">
          <div class="status-card">
            <span class="status-label">{{ copy.cardLabel }}</span>
            <strong>{{ copy.cardTitle }}</strong>
            <p>{{ copy.cardDesc }}</p>
          </div>
          <div class="floating-badge badge-primary">
            <i class="el-icon-shopping-cart-2"></i>
            <span>{{ copy.primaryBadge }}</span>
          </div>
          <div class="floating-badge badge-secondary">
            <i class="el-icon-chat-dot-round"></i>
            <span>{{ copy.secondaryBadge }}</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import md5 from "js-md5";
import request from "@/utils/request.js";
import Logo from "@/components/Logo.vue";

const DELAY_TIME = 1300;

export default {
  name: "Register",
  components: { Logo },
  data() {
    return {
      act: "",
      name: "",
      pwd: "",
      pwdConfirm: "",
      isSubmitting: false,
      focusField: "",
      copy: {
        brandShort: "\u6821\u56ed\u4ea4\u6613",
        backToLogin: "\u8fd4\u56de\u767b\u5f55",
        formKicker: "\u521b\u5efa\u8d26\u53f7",
        formTitle:
          "\u6ce8\u518c\u4f60\u7684\u6821\u56ed\u4ea4\u6613\u8d26\u53f7",
        formDesc:
          "\u51e0\u6b65\u5373\u53ef\u5b8c\u6210\u6ce8\u518c\uff0c\u5f00\u59cb\u6d4f\u89c8\u3001\u53d1\u5e03\u4e0e\u7ba1\u7406\u4f60\u7684\u95f2\u7f6e\u5546\u54c1",
        accountLabel: "\u8d26\u53f7",
        accountPlaceholder: "\u8bf7\u8f93\u5165\u65b0\u8d26\u53f7",
        nameLabel: "\u6635\u79f0",
        namePlaceholder: "\u8bf7\u8f93\u5165\u4f60\u7684\u6635\u79f0",
        passwordLabel: "\u5bc6\u7801",
        passwordPlaceholder: "\u8bf7\u8f93\u5165\u5bc6\u7801",
        confirmLabel: "\u786e\u8ba4\u5bc6\u7801",
        confirmPlaceholder: "\u8bf7\u518d\u6b21\u8f93\u5165\u5bc6\u7801",
        registerText: "\u7acb\u5373\u6ce8\u518c",
        loadingText: "\u6ce8\u518c\u4e2d...",
        hasAccount: "\u5df2\u6709\u8d26\u53f7\uff1f",
        loginNow: "\u53bb\u767b\u5f55",
        safeTip:
          "\u8bf7\u4f7f\u7528\u4f60\u5e38\u7528\u7684\u8d26\u53f7\u4e0e\u6635\u79f0\uff0c\u65b9\u4fbf\u540e\u7eed\u7ba1\u7406\u4e2a\u4eba\u5546\u54c1\u4e0e\u8ba2\u5355",
        publishTip:
          "\u6ce8\u518c\u540e\u5373\u53ef\u53d1\u5e03\u5546\u54c1\u3001\u7ef4\u62a4\u4e2a\u4eba\u4e3b\u9875\uff0c\u5e76\u4e0e\u5176\u4ed6\u540c\u5b66\u5feb\u901f\u6c9f\u901a",
        heroBadge: "\u65b0\u7528\u6237\u5f15\u5bfc",
        heroTitle:
          "\u628a\u4f60\u7684\u95f2\u7f6e\u7269\u54c1\u5e26\u5165\u66f4\u9ad8\u6548\u7684\u6821\u56ed\u6d41\u8f6c",
        heroDesc:
          "\u6ce8\u518c\u4e4b\u540e\uff0c\u4f60\u53ef\u4ee5\u7acb\u5373\u53d1\u5e03\u5546\u54c1\uff0c\u7ba1\u7406\u6536\u85cf\u4e0e\u8ba2\u5355\uff0c\u8ba9\u4ea4\u6613\u66f4\u7b80\u5355",
        cardLabel: "\u6ce8\u518c\u540e\u5373\u53ef",
        cardTitle:
          "\u53d1\u5e03\u5546\u54c1 \u00b7 \u7ba1\u7406\u8ba2\u5355 \u00b7 \u67e5\u770b\u6d88\u606f",
        cardDesc:
          "\u7528\u4e00\u4e2a\u8d26\u53f7\u4e32\u8054\u6574\u4e2a\u6821\u56ed\u4ea4\u6613\u6d41\u7a0b\uff0c\u8ba9\u6bcf\u6b21\u4ea4\u6613\u66f4\u6d41\u7545",
        primaryBadge: "\u5feb\u901f\u53d1\u5e03",
        secondaryBadge: "\u5b9e\u65f6\u6c9f\u901a",
        emptyTitle: "\u586b\u5199\u6821\u9a8c",
        emptyText:
          "\u8d26\u53f7\u3001\u6635\u79f0\u6216\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a",
        mismatchText:
          "\u4e24\u6b21\u8f93\u5165\u7684\u5bc6\u7801\u4e0d\u4e00\u81f4",
        errorTitle: "\u6ce8\u518c\u5931\u8d25",
        successTitle: "\u6ce8\u518c\u6210\u529f",
        successText: "\u5373\u5c06\u8fd4\u56de\u767b\u5f55\u9875",
        networkError:
          "\u7f51\u7edc\u8bf7\u6c42\u51fa\u9519\uff0c\u8bf7\u68c0\u67e5\u540e\u7aef\u670d\u52a1\u662f\u5426\u6b63\u5e38\u8fd0\u884c"
      },
      highlights: [
        {
          icon: "el-icon-sell",
          title: "\u66f4\u5feb\u53d1\u5e03",
          desc:
            "\u4e0a\u67b6\u5546\u54c1\u66f4\u9ad8\u6548\uff0c\u5feb\u901f\u88ab\u540c\u5b66\u770b\u5230"
        },
        {
          icon: "el-icon-collection-tag",
          title: "\u66f4\u597d\u7ba1\u7406",
          desc:
            "\u96c6\u4e2d\u67e5\u770b\u6536\u85cf\u3001\u8ba2\u5355\u4e0e\u4e2a\u4eba\u53d1\u5e03\u8bb0\u5f55"
        },
        {
          icon: "el-icon-message-solid",
          title: "\u66f4\u987a\u7545\u6c9f\u901a",
          desc:
            "\u4e0e\u4e70\u5bb6\u5356\u5bb6\u5feb\u901f\u4ea4\u6d41\uff0c\u964d\u4f4e\u6c9f\u901a\u6210\u672c"
        }
      ]
    };
  },
  computed: {
    canSubmit() {
      return Boolean(this.act && this.name && this.pwd && this.pwdConfirm);
    }
  },
  methods: {
    toDoLogin() {
      this.$router.push("/login");
    },
    async registerFunc() {
      if (this.isSubmitting) {
        return;
      }

      if (!this.act || !this.pwd || !this.pwdConfirm || !this.name) {
        this.$swal.fire({
          title: this.copy.emptyTitle,
          text: this.copy.emptyText,
          icon: "error",
          showConfirmButton: false,
          timer: DELAY_TIME
        });
        return;
      }

      if (this.pwd !== this.pwdConfirm) {
        this.$swal.fire({
          title: this.copy.emptyTitle,
          text: this.copy.mismatchText,
          icon: "error",
          showConfirmButton: false,
          timer: DELAY_TIME
        });
        return;
      }

      this.isSubmitting = true;
      const hashedPwd = md5(md5(this.pwd));
      const paramDTO = {
        userAccount: this.act,
        userPwd: hashedPwd,
        userName: this.name
      };

      try {
        const { data } = await request.post("user/register", paramDTO);

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

        this.$swal.fire({
          title: this.copy.successTitle,
          text: this.copy.successText,
          icon: "success",
          showConfirmButton: false,
          timer: DELAY_TIME
        });

        setTimeout(() => {
          this.$router.push("/login");
        }, DELAY_TIME);
      } catch (error) {
        console.error("register request error:", error);
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
    }
  }
};
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  padding: 28px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(
      circle at top right,
      rgba(14, 165, 233, 0.16),
      transparent 28%
    ),
    radial-gradient(
      circle at bottom left,
      rgba(34, 197, 94, 0.18),
      transparent 32%
    ),
    linear-gradient(135deg, #f6fbff 0%, #f4fff8 48%, #eef8ff 100%);
}

.register-shell {
  width: min(1120px, 100%);
  min-height: 720px;
  display: grid;
  grid-template-columns: 0.98fr 1.02fr;
  border-radius: 30px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.76);
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 28px 80px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(18px);
}

.form-panel,
.hero-panel {
  box-sizing: border-box;
}

.form-panel {
  padding: 44px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
}

.brand-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.back-home-btn {
  height: 40px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
}

.back-home-btn:hover {
  border-color: #bfdbfe;
  background: #dbeafe;
}

.form-header {
  margin: 28px 0 26px;
}

.form-kicker {
  margin: 0 0 10px;
  color: #16a34a;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.form-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 34px;
  line-height: 1.25;
}

.form-header p:last-child {
  margin: 14px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px 16px;
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
  border-color: rgba(14, 165, 233, 0.45);
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.1);
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

.register-btn {
  margin-top: 26px;
  height: 58px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 18px 30px rgba(37, 99, 235, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 22px 36px rgba(37, 99, 235, 0.28);
}

.register-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  box-shadow: none;
}

.login-entry {
  margin-top: 18px;
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
  color: #2563eb;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.link-btn:hover {
  color: #1d4ed8;
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
  line-height: 1.75;
}

.tip-item i {
  margin-top: 2px;
  color: #0ea5e9;
  font-size: 15px;
}

.hero-panel {
  position: relative;
  padding: 56px 54px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.26),
      rgba(255, 255, 255, 0.08)
    ),
    linear-gradient(135deg, rgba(14, 165, 233, 0.88), rgba(37, 99, 235, 0.92));
  color: #ffffff;
}

.hero-panel::before,
.hero-panel::after {
  content: "";
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.hero-panel::before {
  width: 220px;
  height: 220px;
  right: -60px;
  top: -50px;
  background: rgba(255, 255, 255, 0.12);
}

.hero-panel::after {
  width: 280px;
  height: 280px;
  left: -80px;
  bottom: -110px;
  background: rgba(34, 197, 94, 0.16);
}

.hero-copy,
.benefit-list,
.status-board {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.14);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h2 {
  margin: 20px 0 0;
  max-width: 480px;
  font-size: 42px;
  line-height: 1.22;
  letter-spacing: -0.02em;
}

.hero-copy p {
  margin: 18px 0 0;
  max-width: 480px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 15px;
  line-height: 1.85;
}

.benefit-list {
  margin-top: 34px;
  display: grid;
  gap: 16px;
}

.benefit-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
}

.benefit-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.16);
  font-size: 20px;
}

.benefit-title {
  font-size: 16px;
  font-weight: 700;
}

.benefit-desc {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.84);
  font-size: 13px;
  line-height: 1.7;
}

.status-board {
  position: relative;
  margin-top: 36px;
  padding: 30px;
  min-height: 220px;
  border-radius: 30px;
  background: linear-gradient(
    135deg,
    rgba(15, 23, 42, 0.2),
    rgba(15, 23, 42, 0.08)
  );
  border: 1px solid rgba(255, 255, 255, 0.14);
  overflow: hidden;
}

.status-card {
  position: relative;
  z-index: 1;
  max-width: 320px;
}

.status-label {
  display: inline-block;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.status-card strong {
  display: block;
  font-size: 24px;
  line-height: 1.45;
}

.status-card p {
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.84);
  font-size: 14px;
  line-height: 1.8;
}

.floating-badge {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(10px);
  font-size: 14px;
  font-weight: 700;
}

.badge-primary {
  right: 26px;
  top: 34px;
}

.badge-secondary {
  right: 54px;
  bottom: 26px;
}

@media (max-width: 960px) {
  .register-page {
    padding: 18px;
  }

  .register-shell {
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .form-panel,
  .hero-panel {
    padding: 36px 28px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .brand-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-header h1 {
    font-size: 28px;
  }

  .hero-copy h2 {
    font-size: 32px;
  }

  .status-board {
    padding: 24px 20px;
  }

  .floating-badge {
    position: static;
    margin-top: 14px;
    width: fit-content;
  }
}
</style>
