# SKILL.md Template for Azure SDK Package Skills

Use this template when creating a new package skill. Replace placeholders with package-specific content.

```yaml
---
name: <group-id>:<artifact-id>
description: '<Brief description>. WHEN: regenerate <package>; modify <package>; fix <package> bug; add <package> feature; <package> tsp-client update.'
---
```

The `name` field and directory name MUST match the Maven package identifier (e.g., `com.azure:azure-search-documents`).

## Content Principles

- **Keep it static** -- no version numbers, no current API versions. Document design and patterns, not release state.
- **Prefer TypeSpec over Java customizations** -- always note when a customization could be a TypeSpec decorator instead.
- **Don't re-document MCP tools** -- the `generate-sdk-locally` skill and `azsdk_customized_code_update` handle generation workflows.
- **Focus on the convenience layer** -- what does the agent need to know to write/maintain convenience patterns correctly.

## Required Sections (in order)

### Common Pitfalls
List 3-5 most dangerous mistakes. This section MUST come first -- agents read it before analyzing errors.

### Architecture
Source layout, generated vs hand-written code, customization mechanism. No version numbers.

### After Regeneration
Error categorization table, service version management, breaking change detection. Do NOT re-document the generation/build/test steps -- those are in the shared skill.

### Post-Regeneration Customizations (if customizations exist)
Per-method documentation with "when to update" guidance and whether TypeSpec-level customization is possible instead.

### Testing Notes
Commands, recorded test setup, environment requirements.

### References
Table linking to references/*.md files.

## Structural Rules

| Rule | Enforced By |
|---|---|
| `name` matches directory name (= Maven package identifier) | `vally lint` |
| All markdown links resolve | `vally lint` |
| No orphaned reference files | `vally lint` |
| Code references still exist in codebase | Manual review |
| SKILL.md under 5000 tokens | `vally lint` |
| No version numbers or release-specific info | Manual review |
| Trigger phrases include package name | Manual review |
| No cross-language content | Manual review |
