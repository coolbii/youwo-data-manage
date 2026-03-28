import gql from 'graphql-tag';
import * as VueApolloComposable from '@vue/apollo-composable';
import type * as VueCompositionApi from 'vue';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type MakeEmpty<T extends { [key: string]: unknown }, K extends keyof T> = { [_ in K]?: never };
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
export type ReactiveFunction<TParam> = () => TParam;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: { input: string; output: string; }
  String: { input: string; output: string; }
  Boolean: { input: boolean; output: boolean; }
  Int: { input: number; output: number; }
  Float: { input: number; output: number; }
  Long: { input: any; output: any; }
  UUID: { input: any; output: any; }
};

export type Mutation = {
  __typename?: 'Mutation';
  createPerson?: Maybe<PersonPayload>;
  createPinRule?: Maybe<PinRulePayload>;
  deletePerson?: Maybe<Scalars['Boolean']['output']>;
  deletePinRule?: Maybe<Scalars['Boolean']['output']>;
  updatePerson?: Maybe<PersonPayload>;
  updatePinRule?: Maybe<PinRulePayload>;
};


export type MutationCreatePersonArgs = {
  birthdate: Scalars['String']['input'];
  location: Scalars['String']['input'];
  name: Scalars['String']['input'];
  position: Scalars['String']['input'];
};


export type MutationCreatePinRuleArgs = {
  personId: Scalars['UUID']['input'];
  scopeTotal?: InputMaybe<Scalars['Int']['input']>;
  targetPosition: Scalars['Int']['input'];
};


export type MutationDeletePersonArgs = {
  id: Scalars['UUID']['input'];
};


export type MutationDeletePinRuleArgs = {
  id: Scalars['UUID']['input'];
};


export type MutationUpdatePersonArgs = {
  birthdate?: InputMaybe<Scalars['String']['input']>;
  id: Scalars['UUID']['input'];
  location?: InputMaybe<Scalars['String']['input']>;
  name?: InputMaybe<Scalars['String']['input']>;
  position?: InputMaybe<Scalars['String']['input']>;
};


export type MutationUpdatePinRuleArgs = {
  enabled?: InputMaybe<Scalars['Boolean']['input']>;
  id: Scalars['UUID']['input'];
  scopeTotal?: InputMaybe<Scalars['Int']['input']>;
  targetPosition?: InputMaybe<Scalars['Int']['input']>;
};

export type PeopleConnectionPayload = {
  __typename?: 'PeopleConnectionPayload';
  nodes?: Maybe<Array<Maybe<PersonPayload>>>;
  pageInfo?: Maybe<PeoplePageInfoPayload>;
  totalCount?: Maybe<Scalars['Long']['output']>;
};

export type PeoplePageInfoPayload = {
  __typename?: 'PeoplePageInfoPayload';
  endCursor?: Maybe<Scalars['String']['output']>;
  hasNextPage: Scalars['Boolean']['output'];
};

export enum PeopleSortField {
  Birthdate = 'BIRTHDATE',
  CreatedAt = 'CREATED_AT',
  Location = 'LOCATION',
  Name = 'NAME',
  Position = 'POSITION'
}

export type PersonPayload = {
  __typename?: 'PersonPayload';
  age?: Maybe<Scalars['Int']['output']>;
  birthdate?: Maybe<Scalars['String']['output']>;
  createdAt?: Maybe<Scalars['String']['output']>;
  id?: Maybe<Scalars['UUID']['output']>;
  location?: Maybe<Scalars['String']['output']>;
  name?: Maybe<Scalars['String']['output']>;
  position?: Maybe<Scalars['String']['output']>;
  updatedAt?: Maybe<Scalars['String']['output']>;
};

export type PinRulePayload = {
  __typename?: 'PinRulePayload';
  createdAt?: Maybe<Scalars['String']['output']>;
  effectiveIndex?: Maybe<Scalars['Int']['output']>;
  enabled: Scalars['Boolean']['output'];
  id?: Maybe<Scalars['UUID']['output']>;
  personId?: Maybe<Scalars['UUID']['output']>;
  personName?: Maybe<Scalars['String']['output']>;
  state: PinRuleState;
  targetIndex: Scalars['Int']['output'];
  targetPosition: Scalars['Int']['output'];
  updatedAt?: Maybe<Scalars['String']['output']>;
};

export enum PinRuleState {
  Active = 'ACTIVE',
  Clamped = 'CLAMPED',
  Inactive = 'INACTIVE',
  NoMatch = 'NO_MATCH'
}

