# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-01-06)

- Azure Resource Manager StreamAnalytics client library for Java. This package contains Microsoft Azure SDK for StreamAnalytics Management SDK. Stream Analytics Client. Package tag package-pure-2020-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.External` was removed

* `models.CSharpFunctionRetrieveDefaultDefinitionParameters` was removed

* `models.ClusterProperties` was removed

* `models.AzureMachineLearningServiceFunctionRetrieveDefaultDefinitionParameters` was removed

* `models.AzureMachineLearningStudioFunctionBinding` was removed

* `models.AzureMachineLearningServiceInputColumn` was removed

* `models.AzureDataLakeStoreOutputDataSourceProperties` was removed

* `models.AzureMachineLearningStudioInputColumn` was removed

* `models.BlobReferenceInputDataSourceProperties` was removed

* `models.AzureFunctionOutputDataSource` was removed

* `models.CustomClrSerialization` was removed

* `models.BlobStreamInputDataSourceProperties` was removed

* `models.AzureSynapseOutputDataSourceProperties` was removed

* `models.EventHubStreamInputDataSourceProperties` was removed

* `models.AzureSqlDatabaseOutputDataSourceProperties` was removed

* `models.AzureMachineLearningServiceOutputColumn` was removed

* `models.ServiceBusTopicOutputDataSourceProperties` was removed

* `models.PrivateEndpointProperties` was removed

* `models.ServiceBusQueueOutputDataSourceProperties` was removed

* `models.AggregateFunctionProperties` was removed

* `models.AzureMachineLearningStudioInputs` was removed

* `models.AzureMachineLearningServiceFunctionBinding` was removed

* `models.AzureMachineLearningStudioOutputColumn` was removed

* `models.AzureSqlReferenceInputDataSourceProperties` was removed

* `models.EventHubOutputDataSourceProperties` was removed

* `models.StreamingJobSku` was removed

* `models.BlobOutputDataSourceProperties` was removed

* `models.CSharpFunctionBinding` was removed

* `models.AzureMachineLearningStudioFunctionRetrieveDefaultDefinitionParameters` was removed

* `models.StreamingJobSkuName` was removed

* `models.PowerBIOutputDataSourceProperties` was removed

#### `models.Transformation$Update` was modified

* `ifMatch(java.lang.String)` was removed

#### `models.Output$Update` was modified

* `ifMatch(java.lang.String)` was removed

#### `models.Cluster` was modified

* `properties()` was removed

#### `models.ServiceBusQueueOutputDataSource` was modified

* `java.util.Map systemPropertyColumns()` -> `java.lang.Object systemPropertyColumns()`
* `withSystemPropertyColumns(java.util.Map)` was removed

#### `models.BlobReferenceInputDataSource` was modified

* `storageAccounts()` was removed
* `timeFormat()` was removed
* `withContainer(java.lang.String)` was removed
* `withStorageAccounts(java.util.List)` was removed
* `pathPattern()` was removed
* `withTimeFormat(java.lang.String)` was removed
* `withDateFormat(java.lang.String)` was removed
* `dateFormat()` was removed
* `container()` was removed
* `withPathPattern(java.lang.String)` was removed

#### `models.AzureSynapseOutputDataSource` was modified

* `withUser(java.lang.String)` was removed
* `user()` was removed
* `withTable(java.lang.String)` was removed
* `withPassword(java.lang.String)` was removed
* `table()` was removed
* `server()` was removed
* `database()` was removed
* `password()` was removed
* `withDatabase(java.lang.String)` was removed
* `withServer(java.lang.String)` was removed

#### `models.StreamingJob$Definition` was modified

* `withSku(models.StreamingJobSku)` was removed
* `withExternals(models.External)` was removed

#### `models.Cluster$Update` was modified

* `withProperties(models.ClusterProperties)` was removed
* `ifMatch(java.lang.String)` was removed

#### `models.StreamingJob$Update` was modified

* `ifMatch(java.lang.String)` was removed
* `withSku(models.StreamingJobSku)` was removed
* `withExternals(models.External)` was removed

#### `models.PrivateEndpoint$Update` was modified

* `ifMatch(java.lang.String)` was removed
* `withProperties(models.PrivateEndpointProperties)` was removed
* `ifNoneMatch(java.lang.String)` was removed

#### `models.Input$Update` was modified

* `ifMatch(java.lang.String)` was removed

#### `models.PrivateEndpoint` was modified

* `properties()` was removed

#### `models.Function$Update` was modified

* `ifMatch(java.lang.String)` was removed

#### `models.PrivateEndpoint$Definition` was modified

* `withProperties(models.PrivateEndpointProperties)` was removed

#### `models.PrivateLinkServiceConnection` was modified

* `withRequestMessage(java.lang.String)` was removed

#### `models.Cluster$Definition` was modified

* `withProperties(models.ClusterProperties)` was removed

#### `models.ScalarFunctionProperties` was modified

* `withInputs(java.util.List)` was removed
* `withOutput(models.FunctionOutput)` was removed
* `withBinding(models.FunctionBinding)` was removed

#### `models.AzureSqlReferenceInputDataSource` was modified

* `properties()` was removed
* `withProperties(models.AzureSqlReferenceInputDataSourceProperties)` was removed

#### `models.StreamingJob` was modified

* `models.StreamingJobSku sku()` -> `models.Sku sku()`
* `externals()` was removed

#### `models.Compression` was modified

* `java.lang.String type()` -> `models.CompressionType type()`
* `withType(java.lang.String)` was removed

#### `models.FunctionProperties` was modified

* `inputs()` was removed
* `output()` was removed
* `withBinding(models.FunctionBinding)` was removed
* `binding()` was removed
* `withInputs(java.util.List)` was removed
* `withOutput(models.FunctionOutput)` was removed

#### `models.AzureSqlDatabaseOutputDataSource` was modified

* `withMaxBatchCount(java.lang.Float)` was removed
* `password()` was removed
* `withServer(java.lang.String)` was removed
* `withAuthenticationMode(models.AuthenticationMode)` was removed
* `table()` was removed
* `maxWriterCount()` was removed
* `authenticationMode()` was removed
* `maxBatchCount()` was removed
* `server()` was removed
* `database()` was removed
* `withDatabase(java.lang.String)` was removed
* `withTable(java.lang.String)` was removed
* `withMaxWriterCount(java.lang.Float)` was removed
* `withPassword(java.lang.String)` was removed
* `withUser(java.lang.String)` was removed
* `user()` was removed

### Features Added

* `models.AzureMachineLearningWebServiceOutputColumn` was added

* `models.AzureMachineLearningWebServiceInputs` was added

* `models.Sku` was added

* `models.StreamingJobProperties` was added

* `models.AzureMachineLearningWebServiceFunctionRetrieveDefaultDefinitionParameters` was added

* `models.AzureMachineLearningWebServiceFunctionBinding` was added

* `models.SkuName` was added

* `models.RefreshType` was added

* `models.CompressionType` was added

* `models.ScaleStreamingJobParameters` was added

* `models.AzureMachineLearningWebServiceInputColumn` was added

#### `models.Transformation$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withValidStreamingUnits(java.util.List)` was added

#### `models.Output$Update` was modified

* `withIfMatch(java.lang.String)` was added

#### `models.Cluster` was modified

* `listStreamingJobs()` was added
* `capacityAllocated()` was added
* `capacityAssigned()` was added
* `provisioningState()` was added
* `clusterId()` was added
* `createdDate()` was added
* `listStreamingJobs(com.azure.core.util.Context)` was added

#### `models.ServiceBusQueueOutputDataSource` was modified

* `withSystemPropertyColumns(java.lang.Object)` was added

#### `models.Input` was modified

* `test()` was added
* `test(fluent.models.InputInner)` was added
* `test(fluent.models.InputInner,com.azure.core.util.Context)` was added

#### `models.StreamingJob$Definition` was modified

* `withSku(models.Sku)` was added

#### `models.Cluster$Update` was modified

* `withIfMatch(java.lang.String)` was added

#### `StreamAnalyticsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.Transformation$Definition` was modified

* `withValidStreamingUnits(java.util.List)` was added

#### `models.StreamingJob$Update` was modified

* `withSku(models.Sku)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.PrivateEndpoint$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added
* `withManualPrivateLinkServiceConnections(java.util.List)` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.Input$Update` was modified

* `withIfMatch(java.lang.String)` was added

#### `models.PrivateEndpoint` was modified

* `createdDate()` was added
* `manualPrivateLinkServiceConnections()` was added

#### `models.Function` was modified

* `retrieveDefaultDefinition()` was added
* `test()` was added
* `test(fluent.models.FunctionInner)` was added
* `test(fluent.models.FunctionInner,com.azure.core.util.Context)` was added
* `retrieveDefaultDefinitionWithResponse(models.FunctionRetrieveDefaultDefinitionParameters,com.azure.core.util.Context)` was added

#### `models.Function$Update` was modified

* `withIfMatch(java.lang.String)` was added

#### `models.PrivateEndpoint$Definition` was modified

* `withManualPrivateLinkServiceConnections(java.util.List)` was added

#### `models.ScalarFunctionProperties` was modified

* `inputs()` was added
* `binding()` was added
* `output()` was added

#### `models.StreamingJobs` was modified

* `scale(java.lang.String,java.lang.String,models.ScaleStreamingJobParameters)` was added
* `scale(java.lang.String,java.lang.String)` was added
* `scale(java.lang.String,java.lang.String,models.ScaleStreamingJobParameters,com.azure.core.util.Context)` was added

#### `models.AzureSqlReferenceInputDataSource` was modified

* `withRefreshType(models.RefreshType)` was added
* `withServer(java.lang.String)` was added
* `database()` was added
* `withRefreshRate(java.lang.String)` was added
* `fullSnapshotQuery()` was added
* `refreshRate()` was added
* `refreshType()` was added
* `withUser(java.lang.String)` was added
* `withDeltaSnapshotQuery(java.lang.String)` was added
* `withFullSnapshotQuery(java.lang.String)` was added
* `server()` was added
* `table()` was added
* `deltaSnapshotQuery()` was added
* `user()` was added
* `withTable(java.lang.String)` was added
* `withDatabase(java.lang.String)` was added
* `password()` was added
* `withPassword(java.lang.String)` was added

#### `models.StreamingJob` was modified

* `stop(com.azure.core.util.Context)` was added
* `start(models.StartStreamingJobParameters,com.azure.core.util.Context)` was added
* `start(models.StartStreamingJobParameters)` was added
* `scale()` was added
* `stop()` was added
* `scale(models.ScaleStreamingJobParameters)` was added
* `start()` was added
* `scale(models.ScaleStreamingJobParameters,com.azure.core.util.Context)` was added

#### `models.Compression` was modified

* `withType(models.CompressionType)` was added

#### `models.Transformation` was modified

* `validStreamingUnits()` was added

#### `models.Output` was modified

* `test(fluent.models.OutputInner)` was added
* `test(fluent.models.OutputInner,com.azure.core.util.Context)` was added
* `test()` was added

## 1.0.0-beta.1 (2021-04-21)

- Azure Resource Manager StreamAnalytics client library for Java. This package contains Microsoft Azure SDK for StreamAnalytics Management SDK. Stream Analytics Client. Package tag package-2020-03-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
