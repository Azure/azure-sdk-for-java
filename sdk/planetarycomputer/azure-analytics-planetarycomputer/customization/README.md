# Customization – Interim Workaround

> **This entire `customization/` directory is temporary and will be removed once the
> upstream fix is shipped in the Java code generator.**

## Problem

The Java code generator (`@azure-tools/typespec-java`) emits `TypeReference<T>` entries
for discriminated-union subtypes in a non-deterministic order. In this SDK the affected
union is `StacItemOrStacItemCollection` (subtypes `StacItem` and `StacItemCollection`),
which appears in `DataClient.java` and `DataAsyncClient.java`.

Because the ordering varies between regeneration runs, CI detects a spurious diff and
fails the `Compare-CurrentToCodegeneration.ps1` check even when no actual API change
has occurred.

## How the workaround works

`PlanetaryComputerCustomizations.java` is a post-generation customization class that:

1. Scans `DataClient.java` and `DataAsyncClient.java` for consecutive lines containing
   `TypeReference<…>` patterns.
2. Sorts each group of such lines alphabetically by the generic type parameter name.
3. Writes the sorted content back, producing a deterministic output regardless of the
   generator's internal iteration order.

## Connection to the spec repo

This customization is referenced in the TypeSpec emitter configuration at:

```
azure-rest-api-specs/specification/orbital/Microsoft.PlanetaryComputer/tspconfig.yaml
```

under the `@azure-tools/typespec-java` section:

```yaml
customization-class: "customization/src/main/java/PlanetaryComputerCustomizations.java"
```

If you remove this directory, the corresponding `customization-class` line and its
comment block in `tspconfig.yaml` must also be removed.

## Cleanup checklist

Once the Java code generator ships deterministic `TypeReference` ordering:

- [ ] Delete this `customization/` directory (including `pom.xml`, source files, and
      this README).
- [ ] Remove the `customization-class` key and its comment block from `tspconfig.yaml`
      in the spec repo (`specification/orbital/Microsoft.PlanetaryComputer/tspconfig.yaml`).
- [ ] Regenerate the SDK and confirm CI passes without the customization.
