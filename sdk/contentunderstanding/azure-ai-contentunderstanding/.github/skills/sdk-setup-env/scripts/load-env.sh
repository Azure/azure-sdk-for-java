#!/usr/bin/env bash
# Load environment variables from .env file
# Usage: source load-env.sh [path/to/.env]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Find .env file
ENV_FILE="${1:-.env}"

if [ ! -f "$ENV_FILE" ]; then
    # Try to find .env in parent directories
    DIR=$(pwd)
    while [ "$DIR" != "/" ]; do
        if [ -f "$DIR/.env" ]; then
            ENV_FILE="$DIR/.env"
            break
        fi
        DIR=$(dirname "$DIR")
    done
fi

if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}Error: .env file not found${NC}"
    echo "Create a .env file with your Azure credentials"
    exit 1
fi

echo -e "${YELLOW}Loading environment from: $ENV_FILE${NC}"

# Load variables
while IFS='=' read -r key value; do
    # Skip comments and empty lines
    [[ $key =~ ^[[:space:]]*# ]] && continue
    [[ -z $key ]] && continue

    # Remove surrounding quotes from value
    value="${value%\"}"
    value="${value#\"}"
    value="${value%\'}"
    value="${value#\'}"

    # Export variable
    export "$key=$value"
    echo -e "${GREEN}✓${NC} Loaded: $key"
done < "$ENV_FILE"

echo -e "${GREEN}Environment loaded successfully!${NC}"
