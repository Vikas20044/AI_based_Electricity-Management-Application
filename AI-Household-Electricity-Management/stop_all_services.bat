@echo off
echo =====================================================================
echo  AI-Based Household Electricity Management Application
echo  Master Service Shutdown (MCA Major Project - Vikas K R)
echo =====================================================================
echo.
echo Stopping services on ports 3000 (Frontend), 8000 (ML Service), and 8080 (Spring Boot)...

for %%P in (3000 8000 8080) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%P ^| findstr LISTENING') do (
        echo Terminating process PID %%a on port %%P...
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo.
echo =====================================================================
echo  All application services have been stopped.
echo =====================================================================
pause
