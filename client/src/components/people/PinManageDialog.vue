<script setup lang="ts">
import { toRef, watch } from 'vue';
import Button from '../form/Button.vue';
import IconButton from '../form/IconButton.vue';
import TextField from '../form/TextField.vue';
import Select from '../form/Select.vue';
import Dropdown from '../overlay/Dropdown.vue';
import Menu from '../overlay/Menu.vue';
import Modal from '../overlay/Modal.vue';
import BottomSheet from '../overlay/BottomSheet.vue';
import { useToast } from '../../composables/useToast';
import { usePinManage } from '../../composables/usePinManage';
import type { PinRuleFieldsFragment } from '../../graphql/generated';

const props = defineProps<{
  open: boolean;
  isMobile: boolean;
  pinRules: PinRuleFieldsFragment[];
  onRefresh: () => Promise<void>;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const { show } = useToast();

const {
  sortedPinRules,
  addRulePersonSearch,
  addRulePeopleLoading,
  addRulePersonId,
  addRuleTargetPosition,
  addRuleSubmitting,
  addRuleError,
  addRulePersonOptions,
  hasSelectableAddRulePerson,
  addRuleSubmitDisabled,
  showNoPeopleSearchResult,
  showDeleteConfirm,
  deleteTargetRule,
  deleteConfirmSubmitting,
  onDialogOpen,
  onDialogClose,
  isPinRuleBusy,
  pinRuleMenuItems,
  positionInputValue,
  onPositionInput,
  commitPosition,
  onPositionKeydown,
  onPinRuleAction,
  cancelRemovePinRule,
  confirmRemovePinRule,
  submitAddRule,
} = usePinManage({
  pinRules: toRef(props, 'pinRules'),
  onRefresh: props.onRefresh,
  notify: show,
});

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      onDialogOpen();
      return;
    }
    onDialogClose();
  },
  { immediate: true },
);

function closeManage() {
  emit('close');
}
</script>

<template>
  <component
    :is="isMobile ? BottomSheet : Modal"
    :key="isMobile ? 'pin-sheet' : 'pin-modal'"
    :open="open"
    title="Manage pins"
    v-bind="isMobile ? {} : { size: 'sm' }"
    @close="closeManage"
  >
    <div :class="['pin-manage', { 'pin-manage--mobile': isMobile }]">
      <p class="pin-manage__intro">
        Pin rules insert selected people at fixed positions in the current result set. Position is 1-based.
      </p>

      <div
        v-if="sortedPinRules.length === 0"
        class="pin-manage__empty"
      >
        <p>No pin rules yet. Add one below.</p>
      </div>

      <ul
        v-else
        class="pin-manage__list"
      >
        <li
          v-for="rule in sortedPinRules"
          :key="rule.id"
          :class="['pin-manage__item', { 'pin-manage__item--disabled': !rule.enabled }]"
        >
          <label class="pin-manage__pos-label">
            <span class="sr-only">Position</span>
            <input
              type="number"
              min="1"
              class="pin-manage__pos-input"
              :value="positionInputValue(rule)"
              @input="onPositionInput(rule, $event)"
              @blur="commitPosition(rule)"
              @keydown="onPositionKeydown(rule, $event)"
            >
          </label>

          <div class="pin-manage__person">
            <span class="pin-manage__name">{{ rule.personName }}</span>
            <span :class="['pin-manage__state', rule.enabled ? 'pin-manage__state--enabled' : 'pin-manage__state--disabled']">
              {{ rule.enabled ? 'Enabled' : 'Disabled' }}
            </span>
          </div>

          <Dropdown align="end">
            <template #trigger>
              <IconButton
                aria-label="Pin rule actions"
                size="sm"
                variant="ghost"
                :disabled="isPinRuleBusy(rule)"
              >
                ···
              </IconButton>
            </template>
            <template #default="{ close }">
              <Menu
                :items="pinRuleMenuItems(rule)"
                @select="(key) => onPinRuleAction(rule, key, close)"
              />
            </template>
          </Dropdown>
        </li>
      </ul>

      <form
        class="pin-manage__add"
        @submit.prevent="submitAddRule"
      >
        <h3 class="pin-manage__add-title">
          Add New Rule
        </h3>
        <div class="pin-manage__add-grid">
          <TextField
            v-model="addRulePersonSearch"
            class="pin-manage__add-search"
            label="Search Person"
            placeholder="Search by name (global directory)"
            :disabled="addRuleSubmitting"
            :clearable="true"
            leading-icon="search"
          />
          <Select
            v-model="addRulePersonId"
            label="Person"
            placeholder="Select a person"
            :options="addRulePersonOptions"
            :disabled="addRuleSubmitting || addRulePeopleLoading || addRulePersonOptions.length === 0"
          />
          <TextField
            v-model="addRuleTargetPosition"
            label="Target Position"
            type="number"
            placeholder="e.g. 1"
            :disabled="addRuleSubmitting"
          />
        </div>

        <p
          v-if="addRuleError"
          class="pin-manage__error"
        >
          {{ addRuleError }}
        </p>
        <p
          v-else-if="showNoPeopleSearchResult"
          class="pin-manage__helper"
        >
          No people matched this search. Try another keyword.
        </p>
        <p
          v-else-if="!hasSelectableAddRulePerson"
          class="pin-manage__helper"
        >
          All matched people already have pin rules.
        </p>
        <p
          v-else
          class="pin-manage__helper"
        >
          If the target position already exists, later rules are shifted by the backend placement algorithm.
        </p>

        <div :class="['pin-manage__add-actions', { 'pin-manage__add-actions--sticky': isMobile }]">
          <Button
            type="submit"
            :loading="addRuleSubmitting"
            :disabled="addRuleSubmitDisabled"
            :block="isMobile"
          >
            Add Rule
          </Button>
        </div>
      </form>
    </div>
  </component>

  <component
    :is="isMobile ? BottomSheet : Modal"
    :key="isMobile ? 'pin-delete-sheet' : 'pin-delete-modal'"
    :open="showDeleteConfirm"
    title="Remove pin rule"
    v-bind="isMobile ? {} : { size: 'sm' }"
    @close="cancelRemovePinRule"
  >
    <p class="pin-manage-delete__body">
      Remove pin rule for
      <strong>{{ deleteTargetRule?.personName || 'this person' }}</strong>?
      This action cannot be undone.
    </p>

    <template #footer>
      <div :class="['pin-manage-delete__actions', { 'pin-manage-delete__actions--stacked': isMobile }]">
        <Button
          variant="ghost"
          :disabled="deleteConfirmSubmitting"
          :block="isMobile"
          @click="cancelRemovePinRule"
        >
          Cancel
        </Button>
        <Button
          variant="danger"
          :loading="deleteConfirmSubmitting"
          :block="isMobile"
          @click="confirmRemovePinRule"
        >
          Remove
        </Button>
      </div>
    </template>
  </component>
