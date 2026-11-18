@echo off
setlocal
cd /d "%~dp0"
echo ==========================================================
echo Starting Courier Tracking System Localhost Server...
echo ==========================================================
if not exist "bin\com\couriertracking\Main.class" (
    call compile.bat
)
echo Open your browser at http://localhost:8080
java -cp "bin;lib/*;src/main/resources" com.couriertracking.service.LocalhostHttpServer
pause