export type Query = {
  __typename?: 'Query';
  peopleList?: Maybe<PeopleConnectionPayload>;
  person?: Maybe<PersonPayload>;
  pinRules?: Maybe<Array<Maybe<PinRulePayload>>>;
};


export type QueryPeopleListArgs = {
  after?: InputMaybe<Scalars['String']['input']>;
  first?: InputMaybe<Scalars['Int']['input']>;
  search?: InputMaybe<Scalars['String']['input']>;
  sortBy?: InputMaybe<PeopleSortField>;
  sortDirection?: InputMaybe<SortDirection>;
};


export type QueryPersonArgs = {
  id?: InputMaybe<Scalars['UUID']['input']>;
};


export type QueryPinRulesArgs = {
  scopeTotal?: InputMaybe<Scalars['Int']['input']>;
};

export enum SortDirection {
  Asc = 'ASC',
  Desc = 'DESC'
}

export type PersonFieldsFragment = { __typename?: 'PersonPayload', id?: any | null, name?: string | null, position?: string | null, location?: string | null, age?: number | null, birthdate?: string | null, createdAt?: string | null, updatedAt?: string | null };

export type PeopleListQueryVariables = Exact<{
  first?: InputMaybe<Scalars['Int']['input']>;
  after?: InputMaybe<Scalars['String']['input']>;
  search?: InputMaybe<Scalars['String']['input']>;
  sortBy?: InputMaybe<PeopleSortField>;
  sortDirection?: InputMaybe<SortDirection>;
}>;


export type PeopleListQuery = { __typename?: 'Query', peopleList?: { __typename?: 'PeopleConnectionPayload', totalCount?: any | null, nodes?: Array<{ __typename?: 'PersonPayload', id?: any | null, name?: string | null, position?: string | null, location?: string | null, age?: number | null, birthdate?: string | null, createdAt?: string | null, updatedAt?: string | null } | null> | null, pageInfo?: { __typename?: 'PeoplePageInfoPayload', endCursor?: string | null, hasNextPage: boolean } | null } | null };

export type PersonDetailQueryVariables = Exact<{
  id?: InputMaybe<Scalars['UUID']['input']>;
}>;


export type PersonDetailQuery = { __typename?: 'Query', person?: { __typename?: 'PersonPayload', id?: any | null, name?: string | null, position?: string | null, location?: string | null, age?: number | null, birthdate?: string | null, createdAt?: string | null, updatedAt?: string | null } | null };

export type DeletePersonMutationVariables = Exact<{
  id: Scalars['UUID']['input'];
}>;


export type DeletePersonMutation = { __typename?: 'Mutation', deletePerson?: boolean | null };

export type CreatePersonMutationVariables = Exact<{
  name: Scalars['String']['input'];
  position: Scalars['String']['input'];
  location: Scalars['String']['input'];
  birthdate: Scalars['String']['input'];
}>;


export type CreatePersonMutation = { __typename?: 'Mutation', createPerson?: { __typename?: 'PersonPayload', id?: any | null, name?: string | null, position?: string | null, location?: string | null, age?: number | null, birthdate?: string | null, createdAt?: string | null, updatedAt?: string | null } | null };

export type UpdatePersonMutationVariables = Exact<{
  id: Scalars['UUID']['input'];
  name?: InputMaybe<Scalars['String']['input']>;
  position?: InputMaybe<Scalars['String']['input']>;
  location?: InputMaybe<Scalars['String']['input']>;
  birthdate?: InputMaybe<Scalars['String']['input']>;
}>;


export type UpdatePersonMutation = { __typename?: 'Mutation', updatePerson?: { __typename?: 'PersonPayload', id?: any | null, name?: string | null, position?: string | null, location?: string | null, age?: number | null, birthdate?: string | null, createdAt?: string | null, updatedAt?: string | null } | null };

export type PinRuleFieldsFragment = { __typename?: 'PinRulePayload', id?: any | null, personId?: any | null, personName?: string | null, targetPosition: number, targetIndex: number, effectiveIndex?: number | null, state: PinRuleState, enabled: boolean, createdAt?: string | null, updatedAt?: string | null };

export type PinRulesQueryVariables = Exact<{
  scopeTotal?: InputMaybe<Scalars['Int']['input']>;
}>;


export type PinRulesQuery = { __typename?: 'Query', pinRules?: Array<{ __typename?: 'PinRulePayload', id?: any | null, personId?: any | null, personName?: string | null, targetPosition: number, targetIndex: number, effectiveIndex?: number | null, state: PinRuleState, enabled: boolean, createdAt?: string | null, updatedAt?: string | null } | null> | null };

