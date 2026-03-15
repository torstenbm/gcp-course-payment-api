<script setup>
import { ref } from 'vue'

const props = defineProps({ modelValue: String })
const emit = defineEmits(['update'])
const input = ref(props.modelValue || '')
const editing = ref(!props.modelValue)

function save() {
  const url = input.value.trim()
  if (!url) return
  emit('update', url)
  editing.value = false
}
</script>

<template>
  <div class="url-bar">
    <svg class="url-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
    <template v-if="editing">
      <input
        v-model="input"
        type="url"
        placeholder="https://your-api-url.run.app"
        class="url-input"
        @keyup.enter="save"
      />
      <button class="btn btn-save" @click="save">Save</button>
    </template>
    <template v-else>
      <span class="url-display">{{ modelValue }}</span>
      <button class="btn btn-edit" @click="editing = true">Edit</button>
    </template>
  </div>
</template>

<style scoped>
.url-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 0.625rem 1rem;
  margin-bottom: 1.5rem;
}
.url-icon {
  flex-shrink: 0;
  color: var(--text-muted);
}
.url-input {
  flex: 1;
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
.url-input:focus {
  border-color: var(--accent);
}
.url-display {
  flex: 1;
  font-size: 0.875rem;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.btn {
  padding: 0.4rem 0.85rem;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-save {
  background: var(--accent);
  color: #fff;
}
.btn-save:hover { background: var(--accent-hover); }
.btn-edit {
  background: var(--bg-input);
  color: var(--text-muted);
  border: 1px solid var(--border);
}
.btn-edit:hover { background: var(--border); color: var(--text); }
</style>
