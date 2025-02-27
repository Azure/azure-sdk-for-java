# Release History

## 1.0.0 (2025-02-17)

### Features Added

- Introduced `DeidentificationCustomizationOptions` and `DeidentificationJobCustomizationOptions` models
    - Added `SurrogateLocale` field in these models
    - Moved `RedactionFormat` field into these models
- Introduced `Overwrite` flag in `TargetStorageLocation` model

### Breaking Changes

- Changed method names in `DeidentificationClient` to match functionality:
  - Changed the `deidentify` method name to `deidentifyText`
  - Changed the `beginCreateJob` method name to `beginDeidentifyDocuments`
- Changed `outputPrefix` behavior to no longer include `jobName` by default
- Changed `Path` field to `Location` in `SourceStorageLocation` and `TargetStorageLocation`
- Deprecated `DocumentDataType`
- Deprecated `Path` and `Location` from `TaggerResult` model

## 1.0.0-beta.1 (2024-08-15)

- Azure Deidentification client library for Java. This package contains Microsoft Azure Deidentification client library.

### Features Added

- Azure Deidentification client library for Java. This package contains Microsoft Azure Deidentification client library.
