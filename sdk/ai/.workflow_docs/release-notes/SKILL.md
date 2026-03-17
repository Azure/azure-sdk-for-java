---
name: release-notes
description: "Update CHANGELOG.md and README.md for an Azure SDK for Java package based on a GitHub PR. Use when the user wants to write or update release notes, changelogs, or readme docs from a PR reference."
---

# Release Notes Skill

Update `CHANGELOG.md` and/or `README.md` for an Azure SDK for Java package using a GitHub PR as the source of truth.

## Prerequisites

- `gh` CLI authenticated (`gh auth status`).
- The current working directory must be the package root (where `CHANGELOG.md` and `README.md` live).

## Inputs

Ask the user for any missing inputs before proceeding:

| Input | Required | Description |
|-------|----------|-------------|
| PR URL or number | **Yes** | GitHub PR to use as the source of changes. |
| Package directory | No | Defaults to `cwd`. Override if the user specifies a different package. |
| Scope | No | `changelog`, `readme`, or `both` (default: `both`). |

## Step 1 — Gather PR information

Use `gh` to collect the data you need. The diff may be too large for `gh pr diff`; fall back to the files API.

```bash
# PR metadata
gh pr view <number> --json title,body

# File list with status (added/modified/removed/renamed)
gh api repos/{owner}/{repo}/pulls/<number>/files --paginate \
  --jq '.[] | .status + " " + .filename'

# Renamed files (old → new)
gh api repos/{owner}/{repo}/pulls/<number>/files --paginate \
  --jq '.[] | select(.status == "renamed") | "\(.previous_filename) -> \(.filename)"'

# Patch for a specific file (when you need detail)
gh api "repos/{owner}/{repo}/pulls/<number>/files?per_page=100" \
  --jq '.[] | select(.filename | test("<pattern>")) | .patch'
```

Collect:
- Added, removed, and renamed model/enum classes.
- Changes to client classes (method renames, new methods, removed methods).
- Changes to `*ServiceVersion.java` (version string changes).
- Changes to `*ClientBuilder.java` (base URL, authentication, new builder methods).
- Changes to `module-info.java` (transitive exports, new requires).
- Changes to customization files.
- New or modified samples.

## Step 2 — Check for existing entries

Before writing anything, read the current `CHANGELOG.md` and `README.md` (if in scope) **in full** and compare their content against the changes you collected in Step 1.

### 2a. Identify overlapping entries

For each change you plan to document, check whether an entry already covers it:

- **Exact match** — an existing bullet describes the same rename, addition, or removal using the same class/method names.
- **Topical overlap** — an existing bullet covers the same area (e.g., "tool renames" or "new sub-client") but with different detail, wording, or scope.

### 2b. Report findings to the user

If **any** overlap is found, **stop and consult the user before editing**. Present a summary like:

