<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
import Button from '../components/form/Button.vue';
import IconButton from '../components/form/IconButton.vue';
import TextField from '../components/form/TextField.vue';
import Toolbar from '../components/list/Toolbar.vue';
import DataTable from '../components/list/DataTable.vue';
import type { TableColumn } from '../components/list/DataTable.vue';
import CardRow from '../components/list/CardRow.vue';
import StatusBadge from '../components/list/StatusBadge.vue';
import InfiniteFooter from '../components/list/InfiniteFooter.vue';
import Dropdown from '../components/overlay/Dropdown.vue';
import Menu from '../components/overlay/Menu.vue';
import type { MenuItem } from '../components/overlay/Menu.vue';
import Modal from '../components/overlay/Modal.vue';
import { useToast } from '../composables/useToast';
import {
  usePeopleListQuery,
  useDeletePersonMutation,
  usePinRulesQuery,
  useCreatePinRuleMutation,
  PeopleSortField,
  SortDirection,
  type PersonFieldsFragment,
  type PinRuleFieldsFragment,
} from '../graphql/generated';

const PAGE_SIZE = 10;

const search = ref('');
const sortBy = ref(PeopleSortField.CreatedAt);
const sortDirection = ref(SortDirection.Asc);
const isFetchingMore = ref(false);
const loadMoreError = ref(false);
const listScrollEl = ref<HTMLElement | null>(null);
const listViewportHeight = ref<number | null>(null);

const { result, loading, fetchMore, refetch } = usePeopleListQuery(
  () => ({
    first: PAGE_SIZE,
    after: null as string | null,
    search: search.value || null,
    sortBy: sortBy.value,
    sortDirection: sortDirection.value,
  }),
);

const people = computed(
  () => (result.value?.peopleList?.nodes?.filter(Boolean) ?? []) as PersonFieldsFragment[],
);
const pageInfo = computed(() => result.value?.peopleList?.pageInfo);
const totalCount = computed(() => result.value?.peopleList?.totalCount ?? 0);
const loadedCount = computed(() => people.value.length);
const normalizedTotalCount = computed(() => {
  const numeric =
    typeof totalCount.value === 'number'
      ? totalCount.value
      : Number(totalCount.value ?? 0);
  return Number.isFinite(numeric) ? numeric : 0;
});
const activeSearchTerm = computed(() => search.value.trim());

const footerState = computed(() => {
  if (loading.value && people.value.length === 0) return 'loading' as const;
  if (loading.value || isFetchingMore.value) return 'loading' as const;
  if (loadMoreError.value) return 'error' as const;
  if (!pageInfo.value?.hasNextPage) return 'end' as const;
  return 'idle' as const;
});

function formatCount(value: number): string {
  return value.toLocaleString();
}

const loadedSummary = computed(() => {
  if (
    loading.value &&
    loadedCount.value === 0 &&
    normalizedTotalCount.value === 0
  ) {
    return '';
  }

  if (normalizedTotalCount.value === 0 && activeSearchTerm.value) {
    return `0 results matching '${activeSearchTerm.value}'`;
  }

  const base = `${formatCount(loadedCount.value)} of ${formatCount(normalizedTotalCount.value)} loaded`;
  if (activeSearchTerm.value) return `${base} · matching '${activeSearchTerm.value}'`;
  return base;
});

const footerHint = computed(() => {
  if (footerState.value === 'idle' && pageInfo.value?.hasNextPage) {
    return 'Scroll for more';
  }
  if (footerState.value === 'loading' && loadedCount.value > 0) {
    return 'Fetching more...';
  }
  return '';
});

const footerEndLabel = computed(() => {
  if (normalizedTotalCount.value === 0 && activeSearchTerm.value) {
    return `0 results matching '${activeSearchTerm.value}'`;
  }
  return 'All loaded';
});

let searchTimeout: ReturnType<typeof setTimeout> | null = null;
const searchInput = ref('');

watch(searchInput, (val) => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    search.value = val;
  }, 300);
});

function updateListViewportHeight() {
  if (!listScrollEl.value || typeof window === 'undefined') return;
  const top = listScrollEl.value.getBoundingClientRect().top;
  const bottomOffset = window.innerWidth < 600 ? 12 : 24;
  listViewportHeight.value = Math.max(
    240,
    Math.floor(window.innerHeight - top - bottomOffset),
  );
}

onMounted(() => {
  updateListViewportHeight();
  window.addEventListener('resize', updateListViewportHeight);
  requestAnimationFrame(() => {
    updateListViewportHeight();
  });
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateListViewportHeight);
});

