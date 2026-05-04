#!/bin/bash
# Restart Storm — rolling restart all CFP pods
set -euo pipefail

NAMESPACE="${NAMESPACE:-cosmos-soak}"
RELEASE="${RELEASE:-cosmos-soak}"
COMPONENT="${COMPONENT:-avad-cfp}"

echo "[$(date '+%H:%M:%S')] Chaos: restart-storm for ${RELEASE}-${COMPONENT}"
kubectl rollout restart statefulset "${RELEASE}-${COMPONENT}" -n "$NAMESPACE"
echo "  Rolling restart initiated"

# Wait for rollout to complete (with timeout)
kubectl rollout status statefulset "${RELEASE}-${COMPONENT}" \
    -n "$NAMESPACE" --timeout=600s || true
echo "  Restart storm complete"
