# Release History

## 1.0.0 (2024-09-11)

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
