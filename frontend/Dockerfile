FROM node:24-alpine AS base
RUN corepack enable
WORKDIR /app

FROM base AS development-dependencies-env
COPY ./package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

FROM base AS production-dependencies-env
COPY ./package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --prod

FROM base AS build-env
COPY . .
COPY --from=development-dependencies-env /app/node_modules ./node_modules
RUN pnpm run build

FROM node:24-alpine
WORKDIR /app
COPY --chown=node:node ./package.json ./
COPY --from=production-dependencies-env --chown=node:node /app/node_modules ./node_modules
COPY --from=build-env --chown=node:node /app/build ./build
USER node
CMD ["node_modules/.bin/react-router-serve", "./build/server/index.js"]
