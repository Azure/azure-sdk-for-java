#!/bin/bash

../../mvnw package -DskipTests

# Load environment variables from .env
set -a
source ../../.env
set +a

export REGISTRY=${REGISTRY:-}

docker-compose build --no-cache
docker-compose up --force-recreate -d

# wait for the container to be ready
sleep 20

curl -vvv -X POST http://localhost:8088/responses \
    -H "Accept: application/json" \
    -H "Content-Type: application/json" \
    -d '{
        "input": "What is #1234",
        "model": "gpt-4o"
      }' -o -

docker-compose down

