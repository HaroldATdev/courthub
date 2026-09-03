# ============================================================
# CourtHub · Dockerfile del Backend (Node.js + Express)
# ============================================================
FROM node:20-alpine

WORKDIR /app

# Instalar dependencias primero (mejor cacheo de capas)
COPY package*.json ./
RUN npm install --omit=dev --no-audit --no-fund

# Código fuente
COPY src ./src

ENV NODE_ENV=production
EXPOSE 3000

CMD ["node", "src/server.js"]
