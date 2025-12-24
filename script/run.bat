@echo off
echo [1/2] Cleaning previous build...
if exist bin rmdir /s /q bin
if exist sources.txt del sources.txt
mkdir bin

echo [2/2] Compiling Java files...
javac -d bin -encoding UTF-8 com\harugasumi\core\*.java com\harugasumi\model\*.java com\harugasumi\domaincheckapp\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b
)

echo Compile Done
pause