<script setup>
import { ref, reactive } from 'vue'

const emit = defineEmits(['create'])
const open = ref(false)
const submitting = ref(false)
const error = ref('')
const form = reactive({
  accountId: '',
  amount: '',
  dueDate: '',
})

async function submit() {
  if (!form.accountId || !form.amount) return
  submitting.value = true
  error.value = ''
  try {
    await emit('create', {
      accountId: form.accountId,
      amount: parseFloat(form.amount),
      dueDate: form.dueDate || undefined,
    })
    form.accountId = ''
    form.amount = ''
    form.dueDate = ''
    open.value = false
  } catch (e) {
    error.value = e.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="create-section">
    <button v-if="!open" class="btn btn-create" @click="open = true">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
      New Payment
    </button>
    <div v-else class="create-form">
      <div class="form-row">
        <div class="field">
          <label>Account ID</label>
          <input v-model="form.accountId" placeholder="e.g. ACC-001" />
        </div>
        <div class="field">
          <label>Amount</label>
          <input v-model="form.amount" type="number" step="0.01" placeholder="0.00" />
        </div>
        <div class="field">
          <label>Due Date</label>
          <input v-model="form.dueDate" type="date" />
        </div>
        <div class="field actions">
          <button class="btn btn-submit" @click="submit" :disabled="submitting || !form.accountId || !form.amount">
            {{ submitting ? 'Creating...' : 'Create' }}
          </button>
          <button class="btn btn-cancel" @click="open = false">Cancel</button>
        </div>
      </div>
      <p v-if="error" class="form-error">{{ error }}</p>
    </div>
  </div>
</template>

<style scoped>
.create-section {
  margin-bottom: 1.5rem;
}
.btn-create {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: var(--bg-card);
  border: 1px dashed var(--border);
  border-radius: var(--radius);
  color: var(--text-muted);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}
.btn-create:hover {
  border-color: var(--accent);
  color: var(--accent);
}
.create-form {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 1.25rem;
}
.form-row {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  flex-wrap: wrap;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  flex: 1;
  min-width: 140px;
}
.field label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
  font-weight: 500;
}
.field input {
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: 6px;
  color: var(--text);
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  font-family: inherit;
  outline: none;
  transition: border-color 0.15s;
}
.field input:focus {
  border-color: var(--accent);
}
.actions {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
  flex: 0;
}
.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-submit {
  background: var(--accent);
  color: #fff;
}
.btn-submit:hover:not(:disabled) { background: var(--accent-hover); }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-cancel {
  background: var(--bg-input);
  color: var(--text-muted);
}
.btn-cancel:hover { color: var(--text); }
.form-error {
  color: var(--red);
  font-size: 0.8rem;
  margin-top: 0.75rem;
}
</style>
