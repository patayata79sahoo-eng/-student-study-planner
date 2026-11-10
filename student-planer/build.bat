@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo   EduPlan - Building Student Study Planner Application
echo ========================================================

:: Detect JDK
if defined JAVA_HOME (
    set "JAVAC_BIN=%JAVA_HOME%\bin\javac.exe"
    set "JAR_BIN=%JAVA_HOME%\bin\jar.exe"
) else if exist "C:\Users\patay\.tools\jdk-17.0.21+9\bin\javac.exe" (
    set "JAVAC_BIN=C:\Users\patay\.tools\jdk-17.0.21+9\bin\javac.exe"
    set "JAR_BIN=C:\Users\patay\.tools\jdk-17.0.21+9\bin\jar.exe"
) else (
    set "JAVAC_BIN=javac.exe"
    set "JAR_BIN=jar.exe"
)

echo Using Java Compiler: "!JAVAC_BIN!"

:: Prepare classes directory
if not exist "webapp\WEB-INF\classes" mkdir "webapp\WEB-INF\classes"

:: Compile Java sources
echo Compiling Java source files...
dir /s /b "src\*.java" > sources.tmp
"!JAVAC_BIN!" -encoding UTF-8 -cp "lib\servlet-api.jar" -d "webapp\WEB-INF\classes" @sources.tmp
set BUILD_STATUS=%ERRORLEVEL%
del sources.tmp

if !BUILD_STATUS! neq 0 (
    echo [ERROR] Compilation failed with error code !BUILD_STATUS!.
    exit /b !BUILD_STATUS!
)

echo [SUCCESS] Java compilation succeeded!

:: Package WAR file
echo Packaging student-planer.war...
cd webapp
"!JAR_BIN!" cvf "..\student-planer.war" . > nul
cd ..

if exist "student-planer.war" (
    echo [SUCCESS] Package created: student-planer.war
    echo ========================================================
) else (
    echo [ERROR] Failed to package student-planer.war
    exit /b 1
)

endlocal

