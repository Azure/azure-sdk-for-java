# Phase 3: Validate

> 📍 **Phase 3 — Validate** | Run structural validation on the new skill.

> 📖 Read `references/validation-tools.md` for tool details.

## Step 1 — Structural Validation (vally lint)

Run vally lint against the skill:

```powershell
# If vally is installed (npm package from microsoft/evaluate)
vally lint sdk/<service>/<package>/.github/skills/<skill-name>

# If vally is not installed, check manually:
# - SKILL.md exists with valid YAML frontmatter
# - name field matches directory name
# - All markdown links in SKILL.md resolve to existing files
# - No orphaned files in references/ (every file linked from SKILL.md)
```

Expected: 3/3 checks pass (spec-compliance, valid-refs, orphan-files).

**Common failure**: `name-directory-mismatch` -- the `name` in frontmatter doesn't match the directory name. Fix by renaming the directory to match the Maven artifactId.

## Step 2 — Token Budget Check

Verify token budgets:
- SKILL.md: under 500 tokens (soft), under 5000 (hard)
- Each reference file: under 1000 tokens

If over budget, split content into additional reference files.

## Step 3 — DECIDE

Present validation results. If all pass:
Question: "Validation passed. Proceed to register the skill?"

If failures exist, present them and ask:
Question: "These issues need fixing. Fix now, or skip validation?"

📍 **Phase 3 complete** | Validation: pass/fail | Next: Phase 4

---
## → Next: Phase 4 — Register
Read [04-register.md](04-register.md) and begin immediately.
