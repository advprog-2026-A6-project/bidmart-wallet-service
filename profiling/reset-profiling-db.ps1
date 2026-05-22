param(
    [int] $WaitSeconds = 30
)

$ErrorActionPreference = "Stop"

docker compose -f docker-compose.profiling.yml up -d wallet-postgres wallet-rabbitmq

$deadline = (Get-Date).AddSeconds($WaitSeconds)
do {
    docker exec bidmart-wallet-profiling-postgres pg_isready -U bidmart -d bidmart_wallet_profiling | Out-Null
    if ($LASTEXITCODE -eq 0) {
        break
    }
    Start-Sleep -Seconds 1
} while ((Get-Date) -lt $deadline)

if ($LASTEXITCODE -ne 0) {
    throw "PostgreSQL profiling container did not become ready within $WaitSeconds seconds."
}

Get-Content profiling/seed-wallet-profiling.sql |
    docker exec -i bidmart-wallet-profiling-postgres psql -U bidmart -d bidmart_wallet_profiling

Write-Host "Wallet profiling database seeded with 10,000 wallets."
Write-Host "Run the app with: .\gradlew.bat bootRun --args='--spring.profiles.active=profiling'"
