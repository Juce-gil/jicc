<template>
  <section class="user-page-section" :class="{ 'is-compact': compact }">
    <div
      v-if="eyebrow || title || description || $slots.actions"
      class="section-header"
    >
      <div class="header-copy">
        <span v-if="eyebrow" class="eyebrow">{{ eyebrow }}</span>
        <h2 v-if="title">{{ title }}</h2>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="header-actions">
        <slot name="actions"></slot>
      </div>
    </div>

    <div class="section-body">
      <slot></slot>
    </div>
  </section>
</template>

<script>
export default {
  name: "UserPageSection",
  props: {
    eyebrow: {
      type: String,
      default: ""
    },
    title: {
      type: String,
      default: ""
    },
    description: {
      type: String,
      default: ""
    },
    compact: {
      type: Boolean,
      default: false
    }
  }
};
</script>

<style scoped lang="scss">
.user-page-section {
  padding: 26px 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(12px);
}

.user-page-section.is-compact {
  padding: 22px 24px;
  border-radius: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
  margin-bottom: 22px;
}

.header-copy {
  min-width: 0;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  color: #16a34a;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.header-copy h2 {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.25;
}

.header-copy p {
  margin: 10px 0 0;
  max-width: 700px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.85;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.section-body {
  min-width: 0;
}

@media (max-width: 768px) {
  .user-page-section,
  .user-page-section.is-compact {
    padding: 20px 18px;
    border-radius: 22px;
  }

  .section-header {
    flex-direction: column;
    margin-bottom: 18px;
  }

  .header-copy h2 {
    font-size: 24px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
