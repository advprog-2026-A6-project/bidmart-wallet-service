# Wallet Profiling Setup

This folder contains local-only profiling support for the wallet service. It does not change wallet business logic.

## Start Local Dependencies

```powershell
docker compose -f docker-compose.profiling.yml up -d
.\profiling\reset-profiling-db.ps1
```

This starts:

- PostgreSQL on `localhost:5433`
- RabbitMQ on `localhost:5673`
- RabbitMQ management UI on `http://localhost:15673` with `bidmart` / `bidmart`

## Run Wallet With IntelliJ Profiler

Use the Spring profile:

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=profiling'
```

In IntelliJ, create/run a Spring Boot configuration for `BidmartWalletServiceApplication` and set:

```text
Active profiles: profiling
```

Then start it with IntelliJ Profiler. The wallet service runs on `http://localhost:8084`.

## JMeter

Open:

```text
profiling/wallet-jmeter-profiling.jmx
```

The plan includes these GUI listeners:

- `Summary Report`
- `Aggregate Report`
- `View Results Tree` disabled by default; enable it only for small debugging runs

For CLI runs on this machine, use JDK 21. JMeter 5.6.3 can fail with Java 23 and Groovy script errors.

```powershell
.\profiling\run-wallet-jmeter-profile.ps1
```

Default variables:

- `host=localhost`
- `port=8084`
- `threads=50`
- `rampup=10`
- `loops=20`
- `userMax=10000`

The plan spreads requests across seeded users to avoid measuring only one-wallet lock contention. For lock contention profiling, set `userMax=1`.

Each loop picks one buyer before `GET /api/wallet` and reuses that same buyer for `POST /hold` and `POST /release`. This keeps the mixed-traffic profiling run clean and avoids false `release` failures caused by changing users between samplers.

## Useful Endpoints

- `GET /api/wallet`
- `GET /api/wallet/history`
- `POST /api/wallet/hold?userId={id}&amount={amount}`
- `POST /api/wallet/release?userId={id}&amount={amount}`
- `POST /api/wallet/settle?buyerId={buyerId}&sellerId={sellerId}&amount={amount}`

Wallet read endpoints require `X-User-Id`. Auction-style internal endpoints use query parameters plus `X-Idempotency-Key`.
