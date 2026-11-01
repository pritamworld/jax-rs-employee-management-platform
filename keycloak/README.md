# Optional free/self-hosted identity provider: Keycloak

Keycloak is open-source and can run on a developer machine or an Azure VM. It provides OAuth 2.0 / OpenID Connect, user administration, password policies, sessions, MFA options and signing keys. The current starter includes a lightweight local auth module (BCrypt + HMAC JWT) for learning and Postman demos; it does **not** yet validate Keycloak-issued tokens. Do not point a production app at Keycloak until the resource server has been configured to validate its issuer, audience and JWKS keys.

For a disposable local Keycloak instance (development only), add this service to a separate `compose.keycloak.yml` or run it independently:

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:26.2
    command: start-dev
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: change-me-local-only
    ports:
      - "127.0.0.1:8180:8080"
```

Start it with `docker compose -f compose.keycloak.yml up -d`. Open `http://localhost:8180`, create a realm (for example `employee`), a confidential/public client appropriate to your client type, and roles `USER` and `ADMIN`. For production use a supported external database, TLS, a secret manager, backups and a non-development startup mode. Do not expose `start-dev` to the internet.

Integration work required before switching this application to Keycloak:
1. Add a JWT verifier that obtains the realm issuer and JWKS URI from trusted configuration.
2. Verify signature, issuer, audience/authorized party, expiry and expected role claim mapping.
3. Map realm/client roles to the application’s authorization policy.
4. Remove or disable public `/auth/register` and `/auth/login` endpoints when Keycloak owns those flows.
5. Add integration tests using real test tokens and negative cases.
