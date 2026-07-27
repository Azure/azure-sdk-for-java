# Reconciling `CHANGELOG.md` files

Patches revert each library's `CHANGELOG.md` to its last stable release, dropping
the `(Unreleased)` section and any newer entries that exist on `main`. The
merge-back must restore `main`'s content **and** insert the patch entry.

## Target structure of the resolved file

Produce, from top to bottom:

1. **The `## X.Y.Z (Unreleased)` section exactly as it is on `main`** — including
   any unreleased entries already present there. Do not add to, edit, or remove
   unreleased content.
2. **The patch entry from `RELEASE_BRANCH`**, i.e. the `## X.Y.Z (PATCH_DATE)`
   section (e.g. `## 12.33.3 (2026-07-01)`). This is the *only* content taken
   from the release branch.
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
- Verify dependency-bump bullets inside the patch entry reference the correct
  "from" version. The "from" version is the version of each dependency that was
  present in the release that the patch was applied to (i.e., the base version
  the patch built on top of) — this is **not** necessarily the most recent
  version of that dependency released to Maven. Correct any bullets if the
  release branch entry is wrong.

  The release-branch patch entry is frequently wrong here: it often copies the
  "from" version from an older release instead of the immediately preceding one.
  To find the correct "from" version, look at the **immediately preceding
  release entry** in that same `CHANGELOG.md` (the next `## X.Y.Z (DATE)` section
  below the patch entry, on the release branch, which contains the library's full
  history). For each dependency bumped in the patch entry, its correct "from"
  version is the **"to" version recorded for that same dependency in the
  immediately preceding release entry**. If that preceding entry did not touch
  the dependency, walk further back to the most recent release that did.

  Concretely, for every dependency-bump bullet in the patch entry, of the form:

  ```
  - Upgraded `dep` from `A` to version `B`.
  ```

  1. Find the most recent release entry below it that also upgraded `dep`, and
     read that entry's "to" version `C`.
  2. If `A` is not equal to `C`, replace `A` with `C` so the bullet reads
     `from` `C` `to version` `B`.

  Example — a `2.53.9` patch entry says:

  ```
  - Upgraded `azure-resourcemanager-resources` from `2.54.0` to version `2.54.2`.
  ```

  but the preceding `2.53.8` release entry says:

  ```
  - Upgraded `azure-resourcemanager-resources` from `2.54.0` to version `2.54.1`.
  ```

  Because `2.53.8` already shipped `2.54.1`, the `2.53.9` bullet's "from" must be
  corrected to `2.54.1`:

  ```
  - Upgraded `azure-resourcemanager-resources` from `2.54.1` to version `2.54.2`.
  ```

  Apply this check to **every** dependency bullet, since a single patch entry can
  list several dependencies each with its own preceding "to" version.

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
