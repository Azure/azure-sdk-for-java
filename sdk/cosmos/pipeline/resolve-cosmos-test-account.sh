#!/usr/bin/env bash
# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#
# Resolves a single Cosmos live-test account from the one JSON secret and exports
# ACCOUNT_HOST / ACCOUNT_KEY (and optional SECONDARY_ACCOUNT_KEY) for the tests.
#
# All Cosmos live-test matrix legs run on linux agents, so this is bash + jq.
#
# Inputs (environment variables):
#   COSMOS_TEST_ACCOUNTS_JSON  Raw JSON matching live-test-accounts.schema.json
#                              (the value of sub-config-cosmos-azure-cloud-test-resources).
#   COSMOS_ACCOUNT_SELECTOR    Logical account name to select (e.g. multimaster-multiregion-session).
#   COSMOS_ACCOUNTS_LOCAL      Optional. When "true", prints KEY=VALUE to stdout instead of
#                              emitting Azure DevOps ##vso logging commands (used for local tests).
#
# Exit codes: 0 success; non-zero on any validation failure.
set -euo pipefail

fail() { echo "ERROR: $*" >&2; exit 1; }

command -v jq >/dev/null 2>&1 || fail "jq is required but not found on PATH."

json="${COSMOS_TEST_ACCOUNTS_JSON:-}"
selector="${COSMOS_ACCOUNT_SELECTOR:-}"
local_mode="${COSMOS_ACCOUNTS_LOCAL:-false}"

[ -n "$json" ] || fail "COSMOS_TEST_ACCOUNTS_JSON is empty. Wire the sub-config-cosmos-azure-cloud-test-resources secret to this variable."
[ -n "$selector" ] || fail "COSMOS_ACCOUNT_SELECTOR is empty. Set it to a logical account name."

echo "$json" | jq empty 2>/dev/null || fail "COSMOS_TEST_ACCOUNTS_JSON is not valid JSON."

version="$(echo "$json" | jq -r '.version // empty')"
[ "$version" = "1" ] || fail "Unsupported or missing schema version '$version' (parser supports: 1)."

if [ "$(echo "$json" | jq --arg s "$selector" 'has("accounts") and (.accounts | has($s))')" != "true" ]; then
  available="$(echo "$json" | jq -r '.accounts | keys | join(", ")' 2>/dev/null || echo '<none>')"
  fail "Account selector '$selector' not found. Available: ${available}"
fi

acct="$(echo "$json" | jq -c --arg s "$selector" '.accounts[$s]')"

endpoint="$(echo "$acct" | jq -r '.endpoint // empty')"
key="$(echo "$acct" | jq -r '.key // empty')"
secondary_key="$(echo "$acct" | jq -r '.secondaryKey // empty')"

[ -n "$endpoint" ] || fail "Account '$selector' is missing required 'endpoint'."
[ -n "$key" ] || fail "Account '$selector' is missing required 'key'."
case "$endpoint" in
  https://*) : ;;
  *) fail "Account '$selector' endpoint must start with https:// (got '$endpoint')." ;;
esac

emit_public() { # name value
  if [ "$local_mode" = "true" ]; then
    echo "$1=$2"
  else
    echo "##vso[task.setvariable variable=$1;issecret=false]$2"
  fi
}
# Emit a secret using the azure-sdk double-set convention: register the literal value as
# a secret (variable _NAME) so the log scrubber masks it everywhere, AND set a plain
# variable NAME so it still auto-exports as an environment variable to the Maven test
# task. Marking a variable issecret=true alone would prevent env propagation.
emit_secret() { # name value
  if [ "$local_mode" = "true" ]; then
    echo "$1=$2"
  else
    echo "##vso[task.setvariable variable=_$1;issecret=true]$2"
    echo "##vso[task.setvariable variable=$1;issecret=false]$2"
  fi
}

# Only ACCOUNT_HOST / ACCOUNT_KEY (+ optional SECONDARY_ACCOUNT_KEY) are emitted.
# ACCOUNT_CONSISTENCY and PREFERRED_LOCATIONS are intentionally NOT set here: they are
# controlled per matrix leg (DESIRED_CONSISTENCIES / ACCOUNT_CONSISTENCY / PREFERRED_LOCATIONS)
# and the fixed accounts provisioned to match, so emitting them here would clobber
# the matrix values.
emit_public ACCOUNT_HOST "$endpoint"
emit_secret ACCOUNT_KEY "$key"
[ -n "$secondary_key" ] && emit_secret SECONDARY_ACCOUNT_KEY "$secondary_key"

# Masked, secret-free summary for logs.
echo "Resolved Cosmos test account '$selector': endpoint=$endpoint key=***" >&2
