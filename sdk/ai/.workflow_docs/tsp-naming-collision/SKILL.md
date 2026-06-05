---
name: tsp-naming-collision
description: Fix Java codegen parameter names that end with a numeric suffix (e.g. createAgentRequest1) caused by TypeSpec model names colliding with synthetic body type names. Use when generated Java client methods have parameter names ending in '1'.
---

# Fix TypeSpec Naming Collisions in Java Codegen

Fix parameter and implementation-model names that end with a numeric suffix (e.g. `createAgentRequest1`) in generated Java client code. This happens when a TypeSpec **named `model`** collides with the **synthetic body type** the Java codegen creates for an operation that spreads that model.

## Root Cause

When a TypeSpec route **spreads** a named model into an operation's parameters (via `...ModelName`), the Java codegen creates a synthetic body type to hold the body properties. It names this type `{OperationName}Request` (PascalCase of the operation name + `Request`). If a TypeSpec `model` already has that exact name, the codegen resolves the collision by appending `1`.

Example collision:
- TypeSpec model: `CreateAgentRequest`
- Operation: `createAgent` spreads `...CreateAgentRequest`
- Codegen synthetic body: wants name `CreateAgentRequest` → collision → `CreateAgentRequest1`
- Result: parameter `createAgentRequest1`, implementation class `CreateAgentRequest1.java`

> **Why aliases don't collide:** TypeSpec `alias` declarations don't occupy a name in the type namespace, so there is no collision. If the upstream spec used `alias CreateAgentRequest = { ... }` instead of `model CreateAgentRequest { ... }`, no fix would be needed.

## Preconditions

- You must be in the directory that contains `tsp-location.yaml`.
- The TypeSpec must already be synced locally into `TempTypeSpecFiles/`. If not, run `tsp-client sync` first.
- Identify the `client.tsp` file inside `TempTypeSpecFiles/` (usually under a subdirectory like `sdk-agents/`). This is the customization file where fixes are applied.

## Important: TempTypeSpecFiles is volatile

`TempTypeSpecFiles/` is regenerated on every `tsp-client sync` or `tsp-client update`. **Changes made only in `TempTypeSpecFiles/` will be lost.** Always apply the same edits to the corresponding `client.tsp` in a local checkout of `Azure/azure-rest-api-specs` (if available) so the changes can be committed to a PR.

## Workflow

### 1. Identify affected operations

Search the generated Java client classes for parameter names ending with `1`:

```bash
grep -n 'Request1[,)]' src/main/java/com/azure/ai/agents/*Client.java
```

Also check for implementation model classes with the `1` suffix:

```bash
find . -name "*Request1.java" -path "*/implementation/models/*"
```

Collect the list of affected names (e.g. `createAgentRequest1`, `updateAgentRequest1`).

### 2. Trace back to the TypeSpec models

For each affected parameter, find the TypeSpec model that causes the collision. The model name matches the parameter name (PascalCase, without the `1`).

Search the `.tsp` files:

```bash
grep -rn "model CreateAgentRequest\|model UpdateAgentRequest" TempTypeSpecFiles/ --include="*.tsp"
```

Confirm the model is used via spread (`...ModelName`) in the route definitions:

```bash
grep -rn "CreateAgentRequest\|UpdateAgentRequest" TempTypeSpecFiles/ --include="*.tsp"
```

Verify the model is a `model` (not an `alias`). Only named `model` types cause collisions.

### 3. Add `@@clientName` overrides

Edit the `client.tsp` customization file in `TempTypeSpecFiles/`. Add a `@@clientName` directive for each colliding model to give it a different client-side name. This frees the original name for the codegen's synthetic body type.

Use a consistent naming convention. Recommended: rename `*Request` → `*Input`:

```tsp
// Rename request models to avoid collision with synthetic body types generated
// by the Java codegen. The codegen names synthetic bodies as {OperationName}Request,
// which clashes with the identically-named TypeSpec models, causing a "1" suffix.
@@clientName(CreateAgentRequest, "CreateAgentInput");
@@clientName(UpdateAgentRequest, "UpdateAgentInput");
```

> **Note:** These models are typically not emitted as public Java classes — they only exist to be spread into operations. The rename is purely internal and does not affect the public API surface.

### 4. Regenerate and verify

Generate with `--save-inputs` to preserve the edited TypeSpec files:

```bash
tsp-client generate --save-inputs
```

Verify the `1` suffix is gone:

```bash
# Should return zero matches
grep -c "Request1" src/main/java/com/azure/ai/agents/*Client.java

# Old *Request1.java files should no longer exist
find src -name "*Request1.java" -path "*/implementation/models/*"

# New clean-named files should exist
find src -name "*Request.java" -path "*/implementation/models/*"
```

Compile to confirm no breakage:

```bash
mvn compile -Denforcer.skip=true -Dcodesnippet.skip=true -Dcheckstyle.skip=true \
  -Dspotbugs.skip=true -Dspotless.skip=true -Drevapi.skip=true -Djacoco.skip=true \
  -Dmaven.javadoc.skip=true -Dshade.skip=true -Danimal.sniffer.skip=true
```

### 5. Apply changes to the local spec repo (if available)

If the user has a local checkout of `Azure/azure-rest-api-specs`, apply the **same `client.tsp` edits** there. Derive the file path from `tsp-location.yaml`:

- `directory` field gives the relative spec path (e.g. `specification/ai-foundry/data-plane/Foundry/src/sdk-agents`)
- The file to edit is `client.tsp` inside that directory

For example, if the local repo is at `<local-spec-repo>`:

```
<local-spec-repo>/specification/ai-foundry/data-plane/Foundry/src/sdk-agents/client.tsp
```

Verify the file exists before editing. If it doesn't, warn the user and print the expected path.

### 6. Full round-trip from the remote spec (optional)

If the user wants to validate the fix end-to-end from the remote repo:

1. Commit and push the `client.tsp` changes in the spec repo
2. Get the new commit hash
3. Update the `commit:` field in `tsp-location.yaml` with the new hash
4. Run `tsp-client update` to sync and regenerate from the remote
5. Verify the `1` suffix is gone and the build compiles

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `1` suffix persists after adding `@@clientName` | The `@@clientName` target doesn't match the TypeSpec model name exactly | Double-check the model name is the TypeSpec name (not the Java name); names are case-sensitive |
| New suffix appears (e.g. `2`) | Multiple models collide with the same synthetic name | Ensure every colliding model has a unique `@@clientName` |
| Build fails after regeneration | Handwritten code references the old `*1` names | Update any manual references in custom client code, tests, or samples |
| Changes lost after `tsp-client sync` | `TempTypeSpecFiles/` was overwritten | Apply changes to the spec repo `client.tsp` (see step 5) |
