<script setup lang="ts">
import { computed, useId } from 'vue';
import InlineError from './InlineError.vue';

type SelectValue = string | number;

type SelectOption = {
  label: string;
  value: SelectValue;
  disabled?: boolean;
};

const props = withDefaults(
  defineProps<{
    modelValue?: SelectValue | null;
    label?: string;
    id?: string;
    name?: string;
    placeholder?: string;
    hint?: string;
    error?: string;
    disabled?: boolean;
    required?: boolean;
    options: SelectOption[];
  }>(),
  {
    modelValue: null,
    label: '',
    id: '',
    name: '',
    placeholder: 'Please select',
    hint: '',
    error: '',
    disabled: false,
    required: false,
  }
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: SelectValue | null): void;
}>();

const fallbackId = useId();
const selectId = computed(() => props.id || `select-${fallbackId}`);

function onChange(event: Event) {
  const raw = (event.target as HTMLSelectElement).value;
  emit('update:modelValue', raw === '' ? null : raw);
}
</script>

<template>
  <div class="select-field">
    <label
      v-if="label"
      :for="selectId"
      class="select-field__label"
    >
      {{ label }}
    </label>

    <div
      :class="[
        'select-field__control',
        {
          'select-field__control--error': error,
          'select-field__control--disabled': disabled,
        },
      ]"
    >
      <select
        :id="selectId"
        :value="modelValue ?? ''"
        class="select-field__native"
        :name="name || undefined"
        :disabled="disabled"
        :required="required"
        :aria-invalid="!!error || undefined"
        @change="onChange"
      >
        <option
          value=""
          disabled
          :selected="modelValue == null"
        >
          {{ placeholder }}
        </option>
        <option
          v-for="opt in options"
          :key="opt.value"
          :value="opt.value"
          :disabled="opt.disabled"
        >
          {{ opt.label }}
        </option>
      </select>

      <span
        class="select-field__chevron"
        aria-hidden="true"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
        >
          <path
            d="M6 9l6 6 6-6"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </span>
    </div>

    <p
      v-if="hint && !error"
      class="select-field__hint"
    >
      {{ hint }}
    </p>
    <InlineError :message="error" />
  </div>
</template>

<style scoped>
.select-field {
  width: 100%;
}

.select-field__label {
  display: block;
  margin-bottom: var(--space-2);
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--color-text-secondary);
}

.select-field__control {
  position: relative;
  min-height: var(--control-h-md);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  transition: border-color var(--duration-fast) ease, box-shadow var(--duration-fast) ease;
}

.select-field__control:focus-within {
  border-width: 2px;
  border-color: var(--color-accent);
}

.select-field__control--error,
.select-field__control--error:focus-within {
  border-width: 1px;
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px var(--color-error-ring);
}

.select-field__control--disabled {
  background: var(--color-surface-soft);
}

.select-field__native {
  appearance: none;
  width: 100%;
  min-height: var(--control-h-md);
  padding-inline: var(--control-px);
  padding-right: var(--space-10);
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--font-size-md);
  cursor: pointer;
}

.select-field__native:disabled {
  cursor: not-allowed;
  color: var(--color-muted);
}

.select-field__native option[value=""][disabled] {
  color: var(--color-placeholder);
}

.select-field__chevron {
  position: absolute;
  right: var(--control-px);
  top: 50%;
  transform: translateY(-50%);
  width: var(--control-icon-mobile);
  height: var(--control-icon-mobile);
  color: var(--color-muted);
  pointer-events: none;
}

.select-field__chevron svg {
  width: 100%;
  height: 100%;
}

.select-field__hint {
  margin-top: var(--space-2);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

@media (max-width: 599px) {
  .select-field__native {
    min-height: var(--space-10);
    padding-inline: var(--control-px-mobile);
    padding-right: 34px;
    font-size: var(--font-size-sm);
  }

  .select-field__chevron {
    right: var(--control-px-mobile);
  }
}
</style>
