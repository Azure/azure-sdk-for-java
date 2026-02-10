# Azure SDK for Java — Code Structure

Azure SDK packages contain two kinds of code that work together:

## 1. Generated Code

Code **automatically produced** by the TypeSpec Java emitter (or AutoRest) from the API specification (TypeSpec or Swagger).

- Located in `src/main/java/<namespace>/` (alongside customization code)
- Regenerated every time the spec changes and the SDK generation pipeline runs
- Includes client classes, models, serialization, and service method signatures
- **Should not be hand-edited** — changes will be overwritten on the next generation

## 2. Customization Code

Code **hand-written by SDK developers** to extend, override, or augment the generated code.

- Typically placed in a dedicated customization package (e.g., `com.azure.resourcemanager.yourservice.customization`)
- Used for convenience methods, custom serialization, helper types, validation, or API surface adjustments
- Preserved across regeneration when `partial-update: true` is set in `tspconfig.yaml`

### Common Use Cases

| Use Case | Example |
|---|---|
| Convenience methods | Wrapping complex generated calls into simpler APIs |
| Custom serialization | Overriding default JSON serialization for specific models |
| API surface adjustments | Hiding internal models, renaming methods for clarity |
| Validation | Adding client-side validation not expressed in the spec |
| Helper types | Builders, fluent interfaces, or domain-specific utilities |

## Configuration

To enable customization in a TypeSpec-generated package, set these options in `tspconfig.yaml`:

```yaml
options:
  "@azure-tools/typespec-java":
    namespace: "com.azure.resourcemanager.yourservice"
    customization-class: "com.azure.resourcemanager.yourservice.customization"
    partial-update: true
```

| Option | Purpose |
|---|---|
| `customization-class` | Points to the Java package containing your hand-written customization classes. The generator compiles and applies these during code generation. |
| `partial-update` | When `true`, the generator preserves your custom files instead of overwriting the entire output directory. **Required** to keep customization code across regenerations. |

## How They Interact

```
┌─────────────────────────────────────────────┐
│            TypeSpec / Swagger Spec           │
└──────────────────────┬──────────────────────┘
                       │ generation pipeline
                       ▼
┌─────────────────────────────────────────────┐
│              Generated Code                 │
│  (clients, models, serialization, methods)  │
└──────────────────────┬──────────────────────┘
                       │ compiled together
                       ▼
┌─────────────────────────────────────────────┐
│            Customization Code               │
│  (extends, overrides, augments generated)   │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│           Final SDK Package                 │
└─────────────────────────────────────────────┘
```

When the spec changes, the generated code is regenerated. If the customization code references types, methods, or properties that were renamed, removed, or restructured in the new generated code, **compilation errors** will occur. See the [Troubleshooting Guide — Customization Errors](sdk-generation-pipeline-troubleshooting.md#2-customization-errors) for how to diagnose and fix these.
