# Cosmos Java SDK Release Instructions

This file teaches Copilot how to perform Cosmos Java SDK releases. When a user asks to
release a Cosmos package or group of packages, follow the workflows below.

The user will specify the version in their command (e.g., "release spark connector 4.43.0 GA").
Use that version directly — do not prompt for it or look it up.

## Release Groups

| Group | Packages | Notes |
|-------|----------|-------|
| **Spark connector** | `azure-cosmos-spark_3-3_2-12`, `azure-cosmos-spark_3-4_2-12`, `azure-cosmos-spark_3-5_2-12`, `azure-cosmos-spark_3-5_2-13`, `azure-cosmos-spark_4-0_2-13` | All released at the same version simultaneously |
| **Cosmos Java SDK** | `azure-cosmos`, optionally `azure-cosmos-encryption` | |
| **Kafka connector** | `azure-cosmos-kafka-connect` | |
| **Single package** | Any individual cosmos package | |

When the user says "release spark connector", release all 5 spark packages.
When the user says "release cosmos Java SDK", release azure-cosmos (ask if azure-cosmos-encryption should be included).

## Step 1: Run Prepare-Release.ps1

For each package in the release, run the `Prepare-Release.ps1` script. The script handles:
changelog updates, pom.xml version updates, `eng/versioning/version_client.txt` updates,
and dependent pom.xml updates (e.g., `azure-cosmos-spark-account-data-resolver-sample/pom.xml`,
`fabric-cosmos-spark-auth_3/pom.xml`).

The script can be run non-interactively by piping answers:

```bash
echo -e "{GROUP_ID}\n{VERSION}\n{RELEASE_DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 {PACKAGE_NAME} cosmos"
```

Where:
- **{GROUP_ID}**: `com.azure.cosmos.spark` for spark connectors, `com.azure` for core packages
- **{VERSION}**: The version from the user's command (e.g., `4.43.0`)
- **{RELEASE_DATE}**: In `MM/dd/yyyy` format — ask the user if not specified
- The final `n` answers "no" to the prompt about replacing an existing changelog entry title

For **Spark connector group releases**, run for each of the 5 packages:
```bash
echo -e "com.azure.cosmos.spark\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos-spark_3-3_2-12 cosmos"
echo -e "com.azure.cosmos.spark\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos-spark_3-4_2-12 cosmos"
echo -e "com.azure.cosmos.spark\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos-spark_3-5_2-12 cosmos"
echo -e "com.azure.cosmos.spark\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos-spark_3-5_2-13 cosmos"
echo -e "com.azure.cosmos.spark\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos-spark_4-0_2-13 cosmos"
```

After each run, check the tail of the output for `Some changes were made to the repo source` to confirm success.

## Step 2: Manual README Updates (Spark Connector Only)

After `Prepare-Release.ps1` completes for all packages, the following files need manual updates.

### 2a. Version Compatibility Tables

Each spark connector README contains version compatibility tables for **itself and cross-references
to other connectors**. Add a new row as the **first data row** (right after the header separator
`|---|...`) in every table.

**IMPORTANT**: Always copy the format AND values from the topmost existing row, only changing the
version number. The Databricks runtimes, Fabric runtimes, and Spark version ranges evolve over time
— do NOT use hardcoded values from this document. Look at the current top row of each table.

#### Files and tables to update:

| README file | Contains tables for |
|-------------|-------------------|
| `azure-cosmos-spark_3-3_2-12/README.md` | `#### azure-cosmos-spark_3-3_2-12`, `#### azure-cosmos-spark_3-4_2-12`, `#### azure-cosmos-spark_3-5_2-12`, `#### azure-cosmos-spark_3-5_2-13`, `#### azure-cosmos-spark_4-0_2-13` |
| `azure-cosmos-spark_3-4_2-12/README.md` | `#### azure-cosmos-spark_3-4_2-12`, `#### azure-cosmos-spark_3-3_2-12`, `#### azure-cosmos-spark_3-5_2-12`, `#### azure-cosmos-spark_3-5_2-13`, `#### azure-cosmos-spark_4-0_2-13` |
| `azure-cosmos-spark_3-5_2-12/README.md` | `#### azure-cosmos-spark_3-5_2-12`, `#### azure-cosmos-spark_3-4_2-12`, `#### azure-cosmos-spark_3-3_2-12`, `#### azure-cosmos-spark_3-5_2-13`, `#### azure-cosmos-spark_4-0_2-13` |
| `azure-cosmos-spark_3-5_2-13/README.md` | `#### azure-cosmos-spark_3-5_2-13`, `#### azure-cosmos-spark_3-5_2-12`, `#### azure-cosmos-spark_3-4_2-12`, `#### azure-cosmos-spark_3-3_2-12`, `#### azure-cosmos-spark_4-0_2-13` |
| `azure-cosmos-spark_4-0_2-13/README.md` | `#### azure-cosmos-spark_4-0_2-13`, `#### azure-cosmos-spark_3-5_2-13`, `#### azure-cosmos-spark_3-5_2-12` |

