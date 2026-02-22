#!/bin/bash
# =============================================================================
# run-netem-tests.sh — Run tc netem connection lifecycle tests
# =============================================================================
#
# The tests self-manage tc netem (add/remove delay) via Runtime.exec().
# No second terminal needed — just run this script inside the Docker container.
#
# Usage (inside container):
#   ./azure-cosmos-tests/run-netem-tests.sh
#
# Prerequisites:
#   - Container started with --cap-add=NET_ADMIN
#   - ACCOUNT_HOST and ACCOUNT_KEY env vars set
#   - SDK built on host, .m2 volume-mounted
# =============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== Cosmos DB Gateway V2 — tc netem Connection Lifecycle Tests ===${NC}"
echo ""

# Validate credentials
if [[ -z "${ACCOUNT_HOST:-}" || -z "${ACCOUNT_KEY:-}" ]]; then
    echo -e "${RED}ERROR: ACCOUNT_HOST and ACCOUNT_KEY must be set.${NC}"
    echo ""
    echo "  export ACCOUNT_HOST=\"https://<account>.documents.azure.com:443/\""
    echo "  export ACCOUNT_KEY=\"<key>\""
    exit 1
fi

echo -e "Account: ${YELLOW}${ACCOUNT_HOST}${NC}"
echo ""

# Verify tc is available and NET_ADMIN granted
if ! tc qdisc show dev eth0 &> /dev/null; then
    echo -e "${RED}ERROR: Cannot access tc. Did you run with --cap-add=NET_ADMIN?${NC}"
    exit 1
fi

echo -e "${GREEN}tc is available and NET_ADMIN granted. Tests will self-manage network delay.${NC}"
echo ""
echo -e "${GREEN}Starting tests...${NC}"
echo ""

# Pre-req: run setup-dummy-buildtools.sh inside the container to create a dummy
# sdk-build-tools artifact so checkstyle plugin dependency resolves.
# Use -U to force past any cached resolution failures.
# Activate 'thinclient' profile to enable the failsafe plugin execution,
# but override the suite XML to our manual netem tests.
mvn verify -pl azure-cosmos-tests -U -Pthinclient \
    -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true \
    -Dspotbugs.skip=true -Dcheckstyle.skip=true -Drevapi.skip=true \
    -Denforcer.skip=true -Djacoco.skip=true \
    -Dfailsafe.suiteXmlFiles=src/test/resources/manual-thinclient-network-delay-testng.xml \
    2>&1 | tee /tmp/netem-tests.log

echo ""
echo -e "${GREEN}Test log saved to /tmp/netem-tests.log${NC}"
