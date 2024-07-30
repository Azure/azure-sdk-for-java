# Release History

## 1.1.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.5 (2024-06-21)

- Azure Resource Manager Self Help client library for Java. This package contains Microsoft Azure SDK for Self Help Management SDK. Help RP provider. Package tag package-2024-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DiscoverySolutionNlpTenantScopes` was removed

* `models.DiscoverySolutionNlpSubscriptionScopes` was removed

#### `models.CheckNameAvailabilities` was modified

* `post(java.lang.String)` was removed
* `postWithResponse(java.lang.String,models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was removed

#### `SelfHelpManager` was modified

* `discoverySolutionNlpSubscriptionScopes()` was removed
* `discoverySolutionNlpTenantScopes()` was removed

### Features Added

* `models.DiscoverySolutionNlps` was added

#### `models.DiscoveryNlpRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetricsBasedChart` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TriggerCriterion` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SolutionMetadataProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResponseValidationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SolutionWarmUpRequestBody` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Video` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticInvocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StepInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Insight` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VideoGroup` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResponseOption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Error` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClassificationService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContinueRequestBody` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SolutionPatchRequestBody` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Section` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SectionSelfHelp` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutomatedCheckResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SolutionNlpMetadataResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added
* `type()` was added

#### `models.CheckNameAvailabilities` was modified

* `checkAvailability(java.lang.String)` was added
* `checkAvailabilityWithResponse(java.lang.String,models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was added

#### `models.FilterGroup` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SolutionsDiagnostic` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Diagnostic` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiscoveryResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReplacementMapsSelfHelp` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SolutionsTroubleshooters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReplacementMaps` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SearchResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TroubleshooterResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Step` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VideoGroupVideo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Filter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `SelfHelpManager` was modified

* `discoverySolutionNlps()` was added

## 1.1.0-beta.4 (2024-05-21)

- Azure Resource Manager Self Help client library for Java. This package contains Microsoft Azure SDK for Self Help Management SDK. Help RP provider. Package tag package-2024-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DiscoverySolutionNlpSubscriptionScopes` was modified

* `post()` was removed
* `postWithResponse(models.DiscoveryNlpRequest,com.azure.core.util.Context)` was removed

### Features Added

#### `models.DiscoverySolutionNlpSubscriptionScopes` was modified

* `postWithResponse(java.lang.String,models.DiscoveryNlpRequest,com.azure.core.util.Context)` was added
* `post(java.lang.String)` was added

## 1.1.0-beta.3 (2024-04-23)

- Azure Resource Manager Self Help client library for Java. This package contains Microsoft Azure SDK for Self Help Management SDK. Help RP provider. Package tag package-2024-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DiscoverySolutions` was modified

* `list(java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.SolutionResourceSelfHelp` was added

* `models.DiscoveryNlpRequest` was added

* `models.ValidationScope` was added

* `models.SimplifiedSolutionsResource$Definition` was added

* `models.DiscoveryNlpResponse` was added

* `models.SolutionWarmUpRequestBody` was added

* `models.ClassificationService` was added

* `models.SectionSelfHelp` was added

* `models.SimplifiedSolutionsResource$DefinitionStages` was added

* `models.SolutionSelfHelps` was added

* `models.SolutionNlpMetadataResource` was added

* `models.SimplifiedSolutions` was added

* `models.DiscoverySolutionNlpTenantScopes` was added

* `models.ReplacementMapsSelfHelp` was added

* `models.DiscoverySolutionNlpSubscriptionScopes` was added

* `models.SimplifiedSolutionsResource` was added

#### `models.ResponseValidationProperties` was modified

* `validationScope()` was added
* `withValidationScope(models.ValidationScope)` was added

#### `models.StepInput` was modified

* `withQuestionTitle(java.lang.String)` was added
* `questionTitle()` was added

#### `models.SolutionOperations` was modified

* `warmUpWithResponse(java.lang.String,java.lang.String,models.SolutionWarmUpRequestBody,com.azure.core.util.Context)` was added
* `warmUp(java.lang.String,java.lang.String)` was added

#### `models.SolutionResource` was modified

* `warmUpWithResponse(models.SolutionWarmUpRequestBody,com.azure.core.util.Context)` was added
* `warmUp()` was added

#### `models.AutomatedCheckResult` was modified

* `status()` was added
* `version()` was added
* `withStatus(java.lang.String)` was added
* `withVersion(java.lang.String)` was added

#### `models.DiscoverySolutions` was modified

* `list()` was added
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SolutionsDiagnostic` was modified

* `estimatedCompletionTime()` was added
* `withEstimatedCompletionTime(java.lang.String)` was added

#### `SelfHelpManager` was modified

* `solutionSelfHelps()` was added
* `discoverySolutionNlpTenantScopes()` was added
* `discoverySolutionNlpSubscriptionScopes()` was added
* `simplifiedSolutions()` was added

## 1.1.0-beta.2 (2023-12-19)

- Azure Resource Manager Self Help client library for Java. This package contains Microsoft Azure SDK for Self Help Management SDK. Help RP provider. Package tag package-2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SolutionResourceProperties` was removed

#### `models.SolutionResource$Definition` was modified

* `withProperties(models.SolutionResourceProperties)` was removed

#### `models.SolutionResource$Update` was modified

* `withProperties(models.SolutionResourceProperties)` was removed

#### `models.StepInput` was modified

* `withQuestionType(java.lang.String)` was removed
* `java.lang.String questionType()` -> `models.QuestionType questionType()`

#### `models.SolutionPatchRequestBody` was modified

* `properties()` was removed
* `withProperties(models.SolutionResourceProperties)` was removed

#### `models.SolutionResource` was modified

* `properties()` was removed

### Features Added

#### `models.SolutionResource$Definition` was modified

* `withParameters(java.util.Map)` was added
* `withTriggerCriteria(java.util.List)` was added

#### `models.SolutionResource$Update` was modified

* `withTriggerCriteria(java.util.List)` was added
* `withParameters(java.util.Map)` was added

#### `models.StepInput` was modified

* `withQuestionType(models.QuestionType)` was added

#### `models.SolutionPatchRequestBody` was modified

* `title()` was added
* `replacementMaps()` was added
* `withTriggerCriteria(java.util.List)` was added
* `withParameters(java.util.Map)` was added
* `solutionId()` was added
* `parameters()` was added
* `provisioningState()` was added
* `triggerCriteria()` was added
* `sections()` was added
* `content()` was added

#### `models.SolutionResource` was modified

* `parameters()` was added
* `title()` was added
* `content()` was added
* `systemData()` was added
* `triggerCriteria()` was added
* `replacementMaps()` was added
* `provisioningState()` was added
* `solutionId()` was added
* `sections()` was added

## 1.1.0-beta.1 (2023-10-23)

- Azure Resource Manager Self Help client library for Java. This package contains Microsoft Azure SDK for Self Help Management SDK. Help RP provider. Package tag package-2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ProvisioningState` was removed

#### `models.DiagnosticResource` was modified

* `models.ProvisioningState provisioningState()` -> `models.DiagnosticProvisioningState provisioningState()`

#### `models.Diagnostics` was modified

* `checkNameAvailability(java.lang.String)` was removed
* `checkNameAvailabilityWithResponse(java.lang.String,models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was removed

#### `models.SolutionMetadataResource` was modified

* `solutionId()` was removed
* `solutionType()` was removed
* `requiredParameterSets()` was removed
* `description()` was removed

### Features Added

* `models.ExecutionStatus` was added

* `models.QuestionContentType` was added

* `models.AutomatedCheckResult` was added

* `models.SolutionResourceProperties` was added

* `models.SolutionResource$Definition` was added

* `models.TroubleshooterProvisioningState` was added

* `models.TroubleshootersRestartResponse` was added

* `models.TroubleshootersContinueMethodHeaders` was added

* `models.Type` was added

* `models.AggregationType` was added

* `models.TroubleshooterResource` was added

* `models.AutomatedCheckResultType` was added

* `models.Troubleshooters` was added

* `models.MetricsBasedChart` was added

* `models.TriggerCriterion` was added

* `models.Name` was added

* `models.TroubleshooterResource$DefinitionStages` was added

* `models.CheckNameAvailabilities` was added

* `models.FilterGroup` was added

* `models.SolutionsDiagnostic` was added

* `models.SolutionProvisioningState` was added

* `models.TroubleshootersEndHeaders` was added

* `models.Confidence` was added

* `models.SolutionMetadataProperties` was added

* `models.ResponseValidationProperties` was added

* `models.SolutionResource$Update` was added

* `models.SolutionType` was added

* `models.TroubleshootersRestartHeaders` was added

* `models.WebResult` was added

* `models.Video` was added

* `models.StepInput` was added

* `models.SolutionResource$DefinitionStages` was added

* `models.ResultType` was added

* `models.SolutionsTroubleshooters` was added

* `models.VideoGroup` was added

* `models.SolutionOperations` was added

* `models.ReplacementMaps` was added

* `models.SolutionResource$UpdateStages` was added

* `models.ResponseOption` was added

* `models.SearchResult` was added

* `models.TroubleshooterResponse` was added

* `models.TroubleshootersContinueMethodResponse` was added

* `models.ContinueRequestBody` was added

* `models.QuestionType` was added

* `models.Step` was added

* `models.SolutionPatchRequestBody` was added

* `models.TroubleshooterResource$Definition` was added

* `models.Section` was added

* `models.VideoGroupVideo` was added

* `models.SolutionResource` was added

* `models.TroubleshootersEndResponse` was added

* `models.DiagnosticProvisioningState` was added

* `models.RestartTroubleshooterResponse` was added

* `models.Filter` was added

#### `models.SolutionMetadataResource` was modified

* `solutions()` was added

#### `SelfHelpManager` was modified

* `solutionOperations()` was added
* `troubleshooters()` was added
* `checkNameAvailabilities()` was added

## 1.0.0 (2023-06-22)

- Azure Resource Manager Self Help client library for Java. This package contains Microsoft Azure SDK for Self Help Management SDK. Help RP provider. Package tag package-2023-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-05-12)

- Azure Resource Manager Self Help client library for Java. This package contains Microsoft Azure SDK for Self Help Management SDK. Help RP provider. Package tag package-2023-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
