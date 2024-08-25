# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.11 (2024-08-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.
- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-json` from `1.1.0` to version `1.2.0`.


## 1.2.10 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.2.9 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.


## 1.2.8 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.


## 1.2.7 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.2.6 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 1.2.5 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.


## 1.2.4 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.2.3 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.


## 1.2.2 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.


## 1.2.1 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.
- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.


## 1.2.0 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-json` from `1.0.1` to version `1.1.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.


## 1.1.2 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.

## 1.1.1 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.


## 1.1.0 (2023-05-11)

### Features added from version 1.0.14
- Added `ContainerRegistryContentClient` and `ContainerRegistryAsyncContentClient` classes that allow to upload and download images to Azure Container Registry.

### Breaking Changes from version 1.1.0-beta.4
- Added sanity check for manifest size at download time - if manifest is bigger than 4MB, `ServiceResponseException` will be thrown. Previously no exception was thrown. 
- Added sanity check for `Content-Length` header presence on the response when downloading blobs - if it's not present, `ServiceResponseException` will be thrown. 
  Previously, content was buffered and no exception was thrown.
- Renamed `ManifestMediaType.OCI_MANIFEST` to `ManifestMediaType.OCI_IMAGE_MANIFEST`. 

- There are no breaking changes from previous stable version.

### Other changes from version 1.0.14
- `ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_GERMANY` is deprecated following [Azure Germany cloud deprecation](https://learn.microsoft.com/azure/cloud-adoption-framework/migrate/azure-best-practices/multiple-regions)
- Default constructors on following classes were deprecated: `ArtifactManifestPlatform`, `ArtifactManifestOrder`, `ArtifactOperatingSystem`, `ArtifactTagOrder`, `ArtifactManifestPlatform`.

#### Dependency Updates
- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.0.14 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 1.1.0-beta.4 (2023-04-11)

### Breaking Changes from version 1.1.0-beta.3

- `ContainerRegistryBlobClientBuilder`, `ContainerRegistryBlobClient` and `ContainerRegistryBlobAsyncClient` were renamed to `ContainerRegistryContentClientBuilder`,
  `ContainerRegistryContentClient`, and `ContainerRegistryContentAsyncClient` and moved to `com.azure.containers.containerregistry` package. 
- `UploadBlobResult` was renamed to `UploadRegistryBlobResult`
- `ContainerRegistryContentClient` and `ContainerRegistryContentAsyncClient` changes:
  - `uploadManifest` method was renamed to `setManifest`, `uploadManifestWithResponse` renamed to `setManifestWithResponse`, the return type of these methods was renamed to `SetManifestResult`.
    `UploadManifestOptions` renamed to `SetManifestOptions`.
  - `downloadManifest` method was renamed to `getManifest`, `downloadManifestWithResponse` renamed to `getManifestWithResponse`, the return type of these methods renamed to `GetManifestResult`.
  - Removed `DownloadBlobAsyncResult` and changes `ContainerRegistryContentAsyncClient.downloadStream` return type to `Mono<BinaryData>`.
  - Removed `Collection<ManifestMediaType> mediaTypes` parameter from `downloadManifestWithResponse` method on blob clients.
  - Renamed `ContainerRegistryContentClientBuilder.repository` method to `repositoryName`.
  - Removed `ContainerRegistryContentAsyncClient.uploadBlob(Flux<ByteBuffer> content)` and `ContainerRegistryContentClient.uploadBlob(ReadableByteChannel stream, Context context)`, use `uploadBlob` methods that take `BinaryData` instead
- Renamed `GetManifestResult.getMediaType` and `UploadManifestOptions.getMediaType` to `getManifestMediaType`.
- Removed `GetManifestResult.asOciManifest` - use `GetManifestResult.getManifest().toObject(OciImageManifest.class)` instead.
- Renamed `OciImageManifest.getConfig` and `setConfig` methods to `getConfiguration` and `setConfiguration`.
- Renamed `OciAnnotations.getCreated` and `setCreated` methods to `getCreatedOn` and `setCreatedOn`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 1.0.13 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 1.1.0-beta.3 (2023-03-08)

### Features Added
- Added support for blob upload and download from streams for content that does not fit into memory.
- Added support to upload and download custom manifest types.
- Added support ACR access token scopes.

### Breaking Changes from version 1.1.0-beta.2
- Download blob changes:
  - `ContainerRegistryBlobClient.downloadBlob` method was renamed to `downloadStream` and now writes content to channel provided in parameters. It no longer returns `DownloadBlobResult`.
  - `ContainerRegistryBlobAsyncClient.downloadBlob` method was renamed to `downloadStream` and now returns `DownloadBlobAsyncResult`.
  - `downloadBlobWithResponse` methods on `ContainerRegistryBlobClient` and `ContainerRegistryBlobAsyncClient` classes were removed.
- Upload blob changes:
  - `uploadBlobWithResponse` methods on `ContainerRegistryBlobClient` and `ContainerRegistryBlobAsyncClient` were removed.
- Manifest changes:
  - `DownloadManifestOptions` class was removed: `downloadManifest` and `downloadManifestWithResponse` methods
    (on `ContainerRegistryBlobAsyncClient` or `ContainerRegistryBlobClient`) now take tag or digest string value instead of `DownloadManifestOptions`
  - `DownloadManifestResult.getManifestStream` and `getManifest` methods were renamed to `getContent` and `asOciManifest`.
  - `OciBlobDescriptor` class was renamed to `OciDescriptor`, `getSize` and `setSize` methods on it were renamed to `getSizeInBytes` and `setSizeInBytes`
  - `OciManifest` class was renamed to `OciImageManifest`
  - `UploadManifestOptions(binaryData)` constructor was replaced with `UploadManifestOptions(BinaryData, ManifestMediaType)` one.
  - `UploadManifestResult(string)` constructor was removed.
- Misc
  - Default audience was changed from `https://management.azure.com` to `https://containerregistry.azure.net`.

