# azure-ai-projects — Customizations Reference

**File:** `customizations/src/main/java/ProjectsCustomizations.java`

## Current Status: Mostly Inactive

The customizations file exists and overrides `customize(LibraryCustomization, Logger)`, but the only internal method — `removeConversationsClientBuilder()` — is **commented out**.

This means the file is effectively a no-op placeholder. It is invoked during `tsp-client update` but applies no transformations.

## Inactive Method: `removeConversationsClientBuilder()`

**Purpose:** Would remove the `ConversationsClientBuilder` class file from the generated output.

**Why commented out:** The ConversationsClientBuilder may have been removed from the TypeSpec, or the removal is being handled differently. Check whether `ConversationsClientBuilder.java` still exists in the generated output — if it doesn't, this customization is no longer needed.

**When to re-enable:** If a new codegen run re-introduces a class that should be suppressed.

## Adding a New Customization

1. Add a new private method to `ProjectsCustomizations.java`
2. Call it from `customize(LibraryCustomization, Logger)`
3. Run `tsp-client update` to verify the customization applies cleanly
4. Test the generated code compiles: `mvn compile -Dcheckstyle.skip -Dspotbugs.skip`

## Comparison with azure-ai-agents

The agents package has heavier customizations (enum renames, polling strategy modifications). The projects package relies more on TypeSpec-level customizations (`client.tsp` / `client.java.tsp`) and hand-written bridge classes instead of AST post-processing. This is the preferred approach — consider migrating agents customizations to TypeSpec decorators where possible.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Unwanted class appears in generated output | Customization that removes it is commented out | Re-enable the removal method |
| Build fails after regen with no code changes | Inactive customization may need re-activation | Check if a previously-suppressed class reappeared |
