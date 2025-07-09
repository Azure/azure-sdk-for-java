# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

* `implementation.models.CustomInferenceRequest` was added

#### `RadiologyInsightsClient` was modified

* `beginCustomInference(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `beginCustomInference(models.RadiologyInsightsData)` was added
* `beginCustomInference(models.RadiologyInsightsData,java.util.List)` was added

#### `implementation.RadiologyInsightsClientImpl$RadiologyInsightsClientService` was modified

* `customInference(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions,com.azure.core.util.Context)` was added
* `customInferenceSync(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions,com.azure.core.util.Context)` was added

#### `implementation.RadiologyInsightsClientImpl` was modified

* `beginCustomInferenceWithModelAsync(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `beginCustomInferenceAsync(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `beginCustomInferenceWithModel(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `beginCustomInference(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added

#### `RadiologyInsightsAsyncClient` was modified

* `beginCustomInference(models.RadiologyInsightsData)` was added
* `beginCustomInference(models.RadiologyInsightsData,java.util.List)` was added
* `beginCustomInference(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added


## 1.1.1 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.
- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.

## 1.1.0 (2025-06-02)

### Features Added

- Class `QualityMeasureInference` added
- Class `GuidanceInference` added
- Class `ScoringAndAssessmentInference` added

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.5` to version `1.15.11`.
- Upgraded `azure-core` from `1.53.0` to version `1.55.3`.

## 1.0.0 (2024-10-09)

- GA release

### Features Added

- Added sample code for all Inference types
- Added tests for all Inference types

### Breaking Changes

- Request changes:
    - POST call replaced with PUT (this change is taken care of automatically by the SDK)
    - Method `RadiologyInsightsClient.beginInferRadiologyInsights()` expects a String specifying a unique job id as well as a RadiologyInsightsJob (wrapping a RadiologyInsightsData object) instead of a RadiologyInsightsData object
    - Method `RadiologyInsightsAsyncClient.beginInferRadiologyInsights()` expects a String specifying a unique job id as well as a RadiologyInsightsJob (wrapping a RadiologyInsightsData object) instead of a RadiologyInsightsData object
    - Class `Encounter` renamed into `PatientEncounter`
    - Class `FhirR4Extendible` renamed into `OrderedProcedure`
    - Method `PatientDocument.setCreatedDateTime()` renamed into `PatientDocument.setCreatedAt()`
    - Method `PatientRecord.setPatientInfo()` renamed into `PatientRecord.setPatientDetails()`
- Response changes:
    - Class `FhirR4Extendible1` renamed into `RadiologyInsightsInference`
    - Method `FollowupCommunicationInference.getDateTime()` renamed into `FollowupCommunicationInference.getCommunicatedAt()`
    - Method `FollowupCommunicationInference.isWasAcknowledged()` renamed into `FollowupCommunicationInference.isAcknowledged()`
    - Method `FollowupRecommendationInference.getEffectiveDateTime()` renamed into `FollowupRecommendationInference.getEffectiveAt()`
    - Method `RadiologyInsightsJob.getCreatedDateTime()` renamed into `RadiologyInsightsJob.getCreatedAt()`
    - Method `RadiologyInsightsJob.getExpirationDateTime()` renamed into `RadiologyInsightsJob.getExpiresAt()`
    - Method `RadiologyInsightsJob.getLastUpdateDateTime()` renamed into `RadiologyInsightsJob.getUpdatedAt()`

## 1.0.0-beta.1 (2024-02-15)

- Initial preview of the Azure Health Insights Radiology Insights client library.

### Features Added
* Radiology Insights API: Scans radiology reports as text to provide quality checks as feedback on errors and inconsistencies (mismatches). Critical findings are identified and communicated using the full context of the report. Follow-up recommendations and clinical findings with measurements (sizes) documented by the radiologist are also identified.
