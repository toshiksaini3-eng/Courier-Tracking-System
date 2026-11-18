@echo off
setlocal
cd /d "%~dp0"
if not exist "bin\com\couriertracking\Main.class" (
    echo Binaries not found. Running compilation first...
    call compile.bat
)
echo Launching Courier Tracking System...
java -cp "bin;lib/*;src/main/resources" com.couriertracking.Main
