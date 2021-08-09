# Release History

## 1.0.0-beta.5 (2021-08-10)
- Update to 2021-06-01-preview API version

### New Features
- Add Spark configuration APIs

### Breaking Changes
- Change additionalColumns/compressionType to type of object

## 1.0.0-beta.4 (2021-04-06)

### New Features
- Update with Azure Data Factory(ADF) swagger changes/2019-06-01-preview

### Breaking Changes
- `DataFlowDebugSessionClient#createDataFlowDebugSessionWithResponse()` now returns `DataFlowDebugSessionsCreateDataFlowDebugSessionResponse`
- `DataFlowDebugSessionClient#executeCommandWithResponse()` now returns `DataFlowDebugSessionsExecuteCommandResponse`
- `LibraryAsyncClient#getOperationResultWithResponse()` now returns `Mono<Response<LibraryResource>>`
- `LibraryAsyncClient#getOperationResult()` now returns `Mono<Response<LibraryResource>>`
- `LibraryClient#getOperationResultWithResponse()` now returns `Response<LibraryResource>`
- `LibraryClient#getOperationResult()` now returns `Response<LibraryResource>`
- `AvroDataset.avroCompressionCodec` property is now of type `Object`
- `CommonDataServiceForAppsLinkedService.servicePrincipalCredentialType` property is now of type `Object`
- `DatasetDeflateCompression.level` property is now of type `Object`
- `DatasetGZipCompression.level` property is now of type `Object`
- `DatasetZipDeflateCompression.level` property is now of type `Object`
- `DelimitedTextDataset.compressionCoded` property is now of type `CompressionCodec`
- `DelimitedTextDataset.compressionLevel` property is now of type `Object`
- `DynamicsCrmLinkedService.servicePrincipalCredentialType` property is now of type `Object`
- `DynamicsLinkedService.hostName` property is now of type `Object`
- `DynamicsLinkedService.port` property is now of type `Object`
- `DynamicsLinkedService.serviceUri` property is now of type `Object`
- `DynamicsLinkedService.organizationName` property is now of type `Object`
- `ParquetDataset.compressionCodec` property is now of type `Object`
- `RerunTumblingWindowTrigger.maxCurrency` property is renamed to `rerunCurrency`
- `WaitActivity.waitTimeInSeconds` property is now of type `Object`

### Dependency Updates
- Update azure-core to 1.15.0

## 1.0.0-beta.3 (2021-03-09)

- Add new APIs in `LibraryClient` and `LibraryAsyncClient`

## 1.0.0-beta.2 (2021-02-09)

- Support specifying the service API version. (AutoRest update)
- Send missing "Accept" request headers

**Breaking changes:**

- `isHaveLibraryRequirementsChanged()` and `setHaveLibraryRequirementsChanged()` methods on `BigDataPoolResourceInfo` are removed.
- `getProjectConnectionManagers()` and `getPackageConnectionManagers()` now return `Map<String, Map<String, SsisExecutionParameter>>` instead of `Map<String, Object>`.

## 1.0.0-beta.1 (2020-12-08)

Version 1.0.0-beta.1 is a beta of our efforts in creating an Azure Synapse Artifacts client library that is developer-friendly, idiomatic to
the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### New Features

- It uses Azure Synapse 2019-06-01-preview API
- Reactive streams support using [Project Reactor](https://projectreactor.io/)
