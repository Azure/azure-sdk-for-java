#!/usr/bin/env bash
# Compile Azure SDK module
# Usage: ./compile.sh [module-path]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

MODULE_PATH="${1:-.}"
cd "$MODULE_PATH"

echo -e "${YELLOW}Detecting build system...${NC}"

# Detect build system and compile
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}Found pom.xml - Using Maven${NC}"
    mvn compile -f pom.xml -DskipTests

elif [ -f "setup.py" ] || [ -f "pyproject.toml" ]; then
    echo -e "${GREEN}Found Python project - Using pip${NC}"
    pip install -e .

elif [ -f "*.csproj" ] 2>/dev/null; then
    echo -e "${GREEN}Found .csproj - Using dotnet${NC}"
    dotnet build

elif [ -f "package.json" ]; then
    echo -e "${GREEN}Found package.json - Using npm${NC}"
    npm run build

else
    echo -e "${RED}No supported build system found${NC}"
    echo "Supported: pom.xml (Maven), setup.py/pyproject.toml (Python), *.csproj (.NET), package.json (npm)"
    exit 1
fi

echo -e "${GREEN}Compilation completed successfully!${NC}"