Total: **4 READMEs × 5 tables + 1 README × 3 tables = 23 table row insertions**.

#### Table column differences by connector:

| Table header for | Columns |
|-----------------|---------|
| `_3-3_2-12` | Connector, Supported Spark Versions, Supported JVM Versions, Supported Scala Versions, Supported Databricks Runtimes (5 cols) |
| `_3-4_2-12` | Same as 3-3 + Supported Fabric Runtimes (6 cols, Fabric usually empty) |
| `_3-5_2-12` | Connector, Supported Spark Versions, **Minimum Java Version**, Supported Scala Versions, Supported Databricks Runtimes, Supported Fabric Runtimes (6 cols) |
| `_3-5_2-13` | Same structure as 3-5_2-12 but Scala `2.13` |
| `_4-0_2-13` | Same structure as 3-5 but Java `[17, 21]`, Scala `2.13` |

### 2b. Download Section Updates

Each spark connector README has a `### Download` section with a Maven coordinate and SBT dependency.
Update the version number in both places. Find the inline code and `libraryDependencies` lines
and replace the old version with the new one.

**Important**: Each README's Download section references **its own** artifact.

### 2c. Quick-Start Doc Updates

Update `sdk/cosmos/azure-cosmos-spark_3/docs/quick-start.md`:

Five version references to update — search for the old version and replace with the new one:

```
For Spark 3.3:
  ...azure-cosmos-spark_3-3_2-12:{VERSION}...

For Spark 3.4:
  ...azure-cosmos-spark_3-4_2-12:{VERSION}...

For Spark 3.5:
  ...azure-cosmos-spark_3-5_2-12:{VERSION}...

For Spark 3.5 (Scala 2.13):
  ...azure-cosmos-spark_3-5_2-13:{VERSION}...

For Spark 4.0:
  ...azure-cosmos-spark_4-0_2-13:{VERSION}...
```

Each line contains the version twice: once in the link text and once in the URL.

## Step 3: Verification

After all edits, run these checks:

```bash
# 1. Check all changed files (~19 expected for full Spark release)
git diff --stat

# 2. Verify version_client.txt
grep "azure-cosmos-spark" eng/versioning/version_client.txt

# 3. Verify CHANGELOGs
for pkg in azure-cosmos-spark_3-3_2-12 azure-cosmos-spark_3-4_2-12 azure-cosmos-spark_3-5_2-12 azure-cosmos-spark_3-5_2-13 azure-cosmos-spark_4-0_2-13; do
  echo "=== $pkg ===" && head -5 sdk/cosmos/$pkg/CHANGELOG.md
done

# 4. Verify new version in READMEs and quick-start
grep -c "{NEW_VERSION}" sdk/cosmos/azure-cosmos-spark_3-3_2-12/README.md \
  sdk/cosmos/azure-cosmos-spark_3-4_2-12/README.md \
  sdk/cosmos/azure-cosmos-spark_3-5_2-12/README.md \
  sdk/cosmos/azure-cosmos-spark_3-5_2-13/README.md \
  sdk/cosmos/azure-cosmos-spark_4-0_2-13/README.md \
  sdk/cosmos/azure-cosmos-spark_3/docs/quick-start.md
```

## Cosmos Java SDK Release Workflow

For `azure-cosmos` (and optionally `azure-cosmos-encryption`):

1. Run `Prepare-Release.ps1`:
   ```bash
   echo -e "com.azure\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos cosmos"
   echo -e "com.azure\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos-encryption cosmos"
   ```
2. No README table updates needed — the Cosmos Java SDK README uses Azure BOM references.
3. Verify CHANGELOG.md and pom.xml updates.

## Kafka Connector Release Workflow

For `azure-cosmos-kafka-connect`:

1. Run `Prepare-Release.ps1`:
   ```bash
   echo -e "com.azure\n{VERSION}\n{DATE}\nn" | pwsh -Command "./eng/common/scripts/Prepare-Release.ps1 azure-cosmos-kafka-connect cosmos"
   ```
2. No README table updates needed — the version in README is managed by x-version-update tags.
3. Verify CHANGELOG.md and pom.xml updates.

## Reference: Example PRs

- [PR #46852](https://github.com/Azure/azure-sdk-for-java/pull/46852) — Spark connector 4.40.0 release (13 files changed)
