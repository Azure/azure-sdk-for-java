# PR 49483 script test summary

Test target: `eng/scripts/Automation-Sdk-GetSDKChanges.ps1`

## Follow-up script fix

After the initial test pass, the script was updated to make the JAR build explicit and fail-fast:

- Added an explicit **Step 2** that runs `mvn clean package` before both changelog generation and Revapi.
- Kept the build aligned with the existing eng command shape while skipping unrelated work:
  - `-Dgpg.skip`
  - `-Dmaven.javadoc.skip=true`
  - `-DskipTests`
  - `-DskipTestCompile`
  - `-Djacoco.skip`
  - `-Drevapi.skip=true`
- Forced Revapi back on with `-Drevapi.skip=false` so package-level pom overrides do not silently disable breaking-change detection.
- Changed failure handling so a package build failure exits non-zero and writes **no** output JSON.

Validated updated behavior:

### 0. Updated happy path

- Package: `sdk\dns\azure-resourcemanager-dns`
- Result:
  - local JAR is built before changelog generation
  - local JAR is built before Revapi
  - script still completes successfully and writes JSON output

Updated script console output:

```text
========================================
Azure SDK Get Changes Tool
========================================

Step 1: Reading package information from POM...
  Group ID: com.azure.resourcemanager
  Artifact ID: azure-resourcemanager-dns

Step 2: Building local package JAR...
Running: mvn --no-transfer-progress clean package -f C:\github\azure-sdk-for-java\sdk\dns\azure-resourcemanager-dns -Dgpg.skip -Dmaven.javadoc.skip=true -DskipTests -DskipTestCompile -Djacoco.skip -Drevapi.skip=true
  Package JAR built successfully

Step 3: Fetching latest stable released version from Maven Central...
  Latest version: 2.53.8

Step 4: Downloading released JAR from Maven Central...
Downloading JAR from: https://repo1.maven.org/maven2/com/azure/resourcemanager/azure-resourcemanager-dns/2.53.8/azure-resourcemanager-dns-2.53.8.jar

Step 5: Locating built JAR...
  New JAR: ...\target\azure-resourcemanager-dns-2.54.0-beta.1.jar

Step 6: Generating changelog content...
  Changelog content generated

Step 7: Running revapi:check to detect breaking changes...
Running: mvn --no-transfer-progress revapi:check -f C:\github\azure-sdk-for-java\sdk\dns\azure-resourcemanager-dns -Dgpg.skip -Dmaven.javadoc.skip=true -DskipTests -Drevapi.skip=false
  revapi:check passed - no breaking changes detected
  No breaking changes detected

Step 8: Writing output JSON...
✅ SDK changes computed successfully!
```

### 0b. Updated build-failure path

- Injected a temporary compile error into `DnsZoneManager.java`
- Result:
  - script exits with error code `1`
  - script does **not** create the output JSON file
  - script stops before changelog generation and Revapi

Updated script console output:

```text
========================================
Azure SDK Get Changes Tool
========================================

Step 1: Reading package information from POM...
  Group ID: com.azure.resourcemanager
  Artifact ID: azure-resourcemanager-dns

Step 2: Building local package JAR...
Running: mvn --no-transfer-progress clean package -f C:\github\azure-sdk-for-java\sdk\dns\azure-resourcemanager-dns -Dgpg.skip -Dmaven.javadoc.skip=true -DskipTests -DskipTestCompile -Djacoco.skip -Drevapi.skip=true
Package build failed with exit code 1
...
An error occurred: Failed to build package JAR.
...
OUTPUT_JSON_MISSING
```

### 0c. Updated `src/test` error path

- Injected a temporary compile error into `src\test\java\com\azure\resourcemanager\dns\DnsTestBase.java`
- Result:
  - script still exits successfully
  - script still creates the output JSON file
  - changelog generation and Revapi still run
  - this is expected because the build step uses `-DskipTestCompile` and test execution is skipped

Additional Maven evidence from the package-build step:

