# Employee Management API — Java 25, Jersey, Oracle, JWT and Azure VM CI/CD

A layered JAX-RS/Jersey WAR starter with Oracle JDBC/HikariCP, HK2 constructor injection, local username/password authentication, signed JWTs, role-based write restrictions, OpenAPI, Postman, CI security scanning, and a sample Azure Linux VM deployment pipeline.

> **Starter security note:** This is a practical learning/reference project, not a certified production identity provider. The built-in auth module is a minimal first-party auth service (BCrypt password hashes + HMAC-signed JWT). For enterprise SSO/MFA/password reset/lockout/audit and token lifecycle, prefer a free/self-hosted identity provider such as Keycloak. Do not expose public registration to a real employee system without abuse controls and approval workflow.

## Architecture

`Client -> Azure VM/Tomcat -> Jersey Resource -> Service -> JDBC Repository -> Oracle`

Cross-cutting: HK2 dependency injection, JWT auth, ADMIN role check for writes, DTO input, centralized exception mapper, request IDs, HikariCP, GitHub Actions.

## Repository map

- `src/main/java`: application code
- `src/main/resources/application*.properties`: local/test/UAT/prod config
- `src/main/resources/db/migration/V1__initial_schema.sql`: schema baseline
- `db/manual`: user/schema and initial-admin instructions
- `openapi/openapi.yaml`: OpenAPI 3.0 specification
- `postman/Employee-Management.postman_collection.json`: Postman collection
- `.github/workflows/ci-cd.yml`: build, tests, OWASP Dependency-Check, CodeQL, Azure VM deploy
- `ops/`: VM bootstrap and deployment script
- `Dockerfile`, `docker-compose.yml`: local container build

## Requirements

- JDK 25
- Maven 3.9+
- Oracle Free / Oracle Database with a PDB reachable by the application
- Git
- For Azure deploy: Ubuntu VM, network rules, SSH key, Tomcat 10.1, GitHub Actions secrets

## 1. Configure local environment

```bash
cp .env.example .env
```

Edit `.env` and set strong local-only values. Do not commit `.env`.

The checked-in `application-local.properties` has convenience defaults for a local-only Oracle. Environment variables override these values. `APP_ENV` must be one of `local`, `test`, `uat`, `prod`.

Create the database user/schema as described in `db/manual/01_create_schema.sql`. Then run `src/main/resources/db/migration/V1__initial_schema.sql` as the application schema owner.

For a local Oracle Free container, you may use `docker compose up -d oracle`, wait for its health check, then run the schema SQL. Compose uses a named volume, so database files persist after container restart.

## 2. Build and test

```bash
mvn -B clean verify
```

This compiles the WAR and runs unit tests. To build the artifact only:

```bash
mvn -B clean package
```

Artifact: `target/employee-management.war`.

## 3. Run/deploy to Tomcat locally

Use Tomcat 10.1+ (Servlet 6 / Jakarta namespace). Copy the WAR to `webapps/ROOT.war` or `webapps/employee-management.war`. If the latter, base URL is `/employee-management/api/v1`; if ROOT, it is `/api/v1`.

Set environment variables in the Tomcat service environment (not an interactive shell alone), including `APP_ENV`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`. `JWT_SECRET` must be at least 32 characters; use a cryptographically random secret in UAT/production.

Example Linux environment file (restrict to root/service account, never commit):
```bash
APP_ENV=uat
DB_URL=jdbc:oracle:thin:@//oracle-host.example:1521/UATPDB
DB_USERNAME=employee_app
DB_PASSWORD=inject-from-secret-manager
JWT_SECRET=inject-at-deploy-time-at-least-32-random-characters
JWT_ISSUER=employee-management
JWT_TTL_MINUTES=30
```

Restart Tomcat after environment changes. Validate:
```bash
curl -i http://localhost:8080/api/v1/health
```

## 4. Authentication / first admin

1. Register a normal user using `POST /api/v1/auth/register`.
2. Log in using `POST /api/v1/auth/login`; the response includes `accessToken`.
3. New registrations are `USER` role and can read employees only.
4. Use the controlled bootstrap SQL in `db/manual/02_create_first_admin.sql` to promote one reviewed account to `ADMIN`.
5. Log in again to obtain a JWT with the ADMIN role claim.
6. Use `Authorization: Bearer <token>` for employee APIs.

Do not implement a public admin registration endpoint. For a real deployment, consider disabling public registration and provision accounts via an admin workflow or Keycloak.

## 5. Optional free identity provider (Keycloak)

`compose.keycloak.yml` starts a local-only Keycloak development server on `127.0.0.1:8180`. Read `keycloak/README.md` before using it. The application starter currently uses its own lightweight auth module; Keycloak is documented as an optional identity provider but is **not wired into JWT validation yet**. Do not assume Keycloak tokens are accepted by the API.

## 6. Postman

Import `postman/Employee-Management.postman_collection.json` into Postman. Set collection variables:
- `baseUrl`: `http://localhost:8080/api/v1`
- `username`, `password`: test account
- `token`: automatically populated by successful Login/Register requests
- `employeeId`: automatically populated by successful Create request

Use a promoted ADMIN account for create/update/delete. A normal USER receives `403` for write operations.

## 7. OpenAPI

The OpenAPI file is `openapi/openapi.yaml`. Open it in Swagger Editor or import it into an OpenAPI-compatible client. The spec includes payload examples, security requirements and response codes. Keep it aligned with implementation changes.

## 8. GitHub Actions CI/CD

