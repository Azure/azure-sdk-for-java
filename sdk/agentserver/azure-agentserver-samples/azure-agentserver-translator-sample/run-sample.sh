#!/bin/bash

../../mvnw clean package -DskipTests

export REGISTRY=${REGISTRY:-}

docker compose build
docker compose up --force-recreate -d

# wait for the container to be ready
sleep 5

echo "=== Synchronous request ==="
curl -vvv -X POST http://localhost:8088/responses \
    -H "Accept: application/json" \
    -H "Content-Type: application/json" \
    -d '{
        "input": "Hello, how are you today?",
        "model": "gpt-4o"
      }' -o - | json_pp

sleep 1

echo "=== Streaming request ==="
curl -vvv -X POST http://localhost:8088/responses \
    -H "Accept: text/event-stream" \
    -H "Content-Type: application/json" \
    -d '{
        "input": "The quick brown fox jumps over the lazy dog.",
        "model": "gpt-4o",
        "stream": true
      }' -o -

docker compose down
