@echo off
setlocal

cd /d "%~dp0"

echo ========================================
echo DEFCON Masternode Helper - Local Server
echo ========================================

where javac >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java JDK (javac) not found in PATH.
  echo Please install JDK 17+ and try again.
  pause
  exit /b 1
)

if not exist LocalServer.java (
  echo ERROR: LocalServer.java not found in this folder.
  pause
  exit /b 1
)

echo Compiling...
javac LocalServer.java
if errorlevel 1 (
  echo ERROR: Compilation failed.
  pause
  exit /b 1
)

echo Starting server...
start "" /b java LocalServer

echo Waiting for server to start...
powershell -NoProfile -Command "for($i=0;$i -lt 30;$i++){try{$c=New-Object Net.Sockets.TcpClient('127.0.0.1',8080);$c.Close();exit 0}catch{Start-Sleep -Milliseconds 300}}; exit 1"

start "" http://127.0.0.1:8080
pause
