# Agentic Workflows

This directory contains [GitHub Agentic Workflows](https://github.github.com/gh-aw/introduction/overview/)
(`gh-aw`) — LLM-powered GitHub Actions workflows that automate discovery and triaging of
documentation and code quality gaps.

## How gh-aw works

Each workflow consists of two files:

| File | Purpose |
|------|---------|
| `<name>.md` | Human-editable source: YAML front-matter (triggers, permissions, safe-outputs, tools) + Markdown prompt body |
| `<name>.lock.yml` | Compiled GitHub Actions workflow generated from the `.md` by `gh aw compile`. **Do not edit by hand.** |

The LLM prompt is the entire Markdown body of the `.md` file. It is loaded at runtime into the
compiled workflow so only the `.md` needs editing to change agent behavior.

## Workflows

### `update-samples-and-docs`

**Purpose:** On every push to `main`, detect documentation and sample gaps introduced by the
commit, then file a focused GitHub issue with a detailed implementation guide. Copilot coding
agent can be assigned to the issue to auto-implement the fix.

**Flow:**
1. Analyzes the push diff to find new/changed public APIs, types, or methods
2. Scopes work to the changed `sdk/<service>/<package>/` directory using `ci.yml` package maps
3. Checks `README.md` completeness against the canonical section structure
4. Checks Java snippet markers (`// BEGIN: readme-sample-X` / `// END: readme-sample-X`) for orphans
5. Files one GitHub issue with an `Implementation Guide` block
6. Dispatches the issue-triage workflow to label and assign the issue

**Safe outputs:** `create-issue` (max 1 per run), `noop` (if nothing to do)

## Setup

### Prerequisites

- Install the [GitHub CLI](https://cli.github.com/)
- Install the `gh-aw` extension:
  ```bash
  gh extension install github/gh-aw
  ```

### Required repository secrets

Configure these in **Settings → Secrets and variables → Actions** for the repository:

| Secret | Description |
|--------|-------------|
| `COPILOT_GITHUB_TOKEN` | GitHub token that has GitHub Copilot access (used to run the Copilot CLI agent) |
| `GH_AW_GITHUB_TOKEN` | GitHub token used by the MCP server and safe-outputs processor (needs `issues:write`, `contents:read`) |

### Compiling a workflow

After editing an `.md` workflow definition, regenerate its lock file:

```bash
gh aw compile .github/workflows/update-samples-and-docs.md
```

Commit both the `.md` and the updated `.lock.yml` together.

### Testing locally

```bash
# Validate the workflow definition without compiling
gh aw validate .github/workflows/update-samples-and-docs.md

# Dry-run the prompt rendering
gh aw render .github/workflows/update-samples-and-docs.md
```

## Editing guidelines

- Only edit the `.md` file — never edit `.lock.yml` by hand
- After every edit, run `gh aw compile` and commit the new lock
- Keep the prompt body focused: the agent reads everything between `---` fences as its system prompt
- The `safe-outputs` section in the front-matter controls what the agent is allowed to produce
  (creating issues, sending no-ops, etc.) — change it carefully
- `permissions: read-all` at the top means the workflow itself has read-only access to the repo;
  all write operations go through the safe-outputs MCP server which validates them
