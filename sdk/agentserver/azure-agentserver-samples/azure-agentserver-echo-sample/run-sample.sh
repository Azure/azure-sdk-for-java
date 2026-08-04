#!/bin/bash

../../mvnw clean package -DskipTests

export REGISTRY=${REGISTRY:-}

docker compose build
docker compose up --force-recreate -d

# wait for the container to be ready
sleep 5

curl -vvv -X POST http://localhost:8088/responses \
    -H "Accept: application/json" \
    -H "Content-Type: application/json" \
    -d '{
        "input": "Echo this back to me.",
        "model": "gpt-4o"
      }' -o - | json_pp

sleep 1

curl -vvv -X POST http://localhost:8088/responses \
    -H "Accept: text/event-stream" \
    -H "Content-Type: application/json" \
    -d '{
        "input": "Echo this back to me.",
        "model": "gpt-4o",
        "stream": true
      }' -o -

docker-compose down
