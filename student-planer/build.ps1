# Build script for EduPlan Student Study Planner
$ErrorActionPreference = "Stop"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  EduPlan - Building Student Study Planner Application   " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# Locate JDK
$javac = "javac.exe"
$jar = "jar.exe"

if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javac = "$env:JAVA_HOME\bin\javac.exe"
    $jar = "$env:JAVA_HOME\bin\jar.exe"
} elseif (Test-Path "C:\Users\patay\.tools\jdk-17.0.21+9\bin\javac.exe") {
    $javac = "C:\Users\patay\.tools\jdk-17.0.21+9\bin\javac.exe"
    $jar = "C:\Users\patay\.tools\jdk-17.0.21+9\bin\jar.exe"
}

Write-Host "Using Java Compiler: $javac"

# Prepare classes directory
$classesDir = "webapp\WEB-INF\classes"
if (-not (Test-Path $classesDir)) {
    New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
}

# Collect Java sources
$sources = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
Write-Host "Compiling $($sources.Count) Java files..."

& $javac -encoding UTF-8 -cp "lib\servlet-api.jar" -d $classesDir $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Java compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[SUCCESS] All Java classes compiled successfully." -ForegroundColor Green

# Package WAR
Write-Host "Packaging student-planer.war..."
& $jar cvf "student-planer.war" -C "webapp" . | Out-Null

if (Test-Path "student-planer.war") {
    $war = Get-Item "student-planer.war"
    Write-Host "[SUCCESS] Application packaged: student-planer.war ($([math]::Round($war.Length / 1KB, 1)) KB)" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Failed to create WAR file." -ForegroundColor Red
    exit 1
}

Write-Host "========================================================" -ForegroundColor Cyan