async function onLoadMore() {
  if (isFetchingMore.value) return;
  if (!pageInfo.value?.hasNextPage || !pageInfo.value.endCursor) return;

  isFetchingMore.value = true;
  loadMoreError.value = false;

  try {
    await fetchMore({
      variables: { first: PAGE_SIZE, after: pageInfo.value.endCursor },
      updateQuery(prev, { fetchMoreResult }) {
        if (!fetchMoreResult?.peopleList) return prev;
        return {
          peopleList: {
            ...fetchMoreResult.peopleList,
            nodes: [
              ...(prev.peopleList?.nodes ?? []),
              ...(fetchMoreResult.peopleList.nodes ?? []),
            ],
          },
        };
      },
    });
  } catch (err) {
    loadMoreError.value = true;
    show({
      variant: 'error',
      message: err instanceof Error ? err.message : 'Failed to load more people',
    });
  } finally {
    isFetchingMore.value = false;
  }
}

// --- Pin rules ---

const { result: pinResult, refetch: refetchPins } = usePinRulesQuery();
const { mutate: doCreatePin } = useCreatePinRuleMutation({});

const pinRules = computed(
  () => (pinResult.value?.pinRules?.filter(Boolean) ?? []) as PinRuleFieldsFragment[],
);

const activePinCount = computed(() => pinRules.value.filter((r) => r.enabled).length);
const inactivePinCount = computed(() => pinRules.value.filter((r) => !r.enabled).length);

watch(
  [() => pinRules.value.length, activeSearchTerm],
  async () => {
    await nextTick();
    updateListViewportHeight();
  },
  { flush: 'post' },
);

const pinMap = computed(() => {
  const map = new Map<string, PinRuleFieldsFragment>();
  for (const rule of pinRules.value) {
    if (rule.enabled && rule.personId) {
      map.set(rule.personId, rule);
    }
  }
  return map;
});

function personPin(person: PersonFieldsFragment): PinRuleFieldsFragment | undefined {
  if (!person.id) return undefined;
  return pinMap.value.get(person.id);
}

async function pinPerson(person: PersonFieldsFragment) {
  const maxPos = pinRules.value.reduce((max, r) => Math.max(max, r.targetPosition), 0);
  try {
    await doCreatePin({ personId: person.id, targetPosition: maxPos + 1 });
    show({ variant: 'success', message: `Pinned ${person.name} to position ${maxPos + 1}` });
    refetchPins();
    refetch();
  } catch (err) {
    show({
      variant: 'error',
      message: err instanceof Error ? err.message : 'Pin failed',
    });
  }
}

// --- Sort ---

const sortFieldLabels: Record<PeopleSortField, string> = {
  [PeopleSortField.Name]: 'Name',
  [PeopleSortField.Position]: 'Position',
  [PeopleSortField.Location]: 'Location',
  [PeopleSortField.Birthdate]: 'Birthdate',
  [PeopleSortField.CreatedAt]: 'Date created',
};

const sortFields: { key: PeopleSortField; label: string }[] = [
  { key: PeopleSortField.Name, label: 'Name' },
  { key: PeopleSortField.Position, label: 'Position' },
  { key: PeopleSortField.Location, label: 'Location' },
  { key: PeopleSortField.Birthdate, label: 'Age (Birthdate)' },
  { key: PeopleSortField.CreatedAt, label: 'Date created' },
];

const sortChipLabel = computed(() => {
  const fieldLabel = sortFieldLabels[sortBy.value];
  if (activePinCount.value > 0) return `Sort: Pin > ${fieldLabel}`;
  return `Sort: ${fieldLabel}`;
});

function onSortFieldSelect(field: PeopleSortField) {
  if (sortBy.value === field) return;
  sortBy.value = field;
  sortDirection.value = SortDirection.Asc;
}

function setDirection(dir: SortDirection) {
  sortDirection.value = dir;
}

// --- Table columns ---

const columns: TableColumn[] = [
  { key: 'pin', label: 'Pin', align: 'center', width: '50px' },
  { key: 'name', label: 'Name', width: '20%' },
  { key: 'position', label: 'Position', width: '20%' },
  { key: 'location', label: 'Location', width: '16%' },
  { key: 'age', label: 'Age', align: 'end', width: '10%' },
  { key: 'birthdate', label: 'Birthdate', width: '14%' },
  { key: 'createdAt', label: 'Created', width: '14%' },
];

