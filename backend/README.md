# shorten-share-links

URL shortener backend (Spring Boot). Java sources and the Maven build live under **`backend/`**. Docker Compose at the repository root runs PostgreSQL, Redis, and the API.

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
