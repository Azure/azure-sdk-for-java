# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

#### Custom Model Inference Support
- Added comprehensive support for custom model inference operations
- All new APIs follow Azure SDK design guidelines and support both synchronous and asynchronous operations

#### New Model Classes
- `models.RadiologyInsightsCustomJob` - Represents a custom inference job with status tracking
- `models.RadiologyInsightsCustomInferenceResult` - Contains the results of custom model inference
- `implementation.models.InferFromCustomModelIdRequest` - Request model for custom inference operations

#### `RadiologyInsightsClient` - Custom Model Inference Methods
- `beginInferFromCustomModelId(RadiologyInsightsData data)` - Start custom inference with default options
- `beginInferFromCustomModelId(RadiologyInsightsData data, List<String> modelIds)` - Start custom inference with specific model IDs
- `beginInferFromCustomModelId(BinaryData request, RequestOptions options)` - Start custom inference with low-level API

#### `RadiologyInsightsAsyncClient` - Custom Model Inference Methods  
- `beginInferFromCustomModelId(RadiologyInsightsData data)` - Start custom inference asynchronously with default options
- `beginInferFromCustomModelId(RadiologyInsightsData data, List<String> modelIds)` - Start custom inference asynchronously with specific model IDs
- `beginInferFromCustomModelId(BinaryData request, RequestOptions options)` - Start custom inference asynchronously with low-level API

#### Long-Running Operations Support
- All custom inference methods return `SyncPoller` (sync) or `PollerFlux` (async) for proper LRO handling
- Built-in polling support with customizable polling intervals
- Proper status tracking and error handling for long-running inference operations

### Other Changes
- Updated to latest TypeSpec definitions for improved API consistency
- Enhanced error handling and validation for custom model operations
- Updated metadata and configuration files to support new endpoints


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
