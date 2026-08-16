# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-06-02)

- Azure Resource Manager Slis client library for Java. This package contains Microsoft Azure SDK for Slis Management SDK.  Package api-version 2025-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ConditionOperator` was modified

* Wire values changed: `==` to `eq`, `!=` to `ne`, `>` to `gt`, `>=` to `gte`, `<` to `lt`, `<=` to `lte`, `@in` to `in`, `!in` to `notin`, `!contains` to `notcontains`, and `!startswith` to `notstartswith`

#### `models.WindowUptimeCriteriaComparator` was modified

* Wire values changed: `>` to `gt`, `>=` to `gte`, `<` to `lt`, and `<=` to `lte`

#### `models.SamplingType` was modified

* `AVG` was removed
* Wire-value casing changed for `MAX`, `MIN`, and `SUM` to `"Max"`, `"Min"`, and `"Sum"`

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