export type CreatePinRuleMutationVariables = Exact<{
  personId: Scalars['UUID']['input'];
  targetPosition: Scalars['Int']['input'];
  scopeTotal?: InputMaybe<Scalars['Int']['input']>;
}>;


export type CreatePinRuleMutation = { __typename?: 'Mutation', createPinRule?: { __typename?: 'PinRulePayload', id?: any | null, personId?: any | null, personName?: string | null, targetPosition: number, targetIndex: number, effectiveIndex?: number | null, state: PinRuleState, enabled: boolean, createdAt?: string | null, updatedAt?: string | null } | null };

export type UpdatePinRuleMutationVariables = Exact<{
  id: Scalars['UUID']['input'];
  targetPosition?: InputMaybe<Scalars['Int']['input']>;
  enabled?: InputMaybe<Scalars['Boolean']['input']>;
  scopeTotal?: InputMaybe<Scalars['Int']['input']>;
}>;


export type UpdatePinRuleMutation = { __typename?: 'Mutation', updatePinRule?: { __typename?: 'PinRulePayload', id?: any | null, personId?: any | null, personName?: string | null, targetPosition: number, targetIndex: number, effectiveIndex?: number | null, state: PinRuleState, enabled: boolean, createdAt?: string | null, updatedAt?: string | null } | null };

export type DeletePinRuleMutationVariables = Exact<{
  id: Scalars['UUID']['input'];
}>;


export type DeletePinRuleMutation = { __typename?: 'Mutation', deletePinRule?: boolean | null };

export const PersonFieldsFragmentDoc = gql`
    fragment PersonFields on PersonPayload {
  id
  name
  position
  location
  age
  birthdate
  createdAt
  updatedAt
}
    `;
export const PinRuleFieldsFragmentDoc = gql`
    fragment PinRuleFields on PinRulePayload {
  id
  personId
  personName
  targetPosition
  targetIndex
  effectiveIndex
  state
  enabled
  createdAt
  updatedAt
}
    `;
export const PeopleListDocument = gql`
    query PeopleList($first: Int, $after: String, $search: String, $sortBy: PeopleSortField, $sortDirection: SortDirection) {
  peopleList(
    first: $first
    after: $after
    search: $search
    sortBy: $sortBy
    sortDirection: $sortDirection
  ) {
    nodes {
      ...PersonFields
    }
    pageInfo {
      endCursor
      hasNextPage
    }
    totalCount
  }
}
    ${PersonFieldsFragmentDoc}`;
export function usePeopleListQuery(variables: PeopleListQueryVariables | VueCompositionApi.Ref<PeopleListQueryVariables> | ReactiveFunction<PeopleListQueryVariables> = {}, options: VueApolloComposable.UseQueryOptions<PeopleListQuery, PeopleListQueryVariables> | VueCompositionApi.Ref<VueApolloComposable.UseQueryOptions<PeopleListQuery, PeopleListQueryVariables>> | ReactiveFunction<VueApolloComposable.UseQueryOptions<PeopleListQuery, PeopleListQueryVariables>> = {}) {
  return VueApolloComposable.useQuery<PeopleListQuery, PeopleListQueryVariables>(PeopleListDocument, variables, options);
}
export function usePeopleListLazyQuery(variables: PeopleListQueryVariables | VueCompositionApi.Ref<PeopleListQueryVariables> | ReactiveFunction<PeopleListQueryVariables> = {}, options: VueApolloComposable.UseQueryOptions<PeopleListQuery, PeopleListQueryVariables> | VueCompositionApi.Ref<VueApolloComposable.UseQueryOptions<PeopleListQuery, PeopleListQueryVariables>> | ReactiveFunction<VueApolloComposable.UseQueryOptions<PeopleListQuery, PeopleListQueryVariables>> = {}) {
  return VueApolloComposable.useLazyQuery<PeopleListQuery, PeopleListQueryVariables>(PeopleListDocument, variables, options);
}
export type PeopleListQueryCompositionFunctionResult = VueApolloComposable.UseQueryReturn<PeopleListQuery, PeopleListQueryVariables>;
export const PersonDetailDocument = gql`
    query PersonDetail($id: UUID) {
  person(id: $id) {
    ...PersonFields
  }
}
    ${PersonFieldsFragmentDoc}`;
