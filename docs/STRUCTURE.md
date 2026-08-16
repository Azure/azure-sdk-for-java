# Repository Documentation Structure

This document explains the documentation placement decisions for the Azure SDK for Java repository —
what belongs in this `docs/` hub, what stays alongside individual libraries, and what lives in
engineering tooling directories.

---

## Principles

1. **LLM / agent discoverability**: A single entry point (`docs/README.md`) that indexes everything.
2. **Don't move what already works**: Files with stable external links (aka.ms shortlinks, external references) stay in place; `docs/` links to them.
3. **Avoid duplication**: Content lives in exactly one canonical location; everywhere else is a link.
4. **Respect existing conventions**: GitHub standard files (`SECURITY.md`, `CODE_OF_CONDUCT.md`, `CONTRIBUTING.md`) stay at the root.

---

## What Lives Where

### `docs/` (this directory) — Central Documentation Hub

**Put here:**
- Cross-cutting contributor guides that apply to the whole repo (building, testing, versioning, code quality)
- Summaries / in-repo copies of wiki content that contributors need offline
- User-facing guides that span multiple SDK libraries
- Documentation placement decisions (this file)

**Do not put here:**
- Library-specific documentation (use the library's own `README.md` / `CHANGELOG.md`)
- Engineering tooling scripts and pipelines (use `eng/`)
- GitHub-specific files like issue templates or PR templates (use `.github/`)

---

### `sdk/<service>/<library>/` — Per-Library Documentation

Each library directory must contain:

| File | Purpose |
|------|---------|
| `README.md` | Getting started, Key concepts, Examples, Troubleshooting, Next steps |
| `CHANGELOG.md` | Release history following the standard format |
| `SAMPLE.md` (management libs) | Curated code samples |

These files are generated / maintained per-library and are **not** pulled into `docs/`.

---

### `eng/` — Engineering System

Contains CI/CD pipeline definitions, scripts, and build tooling. Content here is often
synced from the shared `azure-sdk-tools` repo and should not be edited directly unless
the change originates in that repo.

Relevant sub-directories:

| Path | Contents |
|------|---------|
| `eng/common/` | Shared engineering scripts (synced from azure-sdk-tools) |
| `eng/common/instructions/` | GitHub Copilot instruction files for SDK workflows |
| `eng/common/knowledge/` | TypeSpec and SDK generation reference docs |
| `eng/common/TestResources/` | Test resource provisioning scripts |
| `eng/pipelines/` | Azure DevOps pipeline YAML files |
| `eng/versioning/` | Version text files and update tooling |
| `eng/lintingconfigs/` | CheckStyle and SpotBugs configuration |

---

### `.github/` — GitHub Tooling

| Path | Contents |
|------|---------|
| `.github/copilot-instructions.md` | Primary Copilot system prompt |
| `.github/copilot-agents/` | Copilot agent definition files |
| `.github/ISSUE_TEMPLATE/` | GitHub issue templates |
| `.github/PULL_REQUEST_TEMPLATE.md` | PR description template |
| `.github/CODEOWNERS` | Code ownership assignments |

---

## Decision Log

| Decision | Rationale |
|----------|-----------|
| New `docs/` root directory, not expanding existing `doc/` | Avoids disrupting `aka.ms` shortlinks in `doc/`; signals a fresh, intentional structure |
| Wiki content summarized in-repo, not fully cloned | Wiki is the long-form reference; in-repo summaries give offline/LLM access to key facts without sync burden |
| `CONTRIBUTING.md` kept at root | GitHub convention; widely linked externally |
| `SECURITY.md` kept at root | GitHub security advisory convention |
| Per-library READMEs stay in `sdk/` | Proximity to code makes them easier to keep current; auto-generation tooling writes them |
