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

permissions:
  contents: read
  copilot-requests: write

env:
  RELEASE_VERSION: ${{ inputs.release_version }}

concurrency:
  job-discriminator: ${{ github.run_id }}

skills:
  - sdk/resourcemanager/azure-resourcemanager/.github/skills/prepare-release

network: {}

steps:
  - name: Prepare ResourceManager release
    shell: bash
    timeout-minutes: 5
    env:
      PYTHONDONTWRITEBYTECODE: "1"
    run: |
      exit_code=0
      python3 sdk/resourcemanager/azure-resourcemanager/.github/skills/prepare-release/scripts/prepare_release.py \
        --release-version "$RELEASE_VERSION" \
        --summary-file /tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json || exit_code=$?
      case "$exit_code" in
        0|2) ;;
        *) exit "$exit_code" ;;
      esac
      test -s /tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json
  - name: Upload release preparation summary
    if: always()
    uses: actions/upload-artifact@043fb46d1a93c77aae656e7c1c64a875d1fc6a0a
    with:
      name: release-preparation-summary
      path: /tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json
      if-no-files-found: warn
      retention-days: 1

tools:
  bash:
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
Always use the current UTC date and include all Breaking Changes from qualifying
minor releases.

The fixed `Prepare ResourceManager release` workflow step has already executed
the deterministic script. It preserves the quoted release-version input and
uses the canonical version when that input is empty. Fatal script errors fail
the step; an intentional release-readiness block remains in the JSON summary.
Do not rerun the script or manually modify its prepared release files.

The input environment is:

- `RELEASE_VERSION=${{ inputs.release_version }}`

Read
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
4. The allowlist result and changed files.
5. State that all qualifying Breaking Changes were included, patch-only prose
   was omitted, and changelog selection came
   directly from the deterministic script.

Never publish a package, modify a premium package changelog, or alter files
outside the safe-output allowlist.
