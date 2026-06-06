---
name: azure-ai-projects
description: 'Post-regeneration guide for azure-ai-projects SDK. Covers bridge class maintenance, openai-java pipeline adapter, and multi-service client architecture. WHEN: regenerate azure-ai-projects; fix azure-ai-projects build errors; azure-ai-projects tsp-client update; add azure-ai-projects feature; modify azure-ai-projects.'
---

# azure-ai-projects — Package Skill

> This skill activates after standard generation tools (`tsp-client update`, `azsdk_customized_code_update`) have been tried. It covers what they can't solve: HTTP pipeline bridge maintenance, multi-service client patterns, and package-specific error diagnosis.

## Common Pitfalls

- **This package depends on `azure-ai-agents`.** Changes to the agents package (especially model renames or module-info changes) can break this package. Always build both together.
- **Never serialize openai-java types with `BinaryData.fromObject()`.** Use `OpenAIJsonHelper.toBinaryData()` from the agents package — the default Jackson ObjectMapper cannot handle Kotlin internals.
- **The HTTP pipeline bridge is hand-written and fragile.** `HttpClientHelper` and `AzureHttpResponseAdapter` adapt Azure's HTTP stack to openai-java's contract. If either SDK's HTTP interfaces change, these break silently at runtime, not compile time.
- **Customizations are currently inactive.** `ProjectsCustomizations.java` has a commented-out method. Check it after regen to see if it needs re-enabling.
- **Follow codegen survival rules for ALL manual edits.** See `codegen-survival-rules` shared skill.

## Architecture

11 sync + 11 async clients (Connections, Datasets, Deployments, EvaluationRules, EvaluationTaxonomies, Evaluators, Indexes, Insights, RedTeams, Schedules, Skills), 113 generated models, 3 hand-written bridge classes. Depends on `azure-ai-agents` and `azure-storage-blob`.

See [references/architecture.md](references/architecture.md) for source layout and bridge class inventory.

## After Regeneration

| Error location | What it means | Where to fix |
|---|---|---|
| Generated file, `@Generated` method | Customization produced broken output | Check `ProjectsCustomizations.java` (mostly inactive — may need re-enabling) |
| Generated file, method WITHOUT `@Generated` | Hand-written wrapper references changed types | Fix the hand-written method |
| Hand-written bridge class | References removed/renamed types in Azure core or openai-java | Fix the bridge class (TokenUtils, HttpClientHelper, AzureHttpResponseAdapter) |
| Module-info compilation error | Missing requires/exports after new dependency added | Update `module-info.java` |

**Post-regen workflow:** `tsp-naming-collision` → `tsp-type-override` → compile → fix bridge classes (all shared skills under `sdk/ai/.github/skills/`).

## Testing Notes

- Test base: `ClientTestBase extends TestProxyTestBase` with RECORD/PLAYBACK/LIVE modes
- Recordings: `assets.json` → `Azure/azure-sdk-assets` (tag prefix: `java/ai/azure-ai-projects`)
- Custom sanitizers for sensitive data, custom matchers excluding Stainless metadata headers
- Serialization tests exist for type-overridden models (e.g., `WeeklyRecurrenceScheduleSerializationTest`)

## References

| File | Contents |
|---|---|
| [references/architecture.md](references/architecture.md) | Source layout, client inventory, bridge classes, module-info |
| [references/customizations.md](references/customizations.md) | ProjectsCustomizations.java status, update triggers |
