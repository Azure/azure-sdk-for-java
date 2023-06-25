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

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Breaking Changes

- Property `uri` removed from class `GalleryArtifactVersionSource`. This property was non-functional.
- Type of property `GalleryImageVersionStorageProfile.source` changed to the class `GalleryArtifactVersionFullSource`, a subclass of `GalleryArtifactVersionSource`.
- Type of property `GalleryOSDiskImage.source` changed to the class `GalleryDiskImageSource`, a subclass of `GalleryArtifactVersionSource`.

## 2.20.0 (2022-10-26)

### Bugs Fixed

- Fixed bug that `ProviderRegistrationPolicy` does not work.

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

### Bugs Fixed

- Fixed a bug that configure of `RetryOptions` does not take effect.

## 2.14.0 (2022-04-11)

### Features Added

- Supported `authenticate(HttpPipeline, AzureProfile)` in managers.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.10.0 (2021-11-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Features Added

- Added overload of `create(Context)` for `Creatable`.
- Added overload of `apply(Context)` for `Appliable`.
### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Added support for Challenge Based Authentication in `AuthenticationPolicy`.

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated parameter of `SupportsBatchCreation.create` from `List<Creatable<ResourceT>>` to `List<? extends Creatable<ResourceT>>`

## 2.3.0 (2021-03-30)

- Added client-side validation for `getByResourceGroup`, `listByResourceGroup`, `deleteByResourceGroup` methods.

## 2.2.0 (2021-02-24)

- Improved performance of `PagedIterable`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Removed non-GA packages

## 2.0.0-beta.5 (2020-10-19)

- Renamed `Azure` to `AzureResourceManager`

## 2.0.0-beta.4 (2020-09-02)

- Added `PrivateDns`
- Added `Redis`
- Added `EventHubs`
- Added `TrafficMananger`
- Exposed `subscriptions`, `tenants` and `tenantId` API
