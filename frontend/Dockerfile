FROM node:22-alpine

RUN npm install -g pnpm

WORKDIR /usr/src/app

COPY package*.json pnpm-lock.yaml* ./

RUN pnpm add -g @angular/cli

RUN pnpm install --frozen-lockfile

COPY . .

RUN addgroup -g 1001 -S nodejs && \
    adduser -S angular -u 1001 -G nodejs

RUN chown -R angular:nodejs /usr/src/app

USER angular

EXPOSE 4200

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:4200 || exit 1

CMD ["ng", "serve", "--host", "0.0.0.0", "--port", "4200", "--disable-host-check", "--poll", "2000"]
