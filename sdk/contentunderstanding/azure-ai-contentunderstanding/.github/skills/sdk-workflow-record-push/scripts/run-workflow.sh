#!/usr/bin/env bash
# SDK Workflow: Record and Push
# Complete workflow to record tests and push to assets repo
# Usage: ./run-workflow.sh

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILLS_DIR="$(dirname "$SCRIPT_DIR")/.."

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     SDK Workflow: Record and Push                          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Step 1: Load Environment
echo -e "${YELLOW}━━━ Step 1/5: Load Environment ━━━${NC}"
if [ -f "$SKILLS_DIR/sdk-setup-env/scripts/load-env.sh" ]; then
    source "$SKILLS_DIR/sdk-setup-env/scripts/load-env.sh"
else
    echo -e "${YELLOW}Warning: load-env.sh not found, using existing environment${NC}"
fi
echo -e "${GREEN}✓ Step 1 complete${NC}"
echo ""

# Step 2: Compile SDK
echo -e "${YELLOW}━━━ Step 2/5: Compile SDK ━━━${NC}"
mvn compile -f pom.xml -DskipTests -q
echo -e "${GREEN}✓ Step 2 complete${NC}"
echo ""

# Step 3: Run RECORD Mode Tests
echo -e "${YELLOW}━━━ Step 3/5: Run RECORD Mode Tests ━━━${NC}"
echo -e "${YELLOW}⚠ Connecting to live Azure services...${NC}"
mvn test -DAZURE_TEST_MODE=RECORD -f pom.xml
echo -e "${GREEN}✓ Step 3 complete${NC}"
echo ""

# Step 4: Push Recordings
echo -e "${YELLOW}━━━ Step 4/5: Push Recordings ━━━${NC}"
if [ -f "assets.json" ]; then
    OLD_TAG=$(grep -o '"Tag"[[:space:]]*:[[:space:]]*"[^"]*"' assets.json | cut -d'"' -f4)
    test-proxy push -a assets.json
    NEW_TAG=$(grep -o '"Tag"[[:space:]]*:[[:space:]]*"[^"]*"' assets.json | cut -d'"' -f4)
    echo -e "Old tag: $OLD_TAG"
    echo -e "New tag: ${GREEN}$NEW_TAG${NC}"
else
    echo -e "${RED}Error: assets.json not found${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Step 4 complete${NC}"
echo ""

# Step 5: Verify with PLAYBACK
echo -e "${YELLOW}━━━ Step 5/5: Verify with PLAYBACK ━━━${NC}"
test-proxy restore -a assets.json
mvn test -DAZURE_TEST_MODE=PLAYBACK -f pom.xml
echo -e "${GREEN}✓ Step 5 complete${NC}"
echo ""

# Summary
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     Workflow Complete!                                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✓ All tests recorded and verified${NC}"
echo -e "${GREEN}✓ Recordings pushed to assets repo${NC}"
echo -e "${GREEN}✓ New tag: $NEW_TAG${NC}"
echo ""
echo -e "${YELLOW}Next step: Commit assets.json to your branch${NC}"
echo -e "  git add assets.json"
echo -e "  git commit -m 'Update session recordings'"
