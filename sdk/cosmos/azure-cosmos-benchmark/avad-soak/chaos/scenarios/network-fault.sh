#!/bin/bash
# Network Fault — block Cosmos endpoint temporarily
set -euo pipefail

NAMESPACE="${NAMESPACE:-cosmos-soak}"
COMPONENT="${COMPONENT:-avad-cfp}"
BLOCK_DURATION="${BLOCK_DURATION:-30}"  # seconds

echo "[$(date '+%H:%M:%S')] Chaos: network-fault (${BLOCK_DURATION}s block)"

# Get a random pod
POD=$(kubectl get pods -n "$NAMESPACE" \
    -l "app.kubernetes.io/component=${COMPONENT}" \
    --field-selector=status.phase=Running \
    -o jsonpath='{.items[*].metadata.name}' | tr ' ' '\n' | shuf -n 1)

if [ -z "$POD" ]; then
    echo "  No running pods found"
    exit 0
fi

echo "  Target pod: $POD"
echo "  Injecting network fault for ${BLOCK_DURATION}s"

# Block outbound to Cosmos port 443 using iptables
kubectl exec -n "$NAMESPACE" "$POD" -- \
    sh -c "iptables -A OUTPUT -p tcp --dport 443 -j DROP 2>/dev/null || echo 'iptables not available (need NET_ADMIN)'"

sleep "$BLOCK_DURATION"

# Remove the block
kubectl exec -n "$NAMESPACE" "$POD" -- \
    sh -c "iptables -D OUTPUT -p tcp --dport 443 -j DROP 2>/dev/null || true"

echo "  Network fault removed from $POD"
