# Reconciling `CHANGELOG.md` files

Patches revert each library's `CHANGELOG.md` to its last stable release, dropping
the `(Unreleased)` section and any newer entries that exist on `main`. The
merge-back must restore `main`'s content **and** insert the patch entry.

## Target structure of the resolved file

Produce, from top to bottom:

1. **The `## X.Y.Z (Unreleased)` section exactly as it is on `main`** — including
   any unreleased entries already present there. Do not add, edit, or remove
   unreleased content.
2. **The patch entry from `RELEASE_BRANCH`**, i.e. the `## X.Y.Z (PATCH_DATE)`
   section (e.g. `## 12.33.3 (2026-07-01)`). This is the *only* content taken
   from the release branch.
3. **The remainder of the file exactly as it is on `main`** (all previously
   released entries, unchanged).

Concretely: take the `main` version of the file and **insert the release
branch's `PATCH_DATE` entry immediately below the `(Unreleased)` section.**
Nothing else changes.

## Rules

- The patch entry's heading must carry `PATCH_DATE` (the `YYYY-MM-DD` derived
  from the release branch name), not `(Unreleased)`.
- Do not merge, reorder, or de-duplicate other entries.
- Do not modify wording anywhere except inserting the patch block.
- Verify dependency-bump bullets inside the patch entry reference the correct
  "from" version (the previously released version). Correct them if the release
  branch entry is wrong.

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
