---
name: prepare-release
description: Prepare a stable com.azure.resourcemanager:azure-resourcemanager release with deterministic changelog selection and a strict changed-file boundary.
---

# Prepare the Azure ResourceManager aggregate release

Use this skill only for preparing a stable release of
`com.azure.resourcemanager:azure-resourcemanager`.

Run the package-local script from the repository root:

```bash
python3 sdk/resourcemanager/azure-resourcemanager/.github/skills/prepare-release/scripts/prepare_release.py \
  --summary-file /tmp/gh-aw/agent/prepare-resourcemanager-release-summary.json
```

Optional arguments:

- `--release-version X.Y.Z` overrides the stable version derived by removing
  the prerelease suffix from the aggregate current version in
  `eng/versioning/version_client.txt`.
- `--release-date YYYY-MM-DD` overrides the current UTC date.
- `--exclude-breaking-changes artifact-a,artifact-b` excludes Breaking Changes
  for only the named bundled artifact IDs in this run.
- `--dry-run` performs discovery, gating, and changelog selection without
  editing files or invoking version propagation.

The script is the authority for release selection. Do not manually reinterpret,
rewrite, or supplement its changelog choices.

## Deterministic behavior

1. Discover bundled premium management libraries from the aggregate `pom.xml`.
2. Stop before editing and report every bundled library whose first CHANGELOG
   release heading has a concrete date rather than `Unreleased`.
3. Read the previous aggregate stable release and cutoff date from the
   aggregate CHANGELOG.
4. Ignore patch-only release prose. For a patch dependency, use the nearest
   stable minor release at or below the consumed version as the changelog
   source.
5. For one qualifying minor release, retain its Features Added, Breaking
   Changes, and API-version update. For multiple qualifying minor releases,
   retain the latest Features Added and API-version update plus Breaking Changes
   from the full interval.
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
