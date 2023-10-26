# Release History

## 1.1.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.2 (2023-10-26)

- Azure Resource Manager ResourceHealth client library for Java. This package contains Microsoft Azure SDK for ResourceHealth Management SDK. The Resource Health Client. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.EventSubTypeValues` was added

#### `models.Event` was modified

* `argQuery()` was added
* `maintenanceType()` was added
* `maintenanceId()` was added
* `eventSubType()` was added

#### `models.EventImpactedResource` was modified

* `status()` was added
* `maintenanceEndTime()` was added
* `resourceName()` was added
* `resourceGroup()` was added
* `maintenanceStartTime()` was added

## 1.1.0-beta.1 (2023-05-26)

- Azure Resource Manager ResourceHealth client library for Java. This package contains Microsoft Azure SDK for ResourceHealth Management SDK. The Resource Health Client. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.EventsOperations` was added

* `models.EventLevelValues` was added

* `models.IssueNameParameter` was added

* `models.SecurityAdvisoryImpactedResources` was added

* `models.KeyValueItem` was added

* `models.Impact` was added

* `models.ImpactedResources` was added

* `models.EventTypeValues` was added

* `models.MetadataEntity` was added

* `models.StatusBanner` was added

* `models.EventPropertiesRecommendedActionsItem` was added

* `models.Events` was added

* `models.ImpactedServiceRegion` was added

* `models.EventStatusValues` was added

* `models.EmergingIssueImpact` was added

* `models.EventOperations` was added

* `models.Event` was added

* `models.ChildAvailabilityStatuses` was added

* `models.EmergingIssues` was added

* `models.EmergingIssuesGetResult` was added

* `models.Metadatas` was added

* `models.SeverityValues` was added

* `models.StageValues` was added

* `models.ChildResources` was added

* `models.ImpactedRegion` was added

* `models.EventPropertiesRecommendedActions` was added

* `models.EventImpactedResourceListResult` was added

* `models.StatusActiveEvent` was added

* `models.MetadataEntityListResult` was added

* `models.LinkTypeValues` was added

* `models.EventImpactedResource` was added

* `models.LevelValues` was added

* `models.EmergingIssueListResult` was added

* `models.Faq` was added

* `models.EventSourceValues` was added

* `models.Link` was added

* `models.Scenario` was added

* `models.LinkDisplayText` was added

* `models.Update` was added

* `models.EventPropertiesArticle` was added

* `models.EventPropertiesAdditionalInformation` was added

* `models.MetadataSupportedValueDetail` was added

#### `models.RecommendedAction` was modified

* `actionUrlComment()` was added
* `withActionUrlComment(java.lang.String)` was added

#### `ResourceHealthManager` was modified

* `metadatas()` was added
* `childResources()` was added
* `impactedResources()` was added
* `childAvailabilityStatuses()` was added
* `eventOperations()` was added
* `securityAdvisoryImpactedResources()` was added
* `emergingIssues()` was added
* `eventsOperations()` was added

#### `models.AvailabilityStatusProperties` was modified

* `withArticleId(java.lang.String)` was added
* `category()` was added
* `withContext(java.lang.String)` was added
* `context()` was added
* `articleId()` was added
* `withCategory(java.lang.String)` was added

## 1.0.0 (2022-12-09)

- Azure Resource Manager ResourceHealth client library for Java. This package contains Microsoft Azure SDK for ResourceHealth Management SDK. The Resource Health Client. Package tag package-2020-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AvailabilityStatusPropertiesRecentlyResolved` was modified

* `unavailabilitySummary()` was removed
* `unavailableOccurredTime()` was removed
* `withUnavailableOccurredTime(java.time.OffsetDateTime)` was removed
* `withUnavailabilitySummary(java.lang.String)` was removed

#### `models.AvailabilityStatusProperties` was modified

* `withOccurredTime(java.time.OffsetDateTime)` was removed
* `occurredTime()` was removed

### Features Added

#### `ResourceHealthManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.AvailabilityStatusPropertiesRecentlyResolved` was modified

* `withUnavailableSummary(java.lang.String)` was added
* `withUnavailableOccuredTime(java.time.OffsetDateTime)` was added
* `unavailableSummary()` was added
* `unavailableOccuredTime()` was added

#### `models.AvailabilityStatusProperties` was modified

* `withOccuredTime(java.time.OffsetDateTime)` was added
* `occuredTime()` was added

#### `ResourceHealthManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.2 (2022-03-29)

- Azure Resource Manager ResourceHealth client library for Java. This package contains Microsoft Azure SDK for ResourceHealth Management SDK. The Resource Health Client. Package tag package-2020-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.EmergingIssuesGetResult` was removed

* `models.EventsOperations` was removed

* `models.EventLevelValues` was removed

* `models.SeverityValues` was removed

* `models.StageValues` was removed

* `models.ImpactedRegion` was removed

* `models.Impact` was removed

* `models.EventTypeValues` was removed

* `models.EventPropertiesRecommendedActions` was removed

* `models.StatusActiveEvent` was removed

* `models.MetadataEntity` was removed

* `models.LinkTypeValues` was removed

* `models.StatusBanner` was removed

* `models.EventPropertiesRecommendedActionsItem` was removed

* `models.Events` was removed

* `models.ImpactedServiceRegion` was removed

* `models.LevelValues` was removed

* `models.EmergingIssueListResult` was removed

* `models.Faq` was removed

* `models.EventStatusValues` was removed

* `models.EventSourceValues` was removed

* `models.Link` was removed

* `models.Scenario` was removed

* `models.EmergingIssueImpact` was removed

* `models.LinkDisplayText` was removed

* `models.Update` was removed

* `models.EventPropertiesArticle` was removed

* `models.Event` was removed

* `models.MetadataSupportedValueDetail` was removed

* `models.EmergingIssues` was removed

#### `ResourceHealthManager` was modified

* `emergingIssues()` was removed
* `eventsOperations()` was removed

### Features Added

#### `models.AvailabilityStatusProperties` was modified

* `withTitle(java.lang.String)` was added
* `title()` was added

#### `ResourceHealthManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-08)

- Azure Resource Manager ResourceHealth client library for Java. This package contains Microsoft Azure SDK for ResourceHealth Management SDK. The Resource Health Client. Package tag package-2018-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
