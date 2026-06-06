---
name: azure-ai-agents
description: 'Post-regeneration guide for azure-ai-agents SDK. Covers openai-java dedup, bridge class maintenance, polling customizations, and codegen survival. WHEN: regenerate azure-ai-agents; fix azure-ai-agents build errors; azure-ai-agents tsp-client update; add azure-ai-agents feature; modify azure-ai-agents.'
---

# azure-ai-agents — Package Skill

> This skill activates after standard generation tools (`tsp-client update`, `azsdk_customized_code_update`) have been tried. It covers what they can't solve: openai-java dedup decisions, bridge class maintenance, and package-specific error diagnosis.

## Common Pitfalls

- **Always check for new openai-java duplicates after codegen.** Use `api-diff` → `dup-classes` → `dedup-openai` (shared skills) on every regeneration. New models may duplicate openai-java types and need suppression.
- **Never serialize openai-java types with `BinaryData.fromObject()`.** The default Jackson ObjectMapper cannot handle Kotlin `SynchronizedLazyImpl` fields. Use `OpenAIJsonHelper.toBinaryData()` which uses `ObjectMappers.jsonMapper()`.
- **Follow codegen survival rules for ALL manual edits.** Remove `@Generated`, place marker comments inside method bodies, not above signatures. See `codegen-survival-rules` shared skill.
- **Check `AgentsCustomizations.java` FIRST when generated files have errors.** The polling strategy and enum rename customizations can break if the generated code structure changes.
- **Do not suppress discriminator hierarchy types.** Classes extending `Tool`, `TextResponseFormatConfiguration`, or other polymorphic base types are structural equivalents, not actionable duplicates — the SDK's `fromJson` dispatch requires them.

## Architecture

5 sync + 5 async clients (Agents, AgentSessionFiles, MemoryStores, Responses, Toolboxes), 185 generated models, 7 hand-written bridge classes. Heavy openai-java integration (v4.14.0).

See [references/architecture.md](references/architecture.md) for source layout and bridge class inventory.

## After Regeneration

| Error location | What it means | Where to fix |
|---|---|---|
| Generated file, `@Generated` method | Customization produced broken output | Fix `AgentsCustomizations.java` |
| Generated file, method WITHOUT `@Generated` | Hand-written wrapper references changed types | Fix the hand-written method to match new generated signatures |
| Hand-written bridge class (OpenAIJsonHelper, etc.) | References removed/renamed generated types | Fix the bridge class |
| Model with openai-java type (toJson/fromJson) | Dedup serialization bridge broken by new fields | Update the `toJson`/`fromJson` bridge — see `dedup-openai` shared skill |

**Post-regen workflow:** `tsp-naming-collision` → `api-diff` → `dup-classes` → `dedup-openai` → `union-type-wrappers` → `codegen-survival-rules` (all shared skills under `sdk/ai/.github/skills/`).

## Post-Regeneration Customizations

See [references/customizations.md](references/customizations.md) for per-method documentation.

## Testing Notes

- Test base: `ClientTestBase extends TestProxyTestBase` with RECORD/PLAYBACK/LIVE modes
- Recordings: `assets.json` → `Azure/azure-sdk-assets` (tag prefix: `java/ai/azure-ai-agents`)
- Coverage: 0% enforced (pre-GA)
- JPMS workaround: `--add-opens com.azure.core/com.azure.core.implementation.util=ALL-UNNAMED`

## References

| File | Contents |
|---|---|
| [references/architecture.md](references/architecture.md) | Source layout, client inventory, bridge classes, module-info |
| [references/customizations.md](references/customizations.md) | AgentsCustomizations.java method docs, update triggers |
