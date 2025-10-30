#!/usr/bin/env bash
set -euo pipefail
export DB_URL=${DB_URL:-"jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true"}
export DB_USER=${DB_USER:-"shop"}
export DB_PASS=${DB_PASS:-"shop_pw"}
./mvnw -q -Dflyway.configFiles= -Denv.DB_URL="$DB_URL" -Denv.DB_USER="$DB_USER" -Denv.DB_PASS="$DB_PASS" flyway:migrate
