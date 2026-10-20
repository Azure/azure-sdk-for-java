# Release History

## 2.0.0 (Unreleased)

### Features Added

### Breaking Changes

- `deployVersionCode` now returns `EdgeActionVersion` instead of `EdgeActionVersionProperties`.
  Read deployment state through `result.properties()`. The low-level client now returns
  `EdgeActionVersionInner`, including in its polling APIs.
- `EdgeActionVersionProperties` changed from an interface to a final, mutable model and no longer
  exposes `innerModel()`. Version creation and `EdgeActionVersionInner.withProperties` now accept
  `EdgeActionVersionProperties` instead of the removed `EdgeActionVersionPropertiesInner`.

### Bugs Fixed

- Corrected deserialization of the full version resource returned after code deployment completes,
  so nested provisioning and validation states are available through `result.properties()`.

### Other Changes

- Regenerated from API version `2026-10-01` with corrected long-running operation response headers
  in [Azure/azure-rest-api-specs#47326](https://github.com/Azure/azure-rest-api-specs/pull/47326).
  `swapDefault` continues to return no value.
- Added offline long-running operation regression tests and response-contract checks for recorded
  management integration tests.

## 1.0.0 (2026-10-10)

- Azure Resource Manager EdgeActions client library for Java. This package contains Microsoft Azure SDK for EdgeActions Management SDK.  Package api-version 2026-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial GA release for the azure-resourcemanager-edgeactions Java SDK.

## 1.0.0-beta.1 (2026-02-07)

- Azure Resource Manager EdgeActions client library for Java. This package contains Microsoft Azure SDK for EdgeActions Management SDK.  Package api-version 2025-12-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-edgeactions Java SDK.
