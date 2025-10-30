#!/usr/bin/env bash
set -euo pipefail
export DB_URL=${DB_URL:-"jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true"}
export DB_USER=${DB_USER:-"shop"}
export DB_PASS=${DB_PASS:-"shop_pw"}
export SEED_COUNT=${SEED_COUNT:-50}
./mvnw -q -Pdev -Dexec.cleanupDaemonThreads=false exec:java
