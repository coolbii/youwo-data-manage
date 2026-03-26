<script setup lang="ts">
import { computed, ref, useId } from 'vue';
import InlineError from './InlineError.vue';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    label?: string;
    id?: string;
    name?: string;
    placeholder?: string;
    autocomplete?: string;
    hint?: string;
    error?: string;
    disabled?: boolean;
    readonly?: boolean;
    required?: boolean;
  }>(),
  {
    modelValue: '',
    label: '',
    id: '',
    name: '',
    placeholder: '',
    autocomplete: 'current-password',
    hint: '',
    error: '',
    disabled: false,
    readonly: false,
    required: false,
  }
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const fallbackId = useId();
const inputId = computed(() => props.id || `password-${fallbackId}`);
const visible = ref(false);

const inputType = computed(() => (visible.value ? 'text' : 'password'));

function onInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value);
}

function toggleVisibility() {
  visible.value = !visible.value;
}
</script>

<template>
  <div class="password-field">
    <label
      v-if="label"
      :for="inputId"
      class="password-field__label"
    >
      {{ label }}
    </label>

    <div
      :class="[
        'password-field__control',
        {
          'password-field__control--error': error,
          'password-field__control--disabled': disabled,
        },
      ]"
    >
      <input
        :id="inputId"
        :value="modelValue"
        class="password-field__input"
        :name="name || undefined"
        :type="inputType"
        :placeholder="placeholder"
        :autocomplete="autocomplete"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :aria-invalid="!!error || undefined"
        @input="onInput"
      >

      <button
        type="button"
        class="password-field__toggle"
        :aria-label="visible ? 'Hide password' : 'Show password'"
        tabindex="-1"
        @click="toggleVisibility"
      >
        <!-- eye-off (hidden state) -->
        <svg
          v-if="!visible"
          viewBox="0 0 24 24"
          fill="none"
        >
          <path
            d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M14.12 14.12a3 3 0 1 1-4.24-4.24"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <line
            x1="1"
            y1="1"
            x2="23"
            y2="23"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
        <!-- eye (visible state) -->
        <svg
          v-else
          viewBox="0 0 24 24"
          fill="none"
        >
          <path
            d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <circle
            cx="12"
            cy="12"
            r="3"
            stroke="currentColor"
            stroke-width="2"
          />
        </svg>
      </button>
    </div>

    <p
      v-if="hint && !error"
      class="password-field__hint"
    >
      {{ hint }}
    </p>

    <InlineError :message="error" />
  </div>
</template>

<style scoped>
.password-field {
  width: 100%;
}

.password-field__label {
  display: block;
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
  font-weight: 500;
}

.password-field__control {
  display: flex;
  align-items: center;
  gap: var(--control-gap-mobile);
  min-height: var(--control-h-md);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  padding-inline: var(--control-px);
  transition: border-color var(--duration-fast) ease, box-shadow var(--duration-fast) ease;
}

.password-field__control:focus-within {
  border-width: 2px;
  border-color: var(--color-accent);
}

.password-field__control--error,
.password-field__control--error:focus-within {
  border-width: 1px;
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px var(--color-error-ring);
}

.password-field__control--disabled {
  background: var(--color-surface-soft);
}

.password-field__input {
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

.password-field__input::placeholder {
  color: var(--color-placeholder);
}

.password-field__toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: var(--control-icon);
  height: var(--control-icon);
  color: var(--color-placeholder);
  border: 0;
  background: transparent;
  padding: 0;
  cursor: pointer;
}

.password-field__toggle:hover {
  color: var(--color-muted);
}

.password-field__toggle svg {
  width: 100%;
  height: 100%;
}

.password-field__hint {
  margin-top: var(--space-2);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

@media (max-width: 599px) {
  .password-field__control {
    min-height: var(--space-10);
    padding-inline: var(--control-px-mobile);
  }

  .password-field__input {
    font-size: var(--font-size-sm);
  }

  .password-field__toggle {
    width: var(--control-icon-mobile);
    height: var(--control-icon-mobile);
  }
}
</style>
