# Release History

## 1.0.0-beta.2 (Unreleased)

### Breaking Changes

- Merges `RunsClient` and `RunStepsClient` into `RunsClient`
- Merges `VectorStoresClient`, `VectorStoreFilesClient`, `VectoreStoreFileBatchesClient` into one `VectoreStoresClient`
- `PersistentAgentsAdministrationClientBuilder` is replace with `PersistentAgentsClientBuilder` and administration related operations are separated out into `PersistentAgentsAdministrationClient`
- `AgentsServiceVersion` is renamed to `PersistentAgentsServiceVersion`

### Bugs Fixed

- Fixed [issue in FilesClient::uploadFile](https://github.com/Azure/azure-sdk-for-java/issues/45549)

### Other Changes

- Deletion operations in service clients will not return any content. Exception will be raised if deletion fails.

## 1.0.0-beta.1 (2025-05-15)

- Initial release of Azure AI Agents Persistent client library for Java.

### Features Added

- Added support for Azure AI Agents Persistent client library for Java.
