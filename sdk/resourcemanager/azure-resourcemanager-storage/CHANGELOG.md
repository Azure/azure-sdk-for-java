# Release History

## 2.16.0-beta.1 (Unreleased)

### Features Added

- Supported toggling blob versioning in `BlobServiceProperties`.

### Other Changes

## 2.15.0 (2022-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-09-01`.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Breaking Changes

- Remove field `STORAGE_FILE_DATA_SMB_SHARE_OWNER` from class `DefaultSharePermission`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-08-01`.

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

- Updated `api-version` to `2021-04-01`.

### Bugs Fixed

- Fixed bug on ETag for `ImmutabilityPolicy`.

### Breaking Changes

- Removed class `GetShareExpand`, `ListSharesExpand`, `PutSharesExpand`. Parameter is now comma-separated strings.
- Moved `destination` field from `BlobInventoryPolicySchema` class to its `rules` (`BlobInventoryPolicyRule` class).

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Supported enabling infrastructure encryption for `StorageAccount`.
- Supported enabling customer-managed key for Tables and Queues in `StorageAccount`.

## 2.4.0 (2021-04-28)

- Supported Private Link in `StorageAccount`

## 2.3.0 (2021-03-30)

- Updated `api-version` to `2021-02-01`
- Storage account default to Transport Layer Security (TLS) 1.2 for HTTPS

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2021-01-01`
- Return type of `Identity.type()` changed from `String` to `IdentityType`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
