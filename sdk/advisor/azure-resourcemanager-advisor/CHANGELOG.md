# Release History

## 1.1.0-beta.1 (2026-03-04)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor Resiliency Reviews. Package api-version 2025-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MetadataEntityListResult` was removed

#### `models.ConfigurationListResult` was removed

#### `models.ResourceRecommendationBaseListResult` was removed

#### `models.SuppressionContractListResult` was removed

#### `models.OperationEntityListResult` was removed

#### `models.MetadataSupportedValueDetail` was modified

* `MetadataSupportedValueDetail()` was changed to private access
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.ShortDescription` was modified

* `ShortDescription()` was changed to private access
* `withProblem(java.lang.String)` was removed
* `validate()` was removed
* `withSolution(java.lang.String)` was removed

#### `models.RecommendationsGenerateHeaders` was modified

* `validate()` was removed
* `java.lang.String retryAfter()` -> `java.lang.Integer retryAfter()`
* `withLocation(java.lang.String)` was removed
* `withRetryAfter(java.lang.String)` was removed

#### `models.ResourceMetadata` was modified

* `ResourceMetadata()` was changed to private access
* `withAction(java.util.Map)` was removed
* `validate()` was removed
* `withResourceId(java.lang.String)` was removed
* `withPlural(java.lang.String)` was removed
* `withSingular(java.lang.String)` was removed
* `withSource(java.lang.String)` was removed

#### `models.Recommendations` was modified

* `getGenerateStatusWithResponse(java.util.UUID,com.azure.core.util.Context)` was removed
* `getGenerateStatus(java.util.UUID)` was removed

#### `models.OperationDisplayInfo` was modified

* `OperationDisplayInfo()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `validate()` was removed
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.SuppressionContract` was modified

* `expirationTimestamp()` was removed

#### `models.DigestConfig` was modified

* `validate()` was removed

### Features Added

* `models.Assessments` was added

* `models.RecommendationPropertiesResourceWorkload` was added

* `models.TriageResource` was added

* `models.ReviewStatus` was added

* `models.RecommendationRejectBody` was added

* `models.Workloads` was added

* `models.RecommendationsGetGenerateStatusHeaders` was added

* `models.TrackedRecommendationPropertiesPayloadProperties` was added

* `models.PredictionType` was added

* `models.ScoreEntity` was added

* `models.AssessmentResult$UpdateStages` was added

* `models.PredictionRequest` was added

* `models.AssessmentTypes` was added

* `models.PredictionResponse` was added

* `models.TriageRecommendation` was added

* `models.AssessmentResult` was added

* `models.AssessmentResult$DefinitionStages` was added

* `models.State` was added

* `models.ReasonForRejectionName` was added

* `models.ResiliencyReviews` was added

* `models.Priority` was added

* `models.Risk` was added

* `models.TrackedRecommendationPropertiesPayload` was added

* `models.TriageRecommendations` was added

* `models.ResiliencyReview` was added

* `models.AssessmentResult$Definition` was added

* `models.AssessmentResult$Update` was added

* `models.AdvisorScores` was added

* `models.TrackedRecommendationProperties` was added

* `models.RecommendationPropertiesReview` was added

* `models.DurationModel` was added

* `models.AdvisorScoreEntity` was added

* `models.RecommendationStatusName` was added

* `models.AssessmentTypeResult` was added

* `models.TimeSeriesEntity` was added

* `models.RecommendationsGetGenerateStatusResponse` was added

* `models.Reason` was added

* `models.Control` was added

* `models.ResourceProviders` was added

* `models.WorkloadResult` was added

* `models.PriorityName` was added

* `models.Aggregated` was added

* `models.TriageResources` was added

#### `models.ConfigData` was modified

* `systemData()` was added
* `duration()` was added

#### `models.MetadataEntity` was modified

* `systemData()` was added

#### `models.Recommendations` was modified

* `getGenerateStatus(java.lang.String)` was added
* `patch(java.lang.String,java.lang.String,models.TrackedRecommendationPropertiesPayload)` was added
* `getGenerateStatusWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `patchWithResponse(java.lang.String,java.lang.String,models.TrackedRecommendationPropertiesPayload,com.azure.core.util.Context)` was added
* `listByTenant(java.lang.String)` was added
* `listByTenant(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ConfigData$Definition` was modified

* `withDuration(models.DurationModel)` was added

#### `AdvisorManager` was modified

* `triageRecommendations()` was added
* `resiliencyReviews()` was added
* `triageResources()` was added
* `resourceProviders()` was added
* `workloads()` was added
* `assessmentTypes()` was added
* `assessments()` was added
* `advisorScores()` was added

#### `models.SuppressionContract` was modified

* `systemData()` was added
* `expirationTimeStamp()` was added

#### `models.ResourceRecommendationBase` was modified

* `sourceSystem()` was added
* `notes()` was added
* `risk()` was added
* `trackedProperties()` was added
* `control()` was added
* `tracked()` was added
* `review()` was added
* `resourceWorkload()` was added
* `systemData()` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor. Package tag package-2020-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager Advisor client library for Java.

## 1.0.0-beta.3 (2024-10-14)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor. Package tag package-2020-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ConfigurationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationEntityListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetadataSupportedValueDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetadataEntityListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplayInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DigestConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceRecommendationBaseListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SuppressionContractListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ShortDescription` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2023-01-11)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor. Package tag package-2020-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Risk` was removed

#### `models.ResourceRecommendationBase` was modified

* `risk()` was removed

### Features Added

#### `AdvisorManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `AdvisorManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor. Package tag package-2020-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
