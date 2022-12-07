# Release History

## 3.0.0-beta.5 (2022-12-01)

### Features Added

- Added `OneTable` and `MultiTable` two data schemas.
- Added `topContributorCoun` in the request body for `getMultivariateBatchDetectionResult`, `detectMultivariateBatchAnomaly` and `detectMultivariateLastAnomaly`.

### Breaking Changes

- Renamed `trainMultivariateModelWithResponse` to `createAndTrainMultivariateModel`
- Renamed `detectAnomalyWithResponse` to `detectMultivariateBatchAnomaly`
- Renamed `getDetectionResult` to `getMultivariateBatchDetectionResult`
- Renamed `listMultivariateModel` to `listMultivariateModels`
- Renamed `lastDetectAnomalyWithResponse` to `detectMultivariateLastAnomaly`
- Renamed `detectEntireSeries` to `detectUnivariateEntireSeries`
- Renamed `detectLastPoint` to `detectUnivariateLastPoint`
- Renamed `detectChangePoint` to `detectUnivariateChangePoint`
- Removed `exportModelWithResponse`
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
