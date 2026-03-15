<script setup>
import { ref, computed, onUnmounted } from 'vue'
import BaseUrlBar from './components/BaseUrlBar.vue'
import PaymentTable from './components/PaymentTable.vue'
import CreatePayment from './components/CreatePayment.vue'

const baseUrl = ref(localStorage.getItem('payments-api-base-url') || '')
const payments = ref([])
const loading = ref(false)
const error = ref('')
const autoRefresh = ref(false)
let intervalId = null

function setBaseUrl(url) {
  const cleaned = url.replace(/\/+$/, '')
  baseUrl.value = cleaned
  localStorage.setItem('payments-api-base-url', cleaned)
  if (cleaned) fetchPayments()
}

async function fetchPayments() {
  if (!baseUrl.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${baseUrl.value}/payments`)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    payments.value = await res.json()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function toggleAutoRefresh(enabled) {
  autoRefresh.value = enabled
  clearInterval(intervalId)
  if (enabled) {
    intervalId = setInterval(fetchPayments, 5000)
  }
}

async function createPayment(data) {
  const res = await fetch(`${baseUrl.value}/payments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  await fetchPayments()
}

if (baseUrl.value) fetchPayments()

onUnmounted(() => clearInterval(intervalId))

const stats = computed(() => {
  const total = payments.value.length
  const pending = payments.value.filter(p => p.status === 'PENDING').length
  const advanced = payments.value.filter(p => p.status === 'ADVANCED').length
  const sum = payments.value.reduce((acc, p) => acc + (p.amount || 0), 0)
  return { total, pending, advanced, sum }
})
</script>

<template>
  <header class="header">
    <h1>Payments Dashboard</h1>
  </header>

  <BaseUrlBar :modelValue="baseUrl" @update="setBaseUrl" />

  <template v-if="baseUrl">
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-label">Total</span>
        <span class="stat-value">{{ stats.total }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Pending</span>
        <span class="stat-value pending">{{ stats.pending }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Advanced</span>
        <span class="stat-value advanced">{{ stats.advanced }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total Amount</span>
        <span class="stat-value">{{ stats.sum.toLocaleString('en', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</span>
      </div>
    </div>

    <div class="toolbar">
      <button class="btn btn-primary" @click="fetchPayments" :disabled="loading">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.3"/></svg>
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
      <label class="toggle-label">
        <input type="checkbox" :checked="autoRefresh" @change="toggleAutoRefresh($event.target.checked)" />
        <span class="toggle-track"><span class="toggle-thumb"></span></span>
        Auto-refresh (5s)
      </label>
    </div>

    <p v-if="error" class="error-msg">{{ error }}</p>

    <CreatePayment @create="createPayment" />

    <PaymentTable :payments="payments" :loading="loading" />
  </template>

  <div v-else class="empty-state">
    <p>Enter an API base URL above to get started.</p>
  </div>
</template>

<style scoped>
.header {
  margin-bottom: 1.5rem;
}
.header h1 {
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}
.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.stat-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
}
.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
}
.stat-value.pending { color: var(--yellow); }
.stat-value.advanced { color: var(--green); }

.toolbar {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--radius);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-primary {
  background: var(--accent);
  color: #fff;
}
.btn-primary:hover:not(:disabled) {
  background: var(--accent-hover);
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-muted);
  cursor: pointer;
  user-select: none;
}
.toggle-label input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}
.toggle-track {
  position: relative;
  width: 36px;
  height: 20px;
  background: var(--bg-input);
  border-radius: 10px;
  border: 1px solid var(--border);
  transition: background 0.2s;
}
.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 14px;
  height: 14px;
  background: var(--text-muted);
  border-radius: 50%;
  transition: transform 0.2s, background 0.2s;
}
.toggle-label input:checked + .toggle-track {
  background: var(--accent);
  border-color: var(--accent);
}
.toggle-label input:checked + .toggle-track .toggle-thumb {
  transform: translateX(16px);
  background: #fff;
}

.error-msg {
  color: var(--red);
  font-size: 0.875rem;
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
  border-radius: var(--radius);
}

.empty-state {
  text-align: center;
  padding: 4rem 1rem;
  color: var(--text-muted);
  font-size: 1rem;
}

@media (max-width: 640px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
