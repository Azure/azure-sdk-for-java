# Release History

## 1.0.0-beta.16 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.15 (2024-06-11)

### Features Added

  - Model Dataset has a new parameter LakeHouseLocation
  - Model Dataset has a new parameter GoogleBigQueryV2ObjectDataset
  - Model Dataset has a new parameter PostgreSqlV2TableDataset
  - Model Dataset has a new parameter SalesforceServiceCloudV2ObjectDataset
  - Model Dataset has a new parameter SalesforceV2ObjectDataset
  - Model Dataset has a new parameter ServiceNowV2ObjectDataset
  - Model Dataset has a new parameter SnowflakeV2Dataset
  - Model Dataset has a new parameter WarehouseTableDataset
  - Model Pipeline has a new parameter ExpressionV2
  - Model Pipeline has a new parameter GoogleBigQueryV2Source
  - Model Pipeline has a new parameter LakeHouseTableSink
  - Model Pipeline has a new parameter LakeHouseTableSource
  - Model Pipeline has a new parameter LakeHouseWriteSettings
  - Model Pipeline has a new parameter LakeHouseReadSettings
  - Model Pipeline has a new parameter Metadata
  - Model Pipeline has a new parameter MetadataItem
  - Model Pipeline has a new parameter ParquetReadSettingsstate
  - Model Pipeline has a new parameter PostgreSqlV2Source
  - Model Pipeline has a new parameter SalesforceServiceCloudV2Sink
  - Model Pipeline has a new parameter SalesforceServiceCloudV2Source
  - Model Pipeline has a new parameter SalesforceV2Sink
  - Model Pipeline has a new parameter SalesforceV2SourceReadBehavior
  - Model Pipeline has a new parameter SalesforceV2Source
  - Model Pipeline has a new parameter ServiceNowV2Source
  - Model Pipeline has a new parameter SnowflakeV2Sink
  - Model Pipeline has a new parameter SnowflakeV2Source
  - Model Pipeline has a new parameter WarehouseSink
  - Model Pipeline has a new parameter WarehouseSource
  - Model LinkedService add supports GoogleAds
  - Model LinkedService has a new parameter GoogleBigQueryV2LinkedService
  - Model LinkedService has a new parameter LakeHouseLinkedService
  - Model LinkedService has a new parameter PostgreSqlV2LinkedService
  - Model LinkedService has a new parameter SalesforceServiceCloudV2LinkedService
  - Model LinkedService has a new parameter SalesforceV2LinkedService
  - Model LinkedService has a new parameter SalesforceV2LinkedService
  - Model LinkedService has a new parameter SnowflakeV2LinkedService
  - Model LinkedService has a new parameter WarehouseLinkedService
  - Model LinkedService has a new parameter WarehouseLinkedService

### Breaking Changes

  - Model LinkedService parameter MariaDBLinkedService update new properties
  - Model LinkedService parameter MySqlLinkedService update new properties
  - Model LinkedService parameter ServiceNowV2LinkedService update properties
  - Model Pipeline parameter ExecuteDataFlowActivity update new properties computeType
  - Model Pipeline parameter ScriptActivityScriptBlock update properties type

## 1.0.0-beta.14 (2023-12-11)

### Other Changes

- Fix runNotebook sessionId from int to string
- Fix placeholder links causing 404s
- Sync expression Support From DataFactory To Synapse

## 1.0.0-beta.13 (2023-07-21)

### Features Added

- Added `authenticationType` , `containerUri`, `sasUri` and `sasToken`  properties to BlobService.
- Added `setSystemVariable` proprety to SetVariableActivityTypeProperties.
- Added `mongoDbAtlasDriverVersion` property to MongoDbAtlasLinkedServiceTypeProperties.
- Added `ActionOnExistingTargetTable`  property for Synapse Link.
- Added `OutputColumn`  Object For Office365Source outputColumns.
- Added  `configurationType` , `targetSparkConfiguration`  and  `sparkConfig`  properties for SynapseNotebookActivityTypeProperties.
- Added  `credential` property for LinkedService.
- Added  `isolationLevel` property for SQLServerSource.
- Added new apis of Create/Cancel/GetStatus/GetSnapshot for RunNotebook.

### Other Changes

- Upgraded `azure-core` to `1.41.0`.
- Upgraded `azure-core-http-netty` to `1.13.5`.

## 1.0.0-beta.12 (2023-01-12)

### Features Added

- Added `workspaceResourceId` to `AzureSynapseArtifactsLinkedService`.
- Added `pythonCodeReference`, `filesV2`, `scanFolder`, `configurationType`, `targetSparkConfiguration` and `sparkConfig` properties to `SynapseSparkJobDefinitionActivity`.
- Added `authHeaders` property to `RestServiceLinkedService`.
- Added new APIs of Pause/Resume for Synapse Link in `LinkConnectionClient`.
- Added `PowerBIWorkspaceLinkedService`.

