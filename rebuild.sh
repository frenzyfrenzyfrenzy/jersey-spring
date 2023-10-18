#!/bin/zsh

docker compose down
mvn clean install
docker compose up -d