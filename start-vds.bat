@echo off
REM ============================================================
REM  DevRadar AI — VDS Kurulum Scripti (Windows)
REM  devradarai.com | IP: 188.240.81.48
REM ============================================================

echo.
echo ╔══════════════════════════════════════════╗
echo ║      DevRadar AI - VDS Kurulum           ║
echo ╚══════════════════════════════════════════╝
echo.

REM ─── ADIM 1: Java 17 kontrolü ─────────────────────────────
echo [1/5] Java 17 kontrol ediliyor...
java -version 2>nul
IF ERRORLEVEL 1 (
    echo [HATA] Java bulunamadi!
    echo Lutfen https://adoptium.net adresinden Java 17 indirin.
    pause
    exit /b 1
)
echo Java OK.
echo.

REM ─── ADIM 2: Nginx indir ve kur ───────────────────────────
echo [2/5] Nginx kontrol ediliyor...
IF NOT EXIST "C:\nginx\nginx.exe" (
    echo Nginx bulunamadi. Indiriliyor...
    powershell -Command "Invoke-WebRequest -Uri 'https://nginx.org/download/nginx-1.26.2.zip' -OutFile '%TEMP%\nginx.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\nginx.zip' -DestinationPath 'C:\' -Force"
    powershell -Command "Rename-Item -Path 'C:\nginx-1.26.2' -NewName 'nginx' -Force" 2>nul
    echo Nginx indirildi: C:\nginx
) ELSE (
    echo Nginx mevcut: C:\nginx
)

REM Nginx config dosyasini kopyala
echo nginx.conf kopyalaniyor...
copy /Y "%~dp0nginx\nginx.conf" "C:\nginx\conf\nginx.conf"
echo.

REM ─── ADIM 3: Projeyi build et ─────────────────────────────
echo [3/5] Maven build basliyor...
cd /d "%~dp0"
call mvn clean package -DskipTests -B
IF ERRORLEVEL 1 (
    echo [HATA] Maven build basarisiz!
    pause
    exit /b 1
)
echo Build tamamlandi: target/devradar-1.0.0.jar
echo.

REM ─── ADIM 4: Nginx baslat ─────────────────────────────────
echo [4/5] Nginx baslatiliyor...
taskkill /f /im nginx.exe 2>nul
timeout /t 1 /nobreak >nul
start "" /b "C:\nginx\nginx.exe" -p "C:\nginx"
echo Nginx baslatildi. (port 80)
echo.

REM ─── ADIM 5: Spring Boot baslat ──────────────────────────
echo [5/5] DevRadar AI baslatiliyor (port 8080)...
echo.
echo Durdurmak icin: Ctrl+C
echo.

java -jar "%~dp0target\devradar-1.0.0.jar" ^
  --spring.profiles.active=prod ^
  --server.port=8080

pause
