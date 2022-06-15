# Release History

## 1.0.0-beta.4 (2022-06-15)

### Features Added

- Supported Atlas 2.2 APIs.

### Breaking Changes

- Exposed required query and header parameters to method signature.

### Bugs Fixed

- Added missing query parameter `includeTermHierarchy` for update term API.
- Added missing query parameter `excludeRelationshipTypes` for get term API.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.29.1`.
- Upgraded `azure-core-http-netty` to `1.12.2`.

## 1.0.0-beta.3 (2022-03-15)

### Breaking Changes

- Removed class `PurviewCatalogClientBuilder`. It was replaced by `CollectionClientBuilder`, `DiscoveryClientBuilder`, `EntityClientBuilder`, `GlossaryClientBuilder`, `LineageClientBuilder`, `RelationshipClientBuilder`, `TypesClientBuilder`.
- Merged the `Context` parameter into the `RequestOptions` parameter in methods of clients.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.26.0`.
- Upgraded `azure-core-http-netty` to `1.11.8`.

## 1.0.0-beta.2 (2021-10-15)

### Breaking Changes

This is a new version of LLC SDK. Changes are

- A sync client and an async client for each operation group.
- Add `RequestOptions` and `Context` to client method parameters.
- Return type of client method is always `Response` except paging and long-running operations.
- Allow users to set `ServiceVersion` in client builder.

## 1.0.0-beta.1 (2021-05-11)

- Initial beta release for Purview Catalog client library.
