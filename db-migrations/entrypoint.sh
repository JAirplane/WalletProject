#!/bin/sh
set -e

liquibase \
  --url="$POSTGRES_DB_URL" \
  --username="$POSTGRES_USER" \
  --password="$POSTGRES_PASSWORD" \
  update