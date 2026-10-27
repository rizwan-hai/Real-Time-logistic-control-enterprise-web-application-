# Security notes

## Security architecture

- JWT bearer tokens are used for API authentication.
- Passwords are encoded with BCrypt.
- Accounts can be deactivated and locked after repeated failed login attempts.
- Authorization is enforced in controllers and service methods using Spring Security.
- Sensitive endpoints are protected and audit records are generated for security-relevant actions.

## Risk and limitations

- This project is an application backend prototype rather than a full enterprise security operation.
- Audit logs are database-backed and should be treated as useful operational evidence, not tamper-proof evidence of record integrity.
- Secret storage should be moved to a dedicated secrets manager in production.
- CSRF is intentionally disabled here because the project uses stateless JWT authentication; stateless APIs should be protected at the gateway or reverse-proxy layer where applicable.
