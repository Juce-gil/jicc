<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showFailToast, showSuccessToast, showToast } from 'vant'
import {
  buyerConfirmTrade,
  cancelReservation,
  queryMyOrders,
  querySellerOrders,
  sellerConfirmReservation,
  sellerConfirmTrade,
  type OrderQuery,
  type OrderRecord
} from '@/api/order'

type ViewMode = 'buyer' | 'seller'

interface ModeOption {
  label: string
  value: ViewMode
}

interface StatusOption {
  label: string
  value: number | null
}

interface TimelineItem {
  key: string
  label: string
  value: string
}

const router = useRouter()

const modeOptions: ModeOption[] = [
  { label: 'Buyer View', value: 'buyer' },
  { label: 'Seller View', value: 'seller' }
]

const statusOptions: StatusOption[] = [
  { label: 'All', value: null },
  { label: 'Pending', value: 1 },
  { label: 'Reserved', value: 2 },
  { label: 'Partial', value: 3 },
  { label: 'Completed', value: 4 },
  { label: 'Cancelled', value: 5 }
]

const activeMode = ref<ViewMode>('buyer')
const selectedStatus = ref<number | null>(null)
const codeKeyword = ref('')
const orders = ref<OrderRecord[]>([])
const loading = ref(false)
const actionOrderId = ref<number | null>(null)
const buyerTotal = ref(0)
const lastLoadedAt = ref('')
let fetchSequence = 0

const pageTitle = computed(() =>
  activeMode.value === 'buyer' ? 'My Reservation Requests' : 'Reservations On My Products'
)

const pageDescription = computed(() =>
  activeMode.value === 'buyer'
    ? 'Track requests you sent, wait for seller confirmation, and finish the meetup flow.'
    : 'Manage buyers who reserved your products and close the offline meetup confirmation flow.'
)

const resultSummary = computed(() => {
  const loaded = orders.value.length
  const total = activeMode.value === 'buyer' ? buyerTotal.value : loaded
  return activeMode.value === 'buyer' ? String(loaded) + ' loaded / ' + String(total) + ' total' : String(loaded) + ' reservation(s)'
})

const emptyHint = computed(() =>
  activeMode.value === 'buyer'
    ? 'No reservation requests matched the current filters.'
    : 'No buyers have reserved your products yet.'
)

const buildQuery = (): OrderQuery => {
  const query: OrderQuery = {}
  const keyword = codeKeyword.value.trim()
  if (keyword) {
    query.code = keyword
  }
  if (selectedStatus.value !== null) {
    query.tradeStatus = selectedStatus.value
  }
  return query
}

const fetchOrders = async () => {
  const currentSequence = ++fetchSequence
  loading.value = true
  try {
    const query = buildQuery()
    const response =
      activeMode.value === 'buyer' ? await queryMyOrders(query) : await querySellerOrders(query)

    if (currentSequence !== fetchSequence) {
      return
    }

    if (response?.code !== 200) {
      orders.value = []
      buyerTotal.value = 0
      showFailToast(response?.msg || 'Failed to load reservation data')
      return
    }

    orders.value = Array.isArray(response.data) ? response.data : []
    buyerTotal.value = activeMode.value === 'buyer' ? response.total ?? orders.value.length : orders.value.length
    lastLoadedAt.value = new Date().toLocaleString()
  } catch (error) {
    if (currentSequence !== fetchSequence) {
      return
    }
    console.error('Failed to load orders', error)
    orders.value = []
    buyerTotal.value = 0
    showFailToast('Failed to load reservation data')
  } finally {
    if (currentSequence === fetchSequence) {
      loading.value = false
    }
  }
}

const applyFilters = async () => {
  await fetchOrders()
}

const resetFilters = async () => {
  codeKeyword.value = ''
  selectedStatus.value = null
  await fetchOrders()
}

const switchMode = (mode: ViewMode) => {
  if (activeMode.value === mode) {
    return
  }
  activeMode.value = mode
}

watch([activeMode, selectedStatus], () => {
  void fetchOrders()
})

onMounted(() => {
  void fetchOrders()
})

const getOrderId = (order: OrderRecord) => {
  if (typeof order.id === 'number') {
    return order.id
  }
  showToast('Invalid order id')
  return null
}

