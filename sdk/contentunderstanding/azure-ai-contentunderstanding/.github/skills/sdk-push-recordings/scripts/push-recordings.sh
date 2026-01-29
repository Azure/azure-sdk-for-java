#!/usr/bin/env bash
# Push session recordings to Azure SDK Assets repository
# Usage: ./push-recordings.sh

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}Pushing session recordings to Azure SDK Assets repo...${NC}"

# Check for assets.json
if [ ! -f "assets.json" ]; then
    echo -e "${RED}Error: assets.json not found${NC}"
    echo "This file is required to push recordings."
    exit 1
fi

# Show current tag
CURRENT_TAG=$(cat assets.json | grep -o '"Tag"[[:space:]]*:[[:space:]]*"[^"]*"' | cut -d'"' -f4)
echo -e "${YELLOW}Current tag: $CURRENT_TAG${NC}"

# Push recordings
echo -e "${YELLOW}Pushing recordings...${NC}"
test-proxy push -a assets.json

# Show new tag
NEW_TAG=$(cat assets.json | grep -o '"Tag"[[:space:]]*:[[:space:]]*"[^"]*"' | cut -d'"' -f4)
echo ""
echo -e "${GREEN}Push completed!${NC}"
echo -e "New tag: ${GREEN}$NEW_TAG${NC}"

if [ "$CURRENT_TAG" == "$NEW_TAG" ]; then
    echo -e "${YELLOW}Note: Tag unchanged (no new recordings)${NC}"
else
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "1. Verify PLAYBACK tests: mvn test -DAZURE_TEST_MODE=PLAYBACK"
    echo "2. Commit assets.json: git add assets.json && git commit -m 'Update session recordings'"
fi
