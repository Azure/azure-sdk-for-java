#!/usr/bin/env bash
# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#
# Local tests for resolve-cosmos-test-account.sh (no ADO required).
# Run: bash sdk/cosmos/pipeline/resolve-cosmos-test-account.tests.sh
set -uo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
SCRIPT="$HERE/resolve-cosmos-test-account.sh"
SAMPLE="$HERE/live-test-accounts.sample.json"

pass=0; fail=0
ok() { echo "  PASS: $1"; pass=$((pass+1)); }
no() { echo "  FAIL: $1"; fail=$((fail+1)); }

run() { # selector json -> sets OUT, RC (local mode)
  OUT="$(COSMOS_ACCOUNTS_LOCAL=true COSMOS_ACCOUNT_SELECTOR="$1" COSMOS_TEST_ACCOUNTS_JSON="$2" bash "$SCRIPT" 2>/dev/null)"
  RC=$?
}

SAMPLE_JSON="$(cat "$SAMPLE")"

echo "Test 1: resolves a valid selector and exports host+key (no consistency/preferred)"
run "multimaster-multiregion-session" "$SAMPLE_JSON"
if [ $RC -eq 0 ] && echo "$OUT" | grep -q "^ACCOUNT_HOST=https://REPLACE-multimaster-multiregion-session" \
   && echo "$OUT" | grep -q "^ACCOUNT_KEY=REPLACE_KEY" \
   && ! echo "$OUT" | grep -q "^ACCOUNT_CONSISTENCY=" \
   && ! echo "$OUT" | grep -q "^PREFERRED_LOCATIONS="; then ok "resolved host+key only"; else no "resolve valid selector (rc=$RC): $OUT"; fi

echo "Test 2: exports SECONDARY_ACCOUNT_KEY when present"
run "kafka-session" "$SAMPLE_JSON"
if [ $RC -eq 0 ] && echo "$OUT" | grep -q "^SECONDARY_ACCOUNT_KEY=REPLACE_SECONDARY_KEY"; then ok "secondary key exported"; else no "secondary key (rc=$RC): $OUT"; fi

echo "Test 3: unknown selector fails with non-zero"
run "does-not-exist" "$SAMPLE_JSON"
if [ $RC -ne 0 ]; then ok "unknown selector rejected"; else no "unknown selector should fail: $OUT"; fi

echo "Test 4: invalid JSON fails"
run "single-session" "{not json"
if [ $RC -ne 0 ]; then ok "invalid json rejected"; else no "invalid json should fail"; fi

echo "Test 5: unsupported version fails"
run "single-session" '{"version":99,"accounts":{"single-session":{"endpoint":"https://x","key":"k"}}}'
if [ $RC -ne 0 ]; then ok "bad version rejected"; else no "bad version should fail"; fi

echo "Test 6: missing key fails"
run "x" '{"version":1,"accounts":{"x":{"endpoint":"https://x"}}}'
if [ $RC -ne 0 ]; then ok "missing key rejected"; else no "missing key should fail"; fi

echo "Test 7: non-https endpoint fails"
run "x" '{"version":1,"accounts":{"x":{"endpoint":"http://x","key":"k"}}}'
if [ $RC -ne 0 ]; then ok "non-https rejected"; else no "non-https should fail"; fi

echo "Test 8: empty selector fails"
OUT="$(COSMOS_ACCOUNTS_LOCAL=true COSMOS_ACCOUNT_SELECTOR="" COSMOS_TEST_ACCOUNTS_JSON="$SAMPLE_JSON" bash "$SCRIPT" 2>/dev/null)"; RC=$?
if [ $RC -ne 0 ]; then ok "empty selector rejected"; else no "empty selector should fail"; fi

echo ""
echo "Results: $pass passed, $fail failed"
[ $fail -eq 0 ]
