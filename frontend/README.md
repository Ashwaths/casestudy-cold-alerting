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

Set `API_PROXY_TARGET` to the backend URL before starting the development
server. For a local-only backend use `http://127.0.0.1:8080`; for a remote
backend provide its HTTPS URL. The proxy target is deliberately externalized
and is not part of the production frontend configuration.
