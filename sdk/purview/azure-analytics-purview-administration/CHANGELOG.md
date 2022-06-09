# Release History

## 1.0.0-beta.2 (Unreleased)

### Breaking Changes

- Removed class `PurviewAccountClientBuilder`. It was replaced by `AccountsClientBuilder`, `CollectionsClient`, `ResourceSetRulesClientBuilder`.
- Removed class `PurviewMetadataClientBuilder`. It was replaced by `MetadataPolicyClientBuilder`, `MetadataRolesClientBuilder`.
- Merged the `Context` parameter into the `RequestOptions` parameter in methods of clients.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.23.0`.

## 1.0.0-beta.1 (2021-10-15)

### Other Changes

- Initial beta release for Purview Administration client library.
