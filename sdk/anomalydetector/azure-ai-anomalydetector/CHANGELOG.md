# Release History

## 3.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 3.0.0-beta.5 (2022-12-08)

### Features Added

- Added `OneTable` and `MultiTable` two data schemas.
- Added Azure Managed Identity data reader access for Azure Blob Storage.
- Added `topContributorCount` in the request body for `getMultivariateBatchDetectionResult`, `detectMultivariateBatchAnomaly` and `detectMultivariateLastAnomaly`.

### Breaking Changes

- Renamed `Model` to `AnomalyDetectionModel`.
- Renamed `DetectionRequest` to `MultivariateBatchDetectionOptions`.
- Renamed `DetectionResult` to `MultivariateDetectionResult`.
- Renamed `DetectionStatus` to `MultivariateBatchDetectionStatus`.
- Renamed `DetectionResultSummary` to `MultivariateBatchDetectionResultSummary`.
- Renamed `FillNaMethod` to `FillNAMethod`.
- Renamed `LastDetectionRequest` to `MultivariateLastDetectionOptions`.
- Renamed `LastDetectionResult` to `MultivariateLastDetectionResult`.
- Replaced `ModelSnapshot` with `AnomalyDetectionModel` in `listMultivariateModel`.
- Renamed `trainMultivariateModelWithResponse` to `trainMultivariateModel`.
- Renamed `detectAnomalyWithResponse` to `detectMultivariateBatchAnomaly`.
- Renamed `getDetectionResult` to `getMultivariateBatchDetectionResult`.
- Renamed `listMultivariateModel` to `listMultivariateModels`.
- Renamed `lastDetectAnomalyWithResponse` to `detectMultivariateLastAnomaly`.
- Renamed `DetectRequest` to `UnivariateDetectionOptions`.
- Renamed `EntireDetectResponse` to `UnivariateEntireDetectionResult`.
- Renamed `LastDetectResponse` to `UnivariateLastDetectionResult`.
- Renamed `ChangePointDetectRequest` to `UnivariateChangePointDetectionOptions`.
- Renamed `ChangePointDetectResponse` to `UnivariateChangePointDetectionResult`.
- Renamed `detectEntireSeries` to `detectUnivariateEntireSeries`.
- Renamed `detectLastPoint` to `detectUnivariateLastPoint`.
- Renamed `detectChangePoint` to `detectUnivariateChangePoint`.
- Added `DataSchema` to `ModelInfo`
- Removed `AnomalyDetectorError`.
- Removed `AnomalyDetectorErrorCodes`.
- Removed `AnomalyDetectorErrorException`.
- Removed `ErrorResponseException`.
- Removed `TrainMultivariateModelHeaders` in `trainMultivariateModelWithResponse`.
- Removed `TrainMultivariateModelResponse` in `trainMultivariateModelWithResponse`.
- Removed `DetectAnomalyHeader` in `detectAnomalyWithResponse`.
- Removed `DetectAnomalyResponse` in `detectAnomalyWithResponse`.
- Removed `exportModelWithResponse`.
- Removed `changedvalue` in the inference response body.
- Removed `detectingPoints` in the sync inference request body.


## 3.0.0-beta.4 (2022-01-23)

- Fix release issues

## 3.0.0-beta.3 (2022-01-18)

- Introduced the new API `lastDetectAnomaly`
- Added 2 new optional properties: `imputeMode` & `imputeFixedValue` to the `DetectRequest` object.
- Added 1 new optional property: `severity` to the `EntireDetectResponse` & `LastDetectResponse` objects.
- Removed the optional property `errors` from the `VariableState` object.
- Refactored the optional property `contributors` to `interpretation` from the `AnomalyValue` object.
- Modified the `FillNAMethod` object into an extensible enum.


## 3.0.0-beta.2 (2021-04-16)

### New Features

- Add support for multivariate anomaly detection

## 3.0.0-beta.1 (2020-08-27)

- Change version to 3.0.0-beta.1

## 1.0.0-beta.1 (2020-08-27)

- Initial beta release for Anomaly Detector client library.
