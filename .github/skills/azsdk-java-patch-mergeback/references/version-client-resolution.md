# Reconciling `eng/versioning/version_client.txt`

## File format

Each SDK line is `<groupId>:<artifactId>;dependency-version;current-version`.
Read the header of [eng/versioning/version_client.txt](../../../../eng/versioning/version_client.txt)
for the authoritative explanation. Key points:

- **`dependency-version`** — what libraries *outside* the release group use as a
  Maven dependency. A patch release bumps this to the newly released stable
  version.
- **`current-version`** — the in-development version of the library on `main`.
  This must **not** change during a merge-back.

## Resolution algorithm

Compare the file on `main` against the file on `RELEASE_BRANCH`, line by line
(match by `groupId:artifactId`):

1. If the release branch line has a **different `dependency-version`** than
   `main`, take the release branch `dependency-version`.
2. **Always keep the `current-version` from `main`**, regardless of what the
   release branch shows (patches revert `current-version` to the stable value).
3. Leave every other line exactly as it is on `main`.

Resulting line = `groupId:artifactId;<release-branch dependency-version>;<main current-version>`.

## Guardrails

- **Do not reset beta versions to `beta.1`.** If a `current-version` on `main`
  is e.g. `1.2.0-beta.4`, it stays `1.2.0-beta.4`.
- Only touch SDK library lines whose `dependency-version` actually changed on
  the release branch. Do not modify parent/BOM lines or unrelated libraries.
- Never alter `current-version` values — a changed `current-version` is the most
  common merge-back bug and surfaces later as a pom.xml mismatch.

## Example

- `main`:           `com.azure:azure-storage-blob;12.33.2;12.34.0-beta.1`
- `RELEASE_BRANCH`: `com.azure:azure-storage-blob;12.33.3;12.33.3`
- **Result:**       `com.azure:azure-storage-blob;12.33.3;12.34.0-beta.1`

Here the dependency-version moves `12.33.2` → `12.33.3` (the patch), while the
`current-version` stays `12.34.0-beta.1` (from `main`).
