@echo off
chcp 65001 >nul
echo [1/2] 启动后端 (Spring Boot, H2 file 模式)...
start "cbm-backend" cmd /c "cd /d %~dp0..\backend && mvnw.cmd spring-boot:run"
echo [2/2] 启动前端 (Vite)...
start "cbm-frontend" cmd /c "cd /d %~dp0..\frontend && (if not exist node_modules npm install) && npm run dev"
echo 前端 http://localhost:5173   后端 http://localhost:8080
