#!/bin/bash
# generate-tenants.sh — Generate tenants.json from clientHostAndKey.txt
#
# Usage:
#   ./generate-tenants.sh --config-dir <path> [--output tenants.json] [--operation ReadThroughput]
#                         [--connection-mode GATEWAY] [--concurrency 20] [--copy-to-vm]
#
# Reads $CONFIG_DIR/clientHostAndKey.txt (format: name,endpoint,key per line)
# Generates tenants.json with globalDefaults and tenant entries.

set -euo pipefail

CONFIG_DIR=""
OUTPUT="tenants.json"
OPERATION="ReadThroughput"
CONNECTION_MODE="GATEWAY"
CONCURRENCY="20"
NUM_OPERATIONS="100000"
NUM_PRECREATED="1000"
COPY_TO_VM=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --config-dir)       CONFIG_DIR="$2"; shift 2 ;;
    --output)           OUTPUT="$2"; shift 2 ;;
    --operation)        OPERATION="$2"; shift 2 ;;
    --connection-mode)  CONNECTION_MODE="$2"; shift 2 ;;
    --concurrency)      CONCURRENCY="$2"; shift 2 ;;
    --num-operations)   NUM_OPERATIONS="$2"; shift 2 ;;
    --num-precreated)   NUM_PRECREATED="$2"; shift 2 ;;
    --copy-to-vm)       COPY_TO_VM=true; shift ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$CONFIG_DIR" ]]; then
  echo "Usage: $0 --config-dir <path> [options]" >&2
  exit 1
fi

INPUT="$CONFIG_DIR/clientHostAndKey.txt"
if [[ ! -f "$INPUT" ]]; then
  echo "ERROR: $INPUT not found" >&2
  exit 1
fi

# Build tenants array
TENANTS=""
INDEX=0
while IFS=',' read -r NAME ENDPOINT KEY; do
  [[ -z "$NAME" ]] && continue
  if [[ $INDEX -gt 0 ]]; then
    TENANTS="$TENANTS,"
  fi
  TENANTS="$TENANTS
    {
      \"id\": \"tenant-$INDEX\",
      \"serviceEndpoint\": \"$ENDPOINT\",
      \"masterKey\": \"$KEY\",
      \"databaseId\": \"benchdb\",
      \"containerId\": \"benchcol\"
    }"
  INDEX=$((INDEX + 1))
done < "$INPUT"

# Write tenants.json
cat > "$OUTPUT" << EOF
{
  "globalDefaults": {
    "connectionMode": "$CONNECTION_MODE",
    "consistencyLevel": "SESSION",
    "concurrency": "$CONCURRENCY",
    "numberOfOperations": "$NUM_OPERATIONS",
    "operation": "$OPERATION",
    "numberOfPreCreatedDocuments": "$NUM_PRECREATED",
    "connectionSharingAcrossClientsEnabled": "false",
    "maxConnectionPoolSize": "1000",
    "applicationName": "cosmos-bench"
  },
  "tenants": [$TENANTS
  ]
}
EOF

echo "Generated $OUTPUT with $INDEX tenant(s)"
echo "  Operation: $OPERATION"
echo "  Connection mode: $CONNECTION_MODE"
echo "  Concurrency: $CONCURRENCY"

# Copy to VM if requested
if [[ "$COPY_TO_VM" == "true" ]]; then
  VM_IP=$(cat "$CONFIG_DIR/vm-ip")
  VM_USER=$(cat "$CONFIG_DIR/vm-user")
  VM_KEY=$(cat "$CONFIG_DIR/vm-key")
  scp -i "$VM_KEY" -o StrictHostKeyChecking=no "$OUTPUT" "$VM_USER@$VM_IP:~/tenants.json"
  echo "Copied to VM: ~/tenants.json"
fi
