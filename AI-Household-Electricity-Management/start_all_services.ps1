Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " AI-Based Household Electricity Management Application" -ForegroundColor Green
Write-Host " Master Service Launcher (MCA Major Project - Vikas K R)" -ForegroundColor White
Write-Host "=====================================================================" -ForegroundColor Cyan

Set-Location $PSScriptRoot

Write-Host "1. Launching Python FastAPI ML Microservice (Port 8000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy Bypass", "-File `"$PSScriptRoot\ml-service\run_ml_service.ps1`""

Write-Host "2. Launching Java Spring Boot Backend (Port 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy Bypass", "-File `"$PSScriptRoot\backend\electricity-management\run_backend.ps1`""

Write-Host "3. Launching Web Frontend Server (Port 3000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy Bypass", "-Command `"Set-Location '$PSScriptRoot\frontend'; python -m http.server 3000`""
Start-Process "http://localhost:3000"

Write-Host "`nAll services triggered successfully!" -ForegroundColor Green
Write-Host " - Frontend UI:        $PSScriptRoot\frontend\index.html" -ForegroundColor White
Write-Host " - Spring Boot API:    http://localhost:8080/api/v1/health" -ForegroundColor White
Write-Host " - FastAPI ML API:     http://localhost:8000/docs" -ForegroundColor White
