# Bidmart Wallet Service

## Serenity Report

Serenity test reports are uploaded as a GitHub Actions artifact on every push to `main`.

Report artifact:
https://github.com/advprog-2026-A6-project/bidmart-wallet-service/actions/workflows/ci-cd-wallet.yml

How to open it:

1. Open the latest successful `main` run in the CI/CD Pipeline workflow.
2. Scroll to **Artifacts**.
3. Download **serenity-report**.
4. Open `index.html` from the downloaded report.

Local report path after running tests:

```bash
target/site/serenity/index.html
```
