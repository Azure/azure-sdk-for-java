#!/usr/bin/env bash
# Run Azure SDK tests in PLAYBACK mode
# Usage: ./test-playback.sh [test-class-pattern]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

TEST_PATTERN="${1:-}"

echo -e "${YELLOW}Running tests in PLAYBACK mode...${NC}"

# Restore recordings first
if [ -f "assets.json" ]; then
    echo -e "${YELLOW}Restoring session recordings...${NC}"
    test-proxy restore -a assets.json
else
    echo -e "${RED}Error: assets.json not found${NC}"
    echo "PLAYBACK mode requires recorded sessions. Run RECORD mode first."
    exit 1
fi

# Detect build system and run tests
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}Running Maven tests in PLAYBACK mode${NC}"

    if [ -n "$TEST_PATTERN" ]; then
        mvn test -DAZURE_TEST_MODE=PLAYBACK -Dtest="$TEST_PATTERN" -f pom.xml
    else
        mvn test -DAZURE_TEST_MODE=PLAYBACK -f pom.xml
    fi

elif [ -f "setup.py" ] || [ -f "pyproject.toml" ]; then
    echo -e "${GREEN}Running pytest in playback mode${NC}"

    if [ -n "$TEST_PATTERN" ]; then
        pytest --azure-test-mode=playback -k "$TEST_PATTERN"
    else
        pytest --azure-test-mode=playback
    fi

elif [ -f "*.csproj" ] 2>/dev/null; then
    echo -e "${GREEN}Running dotnet tests in Playback mode${NC}"

    if [ -n "$TEST_PATTERN" ]; then
        dotnet test /p:TestMode=Playback --filter "$TEST_PATTERN"
    else
        dotnet test /p:TestMode=Playback
    fi

elif [ -f "package.json" ]; then
    echo -e "${GREEN}Running npm tests in playback mode${NC}"

    export AZURE_TEST_MODE=playback
    npm test

else
    echo -e "${RED}No supported build system found${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}PLAYBACK mode tests completed!${NC}"
