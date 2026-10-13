@echo off
echo ========================================================
echo   Simple Gift Recommendation & Database Analytics App
echo ========================================================
echo.
echo Compiling SimpleGiftApp.java...
javac -encoding UTF-8 SimpleGiftApp.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)
echo.
echo Starting Java App on http://localhost:8080 ...
echo.
java SimpleGiftApp
pause
