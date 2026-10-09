---
name: prepare-release
description: Prepare a stable com.azure.resourcemanager:azure-resourcemanager release with deterministic changelog selection and a strict changed-file boundary.
---

# Prepare the Azure ResourceManager aggregate release

Use this skill only for preparing a stable release of
`com.azure.resourcemanager:azure-resourcemanager`.

When invoked by `prepare-resourcemanager-release`, the fixed workflow step has
already run the script and written
`/tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json`. Read that summary
and follow the result handling below. Do not rerun the script or manually modify
its prepared release files.

For standalone use, run the package-local script from the repository root:

```bash
python3 sdk/resourcemanager/azure-resourcemanager/.github/skills/prepare-release/scripts/prepare_release.py \
  --summary-file /tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json
```

Optional arguments:

- `--release-version X.Y.Z` overrides the stable version derived by removing
  the prerelease suffix from the aggregate current version in
  `eng/versioning/version_client.txt`.
- `--dry-run` performs discovery, gating, and changelog selection without
  editing files or invoking version propagation.

The release date is always the current UTC date, including on reruns. All
Breaking Changes from qualifying minor releases are included without exclusions.

The script is the authority for release selection. Do not manually reinterpret,
rewrite, or supplement its changelog choices.

## Deterministic behavior

1. Discover bundled premium management libraries from the aggregate `pom.xml`.
2. Stop before editing and report every bundled library whose first CHANGELOG
   release heading is stable/GA and has a concrete date rather than `Unreleased`.
   A beta/prerelease first heading does not block preparation, whether dated
   or `Unreleased`. Prerelease changelog prose remains excluded from selection.
3. Read the previous aggregate stable release and cutoff date from the
   aggregate CHANGELOG.
4. Ignore patch-only release prose. For a patch dependency, use the nearest
   stable minor release at or below the consumed version as the changelog
   source.
5. Retain Features Added and Breaking Changes from every qualifying minor
   release. Retain only the latest qualifying minor release's API-version update.
6. Update the canonical aggregate current version, then invoke
   `eng/versioning/update_versions.py` for the allowlisted POM and README files.
7. Reject any changed file outside the embedded allowlist.

## Result handling

Read the JSON summary written by `--summary-file`.

- If `status` is `blocked` or `error`, do not create a pull request.
- If `status` is `no_changes`, do not create a pull request.
- If `status` is `ready`, use the summary as the sole source for the pull
  request title and compact description.

Never publish an SDK package from this skill.
