Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Set-Location -Path $PSScriptRoot

Write-Host "========================================"
Write-Host "DEFCON Masternode Helper - Local Server"
Write-Host "========================================"

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Java JDK (javac) not found in PATH."
    Write-Host "Please install JDK 17+ and try again."
    Read-Host "Press Enter to exit"
    exit 1
}

if (-not (Test-Path ".\LocalServer.java")) {
    Write-Host "ERROR: LocalServer.java not found in this folder."
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Compiling..."
javac .\LocalServer.java

Write-Host "Starting server..."
$proc = Start-Process -FilePath "java" -ArgumentList "LocalServer" -PassThru

Write-Host "Waiting for server to start..."
for ($i = 0; $i -lt 30; $i++) {
    try {
        $client = New-Object System.Net.Sockets.TcpClient("127.0.0.1", 8080)
        $client.Close()
        break
    } catch {
        Start-Sleep -Milliseconds 300
    }
}

Start-Process "http://127.0.0.1:8080"

Read-Host "Press Enter to exit"
