# Lance les 9 services dans l'ordre, une fenetre PowerShell par service.
# Usage (depuis la racine du projet) :
#   powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1
# Prerequis : MySQL demarre sur localhost:3306 (root / rootroot)

$root = Split-Path -Parent $PSScriptRoot
$ErrorActionPreference = "Stop"

# --- JAVA_HOME ---
if (-not $env:JAVA_HOME) { $env:JAVA_HOME = "C:\Program Files\Java\jdk-25" }
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Host "JAVA_HOME invalide : $env:JAVA_HOME (installe le JDK 25)" -ForegroundColor Red; exit 1
}

# --- Recherche de Maven : PATH, extension Oracle Java de VS Code, ou C:\maven ---
$mvn = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
if (-not $mvn) {
    $c = Get-ChildItem "$env:USERPROFILE\.vscode\extensions\oracle.oracle-java-*\nbcode\java\maven\bin\mvn.cmd" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($c) { $mvn = $c.FullName }
}
if (-not $mvn) {
    $c = Get-ChildItem "C:\maven\apache-maven-*\bin\mvn.cmd" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($c) { $mvn = $c.FullName }
}
if (-not $mvn) { Write-Host "Maven introuvable : voir README (installation de Maven)." -ForegroundColor Red; exit 1 }
Write-Host "Maven : $mvn"
Write-Host "Java  : $env:JAVA_HOME"

function Test-TcpPort {
    param([int]$Port)

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $attempt = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
        if (-not $attempt.AsyncWaitHandle.WaitOne(500)) { return $false }
        $client.EndConnect($attempt)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Wait-ForPort {
    param(
        [string]$Name,
        [int]$Port,
        [int]$TimeoutSeconds = 180
    )

    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    while ([DateTime]::UtcNow -lt $deadline) {
        if (Test-TcpPort -Port $Port) {
            Write-Host "$Name ecoute sur le port $Port." -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 2
    }

    Write-Host "Delai depasse : $Name n'ecoute pas sur le port $Port. Verifie sa fenetre PowerShell." -ForegroundColor Red
    return $false
}

function Wait-ForEurekaRegistration {
    param(
        [string]$Service,
        [int]$TimeoutSeconds = 90
    )

    $url = "http://localhost:8761/eureka/apps/$($Service.ToUpperInvariant())"
    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    while ([DateTime]::UtcNow -lt $deadline) {
        try {
            $null = Invoke-RestMethod -Uri $url -Headers @{ Accept = "application/json" } -TimeoutSec 3
            Write-Host "$Service enregistre dans Eureka." -ForegroundColor Green
            return $true
        } catch {
            Start-Sleep -Seconds 2
        }
    }

    Write-Host "Delai depasse : $Service n'apparait pas dans Eureka. Verifie ses journaux." -ForegroundColor Red
    return $false
}

function Start-ServiceWindow {
    param([string]$Service)

    Write-Host "Demarrage de $Service ..." -ForegroundColor Cyan
    $servicePath = Join-Path $root $Service
    $escapedRoot = $servicePath.Replace("'", "''")
    $escapedJava = $env:JAVA_HOME.Replace("'", "''")
    $escapedMaven = $mvn.Replace("'", "''")
    $cmd = "`$env:JAVA_HOME='$escapedJava'; Set-Location '$escapedRoot'; `$Host.UI.RawUI.WindowTitle='$Service'; & '$escapedMaven' spring-boot:run"
    Start-Process -FilePath "powershell.exe" -ArgumentList @("-NoExit", "-Command", $cmd) | Out-Null
}

function Start-AndWaitForService {
    param(
        [string]$Service,
        [int]$Port
    )

    Start-ServiceWindow -Service $Service
    if (-not (Wait-ForPort -Name $Service -Port $Port)) { exit 1 }
    if ($Service -ne "register-service" -and -not (Wait-ForEurekaRegistration -Service $Service)) { exit 1 }
}

if (-not (Test-TcpPort -Port 3306)) {
    Write-Host "MySQL est inaccessible sur localhost:3306. Demarre MySQL puis relance ce script." -ForegroundColor Red
    exit 1
}

$servicePorts = @(
    @{ name = "register-service"; port = 8761 },
    @{ name = "api-gateway"; port = 8765 },
    @{ name = "auth-service"; port = 8093 },
    @{ name = "user-service"; port = 8091 },
    @{ name = "question-service"; port = 8096 },
    @{ name = "quiz-service"; port = 8090 },
    @{ name = "gratification-service"; port = 8092 },
    @{ name = "notification-service"; port = 8094 },
    @{ name = "leaderboard-service"; port = 8095 }
)
foreach ($service in $servicePorts) {
    if (Test-TcpPort -Port $service.port) {
        Write-Host "Le port $($service.port) ($($service.name)) est deja utilise. Arrete l'instance existante avant de relancer tous les services." -ForegroundColor Red
        exit 1
    }
}

# Lancer les dependances avant les services qui les appellent.
Start-AndWaitForService -Service "register-service" -Port 8761

Start-ServiceWindow -Service "user-service"
Start-ServiceWindow -Service "question-service"
Start-ServiceWindow -Service "notification-service"
foreach ($service in @(
    @{ name = "user-service"; port = 8091 },
    @{ name = "question-service"; port = 8096 },
    @{ name = "notification-service"; port = 8094 }
)) {
    if (-not (Wait-ForPort -Name $service.name -Port $service.port)) { exit 1 }
    if (-not (Wait-ForEurekaRegistration -Service $service.name)) { exit 1 }
}

Start-ServiceWindow -Service "auth-service"
Start-ServiceWindow -Service "quiz-service"
Start-ServiceWindow -Service "gratification-service"
foreach ($service in @(
    @{ name = "auth-service"; port = 8093 },
    @{ name = "quiz-service"; port = 8090 },
    @{ name = "gratification-service"; port = 8092 }
)) {
    if (-not (Wait-ForPort -Name $service.name -Port $service.port)) { exit 1 }
    if (-not (Wait-ForEurekaRegistration -Service $service.name)) { exit 1 }
}

Start-ServiceWindow -Service "leaderboard-service"
Start-ServiceWindow -Service "api-gateway"
if (-not (Wait-ForPort -Name "leaderboard-service" -Port 8095)) { exit 1 }
if (-not (Wait-ForEurekaRegistration -Service "leaderboard-service")) { exit 1 }
if (-not (Wait-ForPort -Name "api-gateway" -Port 8765)) { exit 1 }
if (-not (Wait-ForEurekaRegistration -Service "api-gateway")) { exit 1 }
Write-Host "Tous les services sont demarres. Verifie leur presence dans http://localhost:8761." -ForegroundColor Green
