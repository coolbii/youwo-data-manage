<script setup lang="ts">
import { computed, useId } from 'vue';
import InlineError from './InlineError.vue';

type LeadingIcon = 'none' | 'search';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    label?: string;
    id?: string;
    name?: string;
    type?: 'text' | 'email' | 'number' | 'search' | 'url' | 'tel' | 'date';
    placeholder?: string;
    autocomplete?: string;
    hint?: string;
    error?: string;
    disabled?: boolean;
    readonly?: boolean;
    required?: boolean;
    clearable?: boolean;
    leadingIcon?: LeadingIcon;
  }>(),
  {
    modelValue: '',
    label: '',
    id: '',
    name: '',
    type: 'text',
    placeholder: '',
    autocomplete: '',
    hint: '',
    error: '',
    disabled: false,
    readonly: false,
    required: false,
    clearable: false,
    leadingIcon: 'none',
  }
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const fallbackId = useId();
const inputId = computed(() => props.id || `textfield-${fallbackId}`);

function onInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value);
}

const showClear = computed(
  () =>
    props.clearable &&
    !!props.modelValue &&
    !props.disabled &&
    !props.readonly
);

function clearValue() {
  emit('update:modelValue', '');
}
</script>

<template>
  <div class="text-field">
    <label
      v-if="label"
      :for="inputId"
      class="text-field__label"
    >
      <span>{{ label }}</span>
      <span
        v-if="required"
        class="text-field__required"
        aria-hidden="true"
      >*</span>
    </label>

    <div
      :class="[
        'text-field__control',
        {
          'text-field__control--error': error,
          'text-field__control--disabled': disabled,
        },
      ]"
    >
      <span
        v-if="leadingIcon === 'search'"
        class="text-field__icon"
        aria-hidden="true"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
        >
          <circle
            cx="11"
            cy="11"
            r="7"
            stroke="currentColor"
            stroke-width="2"
          />
          <path
            d="M20 20L16.65 16.65"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
      </span>

      <input
        :id="inputId"
        :value="modelValue"
        class="text-field__input"
        :name="name || undefined"
        :type="type"
        :placeholder="placeholder"
        :autocomplete="autocomplete || undefined"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :aria-invalid="!!error || undefined"
        @input="onInput"
      >

      <button
        v-if="showClear"
        type="button"
        class="text-field__clear"
        aria-label="Clear input"
        @click="clearValue"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
        >
          <path
            d="M6 6L18 18"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
          <path
            d="M18 6L6 18"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
      </button>

      <slot name="trailing" />
    </div>

    <p
      v-if="hint && !error"
      class="text-field__hint"
    >
      {{ hint }}
    </p>
    <InlineError :message="error" />
  </div>
</template>

<style scoped>
.text-field {
  width: 100%;
}

.text-field__label {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
  font-weight: 500;
}

.text-field__required {
  color: var(--color-danger);
}

.text-field__control {
  display: flex;
  align-items: center;
  gap: var(--control-gap);
  min-height: var(--control-h-md);
  padding-inline: var(--control-px);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  transition: border-color var(--duration-fast) ease, box-shadow var(--duration-fast) ease;
}

.text-field__control:focus-within {
  border-width: 2px;
  border-color: var(--color-accent);
}

.text-field__control--error,
.text-field__control--error:focus-within {
  border-width: 1px;
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px var(--color-error-ring);
}

.text-field__control--disabled {
  background: var(--color-surface-soft);
}

.text-field__icon,
.text-field__clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: var(--control-icon);
  height: var(--control-icon);
  color: var(--color-placeholder);
  flex: 0 0 auto;
}

.text-field__icon svg,
.text-field__clear svg {
  width: 100%;
  height: 100%;
}

.text-field__input {
  width: 100%;
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--font-size-md);
  line-height: 1.4;
  padding: 0;
}

.text-field__input::placeholder {
  color: var(--color-placeholder);
}

.text-field__input:disabled {
  cursor: not-allowed;
  color: var(--color-muted);
}

.text-field__clear {
  border: 0;
  background: transparent;
  padding: 0;
  cursor: pointer;
}

.text-field__clear:hover {
  color: var(--color-muted);
}

.text-field__hint {
  margin-top: var(--space-2);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

@media (max-width: 599px) {
  .text-field__control {
    min-height: var(--space-10);
    gap: var(--control-gap-mobile);
    padding-inline: var(--control-px-mobile);
  }

  .text-field__icon,
  .text-field__clear {
    width: var(--control-icon-mobile);
    height: var(--control-icon-mobile);
  }

  .text-field__input {
    font-size: var(--font-size-sm);
  }
}
</style>
