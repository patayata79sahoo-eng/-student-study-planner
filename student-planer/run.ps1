# Run script for EduPlan Student Study Planner
$ErrorActionPreference = "Stop"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  EduPlan - Deploying & Running on Apache Tomcat         " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Build
& .\build.ps1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Build failed." -ForegroundColor Red
    exit 1
}

# 2. Locate Tomcat
$tomcatDir = $env:CATALINA_HOME
if (-not $tomcatDir -or -not (Test-Path $tomcatDir)) {
    if (Test-Path "C:\Users\patay\.tools\apache-tomcat-10.1.28") {
        $tomcatDir = "C:\Users\patay\.tools\apache-tomcat-10.1.28"
    } else {
        Write-Host "[ERROR] Apache Tomcat not found. Please set `$env:CATALINA_HOME." -ForegroundColor Red
        exit 1
    }
}

# 3. Locate JDK
if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Users\patay\.tools\jdk-17.0.21+9") {
        $env:JAVA_HOME = "C:\Users\patay\.tools\jdk-17.0.21+9"
    }
}

Write-Host "Tomcat Directory: $tomcatDir"
Write-Host "JAVA_HOME:        $env:JAVA_HOME"

# 4. Deploy WAR
Write-Host "Deploying student-planer.war to Tomcat webapps..."
Copy-Item "student-planer.war" -Destination "$tomcatDir\webapps\student-planer.war" -Force

# 5. Start Tomcat
Write-Host "Starting Tomcat server..." -ForegroundColor Green
$startupBat = "$tomcatDir\bin\startup.bat"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"$startupBat`""

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "  EduPlan Application Deployed Successfully!            " -ForegroundColor Green
Write-Host "  URL: http://localhost:8080/student-planer/            " -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Green
Write-Host "To stop the server at any time, run .\stop.ps1"

