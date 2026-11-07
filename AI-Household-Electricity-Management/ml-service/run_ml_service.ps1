Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " AI-Based Household Electricity Management: Python ML Microservice" -ForegroundColor Green
Write-Host "=====================================================================" -ForegroundColor Cyan

Set-Location $PSScriptRoot
$pythonCmd = "python"
if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    if (Test-Path "$env:LOCALAPPDATA\Programs\Python\Python313\python.exe") {
        $pythonCmd = "$env:LOCALAPPDATA\Programs\Python\Python313\python.exe"
    }
}

Write-Host "Starting FastAPI ML Inference Server on http://localhost:8000 ..." -ForegroundColor Yellow
& $pythonCmd -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
