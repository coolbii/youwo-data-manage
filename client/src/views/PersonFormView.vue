<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import Button from '../components/form/Button.vue';
import TextField from '../components/form/TextField.vue';
import { useToast } from '../composables/useToast';
import {
  useCreatePersonMutation,
  usePersonDetailQuery,
  useUpdatePersonMutation,
  type PersonFieldsFragment,
  PeopleSortField,
  SortDirection,
} from '../graphql/generated';

const route = useRoute();
const router = useRouter();
const { show } = useToast();

const VALID_SORT_FIELDS = new Set<string>(Object.values(PeopleSortField));
const VALID_SORT_DIRECTIONS = new Set<string>(Object.values(SortDirection));

function parseQueryString(value: unknown): string {
  return typeof value === 'string' ? value : '';
}

function buildReturnQuery() {
  const query: Record<string, string> = {};
  const search = parseQueryString(route.query.search).trim();
  const sortBy = parseQueryString(route.query.sortBy);
  const sortDirection = parseQueryString(route.query.sortDirection);

  if (search) query.search = search;
  if (VALID_SORT_FIELDS.has(sortBy) && sortBy !== PeopleSortField.CreatedAt) {
    query.sortBy = sortBy;
  }
  if (VALID_SORT_DIRECTIONS.has(sortDirection) && sortDirection !== SortDirection.Asc) {
    query.sortDirection = sortDirection;
  }

  return query;
}

const returnQuery = computed(() => buildReturnQuery());
const backTarget = computed(() => ({ name: 'people', query: returnQuery.value }));

const personId = computed(() => {
  const raw = route.params.id;
  return typeof raw === 'string' ? raw : '';
});
const isEdit = computed(() => !!personId.value);

const title = computed(() => (isEdit.value ? 'Edit Person' : 'Add Person'));
const subtitle = computed(() =>
  isEdit.value
    ? 'Update the person profile and keep the directory aligned with the latest data.'
    : 'Create a new person entry for the directory.',
);
const submitLabel = computed(() => (isEdit.value ? 'Save changes' : 'Create person'));

const name = ref('');
const position = ref('');
const location = ref('');
const birthdate = ref('');

const nameError = ref('');
const positionError = ref('');
const locationError = ref('');
const birthdateError = ref('');

const saving = ref(false);
const initializedFromResult = ref(false);
const originalPerson = ref<PersonFieldsFragment | null>(null);

const {
  result,
  loading: detailLoading,
  error: detailError,
} = usePersonDetailQuery(
  () => ({ id: personId.value || null }),
  () => ({ enabled: isEdit.value }),
);

const person = computed(
  () => (result.value?.person ?? null) as PersonFieldsFragment | null,
);
const isMissingPerson = computed(
  () => isEdit.value && !detailLoading.value && !detailError.value && !person.value,
);
const formDisabled = computed(
  () => saving.value || (isEdit.value && detailLoading.value && !initializedFromResult.value),
);

watch(
  isEdit,
  (value) => {
    if (value) return;
    initializedFromResult.value = true;
    originalPerson.value = null;
    name.value = '';
    position.value = '';
    location.value = '';
    birthdate.value = '';
    clearErrors();
  },
  { immediate: true },
);

watch(
  personId,
  () => {
    initializedFromResult.value = false;
    originalPerson.value = null;
    clearErrors();
  },
);

watch(
  person,
  (value) => {
    if (!isEdit.value || initializedFromResult.value || !value) return;
    name.value = value.name ?? '';
    position.value = value.position ?? '';
    location.value = value.location ?? '';
    birthdate.value = value.birthdate ?? '';
    originalPerson.value = value;
    initializedFromResult.value = true;
    clearErrors();
  },
  { immediate: true },
);

function clearErrors() {
  nameError.value = '';
  positionError.value = '';
  locationError.value = '';
  birthdateError.value = '';
}

function validate(): boolean {
  clearErrors();
  let valid = true;

  if (!name.value.trim()) {
    nameError.value = 'Name is required';
    valid = false;
  }
  if (!position.value.trim()) {
    positionError.value = 'Position is required';
    valid = false;
  }
  if (!location.value.trim()) {
    locationError.value = 'Location is required';
    valid = false;
  }
  if (!birthdate.value.trim()) {
    birthdateError.value = 'Birthdate is required';
    valid = false;
  }

  return valid;
}

function listPlacementChanged() {
  if (!originalPerson.value) return true;

  return (
    (originalPerson.value.name ?? '').trim() !== name.value.trim() ||
    (originalPerson.value.position ?? '').trim() !== position.value.trim() ||
    (originalPerson.value.location ?? '').trim() !== location.value.trim() ||
    (originalPerson.value.birthdate ?? '').trim() !== birthdate.value.trim()
  );
}

async function returnToList(forceRefresh = false) {
  await router.replace({
    name: 'people',
    query: forceRefresh
      ? { ...returnQuery.value, refresh: String(Date.now()) }
      : returnQuery.value,
  });
}

const { mutate: doCreate } = useCreatePersonMutation({});
const { mutate: doUpdate } = useUpdatePersonMutation({});

