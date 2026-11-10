@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo   EduPlan - Starting Tomcat & Launching Web Application
echo ========================================================

:: 1. Run build
call build.bat
if !ERRORLEVEL! neq 0 (
    echo [ERROR] Build failed. Aborting startup.
    exit /b 1
)

:: 2. Locate Tomcat
if defined CATALINA_HOME (
    set "TOMCAT_DIR=%CATALINA_HOME%"
) else if exist "C:\Users\patay\.tools\apache-tomcat-10.1.28" (
    set "TOMCAT_DIR=C:\Users\patay\.tools\apache-tomcat-10.1.28"
) else (
    echo [ERROR] Could not locate Apache Tomcat directory. Please set CATALINA_HOME.
    exit /b 1
)

:: 3. Locate JDK for Tomcat
if not defined JAVA_HOME (
    if exist "C:\Users\patay\.tools\jdk-17.0.21+9" (
        set "JAVA_HOME=C:\Users\patay\.tools\jdk-17.0.21+9"
    )
)

echo Using Tomcat at: "!TOMCAT_DIR!"
echo Using JAVA_HOME: "!JAVA_HOME!"

:: 4. Deploy WAR to webapps
echo Deploying student-planer.war to Tomcat webapps...
copy /y "student-planer.war" "!TOMCAT_DIR!\webapps\student-planer.war" > nul

echo.
echo ========================================================
echo   EduPlan is ready!
echo   Access URL: http://localhost:8080/student-planer/
echo ========================================================
echo Starting Tomcat in background...

start "Apache Tomcat (EduPlan)" "!TOMCAT_DIR!\bin\startup.bat"

echo.
echo Tomcat server started in separate console window!
echo You can open your browser at: http://localhost:8080/student-planer/
echo To stop Tomcat, run stop.bat
echo ========================================================

endlocal

