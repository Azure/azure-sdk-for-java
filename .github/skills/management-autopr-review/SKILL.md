---
name: management-autopr-review
description: Reviews eligible generated Azure Java management-library AutoPR changes for a narrow set of high-value generation risks.
---

# Management AutoPR Review Rules

Use only for `Azure/azure-sdk-for-java` PRs that are non-draft, target `main`,
originate in the same repository, have a title containing
`[AutoPR azure-resourcemanager-`, and are authored by `azure-sdk`,
`azure-sdk-automation[bot]`, or `app/azure-sdk-automation`.

The review is advisory. It does not approve, merge, modify code, access the spec
repository, or replace other automated and human review.

## Java-change gate

Never review a file whose normalized repository-relative path contains a
`generated` segment (`(^|/)generated(/|$)`). This includes generated samples
and tests.

Review `opened`, `reopened`, and `ready_for_review` only when the PR contains a
changed `.java` file outside a `generated` path. On `synchronize`, review only
when the pushed `before..after` range changes such a `.java` file. POM-only,
CHANGELOG-only, metadata-only, generated-only, and refresh-only pushes produce
no comment.

Once the Java gate passes, supporting POM, CHANGELOG, metadata, and CI files may
be read as evidence, except files under a `generated` path.

## Release-plan gate

Normalize casing and Markdown emphasis. Accept both plain
`Release Plan link: https://example.com/release-plan` and formatted
`**Release plan link:** [text](https://example.com/release-plan)`. Require an HTTP(S) URL.

Missing link is `MGMT-RELEASE-PLAN`. Cite the PR description and continue the
remaining review passes so other high-value concerns are not hidden.

## Review rules

### `MGMT-FOLDER`: service-folder mismatch

For a newly added management module, split its directory name on `-`. The
expected service identity is always the third segment:

`azure-resourcemanager-<service-identity>[-<module-suffix>...]`

Ignore the fourth and later segments when comparing the module with its
`sdk/<service>/` folder. For example,
`azure-resourcemanager-compute-bulkactions` belongs in `sdk/compute`.

Report whenever the folder `<service>` differs from the module's third segment.
Do not treat established branding differences or a folder containing only the
new module as exceptions. Explain that the likely source is upstream
`service-dir` configuration without accessing that repository.

### `MGMT-VERSION`: stable package on a preview API

Read all API versions from the current CHANGELOG entry, falling back to
`apiVersions` in the generated metadata JSON. If any ends in `-preview`, the
Java package version must contain a beta suffix. Report a stable package
generated from a preview API.

Do not infer preview status from feature names or dates.

### `MGMT-LRO`: suspicious generated LRO response shape

Report only when the PR newly adds a `<ClientMethod>Response` and a corresponding
`<ClientMethod>Headers` model whose headers include `location` or
`retry-after`. Identify the method and both generated types. Explain that the
shape may indicate incorrect LRO modeling upstream.

Do not report ordinary response wrappers or headers models without those
headers.

### `MGMT-CHANGELOG`: suspicious regeneration

Report either:

- multiple package API-version lines in one release section, or
- regeneration of a release section that was already dated before this PR.

Ask which API version and release the package is intended to represent.
Ordinary dependency, POM, or release metadata changes are expected.

### `MGMT-BREAKING`: generated public API break

Use the current CHANGELOG release section as the primary and authoritative
source. For a GA package version, its breaking-change section compares the
release with the previous GA release and can identify a break that entered the
main branch during an earlier beta.

Do not require the current Java diff to contain the break. Cite the CHANGELOG
file and affected release entry. Optionally cite a non-`generated` Java file
when it provides useful corroboration.

For a GA release, report one `MGMT-BREAKING` concern summarizing substantive
breaking items listed in the current CHANGELOG section, including removed
public types or methods, changed access, renamed APIs, and changed public
signatures or return types. Request human confirmation that the GA breaks are
intended.

Do not raise `MGMT-BREAKING` for a beta package version. Beta packages may
break, and their CHANGELOG comparison may be against a prior beta. Additive
items are not breaking.

## Verification and output

Every concern must cite a repository-relative file and affected symbol or
release entry, state whether it is `New`, `Carried forward`, or `Resolved`, and
request one concrete human action. Reuse stable IDs across commits.

Output:

```markdown
## Automated management AutoPR review

- Head SHA: `<sha>`
- Package: `<package>`
- Release type: `<stable|beta>`
- API version: `<version(s)>`
- CHANGELOG: `<release and date>`
- Java changes reviewed: `<summary>`
- Breaking changes: `<none|summary>`
- Decision: `<no high-confidence concerns|human attention requested>`

### Concerns

- `<none>`
- `[MGMT-...] New|Carried forward|Resolved — evidence and requested action`
```

Silence or `noop` is correct when no review state changes.
