import { computed, ref, type Ref } from 'vue';
import type { ToastVariant } from './useToast';
import {
  PeopleSortField,
  SortDirection,
  type PinRuleFieldsFragment,
  type PersonFieldsFragment,
  useCreatePinRuleMutation,
  useDeletePinRuleMutation,
  usePeopleListQuery,
  useUpdatePinRuleMutation,
} from '../graphql/generated';

export type PinRuleActionMenuItem = {
  key: 'toggle' | 'remove';
  label: string;
  danger?: boolean;
  disabled?: boolean;
};

type NotifyFn = (options: { variant?: ToastVariant; message: string }) => void;

type UsePinManageOptions = {
  pinRules: Ref<PinRuleFieldsFragment[]>;
  resultPeople: Ref<PersonFieldsFragment[]>;
  resultTotalCount: Ref<number>;
  onRefresh: () => Promise<void>;
  notify: NotifyFn;
};

type PinRuleState = 'active' | 'inactive' | 'clamped' | 'no_match' | 'pending';

type PinRuleStateReason = 'not_in_result' | 'not_loaded' | 'overflow' | null;

type PinRuleStateInfo = {
  state: PinRuleState;
  effectivePosition: number | null;
  shiftedByConflict: boolean;
  reason: PinRuleStateReason;
};

function ruleKey(rule: PinRuleFieldsFragment): string {
  return rule.id ? String(rule.id) : '';
}

function parsePositiveInt(raw: string): number | null {
  const parsed = Number.parseInt(raw, 10);
  if (!Number.isFinite(parsed) || parsed < 1) return null;
  return parsed;
}

function parseCreatedAtMs(rule: PinRuleFieldsFragment): number {
  if (!rule.createdAt) return Number.MAX_SAFE_INTEGER;
  const createdAtMs = Date.parse(rule.createdAt);
  return Number.isFinite(createdAtMs) ? createdAtMs : Number.MAX_SAFE_INTEGER;
}

function comparePlacementPriority(a: PinRuleFieldsFragment, b: PinRuleFieldsFragment): number {
  if (a.targetPosition !== b.targetPosition) {
    return a.targetPosition - b.targetPosition;
  }
  const createdAtGap = parseCreatedAtMs(a) - parseCreatedAtMs(b);
  if (createdAtGap !== 0) return createdAtGap;
  return ruleKey(a).localeCompare(ruleKey(b));
}

function normalizeCount(value: number): number {
  if (!Number.isFinite(value) || value < 0) return 0;
  return Math.floor(value);
}

const stateLabelMap: Record<PinRuleState, string> = {
  active: 'Active',
  inactive: 'Inactive',
  clamped: 'Clamped',
  no_match: 'No match',
  pending: 'Pending',
};

const stateBadgeClassMap: Record<PinRuleState, string> = {
  active: 'pin-manage__state--active',
  inactive: 'pin-manage__state--inactive',
  clamped: 'pin-manage__state--clamped',
  no_match: 'pin-manage__state--no-match',
  pending: 'pin-manage__state--pending',
};

const stateDetailClassMap: Record<PinRuleState, string> = {
  active: 'pin-manage__detail--active',
  inactive: 'pin-manage__detail--inactive',
  clamped: 'pin-manage__detail--clamped',
  no_match: 'pin-manage__detail--no-match',
  pending: 'pin-manage__detail--pending',
};

function fallbackRuleState(rule: PinRuleFieldsFragment): PinRuleStateInfo {
  return {
    state: rule.enabled ? 'no_match' : 'inactive',
    effectivePosition: null,
    shiftedByConflict: false,
    reason: rule.enabled ? 'not_in_result' : null,
  };
}

