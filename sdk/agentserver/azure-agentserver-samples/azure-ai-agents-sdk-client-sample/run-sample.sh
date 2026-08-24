#!/usr/bin/env bash
# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
#
# Build and run the financial-jersey hosted-agent SDK client sample.
#
# This script:
#   1. (optional) Builds and pushes the financial-jersey container image to a registry.
#   2. Builds this sample's runnable jar.
#   3. Runs the sample, which deploys the image as a hosted agent to Azure AI Foundry
#      and invokes it via the com.azure:azure-ai-agents SDK.
#
# ── Required environment variables ───────────────────────────────────
#   FOUNDRY_PROJECT_ENDPOINT        Azure AI Foundry project endpoint.
#   FOUNDRY_AGENT_CONTAINER_IMAGE   Financial-jersey container image reference (tag or digest).
#
# ── Optional environment variables ───────────────────────────────────
#   MODEL_DEPLOYMENT_NAME   Model deployment (default: gpt-5.4).
#   AGENT_NAME              Hosted-agent name (default: java-financial-jersey-sdk-sample).
#   AGENT_CPU / AGENT_MEMORY  Resource allocation (defaults: 2 / 4Gi).
#   PROMPT                  Question to send to the agent.
#   SKIP_CLEANUP            If "true", leaves the agent version and session after the run.
#   AZURE_SUBSCRIPTION_ID   Subscription used in the printed RBAC commands.
#   BUILD_IMAGE             If "true", build+push FOUNDRY_AGENT_CONTAINER_IMAGE before running.
#   IMAGE_PLATFORM          Docker build platform (default: linux/amd64).
#
# ── Usage ────────────────────────────────────────────────────────────
#   FOUNDRY_PROJECT_ENDPOINT="https://<account>.services.ai.azure.com/api/projects/<project>" \
#   FOUNDRY_AGENT_CONTAINER_IMAGE="<acr>.azurecr.io/lc4jfinancialjersey:latest" \
#   ./run-sample.sh
#
#   # Also (re)build and push the agent image first:
#   BUILD_IMAGE=true FOUNDRY_PROJECT_ENDPOINT=... FOUNDRY_AGENT_CONTAINER_IMAGE=... ./run-sample.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

MVNW="${SCRIPT_DIR}/../../mvnw"
JERSEY_SAMPLE_DIR="${SCRIPT_DIR}/../financial-agent/azure-agentserver-langchain4j-financial-jersey-sample"
IMAGE_PLATFORM="${IMAGE_PLATFORM:-linux/amd64}"

# ── Load .env (values use ${VAR:-default}, so caller env still wins) ──
if [[ -f "${SCRIPT_DIR}/.env" ]]; then
  # shellcheck disable=SC1091
  source "${SCRIPT_DIR}/.env"
fi

# ── Validate required inputs ─────────────────────────────────────────
: "${FOUNDRY_PROJECT_ENDPOINT:?Set FOUNDRY_PROJECT_ENDPOINT to your Azure AI Foundry project endpoint}"
: "${FOUNDRY_AGENT_CONTAINER_IMAGE:?Set FOUNDRY_AGENT_CONTAINER_IMAGE to the financial-jersey image reference}"

# ── (optional) Build and push the financial-jersey agent image ───────
if [[ "${BUILD_IMAGE:-false}" == "true" ]]; then
  echo "=== Building financial-jersey jar ==="
  "${MVNW}" -f "${JERSEY_SAMPLE_DIR}/pom.xml" -am package -DskipTests

  # Log in to the registry host of the image reference (assumes Azure Container Registry).
  REGISTRY_HOST="${FOUNDRY_AGENT_CONTAINER_IMAGE%%/*}"
  if [[ "${REGISTRY_HOST}" == *.azurecr.io ]]; then
    ACR_NAME="${REGISTRY_HOST%%.azurecr.io}"
    echo "=== Logging in to ACR '${ACR_NAME}' ==="
    az acr login -n "${ACR_NAME}"
  fi

  # Strip any digest suffix; build/push against the tag portion only.
  BUILD_IMAGE_REF="${FOUNDRY_AGENT_CONTAINER_IMAGE%@*}"
  echo "=== Building image ${BUILD_IMAGE_REF} (${IMAGE_PLATFORM}) ==="
  docker build --platform "${IMAGE_PLATFORM}" -t "${BUILD_IMAGE_REF}" "${JERSEY_SAMPLE_DIR}"

  echo "=== Pushing image ${BUILD_IMAGE_REF} ==="
  docker push "${BUILD_IMAGE_REF}"
fi

# ── Build this sample ────────────────────────────────────────────────
echo "=== Building SDK client sample ==="
"${MVNW}" -f "${SCRIPT_DIR}/pom.xml" package -DskipTests

JAR=""
shopt -s nullglob
for candidate in "${SCRIPT_DIR}"/target/azure-ai-agents-sdk-client-sample-*-jar-with-dependencies.jar; do
  JAR="${candidate}"
  break
done
shopt -u nullglob
if [[ -z "${JAR}" || ! -f "${JAR}" ]]; then
  echo "Error: runnable jar not found under target/." >&2
  exit 1
fi

# ── Run ──────────────────────────────────────────────────────────────
echo "=== Running SDK client sample ==="
exec java -jar "${JAR}"
