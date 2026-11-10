# Stop Tomcat server
$tomcatDir = $env:CATALINA_HOME
if (-not $tomcatDir -or -not (Test-Path $tomcatDir)) {
    if (Test-Path "C:\Users\patay\.tools\apache-tomcat-10.1.28") {
        $tomcatDir = "C:\Users\patay\.tools\apache-tomcat-10.1.28"
    }
}

if (-not $env:JAVA_HOME -and (Test-Path "C:\Users\patay\.tools\jdk-17.0.21+9")) {
    $env:JAVA_HOME = "C:\Users\patay\.tools\jdk-17.0.21+9"
}

if ($tomcatDir -and (Test-Path "$tomcatDir\bin\shutdown.bat")) {
    Write-Host "Shutting down Apache Tomcat..." -ForegroundColor Yellow
    & "$tomcatDir\bin\shutdown.bat"
} else {
    Write-Host "Could not find Tomcat installation to shut down." -ForegroundColor Red
}