// --- Row actions ---

const { show } = useToast();

const rowMenuItems: MenuItem[] = [
  { key: 'edit', label: 'Edit person' },
  { key: 'pin', label: 'Pin to top' },
  { key: 'delete', label: 'Delete person', danger: true },
];

function onRowAction(
  row: PersonFieldsFragment,
  key: string,
  close?: () => void,
) {
  close?.();
  if (key === 'delete') {
    confirmDelete(row);
  } else if (key === 'edit') {
    show({ variant: 'info', message: `Edit: ${row.name} (TODO)`, duration: 2000 });
  } else if (key === 'pin') {
    pinPerson(row);
  }
}

// --- Delete ---

const deleteTarget = ref<PersonFieldsFragment | null>(null);
const showDeleteModal = ref(false);

const { mutate: doDelete, loading: deleting } = useDeletePersonMutation({});

function confirmDelete(person: PersonFieldsFragment) {
  deleteTarget.value = person;
  showDeleteModal.value = true;
}

async function executeDelete() {
  if (!deleteTarget.value) return;
  try {
    await doDelete({ id: deleteTarget.value.id });
    show({ variant: 'success', message: `Deleted ${deleteTarget.value.name}` });
    showDeleteModal.value = false;
    deleteTarget.value = null;
    refetch();
  } catch (err) {
    show({
      variant: 'error',
      message: err instanceof Error ? err.message : 'Delete failed',
    });
  }
}

function cancelDelete() {
  showDeleteModal.value = false;
  deleteTarget.value = null;
}

// --- Helpers ---

function formatDate(iso: string | null | undefined): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString();
}
</script>

