# shorten-share-links

URL shortener: **Spring Boot** API under **`backend/`**, **React (Vite)** UI under **`frontend/`**. Docker Compose at the repository root runs PostgreSQL, Redis, and the API.

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

From the repository root, build the Spring Boot image and start Postgres, Redis, and the app:

```bash
docker compose up --build
```

The API listens on **http://localhost:8080**. Override secrets and URLs as needed, for example:

```bash
JWT_SECRET="$(openssl rand -base64 48)" PUBLIC_BASE_URL=http://localhost:8080 docker compose up --build
```

Stop and remove containers:

```bash
docker compose down
```

To enable optional Google OAuth2, add profile **`oauth2`** and credentials (see `backend/src/main/resources/application-oauth2.yml`).
