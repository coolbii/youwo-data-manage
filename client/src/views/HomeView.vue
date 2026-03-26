<script setup lang="ts">
import { computed, ref } from 'vue';
import Button from '../components/form/Button.vue';
import IconButton from '../components/form/IconButton.vue';
import InlineError from '../components/form/InlineError.vue';
import PasswordField from '../components/form/PasswordField.vue';
import Select from '../components/form/Select.vue';
import TextField from '../components/form/TextField.vue';
import CardRow from '../components/list/CardRow.vue';
import DataTable, { type SortState, type TableColumn } from '../components/list/DataTable.vue';
import InfiniteFooter from '../components/list/InfiniteFooter.vue';
import StatusBadge from '../components/list/StatusBadge.vue';
import Toolbar from '../components/list/Toolbar.vue';
import LoadingIndicator from '../components/feedback/LoadingIndicator.vue';
import BottomSheet from '../components/overlay/BottomSheet.vue';
import Dropdown from '../components/overlay/Dropdown.vue';
import Menu, { type MenuItem } from '../components/overlay/Menu.vue';
import Modal from '../components/overlay/Modal.vue';
import { type ToastVariant, useToast } from '../composables/useToast';

type PersonStatus = 'active' | 'paused' | 'blocked';
type DemoPerson = {
  id: string;
  name: string;
  role: string;
  location: string;
  status: PersonStatus;
  score: number;
};

type FooterDemoState = 'idle' | 'loading' | 'end' | 'error';

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

const peopleRows = ref<DemoPerson[]>([
  {
    id: 'u-101',
    name: 'Avery Wong',
    role: 'Backend Engineer',
    location: 'Taipei',
    status: 'active',
    score: 94,
  },
  {
    id: 'u-102',
    name: 'Irene Lin',
    role: 'UX Designer',
    location: 'Kaohsiung',
    status: 'paused',
    score: 88,
  },
  {
    id: 'u-103',
    name: 'Noah Chen',
    role: 'Product Manager',
    location: 'Tainan',
    status: 'active',
    score: 91,
  },
  {
    id: 'u-104',
    name: 'Emma Hsu',
    role: 'Data Analyst',
    location: 'Taichung',
    status: 'blocked',
    score: 67,
  },
]);

const tableSort = ref<SortState>(null);
const peopleQuery = ref('');
const footerState = ref<FooterDemoState>('idle');
const feedItems = ref<number[]>([1, 2, 3, 4]);

const peopleColumns: TableColumn[] = [
  { key: 'name', label: 'Name', sortable: true, width: '26%' },
  { key: 'role', label: 'Role', sortable: true, width: '24%' },
  { key: 'location', label: 'Location', sortable: true, width: '18%' },
  { key: 'status', label: 'Status', width: '14%' },
  { key: 'score', label: 'Score', align: 'end', sortable: true, width: '18%' },
];

const filteredPeople = computed(() => {
  const q = peopleQuery.value.trim().toLowerCase();
  if (!q) return peopleRows.value;
  return peopleRows.value.filter((row) =>
    [row.name, row.role, row.location].some((text) =>
      text.toLowerCase().includes(q),
    ),
  );
});

const sortedPeople = computed(() => {
  const rows = [...filteredPeople.value];
  if (!tableSort.value) return rows;

  const { key, direction } = tableSort.value;
  const factor = direction === 'asc' ? 1 : -1;

  rows.sort((a, b) => {
    const aValue = a[key as keyof DemoPerson];
    const bValue = b[key as keyof DemoPerson];

    if (typeof aValue === 'number' && typeof bValue === 'number') {
      return (aValue - bValue) * factor;
    }

    return String(aValue).localeCompare(String(bValue)) * factor;
  });

  return rows;
});

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

function statusVariant(status: PersonStatus): 'success' | 'warning' | 'danger' {
  if (status === 'active') return 'success';
  if (status === 'paused') return 'warning';
  return 'danger';
}

function onTableSort(key: string) {
  if (!tableSort.value || tableSort.value.key !== key) {
    tableSort.value = { key, direction: 'asc' };
    return;
  }

  tableSort.value = {
    key,
    direction: tableSort.value.direction === 'asc' ? 'desc' : 'asc',
  };
}