async function onSubmit() {
  if (!validate() || formDisabled.value) return;

  saving.value = true;

  try {
    const payload = {
      name: name.value.trim(),
      position: position.value.trim(),
      location: location.value.trim(),
      birthdate: birthdate.value.trim(),
    };

    if (isEdit.value) {
      const needsRefresh = listPlacementChanged();
      const response = await doUpdate({ id: personId.value, ...payload });
      const savedName = response?.data?.updatePerson?.name ?? payload.name;

      show({
        variant: 'success',
        message: needsRefresh
          ? `Updated ${savedName}. List refreshed to match current filters.`
          : `Updated ${savedName}`,
      });

      await returnToList(needsRefresh);
      return;
    }

    const response = await doCreate(payload);
    const savedName = response?.data?.createPerson?.name ?? payload.name;

    show({ variant: 'success', message: `Created ${savedName}` });
    await returnToList(true);
  } catch (err) {
    show({
      variant: 'error',
      message: err instanceof Error ? err.message : 'Save failed',
    });
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <div class="person-form-view stack-6">
    <nav
      class="person-form-view__breadcrumb"
      aria-label="Breadcrumb"
    >
      <RouterLink
        class="person-form-view__breadcrumb-link"
        :to="backTarget"
      >
        People Directory
      </RouterLink>
      <span class="person-form-view__breadcrumb-separator">/</span>
      <span class="person-form-view__breadcrumb-current">{{ title }}</span>
    </nav>

    <header class="person-form-view__header">
      <div class="stack-2">
        <h1 class="person-form-view__title">
          {{ title }}
        </h1>
        <p class="body-sm text-muted">
          {{ subtitle }}
        </p>
      </div>

      <Button
        variant="ghost"
        @click="returnToList()"
      >
        Back to list
      </Button>
    </header>

    <section
      v-if="isEdit && detailLoading && !initializedFromResult"
      class="person-form-view__state surface-card"
    >
      <h2 class="person-form-view__state-title">
        Loading person
      </h2>
      <p class="body-sm text-muted">
        Fetching the latest record before editing.
      </p>
    </section>

    <section
      v-else-if="detailError"
      class="person-form-view__state surface-card"
    >
      <h2 class="person-form-view__state-title">
        Unable to load person
      </h2>
      <p class="body-sm text-muted">
        {{ detailError.message }}
      </p>
      <div class="person-form-view__state-actions">
        <Button @click="returnToList()">
          Return to list
        </Button>
      </div>
    </section>

    <section
      v-else-if="isMissingPerson"
      class="person-form-view__state surface-card"
    >
      <h2 class="person-form-view__state-title">
        Person not found
      </h2>
      <p class="body-sm text-muted">
        The requested record is unavailable or was removed.
      </p>
      <div class="person-form-view__state-actions">
        <Button @click="returnToList(true)">
          Refresh list
        </Button>
      </div>
    </section>

    <form
      v-else
      class="person-form-view__card surface-card"
      @submit.prevent="onSubmit"
    >
      <p class="person-form-view__required-note">
        * Required fields
      </p>

      <div class="person-form-view__fields">
        <TextField
          v-model="name"
          label="Name"
          placeholder="Full name"
          :error="nameError"
          :required="true"
          :disabled="formDisabled"
        />
        <TextField
          v-model="position"
          label="Position"
          placeholder="e.g. Backend Engineer"
          :error="positionError"
          :required="true"
          :disabled="formDisabled"
        />
        <TextField
          v-model="location"
          label="Location"
          placeholder="e.g. Taipei"
          :error="locationError"
          :required="true"
          :disabled="formDisabled"
        />
        <TextField
          v-model="birthdate"
          label="Birthdate"
          type="date"
          :error="birthdateError"
          :required="true"
          :disabled="formDisabled"
        />
      </div>

      <div class="person-form-view__footer">
        <Button
          variant="ghost"
          :disabled="saving"
          @click="returnToList()"
        >
          Cancel
        </Button>
        <Button
          type="submit"
          :loading="saving"
        >
          {{ submitLabel }}
        </Button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.person-form-view__breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

.person-form-view__breadcrumb-link {
  color: var(--color-text-secondary);
  font-weight: 500;
}

.person-form-view__breadcrumb-link:hover {
  color: var(--color-text);
}

.person-form-view__breadcrumb-separator {
  color: var(--color-border-strong);
}

.person-form-view__breadcrumb-current {
  color: var(--color-placeholder);
}

.person-form-view__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.person-form-view__title {
  font-size: clamp(1.5rem, 2vw, 2rem);
  font-weight: 600;
  letter-spacing: -0.02em;
}

.person-form-view__card,
.person-form-view__state {
  padding: var(--space-6);
}

.person-form-view__fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.person-form-view__required-note {
  margin-bottom: var(--space-4);
  color: var(--color-muted);
  font-size: var(--font-size-sm);
}

.person-form-view__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

.person-form-view__state {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  max-width: 720px;
}

.person-form-view__state-title {
  font-size: 1.125rem;
  font-weight: 600;
}

.person-form-view__state-actions {
  display: flex;
  gap: var(--space-3);
}

@media (max-width: 767px) {
  .person-form-view__header {
    flex-direction: column;
    align-items: stretch;
  }

  .person-form-view__card,
  .person-form-view__state {
    padding: var(--space-4);
  }

  .person-form-view__fields {
    grid-template-columns: 1fr;
  }

  .person-form-view__footer {
    flex-direction: column-reverse;
  }
}
</style>
