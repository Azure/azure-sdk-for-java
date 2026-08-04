#!/usr/bin/env bash
# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
#
# Build (once) and run the hosted-agent deployment CLI, forwarding all arguments
# straight through to the tool. This launcher lives in the samples root and
# drives the "azure-agentserver-hosted-agent-cli" sub-project.
#
# Usage:
#   ./hosted-agent-cli.sh <command> [options]
#
# Examples:
#   ./hosted-agent-cli.sh list --endpoint "https://<account>.services.ai.azure.com/api/projects/<project>"
#
#   ./hosted-agent-cli.sh deploy \
#     --endpoint "https://<account>.services.ai.azure.com/api/projects/<project>" \
#     --name my-agent \
#     --image yourregistry.azurecr.io/my-agent:latest \
#     --model gpt-5.4 --env LOG_LEVEL=debug
#
#   ./hosted-agent-cli.sh status --endpoint "$ENDPOINT" --name my-agent
#   ./hosted-agent-cli.sh logs   --endpoint "$ENDPOINT" --name my-agent
#
# Environment variables:
#   SKIP_BUILD   If "true", skip the Maven build and use the existing jar.
#
# Config auto-detection:
#   If "azure-agentserver-hosted-agent-cli/config.yaml" exists and no "--config"
#   flag was passed, it is used automatically (equivalent to
#   "--config azure-agentserver-hosted-agent-cli/config.yaml").

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

# The CLI sub-project this launcher builds and runs.
CLI_DIR="${SCRIPT_DIR}/azure-agentserver-hosted-agent-cli"

MVNW="${SCRIPT_DIR}/../mvnw"
JAR_GLOB="${CLI_DIR}/target/azure-agentserver-hosted-agent-cli-*-jar-with-dependencies.jar"
DEFAULT_CONFIG="${CLI_DIR}/config.yaml"

# ── Build the runnable jar (unless SKIP_BUILD=true) ──────────────────
if [[ "${SKIP_BUILD:-false}" != "true" ]]; then
  echo "=== Building hosted-agent CLI ===" >&2
  "${MVNW}" -q -f "${CLI_DIR}/pom.xml" package -DskipTests
fi

# ── Locate the jar ───────────────────────────────────────────────────
JAR=""
shopt -s nullglob
for candidate in ${JAR_GLOB}; do
  JAR="${candidate}"
  break
done
shopt -u nullglob

if [[ -z "${JAR}" || ! -f "${JAR}" ]]; then
  echo "Error: runnable jar not found under target/. Run without SKIP_BUILD=true first." >&2
  exit 1
fi

# ── Auto-detect config.yaml (unless a --config flag was already given) ─
EXTRA_ARGS=()
if [[ -f "${DEFAULT_CONFIG}" ]]; then
  has_config="false"
  for arg in "$@"; do
    if [[ "${arg}" == "--config" ]]; then
      has_config="true"
      break
    fi
  done
  if [[ "${has_config}" == "false" && "$#" -gt 0 ]]; then
    echo "=== Using ${DEFAULT_CONFIG} ===" >&2
    EXTRA_ARGS=(--config "${DEFAULT_CONFIG}")
  fi
fi

# ── Run, forwarding all arguments to the CLI ─────────────────────────
exec java -jar "${JAR}" "$@" ${EXTRA_ARGS[@]+"${EXTRA_ARGS[@]}"}
