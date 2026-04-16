# Collaboration Service Deployment Guide

This service can run locally with file-based H2 and switch to PostgreSQL in a deployed environment without code changes.

## Local Development

The backend defaults to:

- `server.port=8082`
- H2 file database at `./data/collaboration-db`
- CORS allowed for `http://localhost:5173`

The frontend defaults to:

- `VITE_API_BASE_URL=http://localhost:8082`

## Deployment Strategy

Recommended starter setup:

- Frontend: Vercel
- Backend: Render or Railway
- Database: PostgreSQL (for example Neon or Render Postgres)

If Render does not show a native `Java` runtime in the Web Service screen, choose `Docker`.
This repository includes a `Dockerfile`, so Render can build and run the Spring Boot app directly.

## Backend Environment Variables

Set these in the deployed backend service:

- `PORT`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `CORS_ALLOWED_ORIGINS`
- `H2_CONSOLE_ENABLED=false`

Example values:

```text
PORT=8082
DATABASE_URL=jdbc:postgresql://<host>:5432/<database>
DATABASE_USERNAME=<username>
DATABASE_PASSWORD=<password>
CORS_ALLOWED_ORIGINS=https://<your-frontend-domain>
H2_CONSOLE_ENABLED=false
```

Notes:

- `DATABASE_URL` should be a JDBC URL.
- Keep `spring.sql.init.mode=always` so `schema.sql` initializes the database.

## Frontend Environment Variables

Set this in the frontend deployment:

```text
VITE_API_BASE_URL=https://<your-backend-domain>
```

## Deploy Order

1. Provision PostgreSQL
2. Deploy backend with database and CORS environment variables
3. Deploy frontend with `VITE_API_BASE_URL` pointing to the backend
4. Create or approve an administrator account, then log in with that issued credential
5. Create or approve real user accounts

## Same-WiFi Temporary Sharing

Before full deployment, a same-network test can use the laptop's local IP:

- Frontend: `http://<local-ip>:5173`
- Backend: `http://<local-ip>:8082`

In that case, set:

```text
CORS_ALLOWED_ORIGINS=http://<local-ip>:5173
VITE_API_BASE_URL=http://<local-ip>:8082
```
