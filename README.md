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
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/youwo_homework
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

Local URLs:

- Client: `http://localhost:4200`
- API health: `http://localhost:3000/api/health`
- GraphQL: `http://localhost:3000/api/graphql`

## 3) Database migration (Flyway)

Migrations are stored in `api-java/src/main/resources/db/migration`.

- On app startup, Flyway runs automatically.
- Manual migrate command:

```bash
npm run db:migrate
```

`db:migrate` also loads root `.env` automatically before running Flyway.

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

## 7) Docker and Nginx deployment

Production compose:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

API container uses Spring Boot image build from `Dockerfile.api`.

Required env for API service:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME` (default `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (default `postgres`)

EC2 compose template:

- `deploy/docker-compose.ec2.yml`
- `deploy/nginx.conf`

`deploy/nginx.conf` is set for `youwo.bin-hq.com` by default.