const hasBuyerConfirmed = (order: OrderRecord) => Boolean(order.isConfirm || order.buyerConfirmTime)
const hasSellerConfirmedCompletion = (order: OrderRecord) =>
  Boolean(order.isRefundConfirm || order.sellerFinishTime)

const statusTone = (order: OrderRecord) => {
  switch (order.tradeStatus) {
    case 1:
      return 'warning'
    case 2:
      return 'primary'
    case 3:
      return 'info'
    case 4:
      return 'success'
    case 5:
      return 'danger'
    default:
      return 'neutral'
  }
}

const getStatusLabel = (order: OrderRecord) => {
  switch (order.tradeStatus) {
    case 1:
      return 'Pending Seller Confirmation'
    case 2:
      return 'Reserved For Meetup'
    case 3:
      if (hasBuyerConfirmed(order) && !hasSellerConfirmedCompletion(order)) {
        return 'Buyer Confirmed, Waiting Seller'
      }
      if (!hasBuyerConfirmed(order) && hasSellerConfirmedCompletion(order)) {
        return 'Seller Confirmed, Waiting Buyer'
      }
      return 'Partially Confirmed'
    case 4:
      return 'Completed'
    case 5:
      return 'Cancelled'
    default:
      return 'Unknown Status'
  }
}

const getProgressHint = (order: OrderRecord) => {
  switch (order.tradeStatus) {
    case 1:
      return 'Waiting for the seller to accept this reservation request.'
    case 2:
      return 'Reservation is locked. The meetup can now be arranged offline.'
    case 3:
      if (hasBuyerConfirmed(order) && !hasSellerConfirmedCompletion(order)) {
        return 'Buyer already confirmed the meetup result. Waiting for seller completion confirmation.'
      }
      if (!hasBuyerConfirmed(order) && hasSellerConfirmedCompletion(order)) {
        return 'Seller already confirmed the meetup result. Waiting for buyer completion confirmation.'
      }
      return 'One side has confirmed the meetup result. Waiting for the other side.'
    case 4:
      return 'Both sides confirmed the meetup result. The product is sold.'
    case 5:
      return order.cancelReason ? 'Reservation cancelled: ' + order.cancelReason : 'Reservation has been cancelled.'
    default:
      return 'Status is not recognized.'
  }
}

const formatDateTime = (value?: string | null) => value?.trim() || '--'

const formatCurrency = (value?: number | string | null) => {
  const amount = Number(value ?? 0)
  if (!Number.isFinite(amount)) {
    return '--'
  }
  return '?' + amount.toFixed(2)
}

const formatTotalPrice = (order: OrderRecord) => {
  const price = Number(order.buyPrice ?? 0)
  const quantity = Number(order.buyNumber ?? 0)
  const total = price * quantity
  if (!Number.isFinite(total)) {
    return '--'
  }
  return '?' + total.toFixed(2)
}

const normalizeAssetUrl = (raw: string) => {
  const value = raw.trim()
  if (!value) {
    return ''
  }
  try {
    const origin = window.location.origin
    const url = new URL(value, origin)
    const pathname = url.pathname || ''
    const search = url.search || ''
    const isApiAsset = pathname.startsWith('/api/') || pathname.startsWith('/file/')
    if (!isApiAsset) {
      return /^https?:\/\//i.test(value) ? value : url.toString()
    }
    const normalizedPath = pathname.startsWith('/file/')
      ? '/api/campus-product-sys/v1.0' + pathname
      : pathname.replace('/api/book-manage-sys-api/v1.0', '/api/campus-product-sys/v1.0')
    return origin + normalizedPath + search
  } catch (error) {
    return value
  }
}

const resolveCover = (order: OrderRecord) => {
  const raw = order.productCover?.split(',').map((item) => item.trim()).find(Boolean)
  if (!raw) {
    return ''
  }
  return normalizeAssetUrl(raw)
}

const getCounterpartyLabel = computed(() => (activeMode.value === 'buyer' ? 'Seller' : 'Buyer'))

const getCounterpartyName = (order: OrderRecord) => {
  if (activeMode.value === 'buyer') {
    return order.sellerName?.trim() || (order.sellerId ? 'Seller #' + String(order.sellerId) : 'Unknown seller')
  }
  return order.userName?.trim() || (order.userId ? 'Buyer #' + String(order.userId) : 'Unknown buyer')
}

const openProductDetail = (order: OrderRecord) => {
  if (!order.productId) {
    showToast('Product id is missing')
    return
  }
  router.push({ path: '/goods/detail', query: { productId: String(order.productId) } })
}

