---
name: search-documents
description: 'Post-regeneration customization guide for azure-search-documents SDK. After running tsp-client update, consult this skill to re-apply search-specific customizations and produce a production-ready SDK. WHEN: regenerate search SDK; search tsp-client update; fix search customization errors; search API version update; search SDK release; update search service version.'
---

# azure-search-documents — Package Skill

<!-- TODO: Domain experts should fill in the sections below with tribal knowledge
     specific to this package. -->

## Common Pitfalls

<!-- TODO: List the most dangerous mistakes an agent can make with this package.
     Put these BEFORE the workflow — agents read them first. -->

## Architecture

<!-- TODO: High-level layout. Where does generated code live? What's hand-authored?
     How do they interact? Document the customizations/ directory and SearchCustomizations.java. -->

### Source Layout

<!-- TODO: Show the directory tree. Mark files as GENERATED vs hand-written. -->

### Generated vs Custom Code

<!-- TODO: Explain the @Generated annotation. Methods WITH @Generated are auto-updated
     by the generator. Methods WITHOUT @Generated in generated files are hand-written
     convenience wrappers that need manual updates after regeneration. -->

## Regeneration Workflow

<!-- TODO: The exact commands to regenerate. What breaks after regeneration?
     What needs manual fixup? Include gated phases. -->

## Key Files

<!-- TODO: The files an agent should read before making changes, and what each one does.
     Include tsp-location.yaml, SearchCustomizations.java, pom.xml, CHANGELOG.md. -->

## Service Version Management

<!-- TODO: How SearchServiceVersion enum works. How includeOldApiVersions() customization
     adds backward-compat versions. Deduplication guard needed. -->

## Post-Regeneration Customizations

<!-- TODO: Document each customization method in SearchCustomizations.java:
     - hideGeneratedSearchApis
     - addSearchAudienceScopeHandling
     - includeOldApiVersions
     - removeGetApis
     - hideWithResponseBinaryDataApis (the most fragile one) -->

## Testing Notes

<!-- TODO: How to run tests, recorded test setup with assets.json,
     environment requirements for live tests. -->

## References

| File | Contents |
|------|----------|
| [references/architecture.md](references/architecture.md) | Source layout, package map, dependencies |
| [references/customizations.md](references/customizations.md) | JavaParser AST patterns, per-customization update guidance |
