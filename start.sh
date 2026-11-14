#!/bin/sh
set -eu
cd "$(dirname "$0")"
npm --prefix frontend install
npm --prefix frontend run build
exec mvn -f backend/pom.xml spring-boot:run