> The following changes from PR #NNN already appear to be covered in the current files:
>
> **CHANGELOG.md**
> - _Features Added_ already mentions `FooClient` addition (line …).
> - _Breaking Changes_ already has a bullet about tool renames that partially overlaps the renames in this PR.
>
> **README.md**
> - The "Key concepts" section already lists the `BarClient` sub-client.
>
> Would you like me to:
> 1. Skip the entries that are already covered and only add the new ones?
> 2. Merge/update the overlapping entries (tell me how you'd like them worded)?
> 3. Proceed anyway and add everything as new entries?

Wait for the user's response before continuing to Step 3 or Step 4.

### 2c. No overlap

If there is **no** overlap at all, inform the user briefly (e.g., "No existing entries overlap with this PR — proceeding to update.") and continue.

## Step 3 — Update CHANGELOG.md

### Format rules (CI-enforced)

The CHANGELOG structure is **strict**. Every version section must contain exactly these headings in this order:

```markdown
## <version> (date or Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes
```

- **Do not** add, remove, rename, or reorder these headings.
- **Do not** delete or modify existing entries — all changes are **additive only**. Append new bullets below existing ones.
- If Step 2 identified a topical overlap and the user chose to merge/update an existing entry, that is the **only** case where you may edit an existing bullet — and only as the user directed.
- Only modify the target version section (usually the `(Unreleased)` one).
- Each entry is a markdown list item starting with `- `.

### What goes where

| Heading | Content |
|---------|---------|
| **Features Added** | New public classes, methods, enums, samples, tools, client capabilities. |
| **Breaking Changes** | Renamed/removed classes, renamed methods, changed enum values, changed method signatures, service version changes. |
| **Bugs Fixed** | Fixes to incorrect behavior (e.g., URL construction, serialization bugs). |
| **Other Changes** | Dependency updates, spec regeneration, module-info changes, internal refactors. |

### Writing guidelines

1. **Summarize, don't enumerate.** Group related changes under a single bullet when there is an overarching pattern (e.g., "Methods across sub-clients were renamed to include the resource name" with sub-bullets for each client). Don't list every file touched.
2. **Consumer perspective.** Only mention changes that matter to a user of the library. Internal implementation model renames that are not in the public API can be omitted.
3. **Use code formatting** for class names, method names, and enum values: `` `ClassName` ``.
4. **Show before → after** for renames: `` `OldName` → `NewName` `` or `` `OldName` renamed to `NewName` ``.
5. **Group repetitive renames** by pattern. For example, if 10 tool classes were renamed from `*AgentTool` to `*Tool`, write one bullet with representative examples rather than 10 bullets.
6. **Mention service version changes** (e.g., date-based to `v1`) in Breaking Changes.
7. **Don't over-list new models.** If the PR adds dozens of generated models, mention only the notable ones (new tool types, new feature-area models) and say "and related types" or similar.
8. **Omit trivial internal changes** like parameter reordering in generated `@HostParam`/`@QueryParam` annotations, checkstyle suppression updates, or whitespace.

## Step 4 — Update README.md

### Format rules (CI-enforced)

The README structure is also checked by CI. Follow the existing heading hierarchy exactly:

```
# <Package name> client library for Java
## Documentation
## Getting started
### Prerequisites
### Adding the package to your product
### Authentication
## Key concepts
### <subsections as needed>
## Examples
### <subsections as needed>
### Service API versions
#### Select a service API version
## Troubleshooting
## Next steps
## Contributing
<!-- LINKS -->
```

- **Do not** remove or reorder the top-level headings.
- **Do not** change the `[//]: #` version-update markers.
- **Do not** delete or rewrite existing prose or snippets unless they reference renamed APIs from this PR and Step 2 confirmed no conflict (or the user approved the change).
- You **may** add new `###` subsections under `## Key concepts` or `## Examples`.
- Keep existing code snippets intact unless they reference renamed APIs.

### What to update

1. **Package description** (opening paragraph): mention the REST API version if it changed (e.g., "targets the **v1** REST API").
2. **Code snippets**: update any code that references renamed methods, classes, or builder patterns. Keep the `java com.azure...` snippet tags intact.
3. **Sub-client lists**: if new sub-clients were added, add them. Mark preview sub-clients with **(preview)**.
4. **Preview tools / features**: if the package defines `Tool` subclasses, document which are GA and which are preview (look for `Preview` in the class name or discriminator value). Use a table.
5. **Opt-in flags / experimental features**: if the package uses `FoundryFeaturesOptInKeys`, `AgentDefinitionFeatureKeys`, or `Foundry-Features` headers, document:
   - Which sub-clients auto-set the header (check `*Impl.java` for hardcoded `foundryFeatures` strings).
   - Which accept it as an optional parameter.
   - List known flag values.
6. **OpenAI direct-usage snippets**: if the builder URL construction changed (e.g., `/openai` → `/openai/v1`), update the snippet and surrounding prose. Remove references to removed imports like `AzureUrlPathMode` or `AzureOpenAIServiceVersion` if they no longer apply.

### Determining preview tools

For the `azure-ai-agents` package, look at classes extending `com.azure.ai.agents.models.Tool`:

```bash
# Read the Tool.java discriminator to find all subtypes
grep -A1 'equals(discriminatorValue)' src/main/java/com/azure/ai/agents/models/Tool.java
```

Tools whose discriminator value or class name contains `preview` are preview tools. All others are GA.

### Determining preview operation groups

Check which `*Impl.java` files hardcode a `foundryFeatures` value:

```bash
grep -rl "final String foundryFeatures" src/main/java/*/implementation/*.java
```

Those operation groups are preview and auto-opt-in. Also check convenience client classes for `FoundryFeaturesOptInKeys` parameters — those are opt-in by caller.

## Notes

- **All edits are additive.** Never remove or rewrite existing content unless the user explicitly approves it after being consulted in Step 2.
- When the PR diff is too large for `gh pr diff` (HTTP 406), use `gh api .../pulls/<number>/files --paginate` instead.
- Paginate with `--paginate` and page with `?per_page=100&page=N` as needed.
- Always read the existing CHANGELOG and README **before** editing to avoid duplicating entries or breaking structure.
- If the PR title/body provides a summary, use it as a starting point but verify against the actual file changes.
