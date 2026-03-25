create extension if not exists pgcrypto;
create extension if not exists pg_trgm;

create table people (
    id uuid primary key default gen_random_uuid(),
    name varchar(255) not null,
    position_title varchar(120) not null,
    location varchar(120) not null,
    birth_date date not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_people_name on people (name);
create index idx_people_position_title on people (position_title);
create index idx_people_location on people (location);
create index idx_people_birth_date on people (birth_date);
create index idx_people_created_at_id on people (created_at, id);

create table pin_rules (
    id uuid primary key default gen_random_uuid(),
    person_id uuid not null references people (id) on delete cascade,
    target_position integer not null check (target_position > 0),
    enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index uq_pin_rules_person_id on pin_rules (person_id);
create index idx_pin_rules_enabled_target on pin_rules (enabled, target_position);

create index idx_people_name_id on people (name, id);
create index idx_people_position_title_id on people (position_title, id);
create index idx_people_location_id on people (location, id);
create index idx_people_birth_date_id on people (birth_date, id);
create index idx_people_dedup
    on people (lower(name), lower(position_title), lower(location), birth_date);

create index idx_people_name_trgm on people using gin (name gin_trgm_ops);
create index idx_people_position_trgm on people using gin (position_title gin_trgm_ops);
create index idx_people_location_trgm on people using gin (location gin_trgm_ops);

create or replace function set_updated_at()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create trigger trg_people_updated_at
    before update on people
    for each row execute function set_updated_at();

create trigger trg_pin_rules_updated_at
    before update on pin_rules
    for each row execute function set_updated_at();
