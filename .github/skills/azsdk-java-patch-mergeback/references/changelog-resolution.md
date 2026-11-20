# Reconciling `CHANGELOG.md` files

Patches revert each library's `CHANGELOG.md` to its last stable release, dropping
the `(Unreleased)` section and any newer entries that exist on `main`. The
merge-back must restore `main`'s content **and** insert the patch entry.

## Target structure of the resolved file

Produce, from top to bottom:

1. **The `## X.Y.Z (Unreleased)` section exactly as it is on `main`** — including
   any unreleased entries already present there. Do not add to, edit, or remove
   unreleased content.
2. **The exact patch entry from `patch-changelog.md`**, i.e. the
   `## X.Y.Z (PATCH_DATE)` section (e.g. `## 12.33.3 (2026-07-01)`). This is the
   only release-branch content inserted into the target changelog.
3. **The remainder of the file exactly as it is on `main`** (all previously
   released entries, unchanged).

Concretely: take the `main` version of the file and **insert the release
branch's `PATCH_DATE` entry immediately below the `(Unreleased)` section.**
The only exception is if there is a beta-release entry on `main` that is newer than the patch entry, 
in which case the patch entry should be inserted **after** the beta entry.
Nothing else changes.

## Rules

- The patch entry's heading must carry `PATCH_DATE` (the `YYYY-MM-DD` derived
  from the release branch name), not `(Unreleased)`.
- Do not merge, reorder, or de-duplicate other entries.
- Do not modify wording anywhere except inserting the patch block.
- Preserve dependency-bump bullets exactly as recorded in `patch-changelog.md`.
  Preparation resolves each catalogued dependency's "from" version from the
  **dependency-version column** of `eng/versioning/version_client.txt` on
  `main` **before any package reversion or version updates**. For GA libraries
  this is the stable baseline, even when current-version is a newer preview.
- Never infer baselines from changelog entries, release dates, the latest Maven
  version, or the version catalog on `main` at merge-back time.
- If an older manifest needs recovery, regenerate it with
  `New-PatchChangelog.ps1 -DependencyVersionRef <pre-reversion-main-commit>`.
  Match each dependency by its full `groupId:artifactId` coordinate from the
  package POM. Missing Azure baselines or prerelease baselines are errors, not
  reasons to fall back to changelog history. External dependencies absent from
  the catalog retain their prepared baseline, and destination versions remain
  unchanged.

## Example (before → after)

`main` CHANGELOG top:
```
## 12.34.0-beta.1 (Unreleased)

### Features Added

## 12.33.2 (2026-02-15)
...
```

`RELEASE_BRANCH` patch entry:
```
## 12.33.3 (2026-07-01)

### Bugs Fixed
- Fixed ...
```

Resolved file:
```
## 12.34.0-beta.1 (Unreleased)

### Features Added

## 12.33.3 (2026-07-01)

### Bugs Fixed
- Fixed ...

## 12.33.2 (2026-02-15)
...
```

Repeat for every `CHANGELOG.md` in the last-two-commits diff of the release
branch.
