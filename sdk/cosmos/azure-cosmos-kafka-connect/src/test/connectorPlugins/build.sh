#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
module_dir="$(cd -- "$script_dir/../../.." && pwd)"
repo_dir="$(cd -- "$module_dir/../../.." && pwd)"
uuid_build="$script_dir/insert-uuid-build"
uuid_revision=ea0986d8e3ae33f0889c5aafdb7e75cd224b1ecc
shopt -s nullglob

connector_jars=()
find_connector() {
  connector_jars=()
  for jar in "$module_dir"/target/azure-cosmos-kafka-connect-3*.jar; do
    [[ "$jar" =~ -(sources|javadoc|tests)\.jar$ ]] || connector_jars+=("$jar")
  done
}
find_connector
if [[ ${#connector_jars[@]} -eq 0 ]]; then
  echo "Building the connector and its current SDK dependencies from source..."
  mvn -f "$repo_dir/pom.xml" -pl com.azure.cosmos.kafka:azure-cosmos-kafka-connect -am \
    -Pbuild-kafka package -DskipTests=true -Dmaven.javadoc.skip=true
  find_connector
fi
if [[ ${#connector_jars[@]} -ne 1 ]]; then
  echo "Expected exactly one packaged 3.x connector in $module_dir/target." >&2
  exit 1
fi
if [[ -e "$uuid_build" ]]; then
  echo "Remove or relocate the existing $uuid_build before running integration tests." >&2
  exit 1
fi

# The outer integration-test build already packaged the connector using its CI dependencies.
rm -rf -- "$script_dir/connectors"
mkdir -p "$script_dir/connectors"
cp -- "${connector_jars[0]}" "$script_dir/connectors/"

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
cp -- "${uuid_jars[0]}" "$script_dir/connectors/"
