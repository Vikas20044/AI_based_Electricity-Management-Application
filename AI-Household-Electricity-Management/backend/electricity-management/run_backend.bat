@echo off
echo =====================================================================
echo  AI-Based Household Electricity Management: Java Spring Boot Backend
echo =====================================================================
cd /d "%~dp0"

if exist "target\electricity-management-1.0.0.jar" (
    echo Starting packaged Spring Boot executable JAR on port 8080...
    java -jar target\electricity-management-1.0.0.jar
    pause
    exit /b
)

set "MVN_CMD=mvn"
if exist "C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" (
    set "MVN_CMD=C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"
)

echo Using Maven: %MVN_CMD%
echo Starting Spring Boot application on port 8080...
call "%MVN_CMD%" spring-boot:run
pause
