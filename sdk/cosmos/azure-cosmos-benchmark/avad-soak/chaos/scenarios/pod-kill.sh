#!/bin/bash
# Pod Kill — kill a random CFP pod to test lease rebalancing
set -euo pipefail

NAMESPACE="${NAMESPACE:-cosmos-soak}"
COMPONENT="${COMPONENT:-avad-cfp}"
LABEL="app.kubernetes.io/component=${COMPONENT}"

echo "[$(date '+%H:%M:%S')] Chaos: pod-kill targeting $COMPONENT"

POD=$(kubectl get pods -n "$NAMESPACE" -l "$LABEL" \
    --field-selector=status.phase=Running \
    -o jsonpath='{.items[*].metadata.name}' | tr ' ' '\n' | shuf -n 1)

if [ -z "$POD" ]; then
    echo "  No running pods found for $LABEL"
    exit 0
fi

echo "  Killing pod: $POD"
kubectl delete pod "$POD" -n "$NAMESPACE" --grace-period=0 --force
echo "  Pod $POD killed"
