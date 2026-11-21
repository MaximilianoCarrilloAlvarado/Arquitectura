# Runtime Validation Report

**Generated**: 2026-11-20T20:01:52-06:00  
**Target**: `demo`

## Summary

| Step | Status | Exit Code | Details |
|---|---|---:|---|
| Startup | PASS | 0 | Spring application context started during `.\mvnw.cmd -f .\demo\pom.xml test` with the `test` profile. |
| Integration | PASS | 0 | 4 test classes, 10 tests, all passing using H2 in MySQL compatibility mode. |
| Browser E2E | UNVERIFIED | n/a | Node.js and `npx` are unavailable; the index test verifies its HTTP resource, not JavaScript interactions in a browser. |

**Overall**: NEEDS_SIGNOFF — server-side journeys pass; browser interaction and a real MySQL instance were not exercised.

## Environment and Strategy

- Docker: unavailable; `docker info` reported that the daemon is not responding.
- Node.js: unavailable; `Get-Command node` did not find the command.
- Playwright: unavailable; `Get-Command npx` did not find the command.
- Infrastructure tier: H2 in-memory database, configured with MySQL compatibility mode.
- Browser tier: Spring MVC integration testing; gap: actual browser rendering and client-side JavaScript behavior.
- Authentication: not applicable; the application does not implement authentication.
- Database gap: H2 is not a substitute for validating behavior against the configured MySQL server.

## Test Evidence

`mvnw test`: exit code 0; 10 tests, 0 failures, 0 errors, 0 skipped.

- `EmpleadoCrudIntegrationTest`: create, retrieve, update, delete, and verify not found.
- `EmpleadoValidationIntegrationTest`: invalid name, age zero, negative salary, over-limit salary, excessive decimals, maximum valid salary, and missing employee.
- `EmpleadoBulkDeleteIntegrationTest`: delete selected records and verify the remaining record.
- `IndexPageIntegrationTest`: verify the static index route and primary CRUD/selection controls.

Surefire reports are available under `target/surefire-reports/`.
