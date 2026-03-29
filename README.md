# youwo-homework

Nx monorepo with:

- `client`: Vue 3 + Vite
- `api-java`: Spring Boot + Spring GraphQL + Spring Data JPA + Flyway + PostgreSQL

## Project structure

- `api-java/`: Java backend (Spring Boot)
- `client/`: Vue 3 frontend
- `deploy/`: Nginx and EC2 compose deployment files

## 1) Install and env

```bash
npm install
cp .env.example .env
npm run db:up
```

Set datasource in `.env`:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/youwo_homework
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SERVER_PORT=3000
```

## 2) Local development

Run backend:

```bash
npm run dev:api
```

`dev:api` loads environment variables from root `.env` automatically before starting Spring Boot.

One-command backend start (auto start Docker Postgres, then run API):

```bash
npm run dev:api:docker
```

`dev:api:docker` defaults Docker Postgres host port to `5433` and injects Spring datasource env vars automatically.

Run frontend:

```bash
npm run dev:client
```

Useful DB helper scripts:

```bash
npm run db:logs
npm run db:down
```

Batch import `people` from CSV:

```bash
# 1) ensure DB + schema are ready
npm run db:up
npm run db:migrate

# 2) copy and edit template
cp db/people_import_template.csv /tmp/people_import.csv

# 3) run import
npm run db:import -- --table=people --csv=/tmp/people_import.csv
```

Import files:

- `db/import_people_batch.sql` (staging + validation + dedupe + insert)
- `db/people_import_template.csv` (header and example rows)
- `scripts/db-import.sh` (Docker Postgres wrapper)

CSV format must be exactly:

```csv
name,position_title,location,birth_date
```

`birth_date` must use `YYYY-MM-DD`.

Requirements:

- PostgreSQL 16+ (`db/import_people_batch.sql` uses `pg_input_is_valid` for safe date validation).

Corner cases handled by importer:

- Trims leading/trailing whitespace.
- Removes UTF-8 BOM from first column.
- Rejects blank `name`, `position_title`, `location`, `birth_date`.
- Rejects invalid/future `birth_date`.
- Rejects over-length strings (`name` > 255, `position_title`/`location` > 120).
- Skips duplicates in file and existing DB rows using
  case-insensitive `(name, position_title, location, birth_date)`.

Large data note:

- Import currently runs in a single transaction. For very large files (for example millions of rows), split into chunks and run chunk-by-chunk to control WAL/temp usage.
- Example: `split -l 100000 your.csv /tmp/people_chunk_` (remember each chunk needs the CSV header).

Local URLs:

- Client: `http://localhost:4200`
- API health: `http://localhost:3000/api/health`
- GraphQL: `http://localhost:3000/api/graphql`
- GraphQL Playground (Apollo Sandbox): `http://localhost:3000/api/graphql/playground`

## Auth/session notes

- Browser does not store `accessToken`/`refreshToken` anymore.
- Backend sets:
  - session cookie (HttpOnly, SameSite=Strict)
  - CSRF cookie (SameSite=Strict)
- Frontend sends `X-CSRF-Token` for GraphQL requests using the CSRF cookie value.

Env vars:

- `APP_AUTH_SESSION_COOKIE_NAME` (default: `youwo.sid`)
- `APP_AUTH_CSRF_COOKIE_NAME` (default: `youwo.csrf`)
- `APP_AUTH_COOKIE_SECURE` (default: `false` for local HTTP dev)
- `VITE_AUTH_CSRF_COOKIE_NAME` (default: `youwo.csrf`)

For HTTPS production, set secure host cookies, for example:

```env
APP_AUTH_SESSION_COOKIE_NAME=__Host-sid
APP_AUTH_CSRF_COOKIE_NAME=__Host-csrf
APP_AUTH_COOKIE_SECURE=true
VITE_AUTH_CSRF_COOKIE_NAME=__Host-csrf
```

## 3) Database migration (Flyway)

Migrations are stored in `api-java/src/main/resources/db/migration`.

- Flyway auto-run is disabled by default in this repo (`spring.flyway.enabled=false`).
- Manual migrate command:

