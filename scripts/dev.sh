#!/usr/bin/env bash
# Codespaces / Linux 一键启动：后端(H2) + 前端(Vite)
set -e
cd "$(dirname "$0")/.."

echo "[1/3] 启动后端 (Spring Boot, H2 file 模式)..."
(cd backend && ./mvnw -q spring-boot:run) &
BACKEND_PID=$!

echo "[2/3] 安装并启动前端..."
(cd frontend && [ -d node_modules ] || npm install)
(cd frontend && npm run dev) &
FRONTEND_PID=$!

echo "[3/3] 已启动：前端 http://localhost:5173  后端 http://localhost:8080"
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT
wait