<template>
  <div class="people-view">
    <h1 class="people-view__title">
      People Directory
    </h1>

    <Toolbar sticky>
      <template #start>
        <div class="people-view__search">
          <TextField
            v-model="searchInput"
            placeholder="Search by name, position, location"
            :clearable="true"
            leading-icon="search"
          />
        </div>
        <StatusBadge
          v-if="!loading"
          variant="info"
          :label="`${formatCount(normalizedTotalCount)} results`"
        />
        <span
          v-if="loadedSummary"
          class="people-view__loaded-meta"
        >
          {{ loadedSummary }}
        </span>
      </template>

      <template #end>
        <Dropdown align="end">
          <template #trigger>
            <button class="sort-chip">
              <svg
                class="sort-chip__icon"
                width="16"
                height="16"
                viewBox="0 0 16 16"
                fill="none"
              >
                <path
                  d="M4 6l4-4 4 4M4 10l4 4 4-4"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <span class="sort-chip__label">{{ sortChipLabel }}</span>
              <svg
                class="sort-chip__chevron"
                width="12"
                height="12"
                viewBox="0 0 12 12"
                fill="none"
              >
                <path
                  d="M3 4.5L6 7.5L9 4.5"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </template>
          <template #default="{ close }">
            <div class="sort-dropdown">
              <ul class="sort-dropdown__list">
                <li
                  v-for="field in sortFields"
                  :key="field.key"
                >
                  <button
                    :class="[
                      'sort-dropdown__item',
                      { 'sort-dropdown__item--active': sortBy === field.key },
                    ]"
                    @click="onSortFieldSelect(field.key); close()"
                  >
                    <span class="sort-dropdown__item-label">{{ field.label }}</span>
                    <svg
                      v-if="sortBy === field.key"
                      class="sort-dropdown__check"
                      width="16"
                      height="16"
                      viewBox="0 0 16 16"
                      fill="none"
                    >
                      <path
                        d="M3 8.5L6.5 12L13 4"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                  </button>
                </li>
              </ul>

              <div class="sort-dropdown__divider" />

              <div class="sort-dropdown__direction">
                <span class="sort-dropdown__direction-label">Direction:</span>
                <div class="sort-dropdown__direction-btns">
                  <button
                    :class="[
                      'sort-dropdown__dir-btn',
                      { 'sort-dropdown__dir-btn--active': sortDirection === SortDirection.Asc },
                    ]"
                    @click="setDirection(SortDirection.Asc); close()"
                  >
                    <svg
                      width="12"
                      height="12"
                      viewBox="0 0 12 12"
                      fill="none"
                    >
                      <path
                        d="M6 10V2M6 2L3 5M6 2L9 5"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                    A→Z
                  </button>
                  <button
                    :class="[
                      'sort-dropdown__dir-btn',
                      { 'sort-dropdown__dir-btn--active': sortDirection === SortDirection.Desc },
                    ]"
                    @click="setDirection(SortDirection.Desc); close()"
                  >
                    Z→A
                  </button>
                </div>
              </div>
            </div>
          </template>
        </Dropdown>
      </template>
    </Toolbar>

    <!-- Pin panel -->
    <div
      v-if="pinRules.length > 0"
      class="pin-panel"
    >
      <div class="pin-panel__icon-wrap">
        <svg
          width="18"
          height="18"
          viewBox="0 0 18 18"
          fill="none"
        >
          <path
            d="M10.5 1.5L12.75 3.75L10.5 7.5L13.5 10.5H9L6 16.5L4.5 15L6 10.5H1.5L4.5 7.5L8.25 5.25L10.5 1.5Z"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </div>
      <div class="pin-panel__content">
        <span class="pin-panel__title">
          Pinned Rules ({{ activePinCount }} active<span v-if="inactivePinCount > 0">, {{ inactivePinCount }} inactive</span>)
        </span>
      </div>
      <button class="pin-panel__manage">
        Manage
      </button>
    </div>

    <div
      ref="listScrollEl"
      class="people-view__list-scroll"
      :style="
        listViewportHeight
          ? { maxHeight: `${listViewportHeight}px` }
          : undefined
      "
    >
      <!-- Desktop table -->
      <div class="people-view__table">
        <DataTable
          :columns="columns"
          :rows="(people as unknown as Record<string, unknown>[])"
          row-key="id"
        >
          <template #cell-pin="{ row }">
            <span
              v-if="personPin(row as unknown as PersonFieldsFragment)"
              class="pin-badge"
            >
              {{ personPin(row as unknown as PersonFieldsFragment)!.targetPosition }}
            </span>
            <span
              v-else
              class="pin-badge--empty"
            >—</span>
          </template>

          <template #cell-age="{ value }">
            {{ value ?? '—' }}
          </template>

          <template #cell-birthdate="{ value }">
            {{ formatDate(value as string) }}
          </template>

          <template #cell-createdAt="{ value }">
            {{ formatDate(value as string) }}
          </template>

          <template #row-actions="{ row }">
            <Dropdown align="end">
              <template #trigger>
                <IconButton
                  aria-label="Row actions"
                  size="sm"
                  variant="ghost"
                >
                  ···
                </IconButton>
              </template>
              <template #default="{ close }">
                <Menu
                  :items="rowMenuItems"
                  @select="
                    (key) =>
                      onRowAction(
                        row as unknown as PersonFieldsFragment,
                        key,
                        close,
                      )
                  "
                />
              </template>
            </Dropdown>
          </template>
        </DataTable>
      </div>

      <!-- Mobile cards -->
      <div class="people-view__cards">
        <CardRow
          v-for="person in people"
          :key="person.id"
        >
          <template #primary>
            <span class="people-view__card-name">
              <span
                v-if="personPin(person)"
                class="pin-badge pin-badge--inline"
              >{{ personPin(person)!.targetPosition }}</span>
              {{ person.name }}
            </span>
          </template>
          <template #secondary>
            {{ person.position }} · {{ person.location }}
          </template>
          <template #meta>
            <span v-if="person.age">{{ person.age }} yrs</span>
          </template>
          <template #actions>
            <Dropdown align="end">
              <template #trigger>
                <IconButton
                  aria-label="Row actions"
                  size="sm"
                  variant="ghost"
                >
                  ···
                </IconButton>
              </template>
              <template #default="{ close }">
                <Menu
                  :items="rowMenuItems"
                  @select="(key) => onRowAction(person, key, close)"
                />
              </template>
            </Dropdown>
          </template>
        </CardRow>
      </div>

      <InfiniteFooter
        :state="footerState"
        :hint="footerHint"
        :end-label="footerEndLabel"
        :scroll-root="listScrollEl"
        @load="onLoadMore"
      />
    </div>
  </div>

  <Modal
    :open="showDeleteModal"
    title="Delete person"
    size="sm"
    @close="cancelDelete"
  >
    <p class="body-sm">
      Are you sure you want to delete
      <strong>{{ deleteTarget?.name }}</strong>? This action cannot be undone.
    </p>

    <template #footer>
      <Button
        variant="ghost"
        :disabled="deleting"
        @click="cancelDelete"
      >
        Cancel
      </Button>
      <Button
        variant="danger"
        :loading="deleting"
        @click="executeDelete"
      >
        Delete
      </Button>
    </template>
  </Modal>
</template>

