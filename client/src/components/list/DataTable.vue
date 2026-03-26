<script setup lang="ts">
export type TableColumn = {
  key: string;
  label: string;
  width?: string;
  align?: 'start' | 'center' | 'end';
  sortable?: boolean;
};

export type SortState = {
  key: string;
  direction: 'asc' | 'desc';
} | null;

const props = withDefaults(
  defineProps<{
    columns: TableColumn[];
    rows: Record<string, unknown>[];
    rowKey?: string;
    sort?: SortState;
  }>(),
  {
    rowKey: 'id',
    sort: null,
  },
);

const emit = defineEmits<{
  (e: 'sort', key: string): void;
}>();

function onHeaderClick(col: TableColumn) {
  if (col.sortable) emit('sort', col.key);
}

function sortIcon(col: TableColumn): string | null {
  if (!col.sortable) return null;
  if (!props.sort || props.sort.key !== col.key) return '↕';
  return props.sort.direction === 'asc' ? '↑' : '↓';
}
</script>

<template>
  <div class="data-table-wrap">
    <table class="data-table">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :class="[
              'data-table__th',
              `data-table__th--${col.align ?? 'start'}`,
              { 'data-table__th--sortable': col.sortable },
              {
                'data-table__th--active':
                  sort && sort.key === col.key,
              },
            ]"
            :style="col.width ? { width: col.width } : undefined"
            :tabindex="col.sortable ? 0 : undefined"
            :aria-sort="
              col.sortable && sort && sort.key === col.key
                ? sort.direction === 'asc'
                  ? 'ascending'
                  : 'descending'
                : col.sortable
                  ? 'none'
                  : undefined
            "
            @click="onHeaderClick(col)"
            @keydown.enter="onHeaderClick(col)"
            @keydown.space.prevent="onHeaderClick(col)"
          >
            <span class="data-table__th-label">
              {{ col.label }}
            </span>
            <span
              v-if="sortIcon(col)"
              class="data-table__th-sort"
              aria-hidden="true"
            >
              {{ sortIcon(col) }}
            </span>
          </th>
          <th
            v-if="$slots['row-actions']"
            class="data-table__th data-table__th--actions"
          />
        </tr>
      </thead>

      <tbody>
        <tr
          v-for="row in rows"
          :key="(row[rowKey] as string)"
          class="data-table__row"
        >
          <td
            v-for="col in columns"
            :key="col.key"
            :class="[
              'data-table__td',
              `data-table__td--${col.align ?? 'start'}`,
            ]"
          >
            <slot
              :name="`cell-${col.key}`"
              :value="row[col.key]"
              :row="row"
            >
              {{ row[col.key] }}
            </slot>
          </td>
          <td
            v-if="$slots['row-actions']"
            class="data-table__td data-table__td--actions"
          >
            <slot
              name="row-actions"
              :row="row"
            />
          </td>
        </tr>
      </tbody>
    </table>

    <div
      v-if="rows.length === 0"
      class="data-table__empty"
    >
      <slot name="empty">
        <p class="data-table__empty-text">
          No data
        </p>
      </slot>
    </div>
  </div>
</template>

<style scoped>
.data-table-wrap {
  width: 100%;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-md);
}

.data-table__th {
  padding: var(--space-3) var(--space-4);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  white-space: nowrap;
  border-bottom: 1px solid var(--color-border);
  user-select: none;
}

.data-table__th--start {
  text-align: start;
}

.data-table__th--center {
  text-align: center;
}

.data-table__th--end {
  text-align: end;
}

.data-table__th--sortable {
  cursor: pointer;
  transition: color var(--duration-fast) ease;
}

.data-table__th--sortable:hover {
  color: var(--color-text-secondary);
}

.data-table__th--active {
  color: var(--color-text);
}

.data-table__th--actions {
  width: 48px;
}

.data-table__th-label {
  vertical-align: middle;
}

.data-table__th-sort {
  display: inline-block;
  margin-left: var(--space-1);
  font-size: 0.75rem;
  vertical-align: middle;
}

.data-table__row {
  transition: background-color var(--duration-fast) ease;
}

.data-table__row:hover {
  background: var(--color-surface-soft);
}

.data-table__td {
  padding: var(--space-3) var(--space-4);
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.data-table__td--start {
  text-align: start;
}

.data-table__td--center {
  text-align: center;
}

.data-table__td--end {
  text-align: end;
}

.data-table__td--actions {
  text-align: end;
  white-space: nowrap;
}

.data-table__empty {
  padding: var(--space-10) var(--space-4);
  text-align: center;
}

.data-table__empty-text {
  color: var(--color-muted);
  font-size: var(--font-size-md);
}
</style>
