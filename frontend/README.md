# Angular dashboard

The Angular standalone dashboard requires Node.js 26. The Compose deployment
serves it over TLS and proxies API requests through Nginx. For a local
development build:

```bash
npm ci
npm start
```

```bash
npm ci
npm start
```

The development server proxies `/api` to `http://localhost:8080`; use the
Compose stack for authentication and TLS integration testing.
