# azure-ai-agents — Customizations Reference

**File:** `customizations/src/main/java/AgentsCustomizations.java`

This file applies AST transformations during `tsp-client update` / `tsp-client generate`. It runs after code generation and modifies the emitted Java source.

## Customization Methods

### `renameImageGenToolSize()`

**Problem:** The generated `ImageGenToolSize` enum entries use raw numeric names derived from the TypeSpec values (e.g., `ONE_ZERO_TWO_FOURX_ONE_ZERO_TWO_FOUR` for `1024x1024`), which are unreadable.

**Solution:** Renames enum constants to descriptive names:

| Generated Name | Renamed To |
|---|---|
| `ONE_ZERO_TWO_FOURX_ONE_ZERO_TWO_FOUR` | `RESOLUTION_1024_X_1024` |
| `ONE_ZERO_TWO_FOURX_ONE_FIVE_THREE_SIX` | `RESOLUTION_1024_X_1536` |
| `ONE_FIVE_THREE_SIXX_ONE_ZERO_TWO_FOUR` | `RESOLUTION_1536_X_1024` |

**When to update:** If the TypeSpec adds new image sizes or changes the naming scheme.

**Could this be a TypeSpec customization?** Yes — `@@clientName` in `client.tsp` could rename enum values. Consider migrating.

### `modifyPollingStrategies()`

**Problem:** The generated `OperationLocationPollingStrategy` and `SyncOperationLocationPollingStrategy` need two additions: (1) Foundry-Features headers on polling requests, and (2) remapping of custom terminal status values (`completed`, `superseded`).

**Solution:**
- Modifies the constructor to wrap the pipeline context with `AgentsServicePollUtils.withFoundryFeatures()`
- Overrides the `poll()` method to call `AgentsServicePollUtils::remapStatus` on the poll response

**When to update:**
- If the generated polling strategy class names or constructors change
- If new Foundry-Features values are needed for polling
- If the service adds new terminal poll states

**Could this be a TypeSpec customization?** Partially — the Foundry-Features header could potentially be set via TypeSpec decorators, but the status remapping is Java-specific behavior.

## Adding a New Customization

1. Add a new private method to `AgentsCustomizations.java`
2. Call it from `customize(LibraryCustomization, Logger)`
3. Run `tsp-client update` to verify the customization applies cleanly
4. Test the generated code compiles: `mvn compile -Dcheckstyle.skip -Dspotbugs.skip`

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Customization silently does nothing | AST query doesn't match generated code structure | Update the JavaParser selectors to match the new generated code |
| Build fails in generated file with `@Generated` | Customization produced broken output | Fix the customization method's AST manipulation |
| Customization applies but creates duplicate code | Generated code already includes what the customization adds | Remove or guard the customization |
