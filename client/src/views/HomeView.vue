<script setup lang="ts">
import { ref } from 'vue';
import Button from '../components/form/Button.vue';
import IconButton from '../components/form/IconButton.vue';
import InlineError from '../components/form/InlineError.vue';
import PasswordField from '../components/form/PasswordField.vue';
import Select from '../components/form/Select.vue';
import TextField from '../components/form/TextField.vue';

const name = ref('John Carter');
const query = ref('Taipei');
const password = ref('');
const role = ref('');
const invalidInput = ref('abc');

const roleOptions = [
  { label: 'Backend Engineer', value: 'backend' },
  { label: 'Product Manager', value: 'pm' },
  { label: 'UX Designer', value: 'ux' },
];
</script>

<template>
  <section class="surface-card demo stack-6">
    <h2 class="section-title">
      YW-010 Component Demo
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
  </section>
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
