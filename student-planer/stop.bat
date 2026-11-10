@echo off
setlocal

:: Locate Tomcat
if defined CATALINA_HOME (
    set "TOMCAT_DIR=%CATALINA_HOME%"
) else if exist "C:\Users\patay\.tools\apache-tomcat-10.1.28" (
    set "TOMCAT_DIR=C:\Users\patay\.tools\apache-tomcat-10.1.28"
) else (
    echo [ERROR] Could not locate Tomcat directory.
    exit /b 1
)

:: Locate JDK
if not defined JAVA_HOME (
    if exist "C:\Users\patay\.tools\jdk-17.0.21+9" (
        set "JAVA_HOME=C:\Users\patay\.tools\jdk-17.0.21+9"
    )
)

echo Stopping Apache Tomcat...
call "!TOMCAT_DIR!\bin\shutdown.bat"
echo Tomcat shutdown signal sent.
endlocal

