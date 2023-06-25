# Release History

## 2.29.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Features Added

- Supported private endpoint connection in `Registry`.
- Supported `enableDedicatedDataEndpoints` in `Registry`.
- Supported `withAccessFromTrustedServices` in `Registry`.
- Supported `withAccessFromSelectedNetworks` and `withAccessFromAllNetworks` in `Registry`.
- Supported `withAccessFromIpAddress` and `withAccessFromIpAddressRange` in `Registry`.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Bugs Fixed

- Fixed bug that `refresh` method on `RegistryTaskRun` class fails, when `RegistryTaskRun` is initialized via `RegistryTaskRuns.listByRegistry`.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-12-01`.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.20.0 (2022-10-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.19.0 (2022-09-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.18.0 (2022-08-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Breaking Changes

- Removed preview features of `ExportPipelinesClient`, `ImportPipelinesClient`, `PipelineRunsClient`, `ScopeMapsClient`, `TokensClient`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-09-01`.

## 2.10.0 (2021-11-22)

### Features Added

- Supported disabling public network access in `Registry` via `disablePublicNetworkAccess()`, for private link feature.

### Breaking Changes

- Removed classic SKU support in `Registry`, as service with `api-version` after late 2019 no longer supports it.
- `taskName` property of `TaskRunRequest` renamed to `taskId`.
- `value` property of `TaskRunRequest` moved into `overrideTaskStepProperties` property.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2019-12-01-preview`.

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0-beta.5 (2020-10-19)

- Updated `api-version` to `2019-05-01`

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
