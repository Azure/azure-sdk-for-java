# Release History

## 1.1.0-beta.1 (2026-10-13)

- Azure Resource Manager Advisor client library for Java. This package contains Microsoft Azure SDK for Advisor Management SDK. REST APIs for Azure Advisor Resiliency Reviews. Package api-version 2026-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MetadataEntityListResult` was removed

#### `models.SuppressionContractListResult` was removed

#### `models.ConfigurationListResult` was removed

#### `models.OperationEntityListResult` was removed

#### `models.ResourceRecommendationBaseListResult` was removed

#### `models.ResourceMetadata` was modified

* `ResourceMetadata()` was changed to private access
* `withPlural(java.lang.String)` was removed
* `withResourceId(java.lang.String)` was removed
* `withSource(java.lang.String)` was removed
* `withSingular(java.lang.String)` was removed
* `validate()` was removed
* `withAction(java.util.Map)` was removed

#### `models.MetadataSupportedValueDetail` was modified

* `MetadataSupportedValueDetail()` was changed to private access
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.Recommendations` was modified

* `list(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed
* `getGenerateStatus(java.util.UUID)` was removed
* `getGenerateStatusWithResponse(java.util.UUID,com.azure.core.util.Context)` was removed

#### `models.OperationDisplayInfo` was modified

* `OperationDisplayInfo()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `validate()` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.ShortDescription` was modified

* `ShortDescription()` was changed to private access
* `withProblem(java.lang.String)` was removed
* `validate()` was removed
* `withSolution(java.lang.String)` was removed

#### `models.DigestConfig` was modified

* `validate()` was removed

#### `models.ResourceRecommendationBase` was modified

* `suppressionIds()` was removed

#### `models.RecommendationMetadatas` was modified

* `list(com.azure.core.util.Context)` was removed

#### `models.RecommendationsGenerateHeaders` was modified

* `withLocation(java.lang.String)` was removed
* `java.lang.String retryAfter()` -> `java.lang.Integer retryAfter()`
* `validate()` was removed
* `withRetryAfter(java.lang.String)` was removed

### Features Added

* `models.Priority` was added

* `models.Assessments` was added

* `models.Risk` was added

* `models.RecommendationPropertiesResourceWorkload` was added

* `models.ReviewStatus` was added

* `models.Workloads` was added

* `models.ResourceRecommendationSavingsDetails` was added

* `models.ResiliencyReview` was added

* `models.RecommendationsGetGenerateStatusHeaders` was added

* `models.AssessmentResult$Definition` was added

* `models.AssessmentResult$Update` was added

* `models.AdvisorScores` was added

* `models.TrackedRecommendationProperties` was added

* `models.RecommendationPatchPayload` was added

* `models.PredictionType` was added

* `models.RecommendationPropertiesReview` was added

* `models.DurationModel` was added

* `models.AdvisorScoreEntity` was added

* `models.ScoreEntity` was added

* `models.AssessmentResult$UpdateStages` was added

* `models.PredictionRequest` was added

* `models.RecommendationStatus` was added

* `models.AssessmentTypes` was added

* `models.AssessmentTypeResult` was added

* `models.ResourceRecommendationSavings` was added

* `models.TimeSeriesEntity` was added

* `models.PredictionResponse` was added

* `models.AssessmentResult` was added

* `models.RecommendationsGetGenerateStatusResponse` was added

* `models.RecommendationListMode` was added

* `models.AssessmentResult$DefinitionStages` was added

* `models.RecommendationStatePropertiesPayload` was added

* `models.Control` was added

* `models.RecommendationDismissReason` was added

* `models.ResourceProviders` was added

* `models.WorkloadResult` was added

* `models.Aggregated` was added

* `models.ResiliencyReviews` was added

#### `models.ConfigData` was modified

* `systemData()` was added
* `duration()` was added

#### `models.Recommendations` was modified

* `updateByServiceGroup(java.lang.String,java.lang.String,models.RecommendationPatchPayload)` was added
* `listByTenant(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `getGenerateStatus(java.lang.String)` was added
* `listByServiceGroup(java.lang.String)` was added
* `getGenerateStatusWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,models.RecommendationListMode,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `updateWithResponse(java.lang.String,models.RecommendationPatchPayload,com.azure.core.util.Context)` was added
* `getByServiceGroup(java.lang.String,java.lang.String)` was added
* `listByServiceGroup(java.lang.String,java.lang.String,models.RecommendationListMode,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `update(java.lang.String,models.RecommendationPatchPayload)` was added
* `updateByServiceGroupWithResponse(java.lang.String,java.lang.String,models.RecommendationPatchPayload,com.azure.core.util.Context)` was added
* `listByTenant(java.lang.String)` was added
* `getByServiceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ConfigData$Definition` was modified

* `withDuration(models.DurationModel)` was added

#### `AdvisorManager` was modified

* `assessments()` was added
* `workloads()` was added
* `resourceProviders()` was added
* `advisorScores()` was added
* `assessmentTypes()` was added
* `resiliencyReviews()` was added

#### `models.SuppressionContract` was modified

* `systemData()` was added

#### `models.ResourceRecommendationBase` was modified

* `risk()` was added
* `trackedProperties()` was added
* `completionType()` was added
* `postponedUntilDateTime()` was added
* `scoreChangedAt()` was added
* `lastRefreshed()` was added
* `systemData()` was added
* `savings()` was added
* `notes()` was added
* `criticalityScore()` was added
* `resourceWorkload()` was added
* `serviceGroupId()` was added
* `recommendationStatus()` was added
* `criticality()` was added
* `recommendationDismissReason()` was added
* `review()` was added
* `sourceSystem()` was added
* `control()` was added
* `suppressionId()` was added
* `createdTime()` was added

#### `models.RecommendationMetadatas` was modified

* `list(java.lang.String,com.azure.core.util.Context)` was added

#### `models.MetadataEntity` was modified

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
