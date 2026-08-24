#!/bin/bash

../../../mvnw package -DskipTests

# Load environment variables from .env
set -a
source ../../../.env
set +a

export REGISTRY=${REGISTRY:-}

docker-compose build --no-cache
docker-compose up --force-recreate -d

# wait for the container to be ready
sleep 20

curl -vvv -N -X POST http://localhost:8088/responses \
    -H "Content-Type: application/json" \
    -d '{
        "input": "Transfer 100 from Alice to Bob?",
        "model": "gpt-4o",
        "stream": false
      }'

curl -vvv -N -X POST http://localhost:8088/responses \
    -H "Accept: text/event-stream" \
    -H "Content-Type: application/json" \
    -d '{
        "input": "Transfer 100 from Alice to Bob?",
        "model": "gpt-4o",
        "stream": true
      }'

docker compose down

