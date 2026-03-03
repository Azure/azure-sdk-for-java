#!/bin/bash
# vm-prepare-and-run.sh — Runs ON the VM. Checkout, build, verify, and run benchmark for one ref.
#
# Usage (executed on VM via SSH):
#   bash vm-prepare-and-run.sh <ref> <scenario> <tenants-file> <run-name> [extra-flags...]
#
# Ref auto-detection:
#   PR#<number>   → fetches pull request
#   tag:<name>    → checks out tag
#   7-40 hex chars → commit SHA
#   anything else → branch name
#
# Exit 0 on success, 1 on failure.

set -uo pipefail

# Extract --force-scripts flag if present (can appear anywhere in args)
FORCE_SCRIPTS=false
ARGS=()
for arg in "$@"; do
  if [[ "$arg" == "--force-scripts" ]]; then
    FORCE_SCRIPTS=true
  else
    ARGS+=("$arg")
  fi
done
set -- "${ARGS[@]}"

REF="${1:?Usage: $0 <ref> <scenario> <tenants-file> <run-name> [--force-scripts] [extra-flags...]}"
SCENARIO="${2:-SIMPLE}"
TENANTS_FILE="${3:-~/tenants.json}"
RUN_NAME="${4:-$(date +%Y%m%d)-${SCENARIO}-run}"
shift 4 || true
EXTRA_FLAGS="$*"

REPO_DIR=~/azure-sdk-for-java
BENCH_DIR=$REPO_DIR/sdk/cosmos/azure-cosmos-benchmark
export PATH=/opt/apache-maven-3.9.12/bin:$PATH
MAVEN_FLAGS="-e -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcodesnippet.skip=true -Dspotbugs.skip=true -Drevapi.skip=true"

echo "============================================="
echo "  Ref: $REF"
echo "  Scenario: $SCENARIO"
echo "  Run name: $RUN_NAME"
echo "============================================="

# --- Step 1: Checkout ---
echo ""
echo "=== [1/4] Checkout: $REF ==="

if [[ ! -d "$REPO_DIR/.git" ]]; then
  echo "Cloning repo..."
  git clone --depth 1 https://github.com/Azure/azure-sdk-for-java.git "$REPO_DIR"
fi

cd "$REPO_DIR"