<style scoped>
.people-view__title {
  font-size: clamp(1.35rem, 1.9vw, 1.8rem);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.people-view__search {
  width: min(360px, 100%);
}

.people-view__loaded-meta {
  color: var(--color-placeholder);
  font-size: 0.8125rem;
  white-space: nowrap;
}

.people-view__table {
  display: block;
}

.people-view__list-scroll {
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.people-view__cards {
  display: none;
}

.people-view__card-name {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

/* --- Sort chip --- */

.sort-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  height: var(--control-h-md);
  padding: 0 var(--control-px);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-family: inherit;
  font-size: var(--font-size-md);
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition:
    border-color var(--duration-fast) ease,
    color var(--duration-fast) ease;
}

.sort-chip:hover {
  border-color: var(--color-border-strong);
  color: var(--color-text);
}

.sort-chip__icon,
.sort-chip__chevron {
  flex-shrink: 0;
  color: var(--color-muted);
}

.sort-chip__label {
  line-height: 1;
}

/* --- Sort dropdown --- */

.sort-dropdown {
  width: 200px;
  padding: var(--space-1) 0;
}

.sort-dropdown__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.sort-dropdown__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 40px;
  padding: 0 var(--space-4);
  border: 0;
  background: transparent;
  color: var(--color-text-secondary);
  font-family: inherit;
  font-size: var(--font-size-md);
  text-align: start;
  cursor: pointer;
  transition:
    background-color var(--duration-fast) ease,
    color var(--duration-fast) ease;
}

.sort-dropdown__item:hover {
  background: var(--color-surface-soft);
}

.sort-dropdown__item--active {
  background: #eef2ff;
  color: var(--color-accent-strong);
  font-weight: 500;
}

.sort-dropdown__item--active:hover {
  background: #eef2ff;
}

.sort-dropdown__check {
  flex-shrink: 0;
  color: var(--color-accent-strong);
}

.sort-dropdown__divider {
  height: 1px;
  margin: var(--space-1) 0;
  background: var(--color-border);
}

.sort-dropdown__direction {
  padding: var(--space-2) var(--space-4) var(--space-2);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.sort-dropdown__direction-label {
  font-size: 0.75rem;
  color: var(--color-placeholder);
  font-weight: 400;
}

.sort-dropdown__direction-btns {
  display: flex;
  gap: var(--space-2);
}

.sort-dropdown__dir-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: var(--space-1) var(--space-2);
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--color-placeholder);
  font-family: inherit;
  font-size: var(--font-size-sm);
  font-weight: 400;
  cursor: pointer;
  transition:
    background-color var(--duration-fast) ease,
    color var(--duration-fast) ease;
}

.sort-dropdown__dir-btn:hover {
  background: var(--color-surface-soft);
  color: var(--color-text-secondary);
}

.sort-dropdown__dir-btn--active {
  background: #eef2ff;
  color: var(--color-accent-strong);
  font-weight: 500;
}

.sort-dropdown__dir-btn--active:hover {
  background: #eef2ff;
}

/* --- Pin panel --- */

.pin-panel {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: var(--radius-sm);
}

.pin-panel__icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  background: #e0e7ff;
  border-radius: var(--radius-sm);
  color: var(--color-accent-strong);
}

.pin-panel__content {
  flex: 1;
  min-width: 0;
}

.pin-panel__title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: #3730a3;
}

.pin-panel__manage {
  flex-shrink: 0;
  padding: var(--space-1) var(--space-3);
  background: var(--color-surface);
  border: 1px solid #c7d2fe;
  border-radius: var(--radius-sm);
  color: var(--color-accent-strong);
  font-family: inherit;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition:
    background-color var(--duration-fast) ease,
    border-color var(--duration-fast) ease;
}

.pin-panel__manage:hover {
  background: #eef2ff;
  border-color: var(--color-accent-strong);
}

/* --- Pin badge --- */

.pin-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #fef3c7;
  border-radius: 6px;
  color: #d97706;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
}

.pin-badge--inline {
  flex-shrink: 0;
}

.pin-badge--empty {
  color: var(--color-border-strong);
  font-size: var(--font-size-md);
}

@media (max-width: 599px) {
  .people-view__title {
    font-size: 1.15rem;
  }

  .people-view__loaded-meta {
    font-size: 0.75rem;
  }

  .people-view__table {
    display: none;
  }

  .people-view__cards {
    display: flex;
    flex-direction: column;
    gap: var(--space-2);
  }
}
</style>