export function usePersonDetailQuery(variables: PersonDetailQueryVariables | VueCompositionApi.Ref<PersonDetailQueryVariables> | ReactiveFunction<PersonDetailQueryVariables> = {}, options: VueApolloComposable.UseQueryOptions<PersonDetailQuery, PersonDetailQueryVariables> | VueCompositionApi.Ref<VueApolloComposable.UseQueryOptions<PersonDetailQuery, PersonDetailQueryVariables>> | ReactiveFunction<VueApolloComposable.UseQueryOptions<PersonDetailQuery, PersonDetailQueryVariables>> = {}) {
  return VueApolloComposable.useQuery<PersonDetailQuery, PersonDetailQueryVariables>(PersonDetailDocument, variables, options);
}
export function usePersonDetailLazyQuery(variables: PersonDetailQueryVariables | VueCompositionApi.Ref<PersonDetailQueryVariables> | ReactiveFunction<PersonDetailQueryVariables> = {}, options: VueApolloComposable.UseQueryOptions<PersonDetailQuery, PersonDetailQueryVariables> | VueCompositionApi.Ref<VueApolloComposable.UseQueryOptions<PersonDetailQuery, PersonDetailQueryVariables>> | ReactiveFunction<VueApolloComposable.UseQueryOptions<PersonDetailQuery, PersonDetailQueryVariables>> = {}) {
  return VueApolloComposable.useLazyQuery<PersonDetailQuery, PersonDetailQueryVariables>(PersonDetailDocument, variables, options);
}
export type PersonDetailQueryCompositionFunctionResult = VueApolloComposable.UseQueryReturn<PersonDetailQuery, PersonDetailQueryVariables>;
export const DeletePersonDocument = gql`
    mutation DeletePerson($id: UUID!) {
  deletePerson(id: $id)
}
    `;
export function useDeletePersonMutation(options: VueApolloComposable.UseMutationOptions<DeletePersonMutation, DeletePersonMutationVariables> | ReactiveFunction<VueApolloComposable.UseMutationOptions<DeletePersonMutation, DeletePersonMutationVariables>> = {}) {
  return VueApolloComposable.useMutation<DeletePersonMutation, DeletePersonMutationVariables>(DeletePersonDocument, options);
}
export type DeletePersonMutationCompositionFunctionResult = VueApolloComposable.UseMutationReturn<DeletePersonMutation, DeletePersonMutationVariables>;
export const CreatePersonDocument = gql`
    mutation CreatePerson($name: String!, $position: String!, $location: String!, $birthdate: String!) {
  createPerson(
    name: $name
    position: $position
    location: $location
    birthdate: $birthdate
  ) {
    ...PersonFields
  }
}
    ${PersonFieldsFragmentDoc}`;
export function useCreatePersonMutation(options: VueApolloComposable.UseMutationOptions<CreatePersonMutation, CreatePersonMutationVariables> | ReactiveFunction<VueApolloComposable.UseMutationOptions<CreatePersonMutation, CreatePersonMutationVariables>> = {}) {
  return VueApolloComposable.useMutation<CreatePersonMutation, CreatePersonMutationVariables>(CreatePersonDocument, options);
}
export type CreatePersonMutationCompositionFunctionResult = VueApolloComposable.UseMutationReturn<CreatePersonMutation, CreatePersonMutationVariables>;
export const UpdatePersonDocument = gql`
    mutation UpdatePerson($id: UUID!, $name: String, $position: String, $location: String, $birthdate: String) {
  updatePerson(
    id: $id
    name: $name
    position: $position
    location: $location
    birthdate: $birthdate
  ) {
    ...PersonFields
  }
}
    ${PersonFieldsFragmentDoc}`;
export function useUpdatePersonMutation(options: VueApolloComposable.UseMutationOptions<UpdatePersonMutation, UpdatePersonMutationVariables> | ReactiveFunction<VueApolloComposable.UseMutationOptions<UpdatePersonMutation, UpdatePersonMutationVariables>> = {}) {
  return VueApolloComposable.useMutation<UpdatePersonMutation, UpdatePersonMutationVariables>(UpdatePersonDocument, options);
}
export type UpdatePersonMutationCompositionFunctionResult = VueApolloComposable.UseMutationReturn<UpdatePersonMutation, UpdatePersonMutationVariables>;
export const PinRulesDocument = gql`
    query PinRules($scopeTotal: Int) {
  pinRules(scopeTotal: $scopeTotal) {
    ...PinRuleFields
  }
}
    ${PinRuleFieldsFragmentDoc}`;