### Breaking Changes

- Renamed API `createOrUpdateLinkConnection` to `createOrUpdate` in `LinkConnectionClient`.
- Renamed API `deleteLinkConnection` to `delete` in `LinkConnectionClient`.
- Renamed API `getLinkConnection` to `get` in `LinkConnectionClient`.
- Renamed API `listLinkConnectionsByWorkspace` to `listByWorkspace` in `LinkConnectionClient`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.35.0`.
- Upgraded `azure-core-http-netty` to `1.12.8`.

## 1.0.0-beta.11 (2022-09-19)

### Features Added

- Updated LinkConnection for Synapse Link.
- Added TargetSparkConfiguration property for SparkJobDefinition and Notebook.
- Added GoogleSheets connector.
- Added SAP ODP connector.
- Added support OAuth2ClientCredential auth in RestSevice.
- Added support rejected data linked service in dataflow sink.
- Added Dataworld, AppFigures, Asana, Twilio connectors.
- Added Fail Activity.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.32.0`.
- Upgraded `azure-core-http-netty` to `1.12.5`.

## 1.0.0-beta.10 (2022-04-13)

### Features Added

- Added interfaces from com.azure.core.client.traits to `ArtifactsClientBuilder`.
- Added `retryOptions` method to `ArtifactsClientBuilder`.
- Added `LinkConnectionClient` and `LinkConnectionAsyncClient`.
- Added class `DataworldLinkedService`, `AppFiguresLinkedService`, `AsanaLinkedService`, `TwilioLinkedService` as subclass of `LinkedService`.

### Breaking Changes

- Removed redundant `CloudErrorAutoGeneratedException` and `CloudErrorAutoGenerated`. Use `CloudErrorException` and `CloudError`.
- Changed type of `storedProcedureParameters` from `Map` to `Object` in `SqlServerStoredProcedureActivity`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.27.0`.
- Upgraded `azure-core-http-netty` to `1.11.9`.

## 1.0.0-beta.9 (2022-03-08)

### Features Added

- Added class `ScriptActivity` as subclass of `ExecutionActivity`.
- Added class `TeamDeskLinkedService`, `QuickbaseLinkedService`, `SmartsheetLinkedService`, `ZendeskLinkedService` as subclass of `LinkedService`.
- Added field `sparkPool` to `SynapseNotebookActivity`.
- Added fields to `SynapseSparkJobDefinitionActivity`.

### Breaking Changes

- Changed type of `referenceName` field in `SynapseNotebookReference` from `String` to `Object`.

### Other Changes

- Upgraded to [package-artifacts-composite-v3](https://github.com/Azure/azure-rest-api-specs/blob/f2fb403f64e0fcb1a799c60daf980f0cde495f8f/specification/synapse/data-plane/readme.md#tag-package-artifacts-composite-v3)

#### Dependency Updates

- Upgraded `azure-core` to `1.26.0`.
- Upgraded `azure-core-http-netty` to `1.11.8`.

## 1.0.0-beta.8 (2022-01-26)

### Features Added

- Added `MetaStoreClient` and `MetaStoreAsyncClient`
- Added `MetastoreRegistrationResponse`, `MetastoreRequestSuccessResponse`, `MetastoreUpdateObject`, `MetastoreUpdationResponse`, `NotebookParameter`, `NotebookParameterType`, `RequestStatus`, `ResourceStatus` model classes.

## 1.0.0-beta.7 (2021-11-18)

### Breaking Changes
- Removed methods `getDataSet`, `setDataset`, `getLinkedService` and `setLinkedService` from `DataflowSink` and `DataflowSource` models.
- Removed `DatasetBZip2Compression`, `DatasetDeflateCompression`, `DatasetGZipCompression`, `DatasetTarCompression`, `DatasetTarGZipCompression` and `DatasetZipDeflateCompression` models.

### Other Changes
- Upgrade to [package-artifacts-composite-v2](https://github.com/Azure/azure-rest-api-specs/blob/9ab141452538ce5cf1427300d3c181923a8a8765/specification/synapse/data-plane/readme.md#tag-package-artifacts-composite-v2)

## 1.0.0-beta.6 (2021-11-08)

### Breaking Changes
- `CloudErrorException` is replaced with `CloudErrorAutoGeneratedException`
- `OperationStatus` clients are replaced with `KqlScripts` clients
- Removed `OperationResult` clients.

### Other Changes
- Upgrade to [package-artifacts-composite-v1](https://github.com/Azure/azure-rest-api-specs/blob/bee724836ffdeb5458274037dc75f4d43576b5e3/specification/synapse/data-plane/readme.md#tag-package-artifacts-composite-v1)

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