```text
--- compiler:3.14.0:testCompile (default-testCompile) @ azure-resourcemanager-dns ---
Not compiling test sources

--- compiler:3.14.0:testCompile (base-testCompile) @ azure-resourcemanager-dns ---
Not compiling test sources
```

Updated script console output:

```text
========================================
Azure SDK Get Changes Tool
========================================

Step 1: Reading package information from POM...
  Group ID: com.azure.resourcemanager
  Artifact ID: azure-resourcemanager-dns

Step 2: Building local package JAR...
Running: mvn --no-transfer-progress clean package -f C:\github\azure-sdk-for-java\sdk\dns\azure-resourcemanager-dns -Dgpg.skip -Dmaven.javadoc.skip=true -DskipTests -DskipTestCompile -Djacoco.skip -Drevapi.skip=true
  Package JAR built successfully

Step 3: Fetching latest stable released version from Maven Central...
  Latest version: 2.53.8

Step 4: Downloading released JAR from Maven Central...
Downloading JAR from: https://repo1.maven.org/maven2/com/azure/resourcemanager/azure-resourcemanager-dns/2.53.8/azure-resourcemanager-dns-2.53.8.jar
  Downloaded to: C:\Users\weidxu\AppData\Local\Temp\azure-sdk-changes-a094b38a-4160-48ec-954d-1f6afcdecb1d\azure-resourcemanager-dns-2.53.8.jar

Step 5: Locating built JAR...
  New JAR: C:\github\azure-sdk-for-java\sdk\dns\azure-resourcemanager-dns\target\azure-resourcemanager-dns-2.54.0-beta.1.jar

Step 6: Generating changelog content...
  Changelog content generated

Step 7: Running revapi:check to detect breaking changes...
Running: mvn --no-transfer-progress revapi:check -f C:\github\azure-sdk-for-java\sdk\dns\azure-resourcemanager-dns -Dgpg.skip -Dmaven.javadoc.skip=true -DskipTests -Drevapi.skip=false
  revapi:check passed - no breaking changes detected
  No breaking changes detected

Step 8: Writing output JSON...
✅ SDK changes computed successfully!
OUTPUT_JSON_PRESENT
```

JSON output:

```json
{
  "hasBreakingChange": false,
  "changelogMD": "### Breaking Changes\n\n#### `models.ZoneListResult` was removed\n\n#### `models.RecordSetListResult` was removed\n\n#### `models.DnsResourceReference` was modified\n\n* `DnsResourceReference()` was changed to private access\n* `withTargetResource(com.azure.core.management.SubResource)` was removed\n* `withDnsResources(java.util.List)` was removed\n\n### Features Added\n\n* `models.TlsaRecord` was added\n\n* `models.NaptrRecord` was added\n\n* `models.DelegationSignerInfo` was added\n\n* `models.SigningKey` was added\n\n* `models.Digest` was added\n\n* `models.DsRecord` was added\n\n#### `models.RecordType` was modified\n\n* `TLSA` was added\n* `DS` was added\n* `NAPTR` was added\n\n"
}
```

## Package selection

- Used `sdk\dns\azure-resourcemanager-dns` instead. It:
  - is an `azure-resourcemanager-` package,
  - has `tsp-location.yaml`,
  - does **not** override `revapi.skip` to `true` in its package pom.

## Cases tested

### 1. Baseline: no temporary package edit

- Package: `sdk\dns\azure-resourcemanager-dns`
- Built local jar successfully.
- Script output file: `dns-baseline.json`
- Script console output:

