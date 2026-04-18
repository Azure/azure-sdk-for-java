---
name: create-package-skill
description: 'Interactive wizard that walks service teams through creating a package-specific skill for their Azure SDK package. Scans the package, detects customization patterns, scaffolds a SKILL.md with references, validates with vally lint, and registers in find-package-skill. WHEN: create package skill; add service skill; bootstrap skill for package; new package skill; skill for my SDK package; write skill for search; write skill for cosmos.'
---

# Create Package Skill Wizard

> **Minimal beats comprehensive. Human-written beats auto-generated. Scaffold and iterate.**

> Skills encode tribal knowledge — the "I wish someone had told me" stuff that's hard to learn from just reading code. Focus on what's non-obvious and package-specific.

## Interaction Protocols

**CONFIRM Protocol** (asset-producing steps — creating files):
1. PRESENT the proposed assets and explain why.
2. ASK exactly one question: "Create now (recommended), edit first, or skip?"
3. ACT immediately. Create → write files this turn. Edit → refine, re-ask. Skip → move on.

**DECIDE Protocol** (informational/correction steps — no files created):
1. PRESENT the information or findings.
2. ASK one specific question appropriate to the decision.
3. PROCEED based on the answer.

One question at a time. Respect "skip" — never re-ask or defer.

## Wizard Flow

Run each phase in order. **Progressive loading:** Read only the current phase file.

| Phase | Description | Instructions |
|---|---|---|
| **Phase 0** | 🧭 Scan Package — detect architecture, customizations, key files | [phases/00-scan-package.md](phases/00-scan-package.md) |
| **Phase 1** | 📝 Scaffold SKILL.md — generate skill with common pitfalls, architecture, workflow | [phases/01-scaffold-skill.md](phases/01-scaffold-skill.md) |
| **Phase 2** | 📚 Generate References — create architecture.md and customizations.md | [phases/02-generate-references.md](phases/02-generate-references.md) |
| **Phase 3** | Validate -- run vally lint | [phases/03-validate.md](phases/03-validate.md) |
| **Phase 4** | 📋 Register — add to find-package-skill table | [phases/04-register.md](phases/04-register.md) |

## Guardrails

**Content:**
- Every line must be non-obvious and package-specific. No generic Java/SDK patterns.
- SKILL.md should be under 500 tokens (soft limit). Move details to references/.
- References under 1000 tokens each. Split if larger.
- Never duplicate what's already in `.github/copilot-instructions.md` or shared skills.

**Relationship to existing SDK tools:**
- Package skills **complement** the Azure SDK MCP tools (`azsdk_package_generate_code`, `azsdk_package_build_code`, etc.) and the `generate-sdk-locally` shared skill — they do NOT replace them.
- MCP tools handle deterministic operations (generate, build, test). Package skills provide the reasoning context an agent needs to use those tools correctly for a specific package.
- Never redefine how generation, building, or testing works — reference the existing tools instead (e.g., "Run `tsp-client update`", not custom generation steps).
- If a workflow step is already handled by an MCP tool or shared skill, just reference it — don't re-document it.

**Structure:**
- Skill directory: `sdk/<service>/<package>/.github/skills/<skill-name>/`
- Directory name MUST match `name` field in frontmatter (vally lint enforces this).
- Use semicolons to separate trigger phrases in description (YAML-safe).

**Security:**
- Never embed secrets or credentials in skill content.
- Never instruct agents to bypass CI, linters, or checkstyle rules.
- Never instruct agents to edit generated files directly — always route through customizations.

## Key Principles (from eval data)

Our eval showed that skill **structure** matters more than **volume**:

| Pattern | Impact |
|---|---|
| "Common Pitfalls" section at the TOP | Agent reads pitfalls before analyzing errors → correct diagnosis |
| "Check X FIRST" directives | Changes agent default from "fix the error location" to "check the customization layer" |
| Error categorization tables | Gives agent a decision framework, not just procedures |
| `@Generated` annotation guidance | Agent distinguishes auto-updated vs hand-written methods in generated files |

## References (load on demand)

- [references/skill-template.md](references/skill-template.md) -- SKILL.md template with required sections
- [references/validation-tools.md](references/validation-tools.md) -- vally lint, CI workflow setup
