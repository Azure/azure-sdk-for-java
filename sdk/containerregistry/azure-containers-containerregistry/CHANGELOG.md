# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added
- Added interfaces from `com.azure.core.client.traits` to `ContainerRegistryClientBuilder`.

### Breaking Changes

### Bugs Fixed

### Other Changes

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
