#!/bin/bash
# Node Drain — cordon and drain an AKS node
set -euo pipefail

echo "[$(date '+%H:%M:%S')] Chaos: node-drain"

# Find a node running CFP pods
NAMESPACE="${NAMESPACE:-cosmos-soak}"
COMPONENT="${COMPONENT:-avad-cfp}"

NODE=$(kubectl get pods -n "$NAMESPACE" \
    -l "app.kubernetes.io/component=${COMPONENT}" \
    --field-selector=status.phase=Running \
    -o jsonpath='{.items[0].spec.nodeName}')

if [ -z "$NODE" ]; then
    echo "  No nodes found running $COMPONENT pods"
    exit 0
fi

echo "  Draining node: $NODE"
kubectl cordon "$NODE"
kubectl drain "$NODE" --ignore-daemonsets --delete-emptydir-data \
    --timeout=300s --force || true

echo "  Node $NODE drained. Waiting 120s before uncordoning..."
sleep 120

kubectl uncordon "$NODE"
echo "  Node $NODE uncordoned"