</template>

<style scoped>
.pin-manage__intro {
  margin: 0 0 var(--space-4);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
  line-height: 1.45;
}

.pin-manage__empty {
  padding: var(--space-4) 0;
  color: var(--color-muted);
  font-size: var(--font-size-md);
  text-align: center;
}

.pin-manage__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.pin-manage__item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border);
}

.pin-manage__item:last-child {
  border-bottom: 0;
}

.pin-manage__item--disabled .pin-manage__name {
  color: var(--color-muted);
}

.pin-manage__person {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.pin-manage__pos-label {
  flex-shrink: 0;
}

.pin-manage__pos-input {
  width: 48px;
  height: 32px;
  padding: 0 var(--space-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font-family: inherit;
  font-size: var(--font-size-sm);
  font-weight: 700;
  text-align: center;
  transition: border-color var(--duration-fast) ease;
  appearance: textfield;
}

.pin-manage__pos-input::-webkit-inner-spin-button,
.pin-manage__pos-input::-webkit-outer-spin-button {
  appearance: none;
}

.pin-manage__pos-input:focus {
  outline: none;
  border-color: var(--color-accent);
}

.pin-manage__name {
  font-size: var(--font-size-md);
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pin-manage__state {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 2px var(--space-2);
  border-radius: 999px;
  font-size: 0.6875rem;
  font-weight: 600;
  letter-spacing: 0.01em;
  text-transform: uppercase;
}

.pin-manage__state--enabled {
  background: #dcfce7;
  color: #166534;
}

.pin-manage__state--disabled {
  background: #f3f4f6;
  color: #4b5563;
}

.pin-manage__add {
  margin-top: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

.pin-manage__add-title {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-md);
  font-weight: 600;
}

.pin-manage__add-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 136px;
  gap: var(--space-3);
  margin-top: var(--space-3);
}

.pin-manage__add-search {
  grid-column: 1 / -1;
}

.pin-manage__error {
  margin: var(--space-2) 0 0;
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}

.pin-manage__helper {
  margin: var(--space-2) 0 0;
  color: var(--color-muted);
  font-size: var(--font-size-sm);
  line-height: 1.4;
}

.pin-manage__add-actions {
  margin-top: var(--space-3);
}

.pin-manage-delete__body {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: 1.45;
}

.pin-manage-delete__actions {
  display: flex;
  width: 100%;
  justify-content: flex-end;
  gap: var(--space-3);
}

.pin-manage-delete__actions--stacked {
  flex-direction: column-reverse;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

@media (max-width: 599px) {
  .pin-manage--mobile .pin-manage__add-grid {
    grid-template-columns: 1fr;
  }

  .pin-manage__add-search {
    grid-column: auto;
  }

  .pin-manage__add-actions--sticky {
    position: sticky;
    bottom: 0;
    z-index: 1;
    padding-top: var(--space-3);
    background: linear-gradient(
      to bottom,
      rgb(255 255 255 / 0),
      var(--color-surface) 35%
    );
  }
}
</style>
