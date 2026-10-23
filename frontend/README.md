# Angular dashboard starter

Create a current Angular standalone app:
```bash
npm install -g @angular/cli
ng new nimbus-dashboard --standalone --routing --style=css
```
Copy `src/app` and `src/environments` into the generated app. Run:
```bash
npm start
```
If `npm start` is not configured, use `ng serve`.
Backend defaults to `http://localhost:8080`. This starter assumes local development and no authentication.
