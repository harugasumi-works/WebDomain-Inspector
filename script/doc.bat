@echo off
echo ==========================================
echo      JAVADOC GENERATOR (DEBUG MODE)
echo ==========================================
echo.

:: 1. Where are we?
cd ..
echo Current Folder: %CD%

:: 2. Check if the code is actually here
if not exist "com" (
    echo.
    echo [ERROR] The "com" folder is missing!
    pause
    exit /b
)

echo.
echo [INFO] Found source code. Generating docs...
echo.

:: 3. Run Javadoc with "Shut Up" mode (-quiet) and "No Police" mode (-Xdoclint:none)
:: We removed -quiet so you can see the error if it happens.
javadoc -d docs ^
    -sourcepath . ^
    -subpackages com ^
    -encoding UTF-8 ^
    -charset UTF-8 ^
    -docencoding UTF-8 ^
    -Xdoclint:none

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ==========================================
    echo [FAILED] Javadoc crashed.
    echo Read the error message above carefully.
    echo ==========================================
) else (
    echo.
    echo [SUCCESS] Documentation generated!
    echo Opening...
    start chrome docs\index.html
)

pause