```text
========================================
Azure SDK Get Changes Tool
========================================

Step 1: Reading package information from POM...
  Group ID: com.azure.resourcemanager
  Artifact ID: azure-resourcemanager-dns

Step 2: Fetching latest stable released version from Maven Central...
  Latest version: 2.53.8

Step 3: Downloading released JAR from Maven Central...
Downloading JAR from: https://repo1.maven.org/maven2/com/azure/resourcemanager/azure-resourcemanager-dns/2.53.8/azure-resourcemanager-dns-2.53.8.jar

Step 4: Locating built JAR...
  New JAR: ...\target\azure-resourcemanager-dns-2.54.0-beta.1.jar

Step 5: Generating changelog content...
  Changelog content generated

Step 6: Running revapi:check to detect breaking changes...
  revapi:check passed - no breaking changes detected
  No breaking changes detected

Step 7: Writing output JSON...
✅ SDK changes computed successfully!
```
- Result:
  - `hasBreakingChange: false`
  - `changelogMD`: non-empty

JSON output:

```json
{
  "hasBreakingChange": false,
  "changelogMD": "### Breaking Changes\n\n#### `models.ZoneListResult` was removed\n\n#### `models.RecordSetListResult` was removed\n\n#### `models.DnsResourceReference` was modified\n\n* `DnsResourceReference()` was changed to private access\n* `withDnsResources(java.util.List)` was removed\n* `withTargetResource(com.azure.core.management.SubResource)` was removed\n\n### Features Added\n\n* `models.TlsaRecord` was added\n\n* `models.NaptrRecord` was added\n\n* `models.DelegationSignerInfo` was added\n\n* `models.SigningKey` was added\n\n* `models.Digest` was added\n\n* `models.DsRecord` was added\n\n#### `models.RecordType` was modified\n\n* `TLSA` was added\n* `DS` was added\n* `NAPTR` was added\n\n"
}
```

Observed changelog content already includes existing differences between local `2.54.0-beta.1` and Maven Central `2.53.8`, including:

- removed `models.ZoneListResult`
- removed `models.RecordSetListResult`
- modified `models.DnsResourceReference`
- added several DNS record-related models and enum values

This means the script is comparing the current package against the latest released stable jar as designed, not against the current workspace baseline.

### 2. Non-breaking change

Temporary code change:

- Added `public String serviceName()` to `com.azure.resourcemanager.dns.DnsZoneManager`

Script output file: `dns-nonbreaking.json`

Script console output:

```text
========================================
Azure SDK Get Changes Tool
========================================

Step 1: Reading package information from POM...
  Group ID: com.azure.resourcemanager
  Artifact ID: azure-resourcemanager-dns

Step 2: Fetching latest stable released version from Maven Central...
  Latest version: 2.53.8

Step 3: Downloading released JAR from Maven Central...
Downloading JAR from: https://repo1.maven.org/maven2/com/azure/resourcemanager/azure-resourcemanager-dns/2.53.8/azure-resourcemanager-dns-2.53.8.jar

Step 4: Locating built JAR...
  New JAR: ...\target\azure-resourcemanager-dns-2.54.0-beta.1.jar

Step 5: Generating changelog content...
  Changelog content generated

Step 6: Running revapi:check to detect breaking changes...
  revapi:check passed - no breaking changes detected
  No breaking changes detected

Step 7: Writing output JSON...
✅ SDK changes computed successfully!
```

Result:

- `hasBreakingChange: false`
- `changelogMD`: still contains the existing baseline entries, plus:
  - `DnsZoneManager` was modified
  - `serviceName()` was added

JSON output:

```json
{
  "hasBreakingChange": false,
  "changelogMD": "### Breaking Changes\n\n#### `models.ZoneListResult` was removed\n\n#### `models.RecordSetListResult` was removed\n\n#### `models.DnsResourceReference` was modified\n\n* `DnsResourceReference()` was changed to private access\n* `withTargetResource(com.azure.core.management.SubResource)` was removed\n* `withDnsResources(java.util.List)` was removed\n\n### Features Added\n\n* `models.TlsaRecord` was added\n\n* `models.NaptrRecord` was added\n\n* `models.DelegationSignerInfo` was added\n\n* `models.SigningKey` was added\n\n* `models.Digest` was added\n\n* `models.DsRecord` was added\n\n#### `DnsZoneManager` was modified\n\n* `serviceName()` was added\n\n#### `models.RecordType` was modified\n\n* `TLSA` was added\n* `DS` was added\n* `NAPTR` was added\n\n"
}
```