### Other Changes
- `ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_GERMANY` is deprecated following [Azure Germany cloud deprecation](https://learn.microsoft.com/azure/cloud-adoption-framework/migrate/azure-best-practices/multiple-regions)
- Default constructors on following classes were deprecated: `ArtifactManifestPlatform`, `ArtifactManifestOrder`, `ArtifactOperatingSystem`, `ArtifactTagOrder`., `ArtifactManifestPlatform`.

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.37.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.1`.

## 1.0.12 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.

## 1.1.0-beta.2 (2023-01-11)

### Other Changes
- Refactored sync APIs to leverage sync stack improvements in core and HTTP clients.

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.8`.

## 1.0.11 (2023-01-09)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.

## 1.0.10 (2022-11-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.

## 1.0.9 (2022-10-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 1.0.8 (2022-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.

## 1.0.7 (2022-08-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.4`.

## 1.0.6 (2022-07-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-core-http-netty` from `1.12.2` to version `1.12.3`.

## 1.0.5 (2022-06-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.

## 1.0.4 (2022-05-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.

## 1.1.0-beta.1 (2022-04-08)

### Features Added

- Added interfaces from `com.azure.core.client.traits` to `ContainerRegistryClientBuilder`.
- Added support for `ContainerRegistryBlobAsyncClient`.

## 1.0.3 (2022-04-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.

## 1.0.2 (2022-03-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.11.7` to version `1.11.8`.
- Upgraded `azure-core` from `1.25.0` to version `1.26.0`.

## 1.0.1 (2022-02-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.

## 1.0.0 (2022-01-11)

### Breaking Changes
  - Renamed `ArtifactTagOrderBy` to `ArtifactTagOrder`.
  - Renamed `ArtifactManifestOrderBy` to `ArtifactManifestOrder`.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` to `1.24.1`
- Upgraded `azure-core-http-netty` to `1.11.6`

## 1.0.0-beta.5 (2021-11-09)
### New features
- Enabled support for the stable `2021-07-01` swagger api-version.
- Removed `getTeleportEnabled` and `setTeleportEnabled` field from `RepositoryProperties`.

## 1.0.0-beta.4 (2021-08-20)
### Breaking Changes

- Replaced `authenticationScope` property on `ContainerRegistryClientBuilder` with `audience`.
  `audience` is of type `ContainerRegistryAudience`, which allows customers to select from available audiences or provide their own audience string.
  It is a mandatory property on the builder and needs to be set to instantiate the Container Registry clients.

### Other Changes

- Updated documentation comments.

## 1.0.0-beta.3 (2021-06-08)
### New features
- Flattened the content properties to the Tag, Manifest and Repository properties.
- Enabled support for non-public clouds.
- Added expiration time for the jwt tokens.
- Added more samples and documentation.


## 1.0.0-beta.2 (2021-05-11)
### New Features
- Added helper types for ManifestArtifact and ContainerRepository.
- Restricted builder count to 1.
- Enabled support for anonymous access.
- Improved samples and readme.

## 1.0.0-beta.1 (2021-04-14)
- Initial release. Please see the README and wiki for information on the new design.
