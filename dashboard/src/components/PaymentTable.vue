<script setup>
defineProps({
  payments: Array,
  loading: Boolean,
})
</script>

<template>
  <div class="table-wrap">
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Account ID</th>
          <th>Amount</th>
          <th>Due Date</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading && payments.length === 0">
          <td colspan="5" class="center-cell">Loading...</td>
        </tr>
        <tr v-else-if="payments.length === 0">
          <td colspan="5" class="center-cell muted">No payments found.</td>
        </tr>
        <tr v-for="p in payments" :key="p.id">
          <td class="mono">{{ p.id }}</td>
          <td>{{ p.accountId }}</td>
          <td class="amount">{{ p.amount?.toLocaleString('en', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</td>
          <td>{{ p.dueDate }}</td>
          <td>
            <span class="badge" :class="p.status?.toLowerCase()">{{ p.status }}</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-wrap {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow-x: auto;
}
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}
th {
  text-align: left;
  padding: 0.75rem 1rem;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
  border-bottom: 1px solid var(--border);
  font-weight: 500;
}
td {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border);
}
tr:last-child td {
  border-bottom: none;
}
tr:hover td {
  background: rgba(108, 92, 231, 0.04);
}
.mono {
  font-family: 'SF Mono', 'Cascadia Code', 'Fira Code', monospace;
  font-size: 0.8rem;
  color: var(--text-muted);
}
.amount {
  font-variant-numeric: tabular-nums;
  font-weight: 500;
}
.center-cell {
  text-align: center;
  padding: 2rem 1rem;
}
.muted {
  color: var(--text-muted);
}
.badge {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.badge.pending {
  background: rgba(253, 203, 110, 0.15);
  color: var(--yellow);
}
.badge.advanced {
  background: rgba(0, 184, 148, 0.15);
  color: var(--green);
}
</style>
