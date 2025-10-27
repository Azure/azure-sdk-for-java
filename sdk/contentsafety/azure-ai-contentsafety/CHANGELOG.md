# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.16 (2025-10-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.56.1` to version `1.57.0`.
- Upgraded `azure-core-http-netty` from `1.16.1` to version `1.16.2`.


## 1.0.15 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.16.0` to version `1.16.1`.
- Upgraded `azure-core` from `1.56.0` to version `1.56.1`.


## 1.0.14 (2025-08-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.5` to version `1.56.0`.
- Upgraded `azure-core-http-netty` from `1.15.13` to version `1.16.0`.


## 1.0.13 (2025-07-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.4` to version `1.55.5`.
- Upgraded `azure-core-http-netty` from `1.15.12` to version `1.15.13`.


## 1.0.12 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.
- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.


## 1.0.11 (2025-03-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to version `1.55.3`.
- Upgraded `azure-core-http-netty` from `1.15.10` to version `1.15.11`.


## 1.0.10 (2025-02-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.54.1` to version `1.55.2`.
- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.10`.

## 1.0.9 (2024-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.53.0` to version `1.54.1`.
- Upgraded `azure-core-http-netty` from `1.15.5` to version `1.15.7`.


## 1.0.8 (2024-10-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.4` to version `1.15.5`.
- Upgraded `azure-core` from `1.52.0` to version `1.53.0`.


## 1.0.7 (2024-09-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.


## 1.0.6 (2024-08-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.


## 1.0.5 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.0.4 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.


## 1.0.3 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.


## 1.0.2 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.0.1 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.1`.


## 1.0.0 (2023-12-13)

### Features Added
- Support Microsoft Entra ID Authentication
- Support 8 severity level for AnalyzeText
### Breaking Changes
Contract change for AnalyzeText, AnalyzeImage, Blocklist management related methods
#### AnalyzeText
- AnalyzeTextOptions
  - Renamed `breakByBlocklists` to `haltOnBlocklistHit`
  - Added `AnalyzeTextOutputType`
- AnalyzeTextResult
  - Renamed `blocklistsMatchResults` to `blocklistsMatch`
  - Replaced `TextAnalyzeSeverityResult` by `TextCategoriesAnalysis`
#### AnalyzeImage
- AnalyzeImageOptions
    - Replaced `ImageData` by `ContentSafetyImageData`
    - Added `AnalyzeImageOutputType`
- AnalyzeImageResult
    - Replaced `ImageAnalyzeSeverityResult` by `ImageCategoriesAnalysis`
#### Blocklist management
- Added `BlocklistAsyncClient`
- Renamed `AddBlockItemsOptions` to `AddOrUpdateTextBlocklistItemsOptions`
- Renamed `AddBlockItemsResult` to `AddOrUpdateTextBlocklistItemsResult`
- Renamed `RemoveBlockItemsOptions` to `RemoveTextBlocklistItemsOptions`
- Renamed `TextBlockItemInfo` to `TextBlocklistItem`

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.43.0` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.10`.
- Upgraded `azure-identity` from `1.10.1` to version `1.11.0`
## 1.0.0-beta.1 (2023-09-28)

- Azure AI ContentSafety client library for Java. This package contains Microsoft Azure ContentSafety client library.

### Features Added
* Text Analysis API: Scans text for sexual content, violence, hate, and self harm with multi-severity levels.
* Image Analysis API: Scans images for sexual content, violence, hate, and self harm with multi-severity levels.
* Text Blocklist Management APIs: The default AI classifiers are sufficient for most content safety needs; however, you might need to screen for terms that are specific to your use case. You can create blocklists of terms to use with the Text API.