if [[ "$REF" =~ ^PR#([0-9]+)$ ]]; then
  PR_NUM="${BASH_REMATCH[1]}"
  git fetch origin "pull/$PR_NUM/head:pr-$PR_NUM"
  git checkout "pr-$PR_NUM"
elif [[ "$REF" =~ ^tag:(.+)$ ]]; then
  TAG="${BASH_REMATCH[1]}"
  git fetch --tags origin
  git checkout "tags/$TAG"
elif [[ "$REF" =~ ^[0-9a-f]{7,40}$ ]]; then
  git fetch origin
  git checkout "$REF"
else
  # Detect remote/branch format (e.g., xinlian12/wireConnectionSharingInBenchmark)
  if [[ "$REF" == */* ]]; then
    REMOTE_NAME="${REF%%/*}"
    BRANCH_NAME="${REF#*/}"
    if git remote | grep -qx "$REMOTE_NAME"; then
      echo "Fetching $BRANCH_NAME from remote $REMOTE_NAME"
      git fetch --depth 1 "$REMOTE_NAME" "$BRANCH_NAME"
      git checkout FETCH_HEAD
    else
      # Slash is part of the branch name (e.g., feature/foo on origin)
      git fetch --depth 1 origin "$REF"
      git checkout "$REF"
      git pull origin "$REF" 2>/dev/null || true
    fi
  else
    git fetch --depth 1 origin "$REF"
    git checkout "$REF"
    git pull origin "$REF" 2>/dev/null || true
  fi
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "detached")
COMMIT=$(git rev-parse --short HEAD)
echo "Checked out: $BRANCH @ $COMMIT"

# Resolve script directory:
#   --force-scripts → use ~/benchmark-scripts/ (for testing local script changes)
#   default         → prefer repo scripts (match the ref), fall back to ~/benchmark-scripts/
REPO_SCRIPTS_DIR="$BENCH_DIR/copilot/skills/cosmos-benchmark-run/scripts"
FALLBACK_SCRIPTS_DIR=~/benchmark-scripts
if [[ "$FORCE_SCRIPTS" == "true" ]]; then
  VM_SCRIPTS_DIR="$FALLBACK_SCRIPTS_DIR"
  echo "Using forced scripts: $VM_SCRIPTS_DIR (--force-scripts)"
elif [[ -d "$REPO_SCRIPTS_DIR" ]]; then
  VM_SCRIPTS_DIR="$REPO_SCRIPTS_DIR"
  echo "Using repo scripts: $VM_SCRIPTS_DIR"
else
  VM_SCRIPTS_DIR="$FALLBACK_SCRIPTS_DIR"
  echo "Using fallback scripts: $VM_SCRIPTS_DIR"
fi

# --- Step 2: Build ---
echo ""
echo "=== [2/4] Build ==="

cd "$REPO_DIR"
mvn -e -DskipTests -pl sdk/tools/linting-extensions install

cd "$REPO_DIR/sdk/cosmos"
mvn $MAVEN_FLAGS -pl ,azure-cosmos -am clean install
mvn $MAVEN_FLAGS -pl ,azure-cosmos-test clean install
mvn $MAVEN_FLAGS -pl ,azure-cosmos-encryption clean install
mvn $MAVEN_FLAGS -pl ,azure-cosmos-benchmark clean package -P package-assembly

JAR=$(ls "$BENCH_DIR/target/"*jar-with-dependencies.jar 2>/dev/null | head -1)
if [[ -z "$JAR" ]]; then
  echo "❌ Build failed — JAR not found"
  exit 1
fi
echo "✅ Built: $(basename $JAR)"

# --- Step 3: Verify ---
echo ""
echo "=== [3/4] Verify ==="

READY=true

if ! java -version 2>&1 | grep -qi "openjdk"; then
  echo "  ❌ JDK not found"; READY=false
else
  echo "  ✅ JDK: $(java -version 2>&1 | head -1)"
fi

TENANTS_RESOLVED=$(eval echo "$TENANTS_FILE")
if [[ -f "$TENANTS_RESOLVED" ]]; then
  echo "  ✅ Config: $TENANTS_FILE"
else
  echo "  ❌ Config: $TENANTS_FILE not found"; READY=false
fi

DISK_AVAIL=$(df -BG / | tail -1 | awk '{print $4}' | tr -d 'G')
if [[ "$DISK_AVAIL" -ge 10 ]]; then
  echo "  ✅ Disk: ${DISK_AVAIL}GB"
else
  echo "  ❌ Disk: ${DISK_AVAIL}GB (<10GB)"; READY=false
fi

if [[ "$READY" != "true" ]]; then
  echo "❌ Readiness check failed"
  exit 1
fi

# --- Step 4: Run in tmux (survives SSH disconnection) ---
echo ""
echo "=== [4/4] Run: $SCENARIO (tmux session: bench) ==="

cd "$BENCH_DIR"
RESULTS_DIR="./results/$RUN_NAME"
mkdir -p "$RESULTS_DIR"

# End any previous benchmark tmux session gracefully
tmux send-keys -t bench C-c 2>/dev/null || true
sleep 1
tmux send-keys -t bench "exit" Enter 2>/dev/null || true
sleep 1

# Write run script with resolved paths (executed inside tmux)
cat > "$RESULTS_DIR/.run.sh" <<EOF
#!/bin/bash
set -uo pipefail
cd "$BENCH_DIR"
if [[ -f "$VM_SCRIPTS_DIR/run-benchmark.sh" ]]; then
  bash "$VM_SCRIPTS_DIR/run-benchmark.sh" \\
    "$SCENARIO" "$TENANTS_RESOLVED" "$RESULTS_DIR" $EXTRA_FLAGS
else
  echo "WARNING: run-benchmark.sh not found, running JAR directly"
  JAR=\$(ls target/*jar-with-dependencies.jar 2>/dev/null | head -1)
  java -Xmx8g -Xms8g -XX:+UseG1GC \\
    -Xlog:gc*:"$RESULTS_DIR/gc.log" \\
    -jar "\$JAR" \\
    -tenantsFile "$TENANTS_RESOLVED" \\
    -reportingDirectory "$RESULTS_DIR/metrics" \\
    2>&1 | tee "$RESULTS_DIR/benchmark.log"
fi
echo \$? > "$RESULTS_DIR/.exit-code"
EOF
chmod +x "$RESULTS_DIR/.run.sh"

# Start benchmark in tmux -- process persists even if SSH disconnects
tmux new-session -d -s bench "bash '$RESULTS_DIR/.run.sh'"
echo "  Benchmark running in tmux session 'bench'"
echo "  Monitor:  tmux capture-pane -t bench -p | tail -30"

# Poll interval based on scenario duration (SIMPLE ~30min, EXPAND ~90min, CHURN varies)
case "$SCENARIO" in
  SIMPLE)  POLL_INTERVAL=120 ;;   # 2 min
  EXPAND)  POLL_INTERVAL=300 ;;   # 5 min
  CHURN)   POLL_INTERVAL=300 ;;   # 5 min
  *)       POLL_INTERVAL=120 ;;   # 2 min default
esac

# Wait for tmux session to complete
echo "  Poll interval: ${POLL_INTERVAL}s"
while tmux has-session -t bench 2>/dev/null; do
  sleep $POLL_INTERVAL
done

# Read exit code written by the run script
BENCH_EXIT=$(cat "$RESULTS_DIR/.exit-code" 2>/dev/null || echo 1)

if [[ "$BENCH_EXIT" -eq 0 ]]; then
  echo ""
  echo "Completed: $REF ($BRANCH @ $COMMIT) -> results/$RUN_NAME"
else
  echo ""
  echo "Benchmark failed (exit code: $BENCH_EXIT)"
  exit "$BENCH_EXIT"
fi
