# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2026-10-13)

- Azure Resource Manager Monitor Workspaces client library for Java. This package contains Microsoft Azure SDK for Monitor Workspaces Management SDK.  Package api-version 2026-09-03-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.TraceMetricsState` was added

* `models.MetricConfigurationResource$Update` was added

* `models.MetricConfigurationType` was added

* `models.DefaultActionGroupResource` was added

* `models.MetricConfigurationResource$DefinitionStages` was added

* `models.TraceAssociationsAtResourceGroups` was added

* `models.MetricConfigurationResource$UpdateStages` was added

* `models.MetricNamespaceResource` was added

* `models.MetricAggregationFunctions` was added

* `models.MetricsLimits` was added

* `models.MetricNamespaceProperties` was added

* `models.AzureMonitorWorkspaceEndpoints` was added

* `models.TraceContainers` was added

* `models.TraceAssociationsAtSubscriptions` was added

* `models.AzureMonitorWorkspaceActions` was added

* `models.MetricConfigurationResource$Definition` was added

* `models.TraceAssociationResource` was added

* `models.TraceContainer` was added

* `models.TraceAssociation` was added

* `models.MetricConfigurationResource` was added

* `models.MetricNamespaces` was added

* `models.MetricAggregationConfiguration` was added

* `models.MetricConfigurations` was added

* `models.MetricConfigurationProperties` was added

* `models.TraceContainerResource` was added

* `models.TraceAssociations` was added

#### `models.AzureMonitorWorkspace` was modified

* `endpoints()` was added
* `withActions(models.AzureMonitorWorkspaceActions)` was added
* `actions()` was added

#### `MonitorWorkspacesManager` was modified

* `traceAssociations()` was added
* `traceContainers()` was added
* `traceAssociationsAtSubscriptions()` was added
* `metricNamespaces()` was added
* `metricConfigurations()` was added
* `traceAssociationsAtResourceGroups()` was added

#### `models.MetricsContainer` was modified

* `withLimits(models.MetricsLimits)` was added
* `limits()` was added

## 1.0.0 (2026-06-25)

- Azure Resource Manager Monitor Workspaces client library for Java. This package contains Microsoft Azure SDK for Monitor Workspaces Management SDK.  Package api-version 2025-10-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- GA release for `api-version` 2025-10-03.

## 1.0.0-beta.1 (2026-05-20)

- Azure Resource Manager Monitor Workspaces client library for Java. This package contains Microsoft Azure SDK for Monitor Workspaces Management SDK.  Package api-version 2025-10-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-monitor-workspaces Java SDK.