const buildTimeline = (order: OrderRecord): TimelineItem[] => {
  const items: TimelineItem[] = [
    { key: 'created', label: 'Created', value: formatDateTime(order.createTime) }
  ]

  if (order.sellerConfirmTime) {
    items.push({ key: 'seller-confirm', label: 'Seller confirmed reservation', value: formatDateTime(order.sellerConfirmTime) })
  }

  const meetupParts = [formatDateTime(order.appointmentTime), order.appointmentAddress?.trim() || '']
    .filter((item) => item && item !== '--')
    .join(' / ')
  if (meetupParts) {
    items.push({ key: 'meetup', label: 'Meetup', value: meetupParts })
  }

  if (order.buyerConfirmTime) {
    items.push({ key: 'buyer-finish', label: 'Buyer confirmed completion', value: formatDateTime(order.buyerConfirmTime) })
  }

  if (order.sellerFinishTime) {
    items.push({ key: 'seller-finish', label: 'Seller confirmed completion', value: formatDateTime(order.sellerFinishTime) })
  }

  if (order.cancelTime) {
    items.push({ key: 'cancelled', label: 'Cancelled', value: formatDateTime(order.cancelTime) })
  }

  return items.filter((item) => item.value !== '--')
}

const canSellerConfirmReservation = (order: OrderRecord) =>
  activeMode.value === 'seller' && order.tradeStatus === 1

const canBuyerConfirmCompletion = (order: OrderRecord) =>
  activeMode.value === 'buyer' &&
  (order.tradeStatus === 2 || order.tradeStatus === 3) &&
  !hasBuyerConfirmed(order)

const canSellerConfirmCompletion = (order: OrderRecord) =>
  activeMode.value === 'seller' &&
  (order.tradeStatus === 2 || order.tradeStatus === 3) &&
  !hasSellerConfirmedCompletion(order)

const canCancelOrder = (order: OrderRecord) => order.tradeStatus !== 4 && order.tradeStatus !== 5

const runOrderAction = async (
  order: OrderRecord,
  options: {
    title: string
    message: string
    successFallback: string
    action: (orderId: number) => Promise<{ code: number; msg: string }>
  }
) => {
  const orderId = getOrderId(order)
  if (orderId === null) {
    return
  }

  try {
    await showConfirmDialog({
      title: options.title,
      message: options.message
    })
  } catch {
    return
  }

  actionOrderId.value = orderId
  try {
    const response = await options.action(orderId)
    if (response?.code !== 200) {
      showFailToast(response?.msg || 'Operation failed')
      return
    }
    showSuccessToast(response?.msg || options.successFallback)
    await fetchOrders()
  } catch (error) {
    console.error('Order action failed', error)
    showFailToast('Operation failed, please try again later')
  } finally {
    actionOrderId.value = null
  }
}

const handleSellerConfirmReservation = async (order: OrderRecord) => {
  await runOrderAction(order, {
    title: 'Confirm Reservation',
    message: 'After confirmation, the product will be locked as RESERVED until the meetup is finished or cancelled.',
    successFallback: 'Reservation confirmed successfully',
    action: sellerConfirmReservation
  })
}

const handleBuyerConfirmCompletion = async (order: OrderRecord) => {
  await runOrderAction(order, {
    title: 'Buyer Completion Confirmation',
    message: 'Please confirm the offline meetup has finished successfully before continuing.',
    successFallback: 'Buyer completion confirmation submitted',
    action: buyerConfirmTrade
  })
}

const handleSellerConfirmCompletion = async (order: OrderRecord) => {
  await runOrderAction(order, {
    title: 'Seller Completion Confirmation',
    message: 'Please confirm the offline meetup has finished successfully before continuing.',
    successFallback: 'Seller completion confirmation submitted',
    action: sellerConfirmTrade
  })
}

const handleCancelReservation = async (order: OrderRecord) => {
  await runOrderAction(order, {
    title: 'Cancel Reservation',
    message: 'The reservation will be cancelled for both sides. Continue?',
    successFallback: 'Reservation cancelled',
    action: cancelReservation
  })
}
</script>