function onRowAction(row: Record<string, unknown>, action: string) {
  show({
    variant: 'info',
    message: `${action}: ${String(row.name ?? row.id)}`,
    duration: 2200,
  });
}

function onCardClick(row: DemoPerson) {
  show({
    variant: 'success',
    message: `Open profile: ${row.name}`,
    duration: 2000,
  });
}

function onInfiniteLoad() {
  if (footerState.value !== 'idle') return;

  footerState.value = 'loading';
  setTimeout(() => {
    const next = feedItems.value.length + 1;
    feedItems.value.push(next, next + 1);
    footerState.value = feedItems.value.length >= 10 ? 'end' : 'idle';
  }, 800);
}

function forceFooterState(state: FooterDemoState) {
  footerState.value = state;
}

function resetInfiniteDemo() {
  feedItems.value = [1, 2, 3, 4];
  footerState.value = 'idle';
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

    <div class="stack-3">
      <h3 class="demo-subtitle">
        List Components
      </h3>

      <section class="surface-card list-demo stack-3">
        <Toolbar>
          <template #start>
            <div class="toolbar-search">
              <TextField
                v-model="peopleQuery"
                placeholder="Search name / role / location"
                :clearable="true"
                leading-icon="search"
              />
            </div>
            <StatusBadge
              variant="info"
              :label="`${sortedPeople.length} results`"
            />
          </template>

          <template #end>
            <Button
              variant="secondary"
              size="sm"
            >
              Add Person
            </Button>
          </template>
        </Toolbar>

        <DataTable
          :columns="peopleColumns"
          :rows="sortedPeople"
          row-key="id"
          :sort="tableSort"
          @sort="onTableSort"
        >
          <template #cell-status="{ value }">
            <StatusBadge
              :variant="statusVariant(value as PersonStatus)"
              :label="String(value)"
            />
          </template>

          <template #cell-score="{ value }">
            {{ value }} pts
          </template>

          <template #row-actions="{ row }">
            <IconButton
              aria-label="Row action"
              size="sm"
              @click="onRowAction(row as Record<string, unknown>, 'More')"
            >
              ···
            </IconButton>
          </template>
        </DataTable>
      </section>

      <section class="stack-3">
        <p class="body-sm text-muted">
          Mobile CardRow Preview
        </p>
        <CardRow
          v-for="row in sortedPeople.slice(0, 3)"
          :key="`card-${row.id}`"
          clickable
          @click="onCardClick(row)"
        >
          <template #primary>
            {{ row.name }}
          </template>
          <template #secondary>
            {{ row.role }} · {{ row.location }}
          </template>
          <template #meta>
            <StatusBadge
              :variant="statusVariant(row.status)"
              :label="row.status"
            />
            <span>{{ row.score }} pts</span>
          </template>
          <template #actions>
            <IconButton
              aria-label="Card action"
              size="sm"
              @click="onRowAction(row as unknown as Record<string, unknown>, 'Action')"
            >
              ···
            </IconButton>
          </template>
        </CardRow>
      </section>

      <section class="surface-card list-demo stack-3">
        <p class="body-sm text-muted">
          Infinite Footer Preview
        </p>

        <ul class="demo-feed">
          <li
            v-for="item in feedItems"
            :key="item"
            class="demo-feed__item"
          >
            Feed item #{{ item }}
          </li>
        </ul>

        <InfiniteFooter
          :state="footerState"
          @load="onInfiniteLoad"
        />

        <div class="demo-row">
          <Button
            variant="ghost"
            size="sm"
            @click="resetInfiniteDemo"
          >
            Reset
          </Button>
          <Button
            variant="secondary"
            size="sm"
            @click="forceFooterState('idle')"
          >
            Idle
          </Button>
          <Button
            variant="secondary"
            size="sm"
            @click="forceFooterState('loading')"
          >
            Loading
          </Button>
          <Button
            variant="danger"
            size="sm"
            @click="forceFooterState('error')"
          >
            Error
          </Button>
        </div>
      </section>
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

.list-demo {
  padding: var(--space-4);
}

.toolbar-search {
  width: min(360px, 100%);
}

.demo-feed {
  list-style: none;
  margin: 0;
  padding: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.demo-feed__item {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.demo-feed__item:last-child {
  border-bottom: 0;
}
</style>
