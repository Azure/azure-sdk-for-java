# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2025-04-18)

### Features Added

- Introduced `DeidentificationCustomizationOptions` and `DeidentificationJobCustomizationOptions` models.
    - Added `SurrogateLocale` field in these models.
    - Moved `RedactionFormat` field into these models.
- Introduced `Overwrite` flag in `TargetStorageLocation` model.

### Breaking Changes

- Changed method names in `DeidentificationClient` to match functionality:
    - Changed the `deidentify` method name to `deidentifyText`.
    - Changed the `beginCreateJob` method name to `beginDeidentifyDocuments`.
- Renamed the property `DeidentificationContent.operation` to `operationType`.
- Deprecated `DocumentDataType`.
- Changed the model `DeidentificationDocumentDetails`:
    - Renamed `input` to `inputLocation`.
    - Renamed `output` to `outputLocation`.
- Changed the model `DeidentificationJob`
    - Renamed `name` to `jobName`.
    - Renamed `operation` to `operationType`.
- Renamed the model `OperationState` to `OperationStatus`.
- Changed the model `PhiCategory`:
  - Renamed `IDNUM` to `ID_NUM`.
  - Renamed `IPADDRESS` to `IP_ADDRESS`.
- Changed `Path` field to `Location` in `SourceStorageLocation` and `TargetStorageLocation`.
- Changed `outputPrefix` behavior to no longer include `jobName` by default.
- Deprecated `Path` and `Location` from `TaggerResult` model.

## 1.0.0-beta.1 (2024-08-15)

- Azure Deidentification client library for Java. This package contains Microsoft Azure Deidentification client library.

### Features Added

- Azure Deidentification client library for Java. This package contains Microsoft Azure Deidentification client library.
