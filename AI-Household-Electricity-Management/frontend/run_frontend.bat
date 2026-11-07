@echo off
echo =====================================================================
echo  AI-Based Household Electricity Management: Web Frontend Server
echo =====================================================================
cd /d "%~dp0"

set "PYTHON_CMD=python"
where python >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "%LOCALAPPDATA%\Programs\Python\Python313\python.exe" (
        set "PYTHON_CMD=%LOCALAPPDATA%\Programs\Python\Python313\python.exe"
    )
)

echo Starting Fresh Zero-Cache Frontend Server on http://localhost:3000 ...
start "" "http://localhost:3000"
"%PYTHON_CMD%" server.py
pause
