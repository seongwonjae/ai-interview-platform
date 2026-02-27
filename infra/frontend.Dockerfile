FROM node:20-alpine AS build
WORKDIR /app

# ✅ 실제 프론트 폴더명: interview-frontend
COPY interview-frontend/package*.json ./
RUN npm ci

COPY interview-frontend/ ./

ARG VITE_API_BASE=""
ENV VITE_API_BASE=${VITE_API_BASE}

RUN npm run build

# dist를 공유 볼륨(/usr/share/nginx/html)에 복사하는 one-shot 컨테이너
FROM alpine:3.20
WORKDIR /app
COPY --from=build /app/dist /dist
CMD ["sh","-c","rm -rf /usr/share/nginx/html/* && cp -r /dist/* /usr/share/nginx/html && echo 'dist deployed'"]
