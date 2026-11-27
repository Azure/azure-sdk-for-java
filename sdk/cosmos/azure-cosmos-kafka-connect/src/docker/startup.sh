#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
module_dir="$(cd -- "$script_dir/../.." && pwd)"
compose_file="$script_dir/docker-compose.yml"
uuid_build="$script_dir/insert-uuid-build"
uuid_revision=ea0986d8e3ae33f0889c5aafdb7e75cd224b1ecc

docker compose version
if [[ -e "$uuid_build" ]]; then
  echo "Remove or relocate the existing $uuid_build before starting." >&2
  exit 1
fi

echo "Building the shaded Cosmos DB connector..."
mvn -f "$module_dir/pom.xml" -Pbuild-kafka clean package -DskipTests=true

shopt -s nullglob
cosmos_jars=()
for jar in "$module_dir"/target/azure-cosmos-kafka-connect-3*.jar; do
  [[ "$jar" =~ -(sources|javadoc|tests)\.jar$ ]] || cosmos_jars+=("$jar")
done
if [[ ${#cosmos_jars[@]} -ne 1 ]]; then
  echo "Expected exactly one shaded 3.x connector JAR in $module_dir/target." >&2
  exit 1
fi

echo "Building the Insert UUID SMT against Kafka Connect 4.2.2..."
trap 'rm -rf -- "$uuid_build"' EXIT
git clone --quiet --no-checkout https://github.com/confluentinc/kafka-connect-insert-uuid.git "$uuid_build"
git -C "$uuid_build" checkout --quiet --detach "$uuid_revision"
mvn -f "$uuid_build/pom.xml" clean package -DskipTests=true -Dkafkaconnect.version=4.2.2
uuid_jars=("$uuid_build"/target/InsertUuid-*.jar)
if [[ ${#uuid_jars[@]} -ne 1 ]]; then
  echo "Expected exactly one Insert UUID SMT JAR." >&2
  exit 1
fi

echo "Stopping only this demo's services (including old optional services)..."
docker compose -f "$compose_file" down --remove-orphans
rm -rf -- "$script_dir/connectors/cosmos" "$script_dir/connectors/insert-uuid"
rm -f -- "$script_dir"/connectors/azure-cosmos-kafka-connect-*.jar "$script_dir"/connectors/InsertUuid-*.jar
mkdir -p "$script_dir/connectors/cosmos" "$script_dir/connectors/insert-uuid"
cp -- "${cosmos_jars[0]}" "$script_dir/connectors/cosmos/"
cp -- "${uuid_jars[0]}" "$script_dir/connectors/insert-uuid/"

echo "Building and starting the three-service demo..."
docker compose -f "$compose_file" build connect
docker compose -f "$compose_file" up -d
echo "Services started; follow docs/Confluent_Platform_Setup.md to verify readiness and data flow."