```bash
npm run db:migrate
```

`db:migrate` also loads root `.env` automatically before running Flyway.

JPA/Flyway workflow in this repo:

- `spring.jpa.hibernate.ddl-auto=validate` (JPA validates schema only, does not mutate schema)
- Flyway versioned SQL is the source of truth for schema changes
- Optional DDL draft from JPA metadata (for authoring help, not direct apply):

```bash
npm run db:ddl-draft
```

Generated draft path:

- `api-java/target/generated-ddl/jpa-create.sql`

## 4) Nx targets for Java API

The Java app is configured in `api-java/project.json` using Nx `run-commands`:

- `nx run api-java:serve`
- `nx run api-java:build`
- `nx run api-java:test`
- `nx run api-java:db-migrate`

## 5) Build

```bash
npm run build
```

## 6) GraphQL code-first schema and drift guard

GraphQL runtime is generated from resolver code (code-first), not hand-authored SDL.

Generate committed schema snapshot:

```bash
npm run graphql:codegen
```

Check schema drift (fails when generated schema differs from committed snapshot):

```bash
npm run graphql:check
```

Snapshot path:

- `api-java/src/main/resources/graphql/schema.graphqls`

## 7) Docker deployment modes

Standalone mode (single project owns `80/443`):

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Shared-EC2 mode (recommended when multiple projects share one EC2):

- Use `deploy/docker-compose.ec2.yml`.
- This file only runs `client` + `api` and binds localhost-only ports:
  - `127.0.0.1:${WEB_LOCAL_PORT:-18180}:3000`
  - `127.0.0.1:${API_LOCAL_PORT:-18181}:3000`
- No container in this project binds host `80/443`.
- `deploy/client.nginx.conf` already proxies `/api/*` to `api:3000`, so edge-nginx only needs to proxy `/` to the web local port.

Required env for API service:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME` (default `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (default `postgres`)

## 8) Shared EC2 + edge-nginx setup (one-time)

Example edge-nginx vhost template:

- `deploy/edge-nginx.youwo.bin-hq.com.conf.example`

On EC2 edge-nginx host:

1. copy config to `/opt/edge-nginx/conf.d/youwo.bin-hq.com.conf`
2. place Cloudflare origin cert/key under `/opt/edge-nginx/certs/`
3. validate and reload edge-nginx

```bash
sudo docker exec edge-nginx nginx -t
sudo docker restart edge-nginx
```

## 9) GitHub Actions CI/CD

Workflows:

- `.github/workflows/ci.yml`
- `.github/workflows/cd-ec2.yml`

`ci.yml` runs:

- `npm ci`
- `npm run lint`
- `npm run test:api-java`
- `npm run graphql:check`
- `npm run build`

`cd-ec2.yml` does:

1. build/push Docker images to GHCR (`youwo-api`, `youwo-client`)
2. upload `deploy/docker-compose.ec2.yml` and Flyway SQL files to EC2 deploy path
3. SSH into EC2 and run Flyway migration first
4. then run:
   - `docker compose pull`
   - `docker compose up -d --remove-orphans`
5. health check on localhost ports (`WEB_LOCAL_PORT`, `API_LOCAL_PORT`)

Important safety behavior for shared EC2:

- deploy only uses one compose project name (`EC2_DOCKER_PROJECT_NAME`)
- does not run `docker compose down`
- refuses deployment if local ports are set to `80` or `443`

Required GitHub repository secrets:

- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_PRIVATE_KEY`
- `EC2_DEPLOY_PATH` (example: `/opt/youwo-homework/deploy`)
- `EC2_DOCKER_PROJECT_NAME` (example: `youwo-homework`)
- `EC2_WEB_LOCAL_PORT` (example: `18180`)
- `EC2_API_LOCAL_PORT` (example: `18181`)
- `PROD_SPRING_DATASOURCE_URL`
- `PROD_SPRING_DATASOURCE_USERNAME`
- `PROD_SPRING_DATASOURCE_PASSWORD`
- `GHCR_USERNAME`
- `GHCR_PULL_TOKEN` (PAT with `read:packages`)
