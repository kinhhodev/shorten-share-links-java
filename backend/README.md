# shorten-share-links

URL shortener backend (Spring Boot). Java sources and the Maven build live under **`backend/`**. **`docker-compose.yml`** at the **repository root** runs PostgreSQL, Redis, this API, and the **frontend** (nginx).

## Prerequisites

- **Java 21** and **Maven 3.9+** (for local builds)
- **Docker** and **Docker Compose** (for containerized deploy)

## Build (local)

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

From the **repository root** (not `backend/`), build and run the full stack:

```bash
docker compose up --build
```

See the root **`README.md`** for ports (UI on port 80, API on 8080) and **`PUBLIC_BASE_URL`**. Example:

```bash
JWT_SECRET="$(openssl rand -base64 48)" PUBLIC_BASE_URL=http://localhost docker compose up --build
```

Stop and remove containers:

```bash
docker compose down
```

OAuth2 client settings now live in `backend/src/main/resources/application.yml`; provide `OAUTH2_*` env vars to enable Google/GitHub login.
