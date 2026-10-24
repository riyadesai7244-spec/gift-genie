@echo off
echo ========================================================
echo   GiftGenie - Database Connected Java Web App
echo ========================================================
echo.
echo Compiling SimpleGiftApp.java with JDBC Driver...
javac -encoding UTF-8 -cp ".;lib/*" SimpleGiftApp.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)
echo.
echo Starting GiftGenie on http://localhost:8080 ...
echo Database: SQLite (giftgenie.db)
echo.
java -cp ".;lib/*" SimpleGiftApp
pause
