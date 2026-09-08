# HopeBridge India

HopeBridge is a safety-first support workflow for organising community cases and discovering public schemes. It is **not** a government portal: it does not issue official documents, make eligibility decisions, or replace an official application. Recommendations link back to official sources and case approval must be handled independently.

## Project layout

- `frontend/` — React 18, Vite, React Router, Axios, responsive CSS
- `backend/` — Spring Boot 3, Web, Data JPA, Security, JWT, BCrypt
- `database/schema.sql` — MySQL schema and starter scheme data

## Run locally

### MySQL

Create a database/user (or use an existing one), then run `database/schema.sql`. The application can also create/update tables with Hibernate:

```sql
CREATE USER 'hopebridge'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON hopebridge.* TO 'hopebridge'@'localhost';
```

### API

Set environment variables as needed (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`) and run. MySQL must be running, and `DB_PASSWORD` must match the password configured for the selected MySQL user; the default `root` account is not assumed to have an empty password.

```bash
cd backend
```

PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/hopebridge?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-mysql-password"
$env:JWT_SECRET="replace-with-a-long-random-secret"
mvn spring-boot:run
```

Alternatively, create the least-privileged application user from a MySQL session:

```sql
CREATE DATABASE IF NOT EXISTS hopebridge;
CREATE USER 'hopebridge'@'localhost' IDENTIFIED BY 'change-this-password';
GRANT ALL PRIVILEGES ON hopebridge.* TO 'hopebridge'@'localhost';
FLUSH PRIVILEGES;
```

The API is available at `http://localhost:8081/api` by default. Port 8080 is commonly occupied by Jenkins or another local service; set `SERVER_PORT=8080` if it is available. The development admin account is `jabilimutukuru@gmail.com` / `jabilimutukuru`. Change this before any real deployment.

### Web

```bash
cd frontend
npm install
copy .env.example .env       # Windows; use cp on macOS/Linux
npm run dev
```

Open `http://localhost:5173`. The API client reads `VITE_API_URL` and defaults to `http://localhost:8081/api`.

## Workflow

Users register/login, create a draft case, submit it, and upload JPG/PNG evidence (10 MB maximum). Admins can move cases through review states and independently verify evidence. Points are recorded in a wallet with an immutable reward history. Admin APIs deliberately reject an `APPROVED` decision in this starter slice to prevent self-approval; connect a separate reviewer/approval role for production governance.

## Production checklist

Use a strong random `JWT_SECRET`, managed secrets, HTTPS, object storage for evidence, malware scanning, rate limiting, audit logs, a real independent reviewer role, backups, and a privacy/retention policy. Validate all scheme links and eligibility directly with the relevant official department.
