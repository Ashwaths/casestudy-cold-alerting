# OAuth Cookie SameSite

OAuth authorization-code callbacks need a cookie policy that permits the identity provider's top-level redirect back to the application.

## What Happened
During Nimbus Freight task 35964861-afb5-4a3f-a384-3ca365e2d683, the session cookie used SameSite=Strict while the application relied on a Spring Security Entra OAuth login. The external top-level callback would omit the session cookie, losing the server-side OAuth authorization request/state. Changing the cookie to SameSite=Lax preserves the required top-level safe navigation while excluding cross-site subresource requests. Confirmed the cookie semantics against MDN Set-Cookie documentation and the OAuth login redirect flow in Spring Security documentation.

## Takeaway
When replacing local credentials with OAuth/OIDC, audit session-cookie SameSite settings. For the standard authorization-code callback using a top-level GET, SameSite=Lax is generally compatible; verify any non-default response mode such as cross-site POST separately.

## History
- 2026-11-21 (casestudy-cold-alerting/35964861-afb5-4a3f-a384-3ca365e2d683): initial
