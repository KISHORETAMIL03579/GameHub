@echo off
title GameHub FastAPI Server
echo ===============================================================
echo   Starting GameHub Backend Server
echo.
echo   Local PC Access:            http://127.0.0.1:8000/docs
echo   Android Emulator Access:    http://10.0.2.2:8000
echo   Physical Phone Wi-Fi Access: http://192.168.1.2:8000
echo ===============================================================
cd /d "%~dp0backend"
..\venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause
