#!/usr/bin/env bash
# Run Azure SDK tests in RECORD mode
# Usage: ./test-record.sh [test-class-pattern]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

TEST_PATTERN="${1:-}"

echo -e "${YELLOW}Running tests in RECORD mode...${NC}"
echo -e "${YELLOW}WARNING: This will connect to live Azure services${NC}"

# Check for Azure credentials
if [ -z "$AZURE_CLIENT_ID" ] && [ -z "$CONTENT_UNDERSTANDING_KEY" ]; then
    echo -e "${YELLOW}Tip: Run 'source sdk-setup-env/scripts/load-env.sh' to load credentials${NC}"
fi

# Restore existing recordings first
if [ -f "assets.json" ]; then
    echo -e "${YELLOW}Restoring existing recordings...${NC}"
    test-proxy restore -a assets.json 2>/dev/null || true
fi

# Detect build system and run tests
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}Running Maven tests in RECORD mode${NC}"

    if [ -n "$TEST_PATTERN" ]; then
        mvn test -DAZURE_TEST_MODE=RECORD -Dtest="$TEST_PATTERN" -f pom.xml
    else
        mvn test -DAZURE_TEST_MODE=RECORD -f pom.xml
    fi

elif [ -f "setup.py" ] || [ -f "pyproject.toml" ]; then
    echo -e "${GREEN}Running pytest in record mode${NC}"

    if [ -n "$TEST_PATTERN" ]; then
        pytest --azure-test-mode=record -k "$TEST_PATTERN"
    else
        pytest --azure-test-mode=record
    fi

elif [ -f "*.csproj" ] 2>/dev/null; then
    echo -e "${GREEN}Running dotnet tests in Record mode${NC}"

    if [ -n "$TEST_PATTERN" ]; then
        dotnet test /p:TestMode=Record --filter "$TEST_PATTERN"
    else
        dotnet test /p:TestMode=Record
    fi

elif [ -f "package.json" ]; then
    echo -e "${GREEN}Running npm tests in record mode${NC}"

    export AZURE_TEST_MODE=record
    npm test

else
    echo -e "${RED}No supported build system found${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}RECORD mode tests completed!${NC}"
echo -e "${YELLOW}Next step: Run 'test-proxy push -a assets.json' to push recordings${NC}"