<template>
  <section class="orders-page">
    <div class="panel orders-header">
      <div>
        <p class="orders-kicker">Reservation Order Center</p>
        <h2>{{ pageTitle }}</h2>
        <p class="muted">{{ pageDescription }}</p>
      </div>
      <div class="orders-header__meta">
        <span class="summary-pill">{{ resultSummary }}</span>
        <span v-if="lastLoadedAt" class="muted">Updated {{ lastLoadedAt }}</span>
      </div>
    </div>

    <div class="panel orders-toolbar">
      <div class="segmented-control" role="tablist" aria-label="Reservation role view">
        <button
          v-for="option in modeOptions"
          :key="option.value"
          type="button"
          class="segmented-control__item"
          :class="{ 'is-active': activeMode === option.value }"
          @click="switchMode(option.value)"
        >
          {{ option.label }}
        </button>
      </div>

      <div class="segmented-control segmented-control--status" role="tablist" aria-label="Reservation status filter">
        <button
          v-for="option in statusOptions"
          :key="option.label"
          type="button"
          class="segmented-control__item segmented-control__item--small"
          :class="{ 'is-active': selectedStatus === option.value }"
          @click="selectedStatus = option.value"
        >
          {{ option.label }}
        </button>
      </div>

      <div class="toolbar-row">
        <label class="search-box">
          <span class="search-box__label">Order No.</span>
          <input
            v-model="codeKeyword"
            class="search-box__input"
            type="text"
            placeholder="Search by reservation code"
            @keyup.enter="applyFilters"
          />
        </label>

        <div class="toolbar-actions">
          <button type="button" class="primary-btn" @click="applyFilters">Search</button>
          <button type="button" class="secondary-btn" @click="resetFilters">Reset</button>
          <button type="button" class="secondary-btn" @click="fetchOrders">Refresh</button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="panel orders-state">
      <strong>Loading reservation data...</strong>
      <p class="muted">Please wait while the latest reservation records are being fetched.</p>
    </div>

    <div v-else-if="orders.length === 0" class="panel orders-state">
      <strong>No reservations found</strong>
      <p class="muted">{{ emptyHint }}</p>
    </div>

    <div v-else class="order-list">
      <article v-for="(order, index) in orders" :key="order.id ?? order.code ?? index" class="panel order-card">
        <div class="order-card__header">
          <div>
            <div class="order-title-row">
              <h3>{{ order.productTitle || 'Product #' + String(order.productId ?? '--') }}</h3>
              <button type="button" class="link-btn" @click="openProductDetail(order)">View Product</button>
            </div>
            <p class="muted">Order No. {{ order.code || '--' }}</p>
          </div>
          <span class="status-badge" :class="'status-badge--' + statusTone(order)">
            {{ getStatusLabel(order) }}
          </span>
        </div>

        <div class="order-card__body">
          <button type="button" class="cover-frame" @click="openProductDetail(order)">
            <img v-if="resolveCover(order)" :src="resolveCover(order)" :alt="order.productTitle || 'Product cover'" />
            <span v-else class="cover-fallback">No Image</span>
          </button>

          <div class="order-card__content">
            <div class="meta-grid">
              <div>
                <span class="meta-grid__label">{{ getCounterpartyLabel }}</span>
                <strong>{{ getCounterpartyName(order) }}</strong>
              </div>
              <div>
                <span class="meta-grid__label">Created</span>
                <strong>{{ formatDateTime(order.createTime) }}</strong>
              </div>
              <div>
                <span class="meta-grid__label">Unit Price</span>
                <strong>{{ formatCurrency(order.buyPrice) }}</strong>
              </div>
              <div>
                <span class="meta-grid__label">Quantity</span>
                <strong>{{ order.buyNumber ?? 0 }}</strong>
              </div>
              <div>
                <span class="meta-grid__label">Total</span>
                <strong>{{ formatTotalPrice(order) }}</strong>
              </div>
              <div>
                <span class="meta-grid__label">Meetup Address</span>
                <strong>{{ order.appointmentAddress || 'To be agreed in chat' }}</strong>
              </div>
            </div>

            <p class="progress-hint">{{ getProgressHint(order) }}</p>

            <div v-if="order.detail" class="note-box">
              <span class="meta-grid__label">Buyer Note</span>
              <p>{{ order.detail }}</p>
            </div>

            <div class="timeline-list">
              <div v-for="item in buildTimeline(order)" :key="item.key" class="timeline-item">
                <span class="timeline-item__label">{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>

            <div class="action-list">
              <button
                v-if="canSellerConfirmReservation(order)"
                type="button"
                class="primary-btn action-btn"
                :disabled="actionOrderId === order.id"
                @click="handleSellerConfirmReservation(order)"
              >
                {{ actionOrderId === order.id ? 'Processing...' : 'Confirm Reservation' }}
              </button>

              <button
                v-if="canBuyerConfirmCompletion(order)"
                type="button"
                class="primary-btn action-btn"
                :disabled="actionOrderId === order.id"
                @click="handleBuyerConfirmCompletion(order)"
              >
                {{ actionOrderId === order.id ? 'Processing...' : 'Confirm Completion' }}
              </button>

              <button
                v-if="canSellerConfirmCompletion(order)"
                type="button"
                class="primary-btn action-btn"
                :disabled="actionOrderId === order.id"
                @click="handleSellerConfirmCompletion(order)"
              >
                {{ actionOrderId === order.id ? 'Processing...' : 'Confirm Meetup Result' }}
              </button>

              <button
                v-if="canCancelOrder(order)"
                type="button"
                class="secondary-btn action-btn action-btn--danger"
                :disabled="actionOrderId === order.id"
                @click="handleCancelReservation(order)"
              >
                {{ actionOrderId === order.id ? 'Processing...' : 'Cancel Reservation' }}
              </button>
            </div>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.orders-page {
  display: grid;
  gap: 18px;
}

.orders-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
}

