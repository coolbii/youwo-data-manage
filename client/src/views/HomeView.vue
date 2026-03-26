<script setup lang="ts">
import { ref } from 'vue';
import Button from '../components/form/Button.vue';
import IconButton from '../components/form/IconButton.vue';
import InlineError from '../components/form/InlineError.vue';
import PasswordField from '../components/form/PasswordField.vue';
import Select from '../components/form/Select.vue';
import TextField from '../components/form/TextField.vue';
import LoadingIndicator from '../components/feedback/LoadingIndicator.vue';
import BottomSheet from '../components/overlay/BottomSheet.vue';
import Dropdown from '../components/overlay/Dropdown.vue';
import Menu, { type MenuItem } from '../components/overlay/Menu.vue';
import Modal from '../components/overlay/Modal.vue';
import { type ToastVariant, useToast } from '../composables/useToast';

const name = ref('John Carter');
const query = ref('Taipei');
const password = ref('');
const role = ref('');
const invalidInput = ref('abc');
const selectedAction = ref('No menu action yet.');
const showModal = ref(false);
const showSheet = ref(false);

const roleOptions = [
  { label: 'Backend Engineer', value: 'backend' },
  { label: 'Product Manager', value: 'pm' },
  { label: 'UX Designer', value: 'ux' },
];

const menuItems: MenuItem[] = [
  { key: 'view', label: 'View details' },
  { key: 'edit', label: 'Edit person' },
  { key: 'pin', label: 'Pin to top' },
  { key: 'delete', label: 'Delete person', danger: true },
];

const { show } = useToast();

function onMenuSelect(key: string, close?: () => void) {
  selectedAction.value = `Menu action: ${key}`;
  show({
    variant: 'info',
    message: `Selected action: ${key}`,
    duration: 2200,
  });
  close?.();
}

function openModal() {
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

function openSheet() {
  showSheet.value = true;
}

function closeSheet() {
  showSheet.value = false;
}

function showToast(variant: ToastVariant) {
  const messageByVariant: Record<ToastVariant, string> = {
    info: 'Info toast preview',
    success: 'Saved successfully',
    warning: 'Please double-check the input',
    error: 'Something went wrong',
  };

  show({
    variant,
    message: messageByVariant[variant],
    duration: 2600,
  });
}
</script>

<template>
  <section class="surface-card demo stack-6">
    <h2 class="section-title">
      Component Preview
    </h2>

    <div class="stack-3">
      <h3 class="demo-subtitle">
        Buttons
      </h3>
      <div class="demo-row">
        <Button variant="primary">
          Primary
        </Button>
        <Button variant="secondary">
          Secondary
        </Button>
        <Button variant="ghost">
          Ghost
        </Button>
        <Button variant="danger">
          Danger
        </Button>
        <Button
          variant="primary"
          :loading="true"
        >
          Loading
        </Button>
      </div>
    </div>

    <div class="stack-3">
      <h3 class="demo-subtitle">
        Icon Buttons
      </h3>
      <div class="demo-row">
        <IconButton aria-label="Pin row">
          P
        </IconButton>
        <IconButton
          aria-label="Delete row"
          variant="danger"
        >
          X
        </IconButton>
        <IconButton
          aria-label="Loading icon button"
          :loading="true"
        />
      </div>
    </div>

    <div class="stack-3">
      <h3 class="demo-subtitle">
        Inputs
      </h3>
      <TextField
        v-model="name"
        label="Name"
        placeholder="Enter full name"
        hint="Used in people list and pin rules"
      />
      <TextField
        v-model="query"
        label="Search"
        placeholder="Search by name, position, location"
        :clearable="true"
        leading-icon="search"
      />
      <PasswordField
        v-model="password"
        label="Password"
        placeholder="Enter password"
      />
      <Select
        v-model="role"
        label="Position Title"
        :options="roleOptions"
        placeholder="Choose a role"
      />
      <TextField
        v-model="invalidInput"
        label="Age"
        placeholder="Only numbers"
        error="Please enter numeric value"
      />
      <InlineError
        message="Inline error demo: person not found by this keyword."
      />
    </div>

    <div class="stack-3">
      <h3 class="demo-subtitle">
        Overlay Components
      </h3>
      <div class="demo-row">
        <Dropdown align="start">
          <template #trigger="{ open }">
            <Button variant="secondary">
              {{ open ? 'Close menu' : 'Open menu' }}
            </Button>
          </template>
          <template #default="{ close }">
            <Menu
              :items="menuItems"
              @select="(key) => onMenuSelect(key, close)"
            />
          </template>
        </Dropdown>

        <Button
          variant="secondary"
          @click="openModal"
        >
          Open Modal
        </Button>

        <Button
          variant="secondary"
          @click="openSheet"
        >
          Open Bottom Sheet
        </Button>
      </div>

      <p class="body-sm text-muted">
        {{ selectedAction }}
      </p>
    </div>

    <div class="stack-3">
      <h3 class="demo-subtitle">
        Feedback Components
      </h3>
      <div class="demo-row">
        <LoadingIndicator
          size="sm"
          label="Loading small"
        />
        <LoadingIndicator
          size="md"
          label="Loading medium"
        />
        <LoadingIndicator
          size="lg"
          label="Loading large"
        />
      </div>

      <div class="demo-row">
        <Button @click="showToast('info')">
          Info Toast
        </Button>
        <Button
          variant="secondary"
          @click="showToast('success')"
        >
          Success Toast
        </Button>
        <Button
          variant="ghost"
          @click="showToast('warning')"
        >
          Warning Toast
        </Button>
        <Button
          variant="danger"
          @click="showToast('error')"
        >
          Error Toast
        </Button>
      </div>
    </div>
  </section>

  <Modal
    :open="showModal"
    title="Demo Modal"
    @close="closeModal"
  >
    <p class="body-sm text-muted">
      This is the staged modal component preview.
    </p>

    <template #footer>
      <Button
        variant="ghost"
        @click="closeModal"
      >
        Cancel
      </Button>
      <Button @click="closeModal">
        Confirm
      </Button>
    </template>
  </Modal>

  <BottomSheet
    :open="showSheet"
    title="Demo Bottom Sheet"
    @close="closeSheet"
  >
    <div class="stack-3">
      <p class="body-sm text-muted">
        This is the staged bottom-sheet component preview.
      </p>
      <Button
        block
        @click="closeSheet"
      >
        Close Sheet
      </Button>
    </div>
  </BottomSheet>
</template>

<style scoped>
.demo {
  min-height: calc(100vh - 2 * var(--space-6));
  padding: var(--space-6);
}

.demo-subtitle {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.demo-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
</style>
