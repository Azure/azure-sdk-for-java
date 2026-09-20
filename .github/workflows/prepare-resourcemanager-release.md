---
private: true
name: Prepare ResourceManager Release
description: Prepare a stable azure-resourcemanager aggregate release in a draft pull request

on:
  roles: [admin, maintainer]
  workflow_dispatch:
    inputs:
      release_version:
        description: Optional stable X.Y.Z release version
        required: false
        type: string
      release_date:
        description: Optional UTC release date in YYYY-MM-DD format
        required: false
        type: string
      exclude_breaking_changes:
        description: Optional comma-separated bundled artifact IDs whose Breaking Changes should be excluded
        required: false
        type: string

permissions:
  contents: read
  copilot-requests: write

env:
  RELEASE_VERSION: ${{ inputs.release_version }}
  RELEASE_DATE: ${{ inputs.release_date }}
  EXCLUDE_BREAKING_CHANGES: ${{ inputs.exclude_breaking_changes }}

concurrency:
  job-discriminator: ${{ github.run_id }}

skills:
  - sdk/resourcemanager/azure-resourcemanager/.github/skills/prepare-release

network: {}

tools:
  bash:
    - "python3 sdk/resourcemanager/azure-resourcemanager/.github/skills/prepare-release/scripts/prepare_release.py *"
    - "cat /tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json"
    - "git status --short"
    - "git diff *"

safe-outputs:
  report-failure-as-issue: false
  create-pull-request:
    title-prefix: "[Automation] "
    branch-prefix: "automation/prepare-resourcemanager-release/"
    draft: true
    base-branch: main
    allowed-base-branches: [main]
    allowed-branches:
      - "automation/prepare-resourcemanager-release/*"
    allowed-files:
      - "eng/versioning/version_client.txt"
      - "sdk/resourcemanager/azure-resourcemanager/CHANGELOG.md"
      - "sdk/resourcemanager/azure-resourcemanager/README.md"
      - "sdk/resourcemanager/azure-resourcemanager/pom.xml"
      - "sdk/resourcemanager/azure-resourcemanager-perf/CHANGELOG.md"
      - "sdk/resourcemanager/azure-resourcemanager-perf/README.md"
      - "sdk/resourcemanager/azure-resourcemanager-perf/pom.xml"
      - "sdk/resourcemanager/azure-resourcemanager-samples/CHANGELOG.md"
      - "sdk/resourcemanager/azure-resourcemanager-samples/README.md"
      - "sdk/resourcemanager/azure-resourcemanager-samples/pom.xml"
    protected-files: request_review
    max-patch-files: 10
  noop:
    report-as-issue: false

timeout-minutes: 20
---

# Prepare the ResourceManager aggregate release

Prepare a stable release of
`com.azure.resourcemanager:azure-resourcemanager`. The installed package-local
skill defines the deterministic algorithm and safety rules. Follow it exactly.

Execute this command from the repository root, preserving the environment
variable references so workflow input is never interpolated into shell syntax:

```bash
python3 sdk/resourcemanager/azure-resourcemanager/.github/skills/prepare-release/scripts/prepare_release.py \
  ${RELEASE_VERSION:+--release-version "$RELEASE_VERSION"} \
  ${RELEASE_DATE:+--release-date "$RELEASE_DATE"} \
  ${EXCLUDE_BREAKING_CHANGES:+--exclude-breaking-changes "$EXCLUDE_BREAKING_CHANGES"} \
  --summary-file /tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json
```

The input environment is:

- `RELEASE_VERSION=${{ inputs.release_version }}`
- `RELEASE_DATE=${{ inputs.release_date }}`
- `EXCLUDE_BREAKING_CHANGES=${{ inputs.exclude_breaking_changes }}`

After execution, read
`/tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json`. Do not reinterpret,
rewrite, or supplement the script's changelog selection.

- If `status` is `blocked` or `error`, call `noop` with the gate blockers or
  error and create no pull request.
- If `status` is `no_changes`, call `noop` and create no pull request.
- If `status` is `ready`, create exactly one draft pull request targeting
  `main`. Use title `Prepare azure-resourcemanager <release_version> release`
  and branch suffix `<release_version>-<release_date>`.

Build a compact pull request body using only the JSON summary:

1. Release version and date.
2. Gate result and prior aggregate cutoff.
3. A table of selected artifact IDs, consumed/source versions, selected minor
   versions, and retained API versions.
4. The per-run Breaking Changes exclusions.
5. The allowlist result and changed files.
6. State that patch-only prose was omitted and changelog selection came
   directly from the deterministic script.

Never publish a package, modify a premium package changelog, or alter files
outside the safe-output allowlist.
