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
`MGMT-RELEASE-PLAN` has Blocking severity.

## Review rules

### `MGMT-FOLDER`: service-folder mismatch

- **Severity:** Blocking

For a newly added management module, split its directory name on `-`. The
expected service identity is always the third segment:

`azure-resourcemanager-<service-identity>[-<module-suffix>...]`

Ignore the fourth and later segments when comparing the module with its
`sdk/<service>/` folder. For example,
`azure-resourcemanager-compute-bulkactions` belongs in `sdk/compute`.

Report whenever the folder `<service>` differs from the module's third segment.
Explain that the likely source is the `service-dir` configuration in the
upstream `tspconfig.yaml`, without accessing that repository.

### `MGMT-VERSION`: stable package on a preview API

- **Severity:** Blocking

Read all API versions from the current CHANGELOG entry. A multi-service entry
may use `Package api-version <service-group>: <version>, ...`; use every value
after `:` and do not treat the service-group names as versions. Fall back to
`apiVersions` in the generated `_metadata.json`. When `apiVersions` is an
object that maps service groups to versions, likewise use every object value.
If any extracted version ends in `-preview`, the Java package version must
contain a beta suffix. Report a stable package generated from any preview API,
even when its other API versions are stable.

Do not infer preview status from feature names or dates.

### `MGMT-API-VERSION`: generated API-version context

- **Severity:** Informational

Read the effective API-version set from the current CHANGELOG entry, falling
back to `apiVersions` in the generated `_metadata.json`. For a CHANGELOG
`service-group: version` list or an `apiVersions` object, report the distinct
version values rather than the service-group names. Report the exact version
or versions as review context.

Treat the API-version set as the informational item's state. If a later commit
changes that set, emit `MGMT-API-VERSION` again as `New` with the new values,
even when a prior workflow comment already contains this ID. This provides an
additional guard for `MGMT-API-VERSION-OVERLAP`. Do not request corrective
action.

### `MGMT-LRO`: suspicious generated LRO response shape

- **Severity:** Warning

Report only when the PR newly adds a `<ClientMethod>Response` and a corresponding
`<ClientMethod>Headers` model whose headers include `location` or
`retry-after`. Identify the method and both generated types. Explain that the
shape may indicate incorrect LRO modeling upstream.

Do not report ordinary response wrappers or headers models without those
headers.

### `MGMT-API-VERSION-OVERLAP`: overlapping API-version generations

- **Severity:** Blocking

Report when the branch contains package output from more than one API-version
generation. Evidence includes either:

- multiple package API-version lines in one release section, or
- regeneration of a release section that was already dated before this PR.

The likely root cause is either an earlier generated package that has not been
released or multiple generation runs targeting different API versions on the
same branch. Use the CHANGELOG as evidence, not as the defect itself. Ask which
API-version generation should remain and whether the earlier package should be
released or removed. Ordinary dependency, POM, or release metadata changes are
expected.

### `MGMT-BREAKING`: generated public API break

- **Severity:** Warning

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

### `MGMT-NEW-MODULE`: new management module context

- **Severity:** Informational

Report when the PR adds a new
`sdk/<service>/azure-resourcemanager-<module>/pom.xml`. Identify the service
folder and module so human reviewers know the PR introduces a new module and
may warrant broader attention. Do not imply a defect or request corrective
action.

## Verification and output

Use only these severity levels:

| Severity | Meaning |
| --- | --- |
| Blocking | A high-confidence configuration or release inconsistency that requires attention before merge. |
| Warning | A suspicious generated shape or compatibility signal that needs human verification; it is not an assertion that code must be fixed. |
| Informational | Useful context with no requested corrective action. |

`MGMT-FOLDER`, `MGMT-VERSION`, `MGMT-API-VERSION-OVERLAP`, and
`MGMT-RELEASE-PLAN` are Blocking. `MGMT-LRO` and `MGMT-BREAKING` are Warning.
`MGMT-API-VERSION` and `MGMT-NEW-MODULE` are Informational.

Every item must cite a repository-relative file and affected symbol or release
entry and state whether it is `New`, `Carried forward`, or `Resolved`.
Blocking and Warning items request one concrete human action or verification.
Informational items provide context without requesting action.
`MGMT-RELEASE-PLAN` cites the PR description. Reuse stable IDs across commits,
except that a changed `MGMT-API-VERSION` value is emitted again as `New`.
When a new head SHA passes the Java gate, include each still-applicable prior
item as `Carried forward` in the replacement current-state comment; do not
restate its question as `New`.

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

### Blocking

- `[MGMT-...] New|Carried forward|Resolved — evidence and requested action`

### Warning

- `[MGMT-...] New|Carried forward|Resolved — evidence and verification request`

### Informational

- `[MGMT-...] New|Carried forward|Resolved — context`
```

Order sections as Blocking, Warning, Informational and omit empty sections.
Silence or `noop` is correct when no review state changes.
