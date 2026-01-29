#!/usr/bin/env bash
# Run all samples in the SDK module
# Usage: ./run-all-samples.sh [test-mode]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

TEST_MODE="${1:-PLAYBACK}"

echo -e "${YELLOW}Running ALL samples in $TEST_MODE mode${NC}"

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

# List samples that will be run
echo -e "${YELLOW}Discovering samples...${NC}"
SAMPLES=$(find src/samples -name "Sample*.java" 2>/dev/null | wc -l || echo "0")
echo -e "Found ${GREEN}$SAMPLES${NC} sample files"

# Run all samples
echo -e "${GREEN}Running Maven tests for all samples...${NC}"
mvn test -Dtest="Sample*" -DAZURE_TEST_MODE="$TEST_MODE" -f pom.xml

echo ""
echo -e "${GREEN}All samples completed!${NC}"
