# Release History

## 1.0.0 (2022-12-09)

- Azure Resource Manager ResourceHealth client library for Java. This package contains Microsoft Azure SDK for ResourceHealth Management SDK. The Resource Health Client. Package tag package-2022-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AvailabilityStatusProperties` was modified

* `occurredTime()` was removed
* `withOccurredTime(java.time.OffsetDateTime)` was removed

#### `models.AvailabilityStatusPropertiesRecentlyResolved` was modified

* `unavailableOccurredTime()` was removed
* `unavailabilitySummary()` was removed
* `withUnavailabilitySummary(java.lang.String)` was removed
* `withUnavailableOccurredTime(java.time.OffsetDateTime)` was removed

### Features Added

* `models.EventsOperations` was added

* `models.EventLevelValues` was added

* `models.KeyValueItem` was added

* `models.Impact` was added

* `models.ImpactedResources` was added

* `models.EventTypeValues` was added

* `models.EventPropertiesRecommendedActions` was added

* `models.EventImpactedResourceListResult` was added

* `models.LinkTypeValues` was added

* `models.EventPropertiesRecommendedActionsItem` was added

* `models.Events` was added

* `models.EventImpactedResource` was added

* `models.ImpactedServiceRegion` was added

* `models.LevelValues` was added

* `models.Faq` was added

* `models.EventStatusValues` was added

* `models.EventSourceValues` was added

* `models.Link` was added

* `models.LinkDisplayText` was added

* `models.Update` was added

* `models.EventOperations` was added

* `models.EventPropertiesArticle` was added

* `models.Event` was added

* `models.EventPropertiesAdditionalInformation` was added

#### `ResourceHealthManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `eventOperations()` was added
* `eventsOperations()` was added
* `impactedResources()` was added

#### `models.AvailabilityStatusProperties` was modified

* `withOccuredTime(java.time.OffsetDateTime)` was added
* `occuredTime()` was added

#### `ResourceHealthManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.AvailabilityStatusPropertiesRecentlyResolved` was modified

* `unavailableOccuredTime()` was added
* `unavailableSummary()` was added
* `withUnavailableOccuredTime(java.time.OffsetDateTime)` was added
* `withUnavailableSummary(java.lang.String)` was added

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
