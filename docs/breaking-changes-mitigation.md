# Azure Java Management SDK Breaking Changes Mitigation Guide

This guide helps review and classify breaking changes reported in management-plane SDK changelogs, with a focus on Swagger-to-TypeSpec migrations.

For Java management libraries, the main mitigation surfaces are:

1. `client.tsp` for Java-specific client customizations such as `@@clientName`, `@@clientLocation`, and `@@alternateType`
2. `tspconfig.yaml` for generator options under `@azure-tools/typespec-java`

Use this guide to decide whether a break should be:

1. mitigated through `client.tsp` or `tspconfig.yaml`
2. treated as expected and accepted
3. escalated because the generated public API shape likely indicates a TypeSpec or generator issue

Examples below use changelog bullets, but similar signatures can also appear in Revapi output.

## Adding Optional Parameter to Method

**Changelog Pattern**:

Adding an optional parameter causes the client method signature to change.

```md
#### `models.ContainerAppsRevisions` was modified

* `listRevisions(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listRevisions(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
```

**Spec Pattern**:

Find the corresponding operation in TypeSpec where a new optional parameter was introduced via `@added` versioning decorator.

```typespec
listRevisions is ArmResourceListByParent<
  Revision,
  Parameters = {
    /**
     * The filter to apply on the operation.
     */
    @added(Versions.v2026_01_01)
    @query("$filter")
    $filter?: string;
  },
  Response = ArmResponse<RevisionCollection>,
  Error = DefaultErrorResponse
>;
```

**Breaking**:

A public client method overload is removed and replaced with another overload that includes an extra parameter. Callers compiled against the previous signature fail to compile until they update call sites.

**Reason**:

Service added an optional parameter, which is not a breaking change in the REST API.

**Resolution**:

Preserve the method overload by configuring `tspconfig.yaml`:

```yaml
options:
  "@azure-tools/typespec-java":
    advanced-versioning: true
```

## Similar Old and New Public Types

**Changelog Pattern**:

One public type disappears and a very similar one appears:

```md
* `models.AssessmentResource` was removed
* `models.Assessment` was added
```

And it usually comes with a change of return type of some getters:

```md
* `models.AssessmentResource assessment()` -> `models.Assessment assessment()`
```

**Spec Pattern**:

```typespec
model Assessment {
  ...
}
```

**Breaking**:

The public Java type name changes, breaking imports and references.

**Reason**:

The generated Java type name changed during TypeSpec migration, or an incorrect Java `@@clientName` customization was applied.

**Resolution**:

If the new symbol is a model, restore the previous Java name:

```typespec
@@clientName(Microsoft.Example.Assessment, "AssessmentResource", "java");
```

If the model is not present, inspect the TypeSpec project’s back-compatibility customization file (if any) for an existing `@@clientName` mapping, and add a Java-specific override in `client.tsp`.

## Property or Accessor Renaming

**Changelog Pattern**:

An accessor disappears and a very similar accessor is added, usually because of case normalization or direct reuse of the spec spelling:

```md
* `resourceUri()` was removed
* `resourceURI()` was added
```

**Spec Pattern**:

Find the property on the corresponding model.

```typespec
model ExampleResource {
  resourceURI: string;
}
```

**Breaking**:

Getter, setter, and fluent `withXxx` method names change, breaking callers that compiled against the old accessor name.

**Reason**:

The generated Java property name changed during TypeSpec migration, or an incorrect Java `@@clientName` customization was applied.

**Resolution**:

Rename the property in Java with `@@clientName`:

```typespec
@@clientName(Microsoft.Example.ExampleResource.resourceURI, "resourceUri", "java");
```

## Pageable Wrapper Models Are Removed

**Changelog Pattern**:

List wrapper models disappear, usually with names ending in `List`, `ListResult`, or `ListResponse`:

```md
* `models.ReportResourceListResult` was removed
* `models.OperationListResult` was removed
* `models.ServiceCollection` was removed
```

**Spec Pattern**:

No spec lookup is usually required. This is emitter behavior.

**Breaking**:

Code that directly referenced the wrapper model no longer compiles.

**Reason**:

Emitter change during TypeSpec migration.

**Resolution**:

Accept this break. Callers are unlikely to use these wrapper models directly because pageable operations expose `PagedIterable` and `PagedFlux`.

## Constructors Changed to Private Access

**Changelog Pattern**:

Model constructors become non-public, and setters are removed:

```md
#### `models.ControlFamily` was modified

* `ControlFamily()` was changed to private access
* `withQueryExperiences(java.util.List)` was removed
```

**Spec Pattern**:

No spec lookup is usually required. This is emitter behavior.

**Breaking**:

Callers can no longer instantiate the model directly with `new`.

**Reason**:

Emitter change during TypeSpec migration.

**Resolution**:

Accept this break. Callers are unlikely to instantiate these model classes directly because they are typically output-only shapes.

## Manager Naming Changes

**Changelog Pattern**:

The root manager type changes:

```md
* `VoiceServicesManager` was removed
* `VoiceservicesManager` was added
```

**Spec Pattern**:

Check the service metadata and namespace used by the management package.

```typespec
@service(#{ title: "Voice Services" })
namespace Microsoft.VoiceServices;
```

**Breaking**:

The entry-point manager type changes, which breaks imports, factory creation, and any code that references the old manager name directly.

**Reason**:

`typespec-java` derives the management entry-point name from `service-name` metadata. If `service-name` is missing or configured incorrectly, the generated entry-point manager name changes.

**Resolution**:

Preserve the previous manager naming in `tspconfig.yaml`:

```yaml
options:
  "@azure-tools/typespec-java":
    service-name: Voice Services
```

## `serviceClient()` Return Type Changes

**Changelog Pattern**:

The manager remains, but the fluent `serviceClient()` accessor now returns a differently named generated client:

```md
* `fluent.AppComplianceAutomationManagementClient serviceClient()` -> `fluent.AppComplianceAutomationClient serviceClient()`
```

**Spec Pattern**:

Check the service namespace used to generate the fluent client type.

```typespec
namespace Microsoft.AppComplianceAutomation;
```

**Breaking**:

Callers that depend on the concrete fluent service client type must update their imports and type references.

**Reason**:

The generated fluent client name changed during TypeSpec migration, or an incorrect Java `@@clientName` customization was applied.

**Resolution**:

If the new Java client name ends with `ManagementClient`, no fix is needed. Suffix `ManagementClient` is preferred.

Otherwise, restore the target Java client name with `@@clientName`:

```typespec
@@clientName(Microsoft.AppComplianceAutomation, "AppComplianceAutomationManagementClient", "java");
```

## `java.lang.Object` Changes to `java.util.Map`

**Changelog Pattern**:

An accessor or fluent setter changes from `java.lang.Object` to `java.util.Map`:

```md
* `java.lang.Object metadata()` -> `java.util.Map metadata()`
```

**Spec Pattern**:

Find the related property, which is usually modeled as `Record<string, unknown>`.

```typespec
model ExampleResource {
  metadata?: Record<string, unknown>;
}
```

**Breaking**:

The Java API narrows from an opaque object to a map-shaped value, which can break callers that were passing or consuming arbitrary object graphs.

**Reason**:

Emitter change during TypeSpec migration. The property is being projected as `Map` instead of the previous opaque `Object` shape.

**Resolution**:

Force the Java client type back to the prior opaque shape:

```typespec
@@alternateType(Microsoft.Example.ExampleResource.metadata, unknown, "java");
```

## `java.lang.Object` Changes to `com.azure.core.util.BinaryData`

**Changelog Pattern**:

An accessor or fluent setter changes from `java.lang.Object` to `com.azure.core.util.BinaryData`:

```md
* `java.lang.Object payload()` -> `com.azure.core.util.BinaryData payload()`
```

**Spec Pattern**:

This usually points to an `unknown` payload.

```typespec
model ExampleResource {
  payload?: unknown;
}
```

**Breaking**:

Callers must now convert values to `BinaryData`, which changes both method signatures and the way payloads are created.

**Reason**:

Emitter change during TypeSpec migration. The unknown payload is being projected as `BinaryData` instead of the previous opaque `Object` shape.

**Resolution**:

Preserve the older object-based surface in `tspconfig.yaml`:

```yaml
options:
  "@azure-tools/typespec-java":
    use-object-for-unknown: true
```

## `validate()` Was Removed

**Changelog Pattern**:

Model validation helpers disappear:

```md
* `validate()` was removed
```

**Spec Pattern**:

No spec lookup is usually required. This is emitter behavior.

**Breaking**:

Callers can no longer invoke generated client-side validation helpers on models or request bodies.

**Reason**:

Emitter change. This should only happen to the SDK under TypeSpec migration.

**Resolution**:

Accept this break. Callers are unlikely to invoke generated validation helpers directly.

## `java.util.UUID` Changes to `java.lang.String`

**Changelog Pattern**:

A property, getter, or setter changes from `UUID` to `String`:

```md
* `java.util.UUID correlationId()` -> `java.lang.String correlationId()`
```

**Spec Pattern**:

Find the property that is modeled as `uuid`.

```typespec
model ExampleResource {
  correlationId: uuid;
}
```

**Breaking**:

Callers must switch from `UUID` instances to plain string values, which changes construction, comparison, and serialization at call sites.
**Reason**:

Emitter change during TypeSpec migration.

**Resolution**:

Accept this break. `String` is the preferred Java surface for UUID-valued data.
