@echo off
setlocal
cd /d "%~dp0"
echo ==========================================================
echo Compiling Courier Tracking System...
echo ==========================================================
if not exist "bin" mkdir bin

echo Collecting Java source files...
powershell -NoProfile -Command "(Get-ChildItem -Path 'src/main/java' -Recurse -Filter *.java).FullName | ForEach-Object { Resolve-Path -Relative $_ } | Set-Content sources.txt"

echo Compiling Java source files with javac...
javac -encoding UTF-8 -d bin -cp "lib/*;src/main/resources" "@sources.txt"

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation completed without errors!
    if exist "src\main\resources" xcopy /y /s /q "src\main\resources\*" "bin\" >nul 2>&1
    if exist sources.txt del sources.txt >nul 2>&1
    echo Ready to run using run.bat or VS Code!
) else (
    echo [ERROR] Compilation failed.
)
