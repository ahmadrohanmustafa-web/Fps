@echo off
setlocal enableextensions
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo [VOID FPS] Java not found. Install JDK 25 first, e.g.:  winget install EclipseAdoptium.Temurin.25.JDK
  exit /b 1
)

set "WTMP=%TEMP%\voidfps-wrapper"
if not exist gradlew.bat (
  where gradle >nul 2>nul
  if errorlevel 1 (
    echo [VOID FPS] No Gradle wrapper and no Gradle on PATH.
    echo Install Gradle once:  winget install Gradle.Gradle   ^(then open a NEW terminal and re-run this file^)
    exit /b 1
  )
  echo [VOID FPS] Generating Gradle wrapper 9.6.0 ...
  rmdir /s /q "%WTMP%" 2>nul
  mkdir "%WTMP%"
  type nul > "%WTMP%\settings.gradle"
  pushd "%WTMP%"
  call gradle wrapper --gradle-version 9.6.0
  popd
  if not exist "%WTMP%\gradlew.bat" (
    echo [VOID FPS] Wrapper generation failed. If 9.6.0 does not exist, edit this file and use the newest 9.x from gradle.org/releases.
    exit /b 1
  )
  copy /y "%WTMP%\gradlew" . >nul
  copy /y "%WTMP%\gradlew.bat" . >nul
  xcopy /y /e /i "%WTMP%\gradle" gradle >nul
)

call gradlew.bat clean build test
if errorlevel 1 (
  echo [VOID FPS] BUILD FAILED. Copy the red error text and send it back.
  exit /b 1
)

for /f "tokens=2 delims==" %%v in ('findstr /b "mod_version" gradle.properties') do set "MODVER=%%v"
if not exist build-output mkdir build-output
copy /y "build\libs\void-fps-%MODVER%.jar" "build-output\VOID-FPS.jar" >nul
echo.
echo [VOID FPS] SUCCESS: build-output\VOID-FPS.jar
endlocal
