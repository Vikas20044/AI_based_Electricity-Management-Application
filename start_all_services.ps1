Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " AI-Based Household Electricity Management Application" -ForegroundColor Green
Write-Host " Master Service Launcher (MCA Major Project - Vikas K R)" -ForegroundColor White
Write-Host "=====================================================================" -ForegroundColor Cyan

$projectDir = $PSScriptRoot
if (Test-Path "$PSScriptRoot\AI-Household-Electricity-Management") {
    $projectDir = "$PSScriptRoot\AI-Household-Electricity-Management"
}

Write-Host "Project Directory: $projectDir" -ForegroundColor White

Write-Host "1. Launching Python FastAPI ML Microservice (Port 8000)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k cd /d `"$projectDir\ml-service`" && run_ml_service.bat"

Write-Host "2. Launching Java Spring Boot Backend (Port 8080)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k cd /d `"$projectDir\backend\electricity-management`" && run_backend.bat"

Write-Host "3. Launching Web Frontend Server (Port 3000)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k cd /d `"$projectDir\frontend`" && run_frontend.bat"

Write-Host "`nAll 3 services triggered successfully!" -ForegroundColor Green
Write-Host " - Frontend UI:        http://localhost:3000 (Opens Login Page)" -ForegroundColor White
Write-Host " - Spring Boot API:    http://localhost:8080/api/v1/health" -ForegroundColor White
Write-Host " - FastAPI ML API:     http://localhost:8000/docs" -ForegroundColor White