export function usePinRulesQuery(variables: PinRulesQueryVariables | VueCompositionApi.Ref<PinRulesQueryVariables> | ReactiveFunction<PinRulesQueryVariables> = {}, options: VueApolloComposable.UseQueryOptions<PinRulesQuery, PinRulesQueryVariables> | VueCompositionApi.Ref<VueApolloComposable.UseQueryOptions<PinRulesQuery, PinRulesQueryVariables>> | ReactiveFunction<VueApolloComposable.UseQueryOptions<PinRulesQuery, PinRulesQueryVariables>> = {}) {
  return VueApolloComposable.useQuery<PinRulesQuery, PinRulesQueryVariables>(PinRulesDocument, variables, options);
}
export function usePinRulesLazyQuery(variables: PinRulesQueryVariables | VueCompositionApi.Ref<PinRulesQueryVariables> | ReactiveFunction<PinRulesQueryVariables> = {}, options: VueApolloComposable.UseQueryOptions<PinRulesQuery, PinRulesQueryVariables> | VueCompositionApi.Ref<VueApolloComposable.UseQueryOptions<PinRulesQuery, PinRulesQueryVariables>> | ReactiveFunction<VueApolloComposable.UseQueryOptions<PinRulesQuery, PinRulesQueryVariables>> = {}) {
  return VueApolloComposable.useLazyQuery<PinRulesQuery, PinRulesQueryVariables>(PinRulesDocument, variables, options);
}
export type PinRulesQueryCompositionFunctionResult = VueApolloComposable.UseQueryReturn<PinRulesQuery, PinRulesQueryVariables>;
export const CreatePinRuleDocument = gql`
    mutation CreatePinRule($personId: UUID!, $targetPosition: Int!, $scopeTotal: Int) {
  createPinRule(
    personId: $personId
    targetPosition: $targetPosition
    scopeTotal: $scopeTotal
  ) {
    ...PinRuleFields
  }
}
    ${PinRuleFieldsFragmentDoc}`;
export function useCreatePinRuleMutation(options: VueApolloComposable.UseMutationOptions<CreatePinRuleMutation, CreatePinRuleMutationVariables> | ReactiveFunction<VueApolloComposable.UseMutationOptions<CreatePinRuleMutation, CreatePinRuleMutationVariables>> = {}) {
  return VueApolloComposable.useMutation<CreatePinRuleMutation, CreatePinRuleMutationVariables>(CreatePinRuleDocument, options);
}
export type CreatePinRuleMutationCompositionFunctionResult = VueApolloComposable.UseMutationReturn<CreatePinRuleMutation, CreatePinRuleMutationVariables>;
export const UpdatePinRuleDocument = gql`
    mutation UpdatePinRule($id: UUID!, $targetPosition: Int, $enabled: Boolean, $scopeTotal: Int) {
  updatePinRule(
    id: $id
    targetPosition: $targetPosition
    enabled: $enabled
    scopeTotal: $scopeTotal
  ) {
    ...PinRuleFields
  }
}
    ${PinRuleFieldsFragmentDoc}`;
export function useUpdatePinRuleMutation(options: VueApolloComposable.UseMutationOptions<UpdatePinRuleMutation, UpdatePinRuleMutationVariables> | ReactiveFunction<VueApolloComposable.UseMutationOptions<UpdatePinRuleMutation, UpdatePinRuleMutationVariables>> = {}) {
  return VueApolloComposable.useMutation<UpdatePinRuleMutation, UpdatePinRuleMutationVariables>(UpdatePinRuleDocument, options);
}
export type UpdatePinRuleMutationCompositionFunctionResult = VueApolloComposable.UseMutationReturn<UpdatePinRuleMutation, UpdatePinRuleMutationVariables>;
export const DeletePinRuleDocument = gql`
    mutation DeletePinRule($id: UUID!) {
  deletePinRule(id: $id)
}
    `;
export function useDeletePinRuleMutation(options: VueApolloComposable.UseMutationOptions<DeletePinRuleMutation, DeletePinRuleMutationVariables> | ReactiveFunction<VueApolloComposable.UseMutationOptions<DeletePinRuleMutation, DeletePinRuleMutationVariables>> = {}) {
  return VueApolloComposable.useMutation<DeletePinRuleMutation, DeletePinRuleMutationVariables>(DeletePinRuleDocument, options);
}
export type DeletePinRuleMutationCompositionFunctionResult = VueApolloComposable.UseMutationReturn<DeletePinRuleMutation, DeletePinRuleMutationVariables>;