Conclusion:

- The script correctly surfaced the additive API change in changelog output.
- Revapi correctly kept `hasBreakingChange` as `false`.

### 3. Breaking change

Temporary code change:

- Changed `DnsZoneManager.zones()` to `DnsZoneManager.zones(boolean refresh)`

Script output file: `dns-breaking.json`

Script console output:

```text
========================================
Azure SDK Get Changes Tool
========================================

Step 1: Reading package information from POM...
  Group ID: com.azure.resourcemanager
  Artifact ID: azure-resourcemanager-dns

Step 2: Fetching latest stable released version from Maven Central...
  Latest version: 2.53.8

Step 3: Downloading released JAR from Maven Central...
Downloading JAR from: https://repo1.maven.org/maven2/com/azure/resourcemanager/azure-resourcemanager-dns/2.53.8/azure-resourcemanager-dns-2.53.8.jar

Step 4: Locating built JAR...
  New JAR: ...\target\azure-resourcemanager-dns-2.54.0-beta.1.jar

Step 5: Generating changelog content...
  Changelog content generated

Step 6: Running revapi:check to detect breaking changes...
revapi:check exited with code 1
[INFO] Comparing [com.azure.resourcemanager:azure-resourcemanager-dns:jar:2.53.8] against [com.azure.resourcemanager:azure-resourcemanager-dns:jar:2.54.0-beta.1].
[ERROR] java.method.numberOfParametersChanged: method com.azure.resourcemanager.dns.models.DnsZones com.azure.resourcemanager.dns.DnsZoneManager::zones(boolean)

⚠️  Breaking changes detected by revapi:check

Step 7: Writing output JSON...
✅ SDK changes computed successfully!
```

Result:

- `hasBreakingChange: true`
- `changelogMD`: includes:
  - `DnsZoneManager` was modified
  - `zones()` was removed
  - `zones(boolean)` was added

JSON output:

```json
{
  "hasBreakingChange": true,
  "changelogMD": "### Breaking Changes\n\n#### `models.ZoneListResult` was removed\n\n#### `models.RecordSetListResult` was removed\n\n#### `DnsZoneManager` was modified\n\n* `zones()` was removed\n\n#### `models.DnsResourceReference` was modified\n\n* `DnsResourceReference()` was changed to private access\n* `withTargetResource(com.azure.core.management.SubResource)` was removed\n* `withDnsResources(java.util.List)` was removed\n\n### Features Added\n\n* `models.TlsaRecord` was added\n\n* `models.NaptrRecord` was added\n\n* `models.DelegationSignerInfo` was added\n\n* `models.SigningKey` was added\n\n* `models.Digest` was added\n\n* `models.DsRecord` was added\n\n#### `DnsZoneManager` was modified\n\n* `zones(boolean)` was added\n\n#### `models.RecordType` was modified\n\n* `TLSA` was added\n* `DS` was added\n* `NAPTR` was added\n\n"
}
```

Revapi output specifically reported:

- `java.method.numberOfParametersChanged`
- old: `DnsZoneManager::zones()`
- new: `DnsZoneManager::zones(boolean)`

Conclusion:

- The script correctly detected the breaking API change.
- The JSON output remained well-formed and marked the breaking change as expected.

## Final state

- Temporary edits to `DnsZoneManager.java` were reverted.
- Current working tree only has the pre-existing untracked `0001-Revert-regen.patch` in addition to the PR branch checkout.

## Output artifacts

- `C:\Users\weidxu\.copilot\session-state\9efe40ef-699b-435f-b62f-2b059c74ef2c\files\dns-baseline.json`
- `C:\Users\weidxu\.copilot\session-state\9efe40ef-699b-435f-b62f-2b059c74ef2c\files\dns-nonbreaking.json`
- `C:\Users\weidxu\.copilot\session-state\9efe40ef-699b-435f-b62f-2b059c74ef2c\files\dns-breaking.json`
