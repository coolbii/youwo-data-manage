create table users (
    id uuid primary key default gen_random_uuid(),
    email varchar(255) not null,
    password_hash varchar(255) not null,
    refresh_token_hash varchar(255),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index uq_users_email on users (lower(email));

create trigger trg_users_updated_at
    before update on users
    for each row execute function set_updated_at();

insert into users (email, password_hash)
values (
    'demo@youwo.local',
    '$2y$10$3kLtkF4KfRYToD7/iPMQnu87SF1MC832lG4i0Ov4pTF4FmdhKgt26'
)
on conflict (lower(email)) do nothing;
