# shorten-share-links

URL shortener: **Spring Boot** API under **`backend/`**, **React (Vite)** UI under **`frontend/`**. **`docker-compose.yml`** at the repository root runs PostgreSQL, Redis, the API, and the **nginx** frontend image.

## Prerequisites

- **Java 21** and **Maven 3.9+** (for local builds)
- **Docker** and **Docker Compose** (for containerized deploy)
- **Node.js 20+** and **npm** (for the frontend)

## Frontend (React + Vite)

From the repository root:

```bash
cd frontend
npm install
npm run dev
```

The dev server defaults to **http://localhost:5173** and proxies **`/api`** to the backend on port **8080** (see `frontend/vite.config.ts`). Start the API separately (or use Docker Compose for the stack).

Production build:

```bash
cd frontend
npm run build
```

Serve the static output in `frontend/dist/` behind any static host; set **`VITE_API_BASE_URL`** to your API origin if it is not same-origin (see `frontend/.env.example`).

### Frontend (Docker)

The **`frontend/Dockerfile`** builds the Vite app and serves it with **nginx**. **`frontend/docker/nginx.conf`** proxies **`/api`** and **`/l`** to the Spring Boot service so the browser can use **relative** API URLs (no CORS issues for normal usage).

From the **repository root**:

```bash
docker compose build web
docker compose up -d web
```

Usually you run the full stack:

```bash
docker compose up --build
```

- **UI:** [http://localhost](http://localhost) (port **80**)
- **API (direct):** [http://localhost:8080](http://localhost:8080)

Set **`PUBLIC_BASE_URL`** so short links in JSON match how users reach the app (default **`http://localhost`** when nginx listens on port 80). If you map the UI to another host port (e.g. **`8081:80`**), set e.g. `PUBLIC_BASE_URL=http://localhost:8081`.

To build the UI with a fixed API origin instead of nginx proxying (not typical for this compose setup), pass a build arg:

```bash
docker compose build --build-arg VITE_API_BASE_URL=https://api.example.com web
```

## Build (backend, local)

From the repository root:

```bash
cd backend
mvn clean package
```

Run tests:

```bash
cd backend
mvn test
```

Run the packaged JAR (you still need PostgreSQL and Redis reachable; see `backend/src/main/resources/application.yml` for defaults):

```bash
cd backend
java -jar target/shorten-share-links-*.jar
```

## Deploy (Docker)

From the **repository root**, build and start Postgres, Redis, the API, and the frontend:

```bash
docker compose up --build
```

- **Frontend (nginx):** **http://localhost** (port 80)
- **API:** **http://localhost:8080**

Override secrets and public URL for short links as needed:

```bash
JWT_SECRET="$(openssl rand -base64 48)" PUBLIC_BASE_URL=http://localhost docker compose up --build
```

Stop and remove containers:

```bash
docker compose down
```

OAuth2 client settings now live in `backend/src/main/resources/application.yml`; provide `OAUTH2_*` env vars to enable Google/GitHub login.

## Slug Allocation Rules

- Public redirect URL format stays **`/r/{topic}/{slug}`**.
- Slug uniqueness is global per topic (database unique index: **`(topic, slug)`**), regardless of `created_by_id`.
- When a requested slug is already taken in the same topic, the system auto-allocates the next available suffix:
  - `name` -> `name-1` -> `name-2` -> ...
- This behavior is applied consistently for both:
  - authenticated user links (`created_by_id` is set)
  - guest links (`created_by_id` is null)
