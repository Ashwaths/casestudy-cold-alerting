FROM node:26-slim AS build
WORKDIR /workspace

COPY package.json package-lock.json ./
RUN npm ci

COPY angular.json tsconfig*.json ./
COPY proxy.conf.json ./
COPY src ./src
RUN npm run build

FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /workspace/dist/nimbus-dashboard/browser /usr/share/nginx/html
EXPOSE 80
