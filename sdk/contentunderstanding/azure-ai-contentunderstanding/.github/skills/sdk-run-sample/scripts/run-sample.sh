#!/usr/bin/env bash
# Run sample by name or pattern
# Usage: ./run-sample.sh [sample-name-or-pattern]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SAMPLE_PATTERN="${1:-Sample*}"
TEST_MODE="${AZURE_TEST_MODE:-PLAYBACK}"

echo -e "${YELLOW}Running sample(s): $SAMPLE_PATTERN${NC}"
echo -e "${YELLOW}Test mode: $TEST_MODE${NC}"

# Check for pom.xml
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}Error: pom.xml not found. Run from SDK module directory.${NC}"
    exit 1
fi

# Restore recordings for PLAYBACK mode
if [ "$TEST_MODE" == "PLAYBACK" ] && [ -f "assets.json" ]; then
    echo -e "${YELLOW}Restoring session recordings...${NC}"
    test-proxy restore -a assets.json 2>/dev/null || true
fi

# Run sample
echo -e "${GREEN}Running Maven test...${NC}"
mvn test -Dtest="$SAMPLE_PATTERN" -DAZURE_TEST_MODE="$TEST_MODE" -f pom.xml

echo ""
echo -e "${GREEN}Sample execution completed!${NC}"
