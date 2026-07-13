# Script to bootstrap Maven and run the application
$MavenVersion = "3.9.6"
$MavenDir = Join-Path $PSScriptRoot ".maven"
$M2Dir = Join-Path $PSScriptRoot ".m2"
$ZipPath = Join-Path $PSScriptRoot "maven.zip"
$MvnPath = Join-Path $MavenDir "apache-maven-$MavenVersion\bin\mvn.cmd"

# Set JAVA_HOME if it is not already set or not pointing to a valid Java
if (!(Test-Path Env:JAVA_HOME) -or !(Test-Path "$Env:JAVA_HOME\bin\java.exe")) {
    $PotentialJavaHome = "C:\Users\Administrator\.antigravity\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"
    if (Test-Path "$PotentialJavaHome\bin\java.exe") {
        $Env:JAVA_HOME = $PotentialJavaHome
        $Env:Path = "$PotentialJavaHome\bin;$Env:Path"
        Write-Host "Using auto-detected JAVA_HOME: $Env:JAVA_HOME" -ForegroundColor Green
    } else {
        Write-Host "WARNING: JAVA_HOME is not set and java.exe was not found in the expected location. Build may fail." -ForegroundColor Yellow
    }
}

if (!(Test-Path $MvnPath)) {
    Write-Host "Maven bulunamadi. Apache Maven $MavenVersion indiriliyor..." -ForegroundColor Cyan
    $Url = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"

    # Download Maven
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $Url -OutFile $ZipPath

    Write-Host "Arsiv aciliyor..." -ForegroundColor Cyan
    Expand-Archive -Path $ZipPath -DestinationPath $MavenDir -Force
    Remove-Item $ZipPath
    Write-Host "Maven basariyla kuruldu." -ForegroundColor Green
}

# Set local M2 repository directory inside project for portability
# (FIX: this was declared before but never actually passed to mvn, so it had no effect —
# every build was still using the system-wide ~/.m2 repo. Now it's passed explicitly via
# -Dmaven.repo.local so the whole toolchain, including downloaded dependency jars, stays
# inside the project folder.)
$Env:M2_HOME = Join-Path $MavenDir "apache-maven-$MavenVersion"
$MvnRepoArg = "-Dmaven.repo.local=$M2Dir"

$PropsPath = Join-Path $PSScriptRoot "src\main\resources\application.properties"
if (Test-Path $PropsPath) {
    $Prop = Get-Content $PropsPath | Select-String -Pattern "^gemini.api.key=" | ForEach-Object { $_.Line.Split("=")[1].Trim() }
    if ($Prop -match '\$\{GEMINI_API_KEY:(.*)\}') {
        $Env:GEMINI_API_KEY = $Matches[1]
    } elseif ($Prop -and $Prop -notmatch '\$\{') {
        $Env:GEMINI_API_KEY = $Prop
    }
}

if ($Env:GEMINI_API_KEY) {
    Write-Host "Gemini API key loaded from properties: $($Env:GEMINI_API_KEY.SubString(0, [Math]::Min(15, $Env:GEMINI_API_KEY.Length)))..." -ForegroundColor Green
} else {
    Write-Host "UYARI: GEMINI_API_KEY bulunamadi. Uygulama mock modda baslayacak." -ForegroundColor Yellow
}

Write-Host "Proje derleniyor..." -ForegroundColor Cyan
& $MvnPath $MvnRepoArg clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "Derleme basarili. Uygulama baslatiliyor..." -ForegroundColor Green
    & $MvnPath $MvnRepoArg spring-boot:run
} else {
    Write-Host "Derleme sirasinda hata olustu!" -ForegroundColor Red
}
