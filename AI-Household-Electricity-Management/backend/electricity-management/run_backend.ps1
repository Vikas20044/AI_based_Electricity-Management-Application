Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host " AI-Based Household Electricity Management: Java Spring Boot Backend" -ForegroundColor Green
Write-Host "=====================================================================" -ForegroundColor Cyan

Set-Location $PSScriptRoot

$mvnCmd = $null

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvnCmd = "mvn"
} elseif (Test-Path "C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd") {
    $mvnCmd = "C:\Users\HP\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"
}

if ($mvnCmd) {
    Write-Host "Maven detected. Starting Spring Boot on http://localhost:8080 ..." -ForegroundColor Yellow
    & $mvnCmd spring-boot:run
} else {
    Write-Host "Maven is not currently installed or in your system PATH." -ForegroundColor Red
    Write-Host "You can:" -ForegroundColor Yellow
    Write-Host "  1. Install Apache Maven (choco install maven, winget install Apache.Maven, or download from maven.apache.org)"
    Write-Host "  2. Open the project in IntelliJ IDEA, Eclipse, or VS Code and run:"
    Write-Host "     com.electricity.ElectricityManagementApplication.java" -ForegroundColor White
    Read-Host "Press Enter to exit..."
}