.orders-kicker {
  margin: 0 0 6px;
  color: #2563eb;
  font-weight: 700;
}

.orders-header h2 {
  margin: 0 0 8px;
}

.orders-header__meta {
  display: grid;
  justify-items: end;
  gap: 8px;
}

.summary-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 8px 12px;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 600;
}

.orders-toolbar {
  display: grid;
  gap: 14px;
}

.segmented-control {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.segmented-control__item {
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  padding: 10px 16px;
  background: #f8fafc;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.segmented-control__item--small {
  padding: 8px 14px;
  font-size: 14px;
}

.segmented-control__item.is-active {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: end;
  flex-wrap: wrap;
}

.search-box {
  display: grid;
  gap: 6px;
  min-width: min(100%, 320px);
  flex: 1;
}

.search-box__label {
  color: #475569;
  font-size: 14px;
  font-weight: 600;
}

.search-box__input {
  width: 100%;
  border: 1px solid #dbe4f0;
  border-radius: 12px;
  padding: 12px 14px;
  background: #fff;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.orders-state {
  display: grid;
  gap: 8px;
}

.order-list {
  display: grid;
  gap: 16px;
}

.order-card {
  display: grid;
  gap: 18px;
}

.order-card__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.order-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.order-title-row h3 {
  margin: 0;
}

.link-btn {
  border: none;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  padding: 0;
  font-weight: 600;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.status-badge--warning {
  background: #fff7ed;
  color: #c2410c;
}

.status-badge--primary {
  background: #eff6ff;
  color: #1d4ed8;
}

.status-badge--info {
  background: #eef2ff;
  color: #4338ca;
}

.status-badge--success {
  background: #f0fdf4;
  color: #166534;
}

.status-badge--danger {
  background: #fef2f2;
  color: #b91c1c;
}

.status-badge--neutral {
  background: #f8fafc;
  color: #475569;
}

.order-card__body {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  gap: 18px;
}

.cover-frame {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 160px;
  height: 160px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  overflow: hidden;
  background: #f8fafc;
  cursor: pointer;
}

.cover-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  color: #94a3b8;
  font-weight: 600;
}

.order-card__content {
  display: grid;
  gap: 16px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 12px;
}

.meta-grid > div {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 14px;
}

.meta-grid__label {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.progress-hint {
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: #eff6ff;
  color: #1d4ed8;
}

.note-box {
  display: grid;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff7ed;
}

.note-box p {
  margin: 0;
  color: #7c2d12;
}

.timeline-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.timeline-item {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #ffffff;
}

.timeline-item__label {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
}

.action-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.action-btn {
  min-width: 170px;
}

.action-btn--danger {
  color: #b91c1c;
  border: 1px solid #fecaca;
  background: #fff5f5;
}

@media (max-width: 900px) {
  .orders-header {
    flex-direction: column;
  }

  .orders-header__meta {
    justify-items: start;
  }

  .order-card__body {
    grid-template-columns: 1fr;
  }

  .cover-frame {
    width: 100%;
    height: 220px;
  }
}
</style>
