#!/bin/bash
# Databricks init script: download a JAR from Azure Blob and add it to driver+executor classpath

set -euo pipefail

JAR_URL='<SAS_URI_WITH_READ_PERMISSION>'
JAR_NAME='azure-cosmos-spark_3-5_2-12-latest-ci-candidate.jar'

TARGET_DIR="/databricks/jars"
TMP_FILE="/tmp/${JAR_NAME}.$$"

echo "[init] Downloading $JAR_NAME to $TARGET_DIR ..."

mkdir -p "$TARGET_DIR"

# Download with retries; fail non-zero on HTTP errors
if command -v curl >/dev/null 2>&1; then
  curl -fL --retry 8 --retry-connrefused --retry-delay 2 --max-time 600 \
    "$JAR_URL" -o "$TMP_FILE"
elif command -v wget >/dev/null 2>&1; then
  wget --tries=10 --timeout=60 -O "$TMP_FILE" "$JAR_URL"
else
  echo "[init][error] Neither curl nor wget is available." >&2
  exit 1
fi

# Basic validity check (optional but helpful)
if command -v unzip >/dev/null 2>&1; then
  if ! unzip -tq "$TMP_FILE" >/dev/null 2>&1; then
    echo "[init][error] Downloaded file is not a valid JAR/ZIP." >&2
    rm -f "$TMP_FILE"
    exit 1
  fi
fi

# Atomic replace into /databricks/jars
chmod 0644 "$TMP_FILE"
chown root:root "$TMP_FILE"
mv -f "$TMP_FILE" "$TARGET_DIR/$JAR_NAME"

echo "[init] Installed: $TARGET_DIR/$JAR_NAME"

# Ensure classpath for both driver and executors (usually /databricks/jars is already included,
# but we also set spark-defaults to be explicit).
mkdir -p /databricks/driver/conf
SPARK_DEFAULTS="/databricks/driver/conf/00-cosmos-extra-classpath.conf"

# Use 'key value' format (spark-defaults.conf style)
cat > "$SPARK_DEFAULTS" <<EOF
spark.driver.extraClassPath /databricks/jars/$JAR_NAME
spark.executor.extraClassPath /databricks/jars/$JAR_NAME
EOF

chmod 0644 "$SPARK_DEFAULTS"
echo "[init] Wrote Spark defaults: $SPARK_DEFAULTS"

echo "[init] Done."