The workflow `.github/workflows/ci-cd.yml` runs on pull requests and pushes to `main`:
1. Java 25 setup + Maven cache
2. `mvn clean verify`
3. OWASP Dependency-Check (fails on CVSS >= 7; first run may take several minutes)
4. GitHub CodeQL
5. WAR artifact upload
6. On pushes to `main`, deploy to the configured Azure VM over SSH

### GitHub repository secrets

Create these in **Settings -> Secrets and variables -> Actions**:
- `AZURE_VM_HOST`: public DNS/IP of the VM
- `AZURE_VM_USER`: dedicated deploy account
- `AZURE_VM_SSH_KEY`: private key corresponding to VM authorized key

Never put SSH private keys, database passwords, JWT secrets or cloud credentials in workflow YAML. Restrict the GitHub environment `uat` with reviewers if your plan supports environment protection rules.

### Configure the VM

1. Provision Ubuntu 22.04/24.04 in Azure.
2. Allow inbound SSH only from approved IPs; expose 80/443 via a reverse proxy. Avoid exposing Oracle 1521 publicly.
3. Install Java 25, Tomcat 10.1, and monitoring agent. The bootstrap script is a starting point; package availability differs by Ubuntu image. On systems where `tomcat10` or `temurin-25-jdk` is not available in the configured apt repositories, use the vendor's supported repository/package or install a pinned distribution manually.
4. Install the deploy script at `/usr/local/sbin/deploy-employee-management`.
5. Configure the deploy account with SSH access and tightly scoped sudo permission for that script only.
6. Configure Tomcat service environment with DB and JWT secrets, then restart Tomcat.
7. Set the GitHub secrets above and push a tested commit to `main`.

The workflow uses `ssh-keyscan` to add the host key for convenience. For hardened production, pre-verify the VM host key out-of-band and pin it in `known_hosts` instead of trusting a live scan.

### Least-privilege sudoers example

Use `visudo` to create `/etc/sudoers.d/employee-deploy`:
```
deployuser ALL=(root) NOPASSWD: /usr/local/sbin/deploy-employee-management
```
The script must be owned by root and not writable by the deploy account. Consider validating the artifact checksum/signature and restricting the script to a fixed artifact path.

## 9. Azure networking and deployment strategy

- Prefer a private subnet and Azure Bastion/VPN for administration.
- Use Network Security Groups to restrict inbound traffic.
- Put Nginx or Azure Application Gateway in front of Tomcat for HTTPS, request limits and access logs.
- Store secrets in Azure Key Vault and use managed identity where practical.
- Deploy UAT first; run health checks/smoke tests; require approval before production.
- Keep a previous known-good WAR and database migration rollback plan. The deploy script backs up the prior WAR but does not automatically restore it if the health check fails; implement and test rollback before production.
- For high availability, use at least two application VMs behind a load balancer and externalized session/state. A single VM is a single point of failure.

## 10. Monitoring

See `ops/monitoring/README.md`. Baseline:
- Azure Monitor Agent + Log Analytics for VM metrics/log collection
- Alerts for CPU, disk, VM availability and health endpoint failures
- Prometheus Node Exporter for host metrics (private network only)
- Add JVM JMX exporter or OpenTelemetry/Micrometer for heap, GC, thread pools, request latency and error rates
- Correlate logs with `X-Correlation-ID`
- Never log tokens, passwords, SSNs or sensitive request payloads

## 11. Environment configuration

Property precedence:
1. `application.properties`
2. `application-{APP_ENV}.properties`
3. supported environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_TTL_MINUTES`)

`uat` and `prod` intentionally contain blank DB settings; inject them through environment variables or secret management. Local defaults are for local development only.

## 12. Important production hardening backlog

Before real employee data or public internet exposure:
- Replace homegrown auth with Keycloak/OIDC or enterprise IdP; enable MFA, lockout, reset flows, account disable, revocation/rotation, audit.
- Add authorization at the resource/data level; current starter only applies a simple ADMIN-only rule to writes.
- Add API rate limiting and request-size limits at gateway/reverse proxy.
- Use HTTPS only; configure secure headers.
- Add CORS allowlist if browser clients require it.
- Add a dedicated migration identity and Flyway/Liquibase execution in release pipeline.
- Add Oracle integration tests using a licensed/approved Oracle image; test unique constraints and failover.
- Add tests for auth, security filters, DTO validation, repository, REST endpoints, and deployment.
- Add resilience/timeout policies, structured logs, metrics, distributed tracing, dependency/SBOM scans and container scans.
- Pin GitHub Actions by commit SHA, use artifact attestation/signing, and validate deployment host keys.
- Implement tested rollback and blue/green or rolling deployment for availability.

## API summary

| Method | Path | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public; creates USER |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/auth/health` | Public |
| GET | `/api/v1/health` | Public |
| GET | `/api/v1/employees` | Any authenticated user |
| GET | `/api/v1/employees/{id}` | Any authenticated user |
| POST | `/api/v1/employees` | ADMIN |
| PUT | `/api/v1/employees/{id}` | ADMIN |
| DELETE | `/api/v1/employees/{id}` | ADMIN |

## Troubleshooting

- **401**: ensure `Authorization: Bearer <token>`; token not expired; same `JWT_SECRET` and `JWT_ISSUER` across instances.
- **403**: the logged-in account is USER; promote the initial admin through controlled SQL and log in again.
- **500 database error**: verify PDB service name, network access, grants, schema migration, credentials and Oracle listener.
- **Tomcat fails to deploy WAR**: inspect `/var/log/tomcat10/catalina.out` or `journalctl -u tomcat10`.
- **CI Dependency-Check timeout**: first run downloads vulnerability database; configure a cache/NVD API key if required by your org.
