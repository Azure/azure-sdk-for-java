# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
