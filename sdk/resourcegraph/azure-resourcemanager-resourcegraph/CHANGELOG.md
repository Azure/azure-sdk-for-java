# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2022-04-07)

- Azure Resource Manager ResourceGraph client library for Java. This package contains Microsoft Azure SDK for ResourceGraph Management SDK. Azure Resource Graph API Reference. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ResourcesHistoryRequest` was removed

* `models.ResourceSnapshotData` was removed

* `models.ResourcesHistoryRequestOptions` was removed

* `models.DateTimeInterval` was removed

* `models.ChangeCategory` was removed

* `models.ResourceChangeDetailsRequestParameters` was removed

* `models.ResourceChangeData` was removed

* `models.ColumnDataType` was removed

* `models.ResourceChangeDataAfterSnapshot` was removed

* `models.ResourcePropertyChange` was removed

* `models.Column` was removed

* `models.ResourceChangesRequestParameters` was removed

* `models.ChangeType` was removed

* `models.ResourcesHistoryRequestOptionsResultFormat` was removed

* `models.ResourceChangesRequestParametersInterval` was removed

* `models.ResourceChangeDataBeforeSnapshot` was removed

* `models.PropertyChangeType` was removed

* `models.ResourceChangeList` was removed

#### `models.ResourceProviders` was modified

* `resourceChanges(models.ResourceChangesRequestParameters)` was removed
* `resourcesHistoryWithResponse(models.ResourcesHistoryRequest,com.azure.core.util.Context)` was removed
* `resourceChangesWithResponse(models.ResourceChangesRequestParameters,com.azure.core.util.Context)` was removed
* `resourcesHistory(models.ResourcesHistoryRequest)` was removed
* `resourceChangeDetails(models.ResourceChangeDetailsRequestParameters)` was removed
* `resourceChangeDetailsWithResponse(models.ResourceChangeDetailsRequestParameters,com.azure.core.util.Context)` was removed

### Features Added

#### `ResourceGraphManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `ResourceGraphManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.2 (2021-05-24)

- Azure Resource Manager ResourceGraph client library for Java. This package contains Microsoft Azure SDK for ResourceGraph Management SDK. Azure Resource Graph API Reference. Package tag package-preview-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### New Feature

* `models.ResourcesHistoryRequest` was added

* `models.ResourceSnapshotData` was added

* `models.ResourcesHistoryRequestOptions` was added

* `models.DateTimeInterval` was added

* `models.ChangeCategory` was added

* `models.ResourceChangeDetailsRequestParameters` was added

* `models.ResourceChangeData` was added

* `models.ResourceChangeDataAfterSnapshot` was added

* `models.ResourcePropertyChange` was added

* `models.ResourceChangesRequestParameters` was added

* `models.ResourcesHistoryRequestOptionsResultFormat` was added

* `models.ChangeType` was added

* `models.ResourceChangesRequestParametersInterval` was added

* `models.ResourceChangeDataBeforeSnapshot` was added

* `models.PropertyChangeType` was added

* `models.ResourceChangeList` was added

#### `models.ResourceProviders` was modified

* `resourceChangeDetails(models.ResourceChangeDetailsRequestParameters)` was added
* `resourceChangeDetailsWithResponse(models.ResourceChangeDetailsRequestParameters,com.azure.core.util.Context)` was added
* `resourceChanges(models.ResourceChangesRequestParameters)` was added
* `resourcesHistoryWithResponse(models.ResourcesHistoryRequest,com.azure.core.util.Context)` was added
* `resourcesHistory(models.ResourcesHistoryRequest)` was added
* `resourceChangesWithResponse(models.ResourceChangesRequestParameters,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2021-03-24)

- Azure Resource Manager ResourceGraph client library for Java. This package contains Microsoft Azure SDK for ResourceGraph Management SDK. Azure Resource Graph API Reference. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
- Azure Resource Manager ResourceGraph client library for Java. This package contains Microsoft Azure SDK for ResourceGraph Management SDK. Azure Resource Graph API Reference. Package tag package-2019-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

