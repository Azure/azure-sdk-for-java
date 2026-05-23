# Release History

## 1.0.0-beta.2 (2026-05-23)

- Azure Resource Manager Slis client library for Java. This package contains Microsoft Azure SDK for Slis Management SDK.  Package api-version 2025-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SamplingType` was modified

* `AVG` was removed

### Features Added

#### `models.SamplingType` was modified

* `AVERAGE` was added
* `COUNT` was added

## 1.0.0-beta.1 (2026-04-22)

### Features Added

- Initial preview release of `azure-resourcemanager-monitor-slis` for managing
  Service Level Indicator (SLI) resources under the `Microsoft.Monitor` namespace.
- Support for SLI resource CRUD operations: create or update, get, delete, and list.
- SLI evaluation with Availability and Latency categories, supporting both
  window-based and request-based evaluation types with configurable signal sources,
  aggregation, and SLO baselines.
- Integration with Azure Monitor Workspace (AMW) accounts for metric emission,
  with managed identity and alert support.
