# Phase 2: Generate References 📚

> 📍 **Phase 2 — Generate References** | Create supporting reference documents.

## references/architecture.md

Generate from the package scan. Include:

- **Repository layout** — directory tree with generated/hand-written annotations
- **Source layout** — package structure under `src/main/java/`
- **Code generation** — toolchain (`TypeSpec → emitter → src/`), `tsp-location.yaml` format
- **Generated vs custom** — table showing mechanism, location, when to use
- **Public client types** — all sync/async client classes and their purpose
- **Service version management** — how the version enum works, customization interaction
- **Key supporting files** — table of important files and their purpose
- **Dependencies** — compile and test dependencies
- **Build and test commands** — exact Maven commands

**Important**: Only include information that is accurate based on scanning the actual code. Mark anything uncertain with `<!-- TODO: Verify -->`.

## references/customizations.md (if customizations exist)

Generate from reading the actual customization file. For each method:

- **Problem**: What issue does this customization solve?
- **Solution**: What AST manipulation does it perform?
- **When to update**: What changes in the generated code would break this?
- **Code example**: Show the key JavaParser calls

Also include:
- **Common customization patterns** — table of JavaParser operations (rename, hide, remove, add)
- **Adding a new customization** — step-by-step
- **Removing a customization** — step-by-step
- **Troubleshooting** — silent failures, syntax errors, stale references
- **Quick-reference checklist** — post-regeneration verification steps

## Step 1 — Present

Print the proposed reference files content.

## Step 2 — CONFIRM

Question: "Create these reference files now (recommended), edit first, or skip?"

📍 **Phase 2 complete** | Created: references/ | Next: Phase 3

---
## → Next: Phase 3 — Validate
Read [03-validate.md](03-validate.md) and begin immediately.
