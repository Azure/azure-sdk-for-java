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

Review `opened`, `reopened`, and `ready_for_review` only when the PR contains a
changed `.java` file. On `synchronize`, review only when the pushed
`before..after` range changes a `.java` file. POM-only, CHANGELOG-only,
metadata-only, and refresh-only pushes produce no comment.

Once the Java gate passes, supporting POM, CHANGELOG, metadata, and CI files may
be read as evidence.

## Release-plan gate

Normalize casing and Markdown emphasis. Accept both plain
`Release Plan link: <url>` and formatted
`**Release plan link:** [text](<url>)`. Require an HTTP(S) URL.

Missing link is `MGMT-RELEASE-PLAN`; stop before code review.

## Review rules

### `MGMT-FOLDER`: unrelated service-folder collision

For a newly added
`sdk/<service>/azure-resourcemanager-<module-tail>/pom.xml`, report only when:

1. `<service>` differs materially from `<module-tail>`, and
2. the folder already contains a management module for a different service.

Do not report established branding differences or a folder containing only its
own module. Explain that the likely source is upstream `service-dir`
configuration without accessing that repository.

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

Report only changes introduced by the PR:

- removed or renamed public methods,
- changed return types on existing public methods, or
- generated response/header types that alter an existing method signature and
  also satisfy the LRO suspicion above.

Additive methods, models, overloads, and properties are not breaking.

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