export function usePinManage({
  pinRules,
  resultPeople,
  resultTotalCount,
  onRefresh,
  notify,
}: UsePinManageOptions) {
  const { mutate: doCreatePin } = useCreatePinRuleMutation({});
  const { mutate: doUpdatePin } = useUpdatePinRuleMutation({});
  const { mutate: doDeletePin } = useDeletePinRuleMutation({});

  const pinTogglingIds = ref(new Set<string>());
  const pinDeletingIds = ref(new Set<string>());
  const positionDraft = ref<Record<string, string>>({});

  const addRulePersonSearch = ref('');
  const addRulePersonId = ref<string | null>(null);
  const addRuleTargetPosition = ref('');
  const addRuleSubmitting = ref(false);
  const addRuleError = ref('');

  const showDeleteConfirm = ref(false);
  const deleteTargetRule = ref<PinRuleFieldsFragment | null>(null);
  const deleteConfirmSubmitting = ref(false);

  const sortedPinRules = computed(() =>
    [...pinRules.value].sort((a, b) => a.targetPosition - b.targetPosition),
  );

  const normalizedResultTotal = computed(() => normalizeCount(resultTotalCount.value));
  const loadedResultCount = computed(() => resultPeople.value.length);
  const isResultFullyLoaded = computed(
    () => loadedResultCount.value >= normalizedResultTotal.value,
  );
  const currentResultPersonIds = computed(
    () =>
      new Set(
        resultPeople.value
          .map((person) => (person.id ? String(person.id) : ''))
          .filter((personId) => personId.length > 0),
      ),
  );

  const pinRuleStateMap = computed(() => {
    const map = new Map<string, PinRuleStateInfo>();
    const eligibleRules: PinRuleFieldsFragment[] = [];

    for (const rule of sortedPinRules.value) {
      const id = ruleKey(rule);
      if (!id) continue;

      if (!rule.enabled) {
        map.set(id, {
          state: 'inactive',
          effectivePosition: null,
          shiftedByConflict: false,
          reason: null,
        });
        continue;
      }

      const personId = rule.personId ? String(rule.personId) : '';
      const hasPersonInLoadedResult =
        personId.length > 0 && currentResultPersonIds.value.has(personId);

      if (!hasPersonInLoadedResult && normalizedResultTotal.value < 1) {
        map.set(id, {
          state: 'no_match',
          effectivePosition: null,
          shiftedByConflict: false,
          reason: 'not_in_result',
        });
        continue;
      }

      if (hasPersonInLoadedResult) {
        eligibleRules.push(rule);
        continue;
      }

      if (personId.length > 0 && !isResultFullyLoaded.value) {
        map.set(id, {
          state: 'pending',
          effectivePosition: null,
          shiftedByConflict: false,
          reason: 'not_loaded',
        });
        continue;
      }

      map.set(id, {
        state: 'no_match',
        effectivePosition: null,
        shiftedByConflict: false,
        reason: 'not_in_result',
      });
    }

    const occupiedPositions = new Set<number>();
    for (const rule of [...eligibleRules].sort(comparePlacementPriority)) {
      const id = ruleKey(rule);
      if (!id) continue;

      const total = normalizedResultTotal.value;
      const clampedTarget = Math.min(Math.max(rule.targetPosition, 1), total);
      let effectivePosition = clampedTarget;

      while (occupiedPositions.has(effectivePosition) && effectivePosition < total) {
        effectivePosition += 1;
      }

      if (occupiedPositions.has(effectivePosition)) {
        map.set(id, {
          state: 'no_match',
          effectivePosition: null,
          shiftedByConflict: false,
          reason: 'overflow',
        });
        continue;
      }

      occupiedPositions.add(effectivePosition);

      map.set(id, {
        state: rule.targetPosition > total ? 'clamped' : 'active',
        effectivePosition,
        shiftedByConflict: effectivePosition !== clampedTarget,
        reason: null,
      });
    }

    return map;
  });

  const nextPinTargetPosition = computed(
    () => pinRules.value.reduce((max, rule) => Math.max(max, rule.targetPosition), 0) + 1,
  );

  const addRuleSearchTerm = computed(() => addRulePersonSearch.value.trim());
  const { result: addRulePeopleResult, loading: addRulePeopleLoading } = usePeopleListQuery(
    () => ({
      first: 50,
      after: null as string | null,
      search: addRuleSearchTerm.value || null,
      sortBy: PeopleSortField.Name,
      sortDirection: SortDirection.Asc,
    }),
  );

  const candidatePeople = computed(
    () =>
      (addRulePeopleResult.value?.peopleList?.nodes?.filter(Boolean) ?? []) as PersonFieldsFragment[],
  );

  const usedPersonIds = computed(
    () =>
      new Set(
        pinRules.value
          .map((rule) => (rule.personId ? String(rule.personId) : ''))
          .filter((personId) => personId.length > 0),
      ),
  );

  const addRulePersonOptions = computed(() =>
    candidatePeople.value
      .filter((person) => !!person.id)
      .map((person) => {
        const personId = String(person.id);
        const alreadyPinned = usedPersonIds.value.has(personId);
        return {
          value: personId,
          disabled: alreadyPinned,
          label: alreadyPinned
            ? `${person.name ?? 'Unnamed'} (${person.position ?? 'No position'}) - already pinned`
            : `${person.name ?? 'Unnamed'} (${person.position ?? 'No position'})`,
        };
      }),
  );

  const hasSelectableAddRulePerson = computed(() =>
    addRulePersonOptions.value.some((option) => !option.disabled),
  );

  const addRuleSubmitDisabled = computed(
    () =>
      addRuleSubmitting.value ||
      addRulePeopleLoading.value ||
      !hasSelectableAddRulePerson.value,
  );

  const showNoPeopleSearchResult = computed(
    () =>
      !addRulePeopleLoading.value &&
      addRuleSearchTerm.value.length > 0 &&
      addRulePersonOptions.value.length === 0,
  );

  function resetAddRuleForm() {
    addRulePersonId.value = null;
    addRuleTargetPosition.value = String(nextPinTargetPosition.value);
    addRuleError.value = '';
  }

  function onDialogOpen() {
    addRuleTargetPosition.value = String(nextPinTargetPosition.value);
    addRuleError.value = '';
  }

  function onDialogClose() {
    positionDraft.value = {};
    resetAddRuleForm();
    cancelRemovePinRule();
  }

  function isPinRuleBusy(rule: PinRuleFieldsFragment): boolean {
    const id = ruleKey(rule);
    if (!id) return true;
    return pinTogglingIds.value.has(id) || pinDeletingIds.value.has(id);
  }

  function pinRuleStateInfo(rule: PinRuleFieldsFragment): PinRuleStateInfo {
    const id = ruleKey(rule);
    if (!id) return fallbackRuleState(rule);
    return pinRuleStateMap.value.get(id) ?? fallbackRuleState(rule);
  }

  function pinRuleStateLabel(rule: PinRuleFieldsFragment): string {
    return stateLabelMap[pinRuleStateInfo(rule).state];
  }

  function pinRuleStateBadgeClass(rule: PinRuleFieldsFragment): string {
    return stateBadgeClassMap[pinRuleStateInfo(rule).state];
  }

  function pinRuleStateDetailClass(rule: PinRuleFieldsFragment): string {
    return stateDetailClassMap[pinRuleStateInfo(rule).state];
  }

  function pinRuleStateDescription(rule: PinRuleFieldsFragment): string {
    const info = pinRuleStateInfo(rule);
    const targetSummary = `Target: pos ${rule.targetPosition}`;

    if (info.state === 'inactive') {
      return `${targetSummary} · Rule disabled`;
    }
    if (info.state === 'pending') {
      return `${targetSummary} · Person not yet loaded in view (scroll to load more)`;
    }
    if (info.state === 'no_match') {
      if (info.reason === 'overflow') {
        return `${targetSummary} · No available slot after higher-priority rules`;
      }
      return `${targetSummary} · Not matched in current result`;
    }

    const effectivePosition = info.effectivePosition ?? rule.targetPosition;
    if (info.state === 'clamped') {
      return `${targetSummary} · Effective: pos ${effectivePosition} (clamped to result end)`;
    }
    if (info.shiftedByConflict) {
      return `${targetSummary} · Effective: pos ${effectivePosition} (shifted by conflict)`;
    }
    return `${targetSummary} · Effective: pos ${effectivePosition}`;
  }

  function pinRuleMenuItems(rule: PinRuleFieldsFragment): PinRuleActionMenuItem[] {
    const busy = isPinRuleBusy(rule);
    return [
      {
        key: 'toggle',
        label: rule.enabled ? 'Disable rule' : 'Enable rule',
        disabled: busy,
      },
      {
        key: 'remove',
        label: 'Remove rule',
        danger: true,
        disabled: busy,
      },
    ];
  }

  async function togglePinEnabled(rule: PinRuleFieldsFragment) {
    const id = ruleKey(rule);
    if (!id || pinTogglingIds.value.has(id)) return;
    pinTogglingIds.value.add(id);
    try {
      await doUpdatePin({ id, enabled: !rule.enabled });
      await onRefresh();
    } catch (err) {
      notify({
        variant: 'error',
        message: err instanceof Error ? err.message : 'Update failed',
      });
    } finally {
      pinTogglingIds.value.delete(id);
    }
  }

  function positionInputValue(rule: PinRuleFieldsFragment): string | number {
    const id = ruleKey(rule);
    if (!id) return rule.targetPosition;
    const draft = positionDraft.value[id];
    return draft !== undefined ? draft : rule.targetPosition;
  }

  function onPositionInput(rule: PinRuleFieldsFragment, e: Event) {
    const id = ruleKey(rule);
    if (!id) return;
    const value = (e.target as HTMLInputElement).value;
    positionDraft.value = { ...positionDraft.value, [id]: value };
  }

  async function commitPosition(rule: PinRuleFieldsFragment) {
    const id = ruleKey(rule);
    if (!id) return;
    const draft = positionDraft.value[id];
    if (draft === undefined) return;

    const nextPosition = parsePositiveInt(draft);

    const updated = { ...positionDraft.value };
    delete updated[id];
    positionDraft.value = updated;

    if (nextPosition == null || nextPosition === rule.targetPosition) return;
    try {
      await doUpdatePin({ id, targetPosition: nextPosition });
      await onRefresh();
    } catch (err) {
      notify({
        variant: 'error',
        message: err instanceof Error ? err.message : 'Update failed',
      });
    }
  }

  function onPositionKeydown(rule: PinRuleFieldsFragment, e: KeyboardEvent) {
    const id = ruleKey(rule);
    if (!id) return;
    if (e.key === 'Enter') (e.target as HTMLInputElement).blur();
    if (e.key === 'Escape') {
      const updated = { ...positionDraft.value };
      delete updated[id];
      positionDraft.value = updated;
      (e.target as HTMLInputElement).blur();
    }
  }

  function requestRemovePinRule(rule: PinRuleFieldsFragment) {
    if (!ruleKey(rule)) return;
    deleteTargetRule.value = rule;
    showDeleteConfirm.value = true;
  }

  function cancelRemovePinRule() {
    showDeleteConfirm.value = false;
    deleteTargetRule.value = null;
  }

  async function confirmRemovePinRule() {
    if (!deleteTargetRule.value) return;
    const id = ruleKey(deleteTargetRule.value);
    if (!id || pinDeletingIds.value.has(id)) return;

    pinDeletingIds.value.add(id);
    deleteConfirmSubmitting.value = true;
    try {
      await doDeletePin({ id });
      await onRefresh();
      cancelRemovePinRule();
    } catch (err) {
      notify({
        variant: 'error',
        message: err instanceof Error ? err.message : 'Delete failed',
      });
    } finally {
      pinDeletingIds.value.delete(id);
      deleteConfirmSubmitting.value = false;
    }
  }

  function onPinRuleAction(rule: PinRuleFieldsFragment, key: string, close?: () => void) {
    close?.();
    if (key === 'toggle') {
      void togglePinEnabled(rule);
      return;
    }
    if (key === 'remove') {
      requestRemovePinRule(rule);
    }
  }

  async function submitAddRule() {
    const personId = addRulePersonId.value ? String(addRulePersonId.value) : '';
    const targetPosition = parsePositiveInt(addRuleTargetPosition.value);

    if (!personId) {
      addRuleError.value = 'Please select a person.';
      return;
    }
    if (targetPosition == null) {
      addRuleError.value = 'Target position must be 1 or greater.';
      return;
    }
    if (usedPersonIds.value.has(personId)) {
      addRuleError.value = 'This person already has a pin rule.';
      return;
    }

    addRuleSubmitting.value = true;
    addRuleError.value = '';
    try {
      await doCreatePin({ personId, targetPosition });
      notify({ variant: 'success', message: `Added pin rule at position ${targetPosition}` });
      await onRefresh();
      resetAddRuleForm();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Create rule failed';
      addRuleError.value = message;
      notify({ variant: 'error', message });
    } finally {
      addRuleSubmitting.value = false;
    }
  }

  return {
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
    pinRuleStateLabel,
    pinRuleStateBadgeClass,
    pinRuleStateDetailClass,
    pinRuleStateDescription,
    pinRuleMenuItems,
    positionInputValue,
    onPositionInput,
    commitPosition,
    onPositionKeydown,
    onPinRuleAction,
    cancelRemovePinRule,
    confirmRemovePinRule,
    submitAddRule,
  };
}
