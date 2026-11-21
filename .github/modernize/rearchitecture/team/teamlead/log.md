## [35964861-afb5-4a3f-a384-3ca365e2d683] Initialized Nimbus Freight migration tracking
- Codebase/domain discoveries: The requested Appmod migration session and Java-upgrade branch are identified; the plan directory did not yet contain a `progress.md`.
- Wrong assumptions and corrections: No explicit Appmod `reportPath` was exposed in the available session metadata, so progress tracking was created beside the named plan.
- Debugging dead-ends and what actually worked: Current-session metadata confirmed the expected branch; the migration session ID itself did not expose a report path through the available tools.
- Techniques/patterns worth reusing: Track transformation order and validation/commit steps in both structured todos and `progress.md`; keep all transformation steps unchecked until implementation begins.
- Learnings consumed: []

## [35964861-afb5-4a3f-a384-3ca365e2d683] Completed Stage 3 consistency review
- Codebase/domain discoveries: The Entra OAuth authorization-code callback is a cross-site top-level GET; a session cookie with SameSite=Strict prevents the OAuth state/session from returning to the application. SameSite=Lax allows that navigation while retaining cross-site CSRF protections for unsafe methods.
- Wrong assumptions and corrections: `appmod-version-control checkStatus` confirmed the current migration branch but did not expose a commit SHA; the baseline was resolved as the latest SHA on the repository's only remote branch, `main`.
- Debugging dead-ends and what actually worked: Placeholder tenant issuer discovery caused test context startup to call Microsoft endpoints; explicit test-only provider endpoints and authorization-code/redirect properties made the suite deterministic. `mvn -B clean test` passed 8 tests.
- Techniques/patterns worth reusing: For OAuth code flow behind Spring Security, verify browser cookie SameSite behavior on the IdP callback; use explicit metadata endpoints in tests to avoid contacting a real tenant.
- Learnings consumed: []
