# Release History

## 1.0.0-beta.30 (2024-08-21)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.HDInsightOnDemandLinkedService` was modified

* `withVersion(java.lang.Object)` was removed
* `version()` was removed

### Features Added

#### `models.CmkIdentityDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CosmosDbMongoDbApiSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlMISink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuickBooksSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapEccLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SmartsheetLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TarReadSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TwilioLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Activity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Expression` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HBaseLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.AzureDataLakeStoreLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MicrosoftAccessTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DrillLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GoogleBigQuerySource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkedIntegrationRuntimeRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceNowObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkedIntegrationRuntimeKeyAuthorization` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PowerQuerySource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsisEnvironmentReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptActivityParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WarehouseTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CopyActivityLogSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CopySink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SnowflakeLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.ExecutePipelineActivityPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedVirtualNetworkListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileShareDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapOdpLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebLinkedServiceTypeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DelimitedTextSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RemotePrivateEndpointConnection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlUpsertSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Trigger` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MongoDbAtlasLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.IntegrationRuntime` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebBasicAuthentication` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EloquaObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkConnectionApprovalRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileServerWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SsisLogLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RedirectIncompatibleRowSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OdbcTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PipelineFolder` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeOutboundNetworkDependenciesEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TumblingWindowTrigger` was modified

* `runtimeState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureFileStorageLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapOpenHubLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureFileStorageWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SsisProject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapHanaTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlServerSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CosmosDbSqlApiSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceServiceCloudSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HubspotObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetezzaSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonMwsObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VerticaLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `withUid(java.lang.Object)` was added
* `port()` was added
* `server()` was added
* `withDatabase(java.lang.Object)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `uid()` was added
* `withPort(java.lang.Object)` was added
* `database()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withServer(java.lang.Object)` was added

#### `models.DatasetStorageFormat` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HDInsightOnDemandLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `versionTypePropertiesVersion()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersionTypePropertiesVersion(java.lang.Object)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OrcFormat` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TumblingWindowTriggerDependencyReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlServerBaseLinkedServiceTypeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureTableSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatasetLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PostgreSqlV2TableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureTableStorageLinkedService` was modified

* `withServiceEndpoint(java.lang.Object)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `credential()` was added
* `serviceEndpoint()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `withCredential(models.CredentialReference)` was added

#### `models.MySqlSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FileServerLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFlowDebugCommandRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlMISource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateIntegrationRuntimeNodeRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDataLakeStoreReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SelfDependencyTumblingWindowTriggerReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScriptActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ParquetFormat` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InformixTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PurviewConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GoogleBigQueryObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SynapseNotebookReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleServiceCloudObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MongoDbCollectionDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HDInsightSparkActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonS3LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetezzaLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DocumentDbCollectionSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JsonFormat` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DependencyReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIdentityCredential` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeCustomerVirtualNetwork` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FormatReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MagentoLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InformixLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentVariableSetup` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeComputeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedPrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMariaDBTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VerticaTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PaypalSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MapperPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ParquetSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServicePrincipalCredential` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAccessPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HttpReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetMetadataActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SalesforceV2Source` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ZohoSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ActivityPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GoogleAdWordsObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PackageStore` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FactoryListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FilterActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Dataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExpressionV2` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Office365Source` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HttpLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapOpenHubSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HttpServerLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GreenplumLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ParquetWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PostgreSqlLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForEachActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BinaryDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommonDataServiceForAppsLinkedService` was modified

* `domain()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDomain(java.lang.Object)` was added

#### `models.DataFlowDebugCommandPayload` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExecuteWranglingDataflowActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkConnectionApprovalRequestResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SsisPackageLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SquareLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FactoryRepoConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LakeHouseTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonS3CompatibleLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzurePostgreSqlTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SnowflakeDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HttpSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DynamicsCrmSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBlobFSReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HiveSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetezzaPartitionSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceServiceCloudV2LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIntegrationRuntimeOperationResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeOutboundNetworkDependenciesEndpointDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BinarySource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonMwsSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CassandraTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SalesforceSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MicrosoftAccessSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DelimitedTextWriteSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InformixSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureTableSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDatabricksDeltaLakeExportCommand` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StagingSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AppFiguresLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRdsForOracleSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataFlowDebugPackageDebugSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SynapseSparkJobReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComponentSetup` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBatchLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.MongoDbLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SubResourceDebugResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MongoDbAtlasCollectionDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFlowSourceSetting` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabricksNotebookActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDataExplorerTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DistcpSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DocumentDbCollectionDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GoogleBigQueryV2Source` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PostgreSqlV2Source` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapHanaLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.WaitActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleCloudStorageLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CopyActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SnowflakeSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VariableSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CmdkeySetup` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogStorageSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WarehouseSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzurePostgreSqlSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataFlowDebugPackage` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IfConditionActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlDWSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ActivityDependency` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RunQueryOrderBy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsisParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrestoSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDataLakeStoreSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureSynapseArtifactsLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TeamDeskLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AsanaLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceServiceCloudV2ObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFlowStagingInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatasetReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ShopifyObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExecutionActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapTableResourceDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebhookActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapCloudForCustomerSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Office365Dataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MicrosoftAccessSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceNowV2LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidationActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LakeHouseLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkedIntegrationRuntime` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConcurLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SquareObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BlobSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JsonWriteSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapTablePartitionSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PipelineRunInvokedBy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionStateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AvroSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OracleTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureMariaDBSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.XeroObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomEventsTrigger` was modified

* `runtimeState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeSsisProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PowerQuerySinkMapping` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SynapseNotebookActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRedshiftTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MariaDBLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.XeroLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SnowflakeSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzurePostgreSqlSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureSqlDWTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OrcWriteSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceNowV2ObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GoogleAdWordsSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HiveObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapOdpResourceDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WarehouseSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TeradataTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConcurObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MultiplePipelineTrigger` was modified

* `runtimeState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceNowV2Source` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CouchbaseTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OdbcSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MariaDBSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecureInputOutputPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureFileStorageReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RestSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FtpServerLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureMLServiceLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.SapEccSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FtpReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapOpenHubTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CassandraSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeDataFlowProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RerunTumblingWindowTrigger` was modified

* `runtimeState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDataLakeStoreDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapTableSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ArmIdWrapper` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommonDataServiceForAppsSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScriptActivityScriptBlock` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LakeHouseWriteSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DynamicsAXLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBlobFSWriteSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileServerReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataFlowDebugResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HdfsSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TabularTranslator` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CopySource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MongoDbV2LinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MySqlTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SalesforceServiceCloudV2Source` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrestoLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CosmosDbMongoDbApiCollectionDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ChangeDataCaptureFolder` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsCrmEntityDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureMLBatchExecutionActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRdsForOracleLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFlowReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Db2TableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatasetCompression` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DrillSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureStorageLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.SsisChildPackage` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SetVariableActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SalesforceLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.MongoDbCursorMethodsProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMLWebServiceFile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PowerQuerySink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FactoryRepoUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetadataItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RestResourceDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapBWLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsisFolder` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RetryPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SharePointOnlineListResourceDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ZohoLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapTableLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ChangeDataCaptureListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RecurrenceScheduleOccurrence` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceNowSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebActivityAuthentication` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MongoDbSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JiraLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PostgreSqlTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LakeHouseReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JsonReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsAXResourceDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InformixSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolybaseSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureFileStorageLinkedService` was modified

* `credential()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `serviceEndpoint()` was added
* `withCredential(models.CredentialReference)` was added
* `withServiceEndpoint(java.lang.Object)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SparkObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ParameterSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RecurrenceSchedule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvroDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ParquetSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SftpWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleTrigger` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `runtimeState()` was added

#### `models.DeleteDataFlowDebugSessionRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConcurSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataLakeAnalyticsUsqlActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRdsForSqlServerSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImportSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FactoryGitHubConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OrcSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PhoenixSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CosmosDbMongoDbApiSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JiraObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BinaryReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataFlow` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ParquetDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureSqlDatabaseLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureSqlDWLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkedServiceReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SsisPackage` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureMLExecutePipelineActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SalesforceMarketingCloudSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WranglingDataFlow` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatabricksSparkJarActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GitHubClientSecret` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RedshiftUnloadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ShopifyLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PipelineReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TeradataSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DrillTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MapperSourceConnectionsInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrestoObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GreenplumSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceServiceCloudSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceV2LinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TeradataLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapBwSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OdbcSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIntegrationRuntime` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DelimitedTextSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonRdsForOraclePartitionSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsCrmSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDataLakeStoreWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapHanaSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DelimitedTextReadSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogLocationSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SparkLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FactoryIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OraclePartitionSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScriptAction` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapCloudForCustomerSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleServiceCloudLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SnowflakeExportCopyCommand` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBlobStorageLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsisPropertyOverride` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Transformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SnowflakeV2Sink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ODataResourceDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsisAccessCredential` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBlobFSSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.QueryDataFlowDebugSessionsResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BlobEventsTrigger` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `runtimeState()` was added

#### `models.AzureBlobStorageLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TriggerDependencyReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SquareSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContinuationSettingsReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDataLakeStoreLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BigDataPoolParametrizationReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TeradataPartitionSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFlowListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomDataSourceLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebAnonymousAuthentication` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceNowLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.OrcDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlServerTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TriggerFilterParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMySqlTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureSearchLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.IntegrationRuntimeDebugResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HBaseObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScriptActivityTypePropertiesLogSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonS3Location` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StoreWriteSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExcelDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JsonDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExecuteSsisPackageActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImpalaSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SybaseTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatasetListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsisObjectMetadata` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UntilActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBlobFSLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeleteActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureMySqlLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LakeHouseLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Credential` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureQueueSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JsonSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SynapseSparkJobDefinitionActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExecuteDataFlowActivityTypePropertiesCompute` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HubspotLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureSqlMITableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceV2ObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ZohoObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapOdpSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SwitchCase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationMetricSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SnowflakeV2LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapEccResourceDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ControlActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonS3CompatibleReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CommonDataServiceForAppsEntityDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MappingDataFlow` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBlobStorageWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HDInsightPigActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapBwCubeDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomSetupBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SybaseSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MapperDslConnectorProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JiraSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedVirtualNetworkReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SftpLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBlobDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DWCopyCommandSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkipErrorFile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `domain()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withDomain(java.lang.Object)` was added

#### `models.SqlDWUpsertSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlAlwaysEncryptedProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CassandraLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.AvroFormat` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CreateDataFlowDebugSessionRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SftpReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MapperConnectionReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ODataLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GoogleAdWordsLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TypeConversionSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeOutboundNetworkDependenciesCategoryEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HDInsightLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GoogleCloudStorageLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureBlobStorageReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResponsysSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RestServiceLinkedService` was modified

* `withServicePrincipalEmbeddedCert(models.SecretBase)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `servicePrincipalEmbeddedCertPassword()` was added
* `servicePrincipalEmbeddedCert()` was added
* `withVersion(java.lang.String)` was added
* `withServicePrincipalEmbeddedCertPassword(models.SecretBase)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `servicePrincipalCredentialType()` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added

#### `models.CosmosDbLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBlobFSLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JsonSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapCloudForCustomerResourceDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SparkSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RestSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CopyTranslator` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeDataFlowPropertiesCustomPropertiesItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MongoDbAtlasSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExposureControlBatchRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HBaseSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SelfHostedIntegrationRuntime` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BinarySink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedIntegrationRuntimeNode` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonS3ReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OracleCloudStorageLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VerticaSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlServerSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SwitchActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatasetDebugResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDataLakeAnalyticsLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduleTriggerRecurrence` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MariaDBTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BlobSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HiveLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CopyComputeScaleProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExportSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SnowflakeV2Dataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StoreReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDatabricksDeltaLakeSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationLogSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDatabricksDeltaLakeLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HttpDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TriggerRun` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HdfsLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LakeHouseTableSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRedshiftSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GoogleSheetsLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketoSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ZipDeflateReadSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EloquaLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.MongoDbV2CollectionDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CompressionReadSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SapHanaPartitionSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMLUpdateResourceActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationServiceSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetezzaTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlDWSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureFunctionActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzPowerShellSetup` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExecutePipelineActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.XeroSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMySqlSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeDataProxyProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeVNetProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ODataSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureSearchIndexDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDatabricksDeltaLakeImportCommand` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HDInsightHiveActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SapCloudForCustomerLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GoogleCloudStorageLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FailActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.XmlSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MapperAttributeMapping` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OdbcLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.GlobalParameterListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIntegrationRuntimeError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MongoDbAtlasSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TabularSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileSystemSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `version()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WarehouseLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RelationalSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFlowFolder` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HdfsLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.SalesforceMarketingCloudObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SnowflakeV2Source` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TextFormat` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PaypalObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzurePostgreSqlLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SharePointOnlineListSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlPartitionSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BlobTrigger` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `runtimeState()` was added

#### `models.OracleSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SnowflakeImportCopyCommand` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationMetricAvailability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SparkConfigurationParametrizationReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateIntegrationRuntimeRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebClientCertificateAuthentication` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDatabricksDeltaLakeDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeRegenerateKeyParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMLLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SalesforceServiceCloudV2Sink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.SqlSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureFunctionLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FactoryUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CredentialListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IntegrationRuntimeSsisCatalogInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureSqlSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBlobFSDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PostgreSqlSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HdfsReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonS3CompatibleLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MongoDbV2Source` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DelimitedTextDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PaypalLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ParquetReadSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceMarketingCloudLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.GitHubAccessTokenRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDataExplorerSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExposureControlRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SsisExecutionParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TarGZipReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureSqlTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FormatWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDatabricksLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.GoogleBigQueryV2LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonS3Dataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SelfHostedIntegrationRuntimeStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `dataFactoryName()` was added
* `state()` was added

#### `models.PostgreSqlV2LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PhoenixObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketoObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkedServiceDebugResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EncryptionConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuickbaseLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResponsysObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DWCopyCommandDefaultValue` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDatabricksDeltaLakeSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MicrosoftAccessLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CommonDataServiceForAppsSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GlobalParameterSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabricksSparkPythonActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleServiceCloudSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CosmosDbSqlApiCollectionDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecretBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMariaDBLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OrcSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinkedIntegrationRuntimeRbacAuthorization` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExecuteDataFlowActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlServerLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsEntityDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DocumentDbCollectionSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureBlobFSSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRedshiftLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MapperTargetConnectionsInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinkedServiceListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CreateLinkedIntegrationRuntimeRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CosmosDbSqlApiSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetDataFactoryOperationStatusResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MapperAttributeMappings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MapperAttributeReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResponsysLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SsisExecutionCredential` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImpalaLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.FileSystemSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileServerLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SftpServerLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.AzureKeyVaultSecretReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MySqlLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.MagentoSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LakeHouseTableSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CouchbaseSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HDInsightStreamingActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PhoenixLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Db2LinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RunQueryFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsCrmLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `domain()` was added
* `withDomain(java.lang.Object)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LookupActivity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FtpServerLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.XmlDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuickBooksLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added

#### `models.GreenplumTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatasetSchemaDataElement` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SsisVariable` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NotebookParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceServiceCloudLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecureString` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvroSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GoogleCloudStorageReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SalesforceServiceCloudObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SalesforceV2Sink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDataExplorerLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.Db2Source` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationMetricDimension` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HubspotSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CouchbaseLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MagentoObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SharePointOnlineListLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `servicePrincipalEmbeddedCertPassword()` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `servicePrincipalEmbeddedCert()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withServicePrincipalEmbeddedCert(models.SecretBase)` was added
* `servicePrincipalCredentialType()` was added
* `withServicePrincipalEmbeddedCertPassword(models.SecretBase)` was added

#### `models.AppendVariableActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MapperConnection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SybaseLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.SsisEnvironment` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FactoryVstsConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Flowlet` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HDInsightMapReduceActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonMwsLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MongoDbV2Sink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PipelineListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.QuickBooksObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CosmosDbMongoDbApiLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatasetFolder` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RelationalTableDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GoogleBigQueryLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataFlowSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MapperTable` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRdsForOracleTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlServerStoredProcedureActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureSqlSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataworldLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PipelineElapsedTimeMetricPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExcelSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeNodeMonitoringData` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureKeyVaultLinkedService` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIntegrationRuntimeStatus` was modified

* `dataFactoryName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `state()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OracleCloudStorageReadSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureSearchIndexSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataMapperMapping` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDataExplorerCommandActivity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureSqlMILinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkedIntegrationRuntimeType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImpalaObjectDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PipelinePolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedPrivateEndpointListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedVirtualNetwork` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataFlowSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmazonRdsForSqlServerTableDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PipelineExternalComputeScaleProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureDataExplorerSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureMySqlSink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetSsisObjectMetadataRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Office365LinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.XmlReadSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MapperTableSchema` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EloquaSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureDataLakeStoreSink` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvroWriteSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TriggerPipelineReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ChainingTrigger` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `runtimeState()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AmazonRdsForSqlServerLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ActivityRun` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.MapperPolicyRecurrence` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CredentialReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ZendeskLinkedService` was modified

* `withVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MarketoLinkedService` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withVersion(java.lang.String)` was added

#### `models.ShopifySource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomActivityReferenceObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GoogleBigQueryV2ObjectDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DynamicsAXSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RunFilterParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IntegrationRuntimeCustomSetupScriptProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.29 (2024-06-19)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedIdentityCredentialResource` was removed

* `models.ServicePrincipalCredentialResource` was removed

#### `models.CredentialOperations` was modified

* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.CredentialResourceInner,java.lang.String,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.CredentialResourceInner)` was removed

#### `models.ExpressionV2` was modified

* `operator()` was removed
* `withOperator(java.lang.String)` was removed

### Features Added

* `models.CredentialResource$Update` was added

* `models.SqlServerBaseLinkedServiceTypeProperties` was added

* `models.CredentialResource$DefinitionStages` was added

* `models.CredentialResource$UpdateStages` was added

* `models.CredentialResource$Definition` was added

* `models.AmazonRdsForSqlAuthenticationType` was added

* `models.AzureSqlMIAuthenticationType` was added

* `models.ContinuationSettingsReference` was added

* `models.SqlServerAuthenticationType` was added

* `models.AzureSqlDWAuthenticationType` was added

* `models.AzureSqlDatabaseAuthenticationType` was added

#### `models.CredentialOperations` was modified

* `getById(java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.SalesforceV2Source` was modified

* `withQuery(java.lang.Object)` was added
* `query()` was added

#### `models.ExpressionV2` was modified

* `withOperators(java.util.List)` was added
* `operators()` was added

#### `models.ExecuteWranglingDataflowActivity` was modified

* `continuationSettings()` was added
* `withContinuationSettings(models.ContinuationSettingsReference)` was added

#### `models.LakeHouseTableDataset` was modified

* `withSchemaTypePropertiesSchema(java.lang.Object)` was added
* `schemaTypePropertiesSchema()` was added

#### `models.SalesforceServiceCloudV2Source` was modified

* `withQuery(java.lang.Object)` was added
* `query()` was added

#### `models.AzureSqlDatabaseLinkedService` was modified

* `withConnectTimeout(java.lang.Object)` was added
* `multipleActiveResultSets()` was added
* `withLoadBalanceTimeout(java.lang.Object)` was added
* `integratedSecurity()` was added
* `withServer(java.lang.Object)` was added
* `withDatabase(java.lang.Object)` was added
* `withMultiSubnetFailover(java.lang.Object)` was added
* `withIntegratedSecurity(java.lang.Object)` was added
* `withMinPoolSize(java.lang.Object)` was added
* `server()` was added
* `withMaxPoolSize(java.lang.Object)` was added
* `withAuthenticationType(models.AzureSqlDatabaseAuthenticationType)` was added
* `withConnectRetryCount(java.lang.Object)` was added
* `withUsername(java.lang.Object)` was added
* `withConnectRetryInterval(java.lang.Object)` was added
* `username()` was added
* `connectTimeout()` was added
* `connectRetryCount()` was added
* `database()` was added
* `authenticationType()` was added
* `trustServerCertificate()` was added
* `withEncrypt(java.lang.Object)` was added
* `multiSubnetFailover()` was added
* `minPoolSize()` was added
* `servicePrincipalCredential()` was added
* `withHostnameInCertificate(java.lang.Object)` was added
* `failoverPartner()` was added
* `withServicePrincipalCredential(models.SecretBase)` was added
* `withTrustServerCertificate(java.lang.Object)` was added
* `servicePrincipalCredentialType()` was added
* `withPacketSize(java.lang.Object)` was added
* `loadBalanceTimeout()` was added
* `withCommandTimeout(java.lang.Object)` was added
* `applicationIntent()` was added
* `packetSize()` was added
* `commandTimeout()` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added
* `connectRetryInterval()` was added
* `hostnameInCertificate()` was added
* `maxPoolSize()` was added
* `withFailoverPartner(java.lang.Object)` was added
* `encrypt()` was added
* `withMultipleActiveResultSets(java.lang.Object)` was added
* `withPooling(java.lang.Object)` was added
* `withApplicationIntent(java.lang.Object)` was added
* `pooling()` was added

#### `models.AzureSqlDWLinkedService` was modified

* `withMultipleActiveResultSets(java.lang.Object)` was added
* `withLoadBalanceTimeout(java.lang.Object)` was added
* `commandTimeout()` was added
* `withDatabase(java.lang.Object)` was added
* `connectTimeout()` was added
* `connectRetryInterval()` was added
* `minPoolSize()` was added
* `applicationIntent()` was added
* `withCommandTimeout(java.lang.Object)` was added
* `withServer(java.lang.Object)` was added
* `integratedSecurity()` was added
* `withConnectTimeout(java.lang.Object)` was added
* `database()` was added
* `withFailoverPartner(java.lang.Object)` was added
* `withEncrypt(java.lang.Object)` was added
* `withHostnameInCertificate(java.lang.Object)` was added
* `withMaxPoolSize(java.lang.Object)` was added
* `withPacketSize(java.lang.Object)` was added
* `multiSubnetFailover()` was added
* `withIntegratedSecurity(java.lang.Object)` was added
* `connectRetryCount()` was added
* `encrypt()` was added
* `withServicePrincipalCredential(models.SecretBase)` was added
* `username()` was added
* `withAuthenticationType(models.AzureSqlDWAuthenticationType)` was added
* `authenticationType()` was added
* `trustServerCertificate()` was added
* `maxPoolSize()` was added
* `withPooling(java.lang.Object)` was added
* `withApplicationIntent(java.lang.Object)` was added
* `withTrustServerCertificate(java.lang.Object)` was added
* `packetSize()` was added
* `withMultiSubnetFailover(java.lang.Object)` was added
* `failoverPartner()` was added
* `servicePrincipalCredentialType()` was added
* `server()` was added
* `pooling()` was added
* `withMinPoolSize(java.lang.Object)` was added
* `loadBalanceTimeout()` was added
* `multipleActiveResultSets()` was added
* `servicePrincipalCredential()` was added
* `hostnameInCertificate()` was added
* `withConnectRetryCount(java.lang.Object)` was added
* `withConnectRetryInterval(java.lang.Object)` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added
* `withUsername(java.lang.Object)` was added

#### `models.SnowflakeExportCopyCommand` was modified

* `storageIntegration()` was added
* `withStorageIntegration(java.lang.Object)` was added

#### `models.CredentialResource` was modified

* `resourceGroupName()` was added
* `refresh()` was added
* `update()` was added
* `refresh(com.azure.core.util.Context)` was added

#### `models.SnowflakeImportCopyCommand` was modified

* `withStorageIntegration(java.lang.Object)` was added
* `storageIntegration()` was added

#### `models.ExecuteDataFlowActivity` was modified

* `withContinuationSettings(models.ContinuationSettingsReference)` was added
* `continuationSettings()` was added

#### `models.SqlServerLinkedService` was modified

* `connectRetryCount()` was added
* `applicationIntent()` was added
* `withDatabase(java.lang.Object)` was added
* `withMultipleActiveResultSets(java.lang.Object)` was added
* `withCommandTimeout(java.lang.Object)` was added
* `maxPoolSize()` was added
* `withMultiSubnetFailover(java.lang.Object)` was added
* `connectTimeout()` was added
* `withEncrypt(java.lang.Object)` was added
* `withCredential(models.CredentialReference)` was added
* `withPacketSize(java.lang.Object)` was added
* `minPoolSize()` was added
* `multipleActiveResultSets()` was added
* `withTrustServerCertificate(java.lang.Object)` was added
* `packetSize()` was added
* `credential()` was added
* `withMaxPoolSize(java.lang.Object)` was added
* `withIntegratedSecurity(java.lang.Object)` was added
* `withLoadBalanceTimeout(java.lang.Object)` was added
* `withConnectRetryCount(java.lang.Object)` was added
* `integratedSecurity()` was added
* `withHostnameInCertificate(java.lang.Object)` was added
* `pooling()` was added
* `multiSubnetFailover()` was added
* `trustServerCertificate()` was added
* `withConnectRetryInterval(java.lang.Object)` was added
* `withPooling(java.lang.Object)` was added
* `hostnameInCertificate()` was added
* `failoverPartner()` was added
* `authenticationType()` was added
* `withConnectTimeout(java.lang.Object)` was added
* `loadBalanceTimeout()` was added
* `withMinPoolSize(java.lang.Object)` was added
* `withAuthenticationType(models.SqlServerAuthenticationType)` was added
* `commandTimeout()` was added
* `withApplicationIntent(java.lang.Object)` was added
* `withServer(java.lang.Object)` was added
* `connectRetryInterval()` was added
* `withFailoverPartner(java.lang.Object)` was added
* `encrypt()` was added
* `server()` was added
* `database()` was added

#### `models.DynamicsCrmLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

#### `models.AzureSqlMILinkedService` was modified

* `withHostnameInCertificate(java.lang.Object)` was added
* `multipleActiveResultSets()` was added
* `trustServerCertificate()` was added
* `server()` was added
* `packetSize()` was added
* `withMinPoolSize(java.lang.Object)` was added
* `hostnameInCertificate()` was added
* `encrypt()` was added
* `connectRetryCount()` was added
* `withIntegratedSecurity(java.lang.Object)` was added
* `withCommandTimeout(java.lang.Object)` was added
* `maxPoolSize()` was added
* `withPooling(java.lang.Object)` was added
* `withDatabase(java.lang.Object)` was added
* `username()` was added
* `withLoadBalanceTimeout(java.lang.Object)` was added
* `loadBalanceTimeout()` was added
* `withUsername(java.lang.Object)` was added
* `integratedSecurity()` was added
* `database()` was added
* `withServer(java.lang.Object)` was added
* `pooling()` was added
* `withEncrypt(java.lang.Object)` was added
* `withFailoverPartner(java.lang.Object)` was added
* `commandTimeout()` was added
* `multiSubnetFailover()` was added
* `withConnectRetryCount(java.lang.Object)` was added
* `withMultiSubnetFailover(java.lang.Object)` was added
* `withMultipleActiveResultSets(java.lang.Object)` was added
* `withApplicationIntent(java.lang.Object)` was added
* `connectRetryInterval()` was added
* `servicePrincipalCredentialType()` was added
* `servicePrincipalCredential()` was added
* `connectTimeout()` was added
* `failoverPartner()` was added
* `withMaxPoolSize(java.lang.Object)` was added
* `applicationIntent()` was added
* `withAuthenticationType(models.AzureSqlMIAuthenticationType)` was added
* `minPoolSize()` was added
* `withConnectTimeout(java.lang.Object)` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added
* `withServicePrincipalCredential(models.SecretBase)` was added
* `withPacketSize(java.lang.Object)` was added
* `withConnectRetryInterval(java.lang.Object)` was added
* `withTrustServerCertificate(java.lang.Object)` was added
* `authenticationType()` was added

#### `models.AmazonRdsForSqlServerLinkedService` was modified

* `withFailoverPartner(java.lang.Object)` was added
* `withPacketSize(java.lang.Object)` was added
* `authenticationType()` was added
* `minPoolSize()` was added
* `multipleActiveResultSets()` was added
* `withConnectRetryInterval(java.lang.Object)` was added
* `withConnectTimeout(java.lang.Object)` was added
* `failoverPartner()` was added
* `hostnameInCertificate()` was added
* `withLoadBalanceTimeout(java.lang.Object)` was added
* `withMaxPoolSize(java.lang.Object)` was added
* `applicationIntent()` was added
* `connectRetryCount()` was added
* `encrypt()` was added
* `withAuthenticationType(models.AmazonRdsForSqlAuthenticationType)` was added
* `loadBalanceTimeout()` was added
* `multiSubnetFailover()` was added
* `connectRetryInterval()` was added
* `withConnectRetryCount(java.lang.Object)` was added
* `withHostnameInCertificate(java.lang.Object)` was added
* `withCommandTimeout(java.lang.Object)` was added
* `withEncrypt(java.lang.Object)` was added
* `packetSize()` was added
* `withPooling(java.lang.Object)` was added
* `connectTimeout()` was added
* `trustServerCertificate()` was added
* `integratedSecurity()` was added
* `withMinPoolSize(java.lang.Object)` was added
* `server()` was added
* `withIntegratedSecurity(java.lang.Object)` was added
* `commandTimeout()` was added
* `withDatabase(java.lang.Object)` was added
* `withServer(java.lang.Object)` was added
* `database()` was added
* `maxPoolSize()` was added
* `withApplicationIntent(java.lang.Object)` was added
* `withMultiSubnetFailover(java.lang.Object)` was added
* `pooling()` was added
* `withMultipleActiveResultSets(java.lang.Object)` was added
* `withTrustServerCertificate(java.lang.Object)` was added

## 1.0.0-beta.28 (2024-04-18)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedIdentityCredentialResource$Update` was removed

* `models.ManagedIdentityCredentialResource$DefinitionStages` was removed

* `models.ManagedIdentityCredentialResource$Definition` was removed

* `models.ManagedIdentityCredentialResource$UpdateStages` was removed

* `models.ScriptType` was removed

#### `models.CredentialOperations` was modified

* `define(java.lang.String)` was removed
* `getById(java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `models.ManagedIdentityCredentialResource get(java.lang.String,java.lang.String,java.lang.String)` -> `models.CredentialResource get(java.lang.String,java.lang.String,java.lang.String)`

#### `models.ScriptActivityScriptBlock` was modified

* `models.ScriptType type()` -> `java.lang.Object type()`
* `withType(models.ScriptType)` was removed

#### `models.ManagedIdentityCredentialResource` was modified

* `refresh()` was removed
* `update()` was removed
* `etag()` was removed
* `innerModel()` was removed
* `type()` was removed
* `resourceGroupName()` was removed
* `id()` was removed
* `refresh(com.azure.core.util.Context)` was removed
* `name()` was removed
* `models.ManagedIdentityCredential properties()` -> `models.ManagedIdentityCredential properties()`

### Features Added

* `models.CredentialResource` was added

* `models.ServicePrincipalCredentialResource` was added

#### `models.CosmosDbMongoDbApiSource` was modified

* `type()` was added

#### `models.SqlMISink` was modified

* `type()` was added

#### `models.CredentialOperations` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.CredentialResourceInner)` was added
* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.CredentialResourceInner,java.lang.String,com.azure.core.util.Context)` was added

#### `models.QuickBooksSource` was modified

* `type()` was added

#### `models.SapEccLinkedService` was modified

* `type()` was added

#### `models.SmartsheetLinkedService` was modified

* `type()` was added

#### `models.TarReadSettings` was modified

* `type()` was added

#### `models.TwilioLinkedService` was modified

* `type()` was added

#### `models.Activity` was modified

* `type()` was added

#### `models.HBaseLinkedService` was modified

* `type()` was added

#### `models.AzureDataLakeStoreLocation` was modified

* `type()` was added

#### `models.MicrosoftAccessTableDataset` was modified

* `type()` was added

#### `models.DrillLinkedService` was modified

* `type()` was added

#### `models.GoogleBigQuerySource` was modified

* `type()` was added

#### `models.ServiceNowObjectDataset` was modified

* `type()` was added

#### `models.LinkedIntegrationRuntimeKeyAuthorization` was modified

* `authorizationType()` was added

#### `models.WarehouseTableDataset` was modified

* `type()` was added

#### `models.CopySink` was modified

* `type()` was added

#### `models.SnowflakeLinkedService` was modified

* `type()` was added

#### `models.FileShareDataset` was modified

* `type()` was added

#### `models.SapOdpLinkedService` was modified

* `type()` was added

#### `models.WebLinkedServiceTypeProperties` was modified

* `authenticationType()` was added

#### `models.DelimitedTextSource` was modified

* `type()` was added

#### `models.Trigger` was modified

* `type()` was added

#### `models.MongoDbAtlasLinkedService` was modified

* `type()` was added

#### `models.IntegrationRuntime` was modified

* `type()` was added

#### `models.WebBasicAuthentication` was modified

* `authenticationType()` was added

#### `models.EloquaObjectDataset` was modified

* `type()` was added

#### `models.CustomDataset` was modified

* `type()` was added

#### `models.FileServerWriteSettings` was modified

* `type()` was added

#### `models.OdbcTableDataset` was modified

* `type()` was added

#### `models.TumblingWindowTrigger` was modified

* `type()` was added

#### `models.AzureFileStorageLocation` was modified

* `type()` was added

#### `models.SapOpenHubLinkedService` was modified

* `type()` was added

#### `models.AzureFileStorageWriteSettings` was modified

* `type()` was added

#### `models.SsisProject` was modified

* `type()` was added

#### `models.SapHanaTableDataset` was modified

* `type()` was added

#### `models.SalesforceSource` was modified

* `type()` was added

#### `models.SqlServerSink` was modified

* `type()` was added

#### `models.CosmosDbSqlApiSink` was modified

* `type()` was added

#### `models.SalesforceServiceCloudSource` was modified

* `type()` was added

#### `models.HubspotObjectDataset` was modified

* `type()` was added

#### `models.NetezzaSource` was modified

* `type()` was added

#### `models.AmazonMwsObjectDataset` was modified

* `type()` was added

#### `models.VerticaLinkedService` was modified

* `type()` was added

#### `models.DatasetStorageFormat` was modified

* `type()` was added

#### `models.HDInsightOnDemandLinkedService` was modified

* `type()` was added

#### `models.OrcFormat` was modified

* `type()` was added

#### `models.TumblingWindowTriggerDependencyReference` was modified

* `type()` was added

#### `models.AzureTableSink` was modified

* `type()` was added

#### `models.DatasetLocation` was modified

* `type()` was added

#### `models.PostgreSqlV2TableDataset` was modified

* `type()` was added

#### `models.AzureTableStorageLinkedService` was modified

* `type()` was added

#### `models.MySqlSource` was modified

* `type()` was added

#### `models.FileServerLinkedService` was modified

* `type()` was added

#### `models.SqlMISource` was modified

* `type()` was added

#### `models.AzureDataLakeStoreReadSettings` was modified

* `type()` was added

#### `models.SelfDependencyTumblingWindowTriggerReference` was modified

* `type()` was added

#### `models.ScriptActivity` was modified

* `type()` was added

#### `models.ParquetFormat` was modified

* `type()` was added

#### `models.InformixTableDataset` was modified

* `type()` was added

#### `models.GoogleBigQueryObjectDataset` was modified

* `type()` was added

#### `models.OracleServiceCloudObjectDataset` was modified

* `type()` was added

#### `models.MongoDbCollectionDataset` was modified

* `type()` was added

#### `models.HDInsightSparkActivity` was modified

* `type()` was added

#### `models.AmazonS3LinkedService` was modified

* `type()` was added

#### `models.NetezzaLinkedService` was modified

* `type()` was added

#### `models.DocumentDbCollectionSink` was modified

* `type()` was added

#### `models.JsonFormat` was modified

* `type()` was added

#### `models.DependencyReference` was modified

* `type()` was added

#### `models.ManagedIdentityCredential` was modified

* `type()` was added

#### `models.FormatReadSettings` was modified

* `type()` was added

#### `models.MagentoLinkedService` was modified

* `type()` was added

#### `models.InformixLinkedService` was modified

* `type()` was added

#### `models.EnvironmentVariableSetup` was modified

* `type()` was added

#### `models.WebActivity` was modified

* `type()` was added

#### `models.AzureMariaDBTableDataset` was modified

* `type()` was added

#### `models.VerticaTableDataset` was modified

* `type()` was added

#### `models.PaypalSource` was modified

* `type()` was added

#### `models.ParquetSink` was modified

* `type()` was added

#### `models.ServicePrincipalCredential` was modified

* `type()` was added

#### `models.HttpReadSettings` was modified

* `type()` was added

#### `models.GetMetadataActivity` was modified

* `type()` was added

#### `models.SalesforceObjectDataset` was modified

* `type()` was added

#### `models.SalesforceV2Source` was modified

* `type()` was added

#### `models.ZohoSource` was modified

* `type()` was added

#### `models.GoogleAdWordsObjectDataset` was modified

* `type()` was added

#### `models.FilterActivity` was modified

* `type()` was added

#### `models.Dataset` was modified

* `type()` was added

#### `models.Office365Source` was modified

* `type()` was added

#### `models.HttpLinkedService` was modified

* `type()` was added

#### `models.SapOpenHubSource` was modified

* `type()` was added

#### `models.HttpServerLocation` was modified

* `type()` was added

#### `models.GreenplumLinkedService` was modified

* `type()` was added

#### `models.ParquetWriteSettings` was modified

* `type()` was added

#### `models.PostgreSqlLinkedService` was modified

* `type()` was added

#### `models.ForEachActivity` was modified

* `type()` was added

#### `models.BinaryDataset` was modified

* `type()` was added

#### `models.CommonDataServiceForAppsLinkedService` was modified

* `type()` was added

#### `models.ExecuteWranglingDataflowActivity` was modified

* `type()` was added

#### `models.SquareLinkedService` was modified

* `type()` was added

#### `models.FactoryRepoConfiguration` was modified

* `type()` was added

#### `models.LakeHouseTableDataset` was modified

* `type()` was added

#### `models.AmazonS3CompatibleLocation` was modified

* `type()` was added

#### `models.AzurePostgreSqlTableDataset` was modified

* `type()` was added

#### `models.SnowflakeDataset` was modified

* `type()` was added

#### `models.HttpSource` was modified

* `type()` was added

#### `models.DynamicsCrmSink` was modified

* `type()` was added

#### `models.AzureBlobFSReadSettings` was modified

* `type()` was added

#### `models.HiveSource` was modified

* `type()` was added

#### `models.SalesforceServiceCloudV2LinkedService` was modified

* `type()` was added

#### `models.BinarySource` was modified

* `type()` was added

#### `models.AmazonMwsSource` was modified

* `type()` was added

#### `models.CassandraTableDataset` was modified

* `type()` was added

#### `models.SalesforceSink` was modified

* `type()` was added

#### `models.MicrosoftAccessSink` was modified

* `type()` was added

#### `models.DelimitedTextWriteSettings` was modified

* `type()` was added

#### `models.InformixSink` was modified

* `type()` was added

#### `models.AzureTableSource` was modified

* `type()` was added

#### `models.AzureDatabricksDeltaLakeExportCommand` was modified

* `type()` was added

#### `models.AppFiguresLinkedService` was modified

* `type()` was added

#### `models.AmazonRdsForOracleSource` was modified

* `type()` was added

#### `models.ComponentSetup` was modified

* `type()` was added

#### `models.AzureBatchLinkedService` was modified

* `type()` was added

#### `models.MongoDbLinkedService` was modified

* `type()` was added

#### `models.MongoDbAtlasCollectionDataset` was modified

* `type()` was added

#### `models.DatabricksNotebookActivity` was modified

* `type()` was added

#### `models.AzureDataExplorerTableDataset` was modified

* `type()` was added

#### `models.DocumentDbCollectionDataset` was modified

* `type()` was added

#### `models.GoogleBigQueryV2Source` was modified

* `type()` was added

#### `models.PostgreSqlV2Source` was modified

* `type()` was added

#### `models.SapHanaLinkedService` was modified

* `type()` was added

#### `models.WaitActivity` was modified

* `type()` was added

#### `models.OracleCloudStorageLinkedService` was modified

* `type()` was added

#### `models.CopyActivity` was modified

* `type()` was added

#### `models.SnowflakeSink` was modified

* `type()` was added

#### `models.CmdkeySetup` was modified

* `type()` was added

#### `models.WarehouseSource` was modified

* `type()` was added

#### `models.AzurePostgreSqlSource` was modified

* `type()` was added

#### `models.IfConditionActivity` was modified

* `type()` was added

#### `models.SqlDWSink` was modified

* `type()` was added

#### `models.PrestoSource` was modified

* `type()` was added

#### `models.AzureDataLakeStoreSource` was modified

* `type()` was added

#### `models.AzureSynapseArtifactsLinkedService` was modified

* `type()` was added

#### `models.TeamDeskLinkedService` was modified

* `type()` was added

#### `models.AsanaLinkedService` was modified

* `type()` was added

#### `models.SalesforceServiceCloudV2ObjectDataset` was modified

* `type()` was added

#### `models.ShopifyObjectDataset` was modified

* `type()` was added

#### `models.ExecutionActivity` was modified

* `type()` was added

#### `models.SapTableResourceDataset` was modified

* `type()` was added

#### `models.WebhookActivity` was modified

* `type()` was added

#### `models.SapCloudForCustomerSource` was modified

* `type()` was added

#### `models.Office365Dataset` was modified

* `type()` was added

#### `models.MicrosoftAccessSource` was modified

* `type()` was added

#### `models.ServiceNowV2LinkedService` was modified

* `type()` was added

#### `models.ValidationActivity` was modified

* `type()` was added

#### `models.LakeHouseLinkedService` was modified

* `type()` was added

#### `models.ConcurLinkedService` was modified

* `type()` was added

#### `models.SquareObjectDataset` was modified

* `type()` was added

#### `models.BlobSink` was modified

* `type()` was added

#### `models.JsonWriteSettings` was modified

* `type()` was added

#### `models.AvroSink` was modified

* `type()` was added

#### `models.CustomActivity` was modified

* `type()` was added

#### `models.OracleTableDataset` was modified

* `type()` was added

#### `models.AzureMariaDBSource` was modified

* `type()` was added

#### `models.XeroObjectDataset` was modified

* `type()` was added

#### `models.CustomEventsTrigger` was modified

* `type()` was added

#### `models.SynapseNotebookActivity` was modified

* `type()` was added

#### `models.AmazonRedshiftTableDataset` was modified

* `type()` was added

#### `models.MariaDBLinkedService` was modified

* `type()` was added

#### `models.XeroLinkedService` was modified

* `type()` was added

#### `models.SnowflakeSource` was modified

* `type()` was added

#### `models.AzurePostgreSqlSink` was modified

* `type()` was added

#### `models.AzureSqlDWTableDataset` was modified

* `type()` was added

#### `models.OrcWriteSettings` was modified

* `type()` was added

#### `models.ServiceNowV2ObjectDataset` was modified

* `type()` was added

#### `models.GoogleAdWordsSource` was modified

* `type()` was added

#### `models.HiveObjectDataset` was modified

* `type()` was added

#### `models.SapOdpResourceDataset` was modified

* `type()` was added

#### `models.WarehouseSink` was modified

* `type()` was added

#### `models.TeradataTableDataset` was modified

* `type()` was added

#### `models.ConcurObjectDataset` was modified

* `type()` was added

#### `models.MultiplePipelineTrigger` was modified

* `type()` was added

#### `models.ServiceNowV2Source` was modified

* `type()` was added

#### `models.CouchbaseTableDataset` was modified

* `type()` was added

#### `models.OdbcSink` was modified

* `type()` was added

#### `models.MariaDBSource` was modified

* `type()` was added

#### `models.AzureFileStorageReadSettings` was modified

* `type()` was added

#### `models.RestSource` was modified

* `type()` was added

#### `models.FtpServerLocation` was modified

* `type()` was added

#### `models.AzureMLServiceLinkedService` was modified

* `type()` was added

#### `models.SapEccSource` was modified

* `type()` was added

#### `models.FtpReadSettings` was modified

* `type()` was added

#### `models.SapOpenHubTableDataset` was modified

* `type()` was added

#### `models.CassandraSource` was modified

* `type()` was added

#### `models.RerunTumblingWindowTrigger` was modified

* `type()` was added

#### `models.AzureDataLakeStoreDataset` was modified

* `type()` was added

#### `models.SapTableSource` was modified

* `type()` was added

#### `models.CommonDataServiceForAppsSource` was modified

* `type()` was added

#### `models.ScriptActivityScriptBlock` was modified

* `withType(java.lang.Object)` was added

#### `models.OracleSource` was modified

* `type()` was added

#### `models.LakeHouseWriteSettings` was modified

* `type()` was added

#### `models.DynamicsAXLinkedService` was modified

* `type()` was added

#### `models.AzureBlobFSWriteSettings` was modified

* `type()` was added

#### `models.FileServerReadSettings` was modified

* `type()` was added

#### `models.HdfsSource` was modified

* `type()` was added

#### `models.TabularTranslator` was modified

* `type()` was added

#### `models.CopySource` was modified

* `type()` was added

#### `models.MongoDbV2LinkedService` was modified

* `type()` was added

#### `models.MySqlTableDataset` was modified

* `type()` was added

#### `models.SalesforceServiceCloudV2Source` was modified

* `type()` was added

#### `models.PrestoLinkedService` was modified

* `type()` was added

#### `models.CosmosDbMongoDbApiCollectionDataset` was modified

* `type()` was added

#### `models.WebSource` was modified

* `type()` was added

#### `models.DynamicsCrmEntityDataset` was modified

* `type()` was added

#### `models.AzureMLBatchExecutionActivity` was modified

* `type()` was added

#### `models.AmazonRdsForOracleLinkedService` was modified

* `type()` was added

#### `models.Db2TableDataset` was modified

* `type()` was added

#### `models.DrillSource` was modified

* `type()` was added

#### `models.AzureStorageLinkedService` was modified

* `type()` was added

#### `models.ManagedIdentityCredentialResource` was modified

* `validate()` was added
* `withId(java.lang.String)` was added
* `withProperties(models.ManagedIdentityCredential)` was added
* `properties()` was added

#### `models.SetVariableActivity` was modified

* `type()` was added

#### `models.SalesforceLinkedService` was modified

* `type()` was added

#### `models.RestResourceDataset` was modified

* `type()` was added

#### `models.SapBWLinkedService` was modified

* `type()` was added

#### `models.SsisFolder` was modified

* `type()` was added

#### `models.SharePointOnlineListResourceDataset` was modified

* `type()` was added

#### `models.ZohoLinkedService` was modified

* `type()` was added

#### `models.SapTableLinkedService` was modified

* `type()` was added

#### `models.ServiceNowSource` was modified

* `type()` was added

#### `models.MongoDbSource` was modified

* `type()` was added

#### `models.JiraLinkedService` was modified

* `type()` was added

#### `models.PostgreSqlTableDataset` was modified

* `type()` was added

#### `models.LakeHouseReadSettings` was modified

* `type()` was added

#### `models.JsonReadSettings` was modified

* `type()` was added

#### `models.DynamicsAXResourceDataset` was modified

* `type()` was added

#### `models.InformixSource` was modified

* `type()` was added

#### `models.AzureFileStorageLinkedService` was modified

* `type()` was added

#### `models.SparkObjectDataset` was modified

* `type()` was added

#### `models.AvroDataset` was modified

* `type()` was added

#### `models.ParquetSource` was modified

* `type()` was added

#### `models.SftpWriteSettings` was modified

* `type()` was added

#### `models.ScheduleTrigger` was modified

* `type()` was added

#### `models.ConcurSource` was modified

* `type()` was added

#### `models.DataLakeAnalyticsUsqlActivity` was modified

* `type()` was added

#### `models.AmazonRdsForSqlServerSource` was modified

* `type()` was added

#### `models.ImportSettings` was modified

* `type()` was added

#### `models.FactoryGitHubConfiguration` was modified

* `type()` was added

#### `models.OrcSink` was modified

* `type()` was added

#### `models.PhoenixSource` was modified

* `type()` was added

#### `models.CosmosDbMongoDbApiSink` was modified

* `type()` was added

#### `models.JiraObjectDataset` was modified

* `type()` was added

#### `models.BinaryReadSettings` was modified

* `type()` was added

#### `models.DataFlow` was modified

* `type()` was added

#### `models.ParquetDataset` was modified

* `type()` was added

#### `models.AzureSqlDatabaseLinkedService` was modified

* `type()` was added

#### `models.AzureSqlDWLinkedService` was modified

* `type()` was added

#### `models.SsisPackage` was modified

* `type()` was added

#### `models.AzureMLExecutePipelineActivity` was modified

* `type()` was added

#### `models.SalesforceMarketingCloudSource` was modified

* `type()` was added

#### `models.WranglingDataFlow` was modified

* `type()` was added

#### `models.DatabricksSparkJarActivity` was modified

* `type()` was added

#### `models.ShopifyLinkedService` was modified

* `type()` was added

#### `models.TeradataSource` was modified

* `type()` was added

#### `models.DrillTableDataset` was modified

* `type()` was added

#### `models.PrestoObjectDataset` was modified

* `type()` was added

#### `models.GreenplumSource` was modified

* `type()` was added

#### `models.SalesforceServiceCloudSink` was modified

* `type()` was added

#### `models.SalesforceV2LinkedService` was modified

* `type()` was added

#### `models.TeradataLinkedService` was modified

* `type()` was added

#### `models.SapBwSource` was modified

* `type()` was added

#### `models.OdbcSource` was modified

* `type()` was added

#### `models.ManagedIntegrationRuntime` was modified

* `type()` was added

#### `models.DelimitedTextSink` was modified

* `type()` was added

#### `models.DynamicsCrmSource` was modified

* `type()` was added

#### `models.AzureDataLakeStoreWriteSettings` was modified

* `type()` was added

#### `models.SapHanaSource` was modified

* `type()` was added

#### `models.DelimitedTextReadSettings` was modified

* `type()` was added

#### `models.SparkLinkedService` was modified

* `type()` was added

#### `models.SapCloudForCustomerSink` was modified

* `type()` was added

#### `models.OracleServiceCloudLinkedService` was modified

* `type()` was added

#### `models.SnowflakeExportCopyCommand` was modified

* `type()` was added

#### `models.AzureBlobStorageLinkedService` was modified

* `type()` was added

#### `models.SnowflakeV2Sink` was modified

* `type()` was added

#### `models.ODataResourceDataset` was modified

* `type()` was added

#### `models.AzureBlobFSSource` was modified

* `type()` was added

#### `models.BlobEventsTrigger` was modified

* `type()` was added

#### `models.AzureBlobStorageLocation` was modified

* `type()` was added

#### `models.TriggerDependencyReference` was modified

* `type()` was added

#### `models.SquareSource` was modified

* `type()` was added

#### `models.AzureDataLakeStoreLinkedService` was modified

* `type()` was added

#### `models.CustomDataSourceLinkedService` was modified

* `type()` was added

#### `models.WebAnonymousAuthentication` was modified

* `authenticationType()` was added

#### `models.ServiceNowLinkedService` was modified

* `type()` was added

#### `models.OrcDataset` was modified

* `type()` was added

#### `models.SqlServerTableDataset` was modified

* `type()` was added

#### `models.DynamicsSource` was modified

* `type()` was added

#### `models.AzureMySqlTableDataset` was modified

* `type()` was added

#### `models.AzureSearchLinkedService` was modified

* `type()` was added

#### `models.HBaseObjectDataset` was modified

* `type()` was added

#### `models.AmazonS3Location` was modified

* `type()` was added

#### `models.DynamicsSink` was modified

* `type()` was added

#### `models.StoreWriteSettings` was modified

* `type()` was added

#### `models.ExcelDataset` was modified

* `type()` was added

#### `models.JsonDataset` was modified

* `type()` was added

#### `models.ExecuteSsisPackageActivity` was modified

* `type()` was added

#### `models.ImpalaSource` was modified

* `type()` was added

#### `models.SybaseTableDataset` was modified

* `type()` was added

#### `models.SsisObjectMetadata` was modified

* `type()` was added

#### `models.UntilActivity` was modified

* `type()` was added

#### `models.AzureBlobFSLocation` was modified

* `type()` was added

#### `models.DeleteActivity` was modified

* `type()` was added

#### `models.AzureMySqlLinkedService` was modified

* `type()` was added

#### `models.LakeHouseLocation` was modified

* `type()` was added

#### `models.Credential` was modified

* `type()` was added

#### `models.AzureQueueSink` was modified

* `type()` was added

#### `models.JsonSink` was modified

* `type()` was added

#### `models.SynapseSparkJobDefinitionActivity` was modified

* `type()` was added

#### `models.HubspotLinkedService` was modified

* `type()` was added

#### `models.AzureSqlMITableDataset` was modified

* `type()` was added

#### `models.SalesforceV2ObjectDataset` was modified

* `type()` was added

#### `models.ZohoObjectDataset` was modified

* `type()` was added

#### `models.SapOdpSource` was modified

* `type()` was added

#### `models.SnowflakeV2LinkedService` was modified

* `type()` was added

#### `models.AzureTableDataset` was modified

* `type()` was added

#### `models.SapEccResourceDataset` was modified

* `type()` was added

#### `models.ControlActivity` was modified

* `type()` was added

#### `models.AmazonS3CompatibleReadSettings` was modified

* `type()` was added

#### `models.CommonDataServiceForAppsEntityDataset` was modified

* `type()` was added

#### `models.MappingDataFlow` was modified

* `type()` was added

#### `models.AzureBlobStorageWriteSettings` was modified

* `type()` was added

#### `models.HDInsightPigActivity` was modified

* `type()` was added

#### `models.SapBwCubeDataset` was modified

* `type()` was added

#### `models.CustomSetupBase` was modified

* `type()` was added

#### `models.SybaseSource` was modified

* `type()` was added

#### `models.JiraSource` was modified

* `type()` was added

#### `models.SftpLocation` was modified

* `type()` was added

#### `models.AzureBlobDataset` was modified

* `type()` was added

#### `models.DynamicsLinkedService` was modified

* `type()` was added

#### `models.WebTableDataset` was modified

* `type()` was added

#### `models.CassandraLinkedService` was modified

* `type()` was added

#### `models.AvroFormat` was modified

* `type()` was added

#### `models.SftpReadSettings` was modified

* `type()` was added

#### `models.ODataLinkedService` was modified

* `type()` was added

#### `models.GoogleAdWordsLinkedService` was modified

* `type()` was added

#### `models.HDInsightLinkedService` was modified

* `type()` was added

#### `models.GoogleCloudStorageLocation` was modified

* `type()` was added

#### `models.AzureBlobStorageReadSettings` was modified

* `type()` was added

#### `models.ResponsysSource` was modified

* `type()` was added

#### `models.RestServiceLinkedService` was modified

* `type()` was added

#### `models.CosmosDbLinkedService` was modified

* `type()` was added

#### `models.AzureBlobFSLinkedService` was modified

* `type()` was added

#### `models.JsonSource` was modified

* `type()` was added

#### `models.SapCloudForCustomerResourceDataset` was modified

* `type()` was added

#### `models.SparkSource` was modified

* `type()` was added

#### `models.RestSink` was modified

* `type()` was added

#### `models.CopyTranslator` was modified

* `type()` was added

#### `models.MongoDbAtlasSink` was modified

* `type()` was added

#### `models.HBaseSource` was modified

* `type()` was added

#### `models.SelfHostedIntegrationRuntime` was modified

* `type()` was added

#### `models.BinarySink` was modified

* `type()` was added

#### `models.AmazonS3ReadSettings` was modified

* `type()` was added

#### `models.OracleCloudStorageLocation` was modified

* `type()` was added

#### `models.VerticaSource` was modified

* `type()` was added

#### `models.SqlServerSource` was modified

* `type()` was added

#### `models.SwitchActivity` was modified

* `type()` was added

#### `models.AzureDataLakeAnalyticsLinkedService` was modified

* `type()` was added

#### `models.MariaDBTableDataset` was modified

* `type()` was added

#### `models.BlobSource` was modified

* `type()` was added

#### `models.HiveLinkedService` was modified

* `type()` was added

#### `models.ExportSettings` was modified

* `type()` was added

#### `models.SnowflakeV2Dataset` was modified

* `type()` was added

#### `models.StoreReadSettings` was modified

* `type()` was added

#### `models.AzureDatabricksDeltaLakeSink` was modified

* `type()` was added

#### `models.AzureDatabricksDeltaLakeLinkedService` was modified

* `type()` was added

#### `models.HttpDataset` was modified

* `type()` was added

#### `models.HdfsLocation` was modified

* `type()` was added

#### `models.LakeHouseTableSource` was modified

* `type()` was added

#### `models.AmazonRedshiftSource` was modified

* `type()` was added

#### `models.GoogleSheetsLinkedService` was modified

* `type()` was added

#### `models.MarketoSource` was modified

* `type()` was added

#### `models.ZipDeflateReadSettings` was modified

* `type()` was added

#### `models.EloquaLinkedService` was modified

* `type()` was added

#### `models.MongoDbV2CollectionDataset` was modified

* `type()` was added

#### `models.CompressionReadSettings` was modified

* `type()` was added

#### `models.AzureMLUpdateResourceActivity` was modified

* `type()` was added

#### `models.NetezzaTableDataset` was modified

* `type()` was added

#### `models.SqlDWSource` was modified

* `type()` was added

#### `models.AzureFunctionActivity` was modified

* `type()` was added

#### `models.IntegrationRuntimeStatus` was modified

* `type()` was added

#### `models.AzPowerShellSetup` was modified

* `type()` was added

#### `models.ExecutePipelineActivity` was modified

* `type()` was added

#### `models.XeroSource` was modified

* `type()` was added

#### `models.AzureMySqlSource` was modified

* `type()` was added

#### `models.ODataSource` was modified

* `type()` was added

#### `models.AzureSearchIndexDataset` was modified

* `type()` was added

#### `models.AzureDatabricksDeltaLakeImportCommand` was modified

* `type()` was added

#### `models.HDInsightHiveActivity` was modified

* `type()` was added

#### `models.SapCloudForCustomerLinkedService` was modified

* `type()` was added

#### `models.GoogleCloudStorageLinkedService` was modified

* `type()` was added

#### `models.FailActivity` was modified

* `type()` was added

#### `models.XmlSource` was modified

* `type()` was added

#### `models.OdbcLinkedService` was modified

* `type()` was added

#### `models.MongoDbAtlasSource` was modified

* `type()` was added

#### `models.TabularSource` was modified

* `type()` was added

#### `models.FileSystemSink` was modified

* `type()` was added

#### `models.LinkedService` was modified

* `type()` was added

#### `models.WarehouseLinkedService` was modified

* `type()` was added

#### `models.RelationalSource` was modified

* `type()` was added

#### `models.HdfsLinkedService` was modified

* `type()` was added

#### `models.SalesforceMarketingCloudObjectDataset` was modified

* `type()` was added

#### `models.SnowflakeV2Source` was modified

* `type()` was added

#### `models.TextFormat` was modified

* `type()` was added

#### `models.PaypalObjectDataset` was modified

* `type()` was added

#### `models.AzurePostgreSqlLinkedService` was modified

* `type()` was added

#### `models.SharePointOnlineListSource` was modified

* `type()` was added

#### `models.BlobTrigger` was modified

* `type()` was added

#### `models.OracleSink` was modified

* `type()` was added

#### `models.SnowflakeImportCopyCommand` was modified

* `type()` was added

#### `models.WebClientCertificateAuthentication` was modified

* `authenticationType()` was added

#### `models.AzureDatabricksDeltaLakeDataset` was modified

* `type()` was added

#### `models.AzureMLLinkedService` was modified

* `type()` was added

#### `models.SalesforceServiceCloudV2Sink` was modified

* `type()` was added

#### `models.OracleLinkedService` was modified

* `type()` was added

#### `models.SqlSink` was modified

* `type()` was added

#### `models.AzureFunctionLinkedService` was modified

* `type()` was added

#### `models.AzureSqlSink` was modified

* `type()` was added

#### `models.AzureBlobFSDataset` was modified

* `type()` was added

#### `models.PostgreSqlSource` was modified

* `type()` was added

#### `models.HdfsReadSettings` was modified

* `type()` was added

#### `models.AmazonS3CompatibleLinkedService` was modified

* `type()` was added

#### `models.MongoDbV2Source` was modified

* `type()` was added

#### `models.DelimitedTextDataset` was modified

* `type()` was added

#### `models.PaypalLinkedService` was modified

* `type()` was added

#### `models.ParquetReadSettings` was modified

* `type()` was added

#### `models.SalesforceMarketingCloudLinkedService` was modified

* `type()` was added

#### `models.AzureDataExplorerSource` was modified

* `type()` was added

#### `models.TarGZipReadSettings` was modified

* `type()` was added

#### `models.AzureSqlTableDataset` was modified

* `type()` was added

#### `models.FormatWriteSettings` was modified

* `type()` was added

#### `models.AzureDatabricksLinkedService` was modified

* `type()` was added

#### `models.GoogleBigQueryV2LinkedService` was modified

* `type()` was added

#### `models.AmazonS3Dataset` was modified

* `type()` was added

#### `models.SelfHostedIntegrationRuntimeStatus` was modified

* `type()` was added

#### `models.PostgreSqlV2LinkedService` was modified

* `type()` was added

#### `models.PhoenixObjectDataset` was modified

* `type()` was added

#### `models.MarketoObjectDataset` was modified

* `type()` was added

#### `models.SqlSource` was modified

* `type()` was added

#### `models.QuickbaseLinkedService` was modified

* `type()` was added

#### `models.ResponsysObjectDataset` was modified

* `type()` was added

#### `models.AzureDatabricksDeltaLakeSource` was modified

* `type()` was added

#### `models.MicrosoftAccessLinkedService` was modified

* `type()` was added

#### `models.CommonDataServiceForAppsSink` was modified

* `type()` was added

#### `models.DatabricksSparkPythonActivity` was modified

* `type()` was added

#### `models.OracleServiceCloudSource` was modified

* `type()` was added

#### `models.CosmosDbSqlApiCollectionDataset` was modified

* `type()` was added

#### `models.SecretBase` was modified

* `type()` was added

#### `models.AzureMariaDBLinkedService` was modified

* `type()` was added

#### `models.OrcSource` was modified

* `type()` was added

#### `models.LinkedIntegrationRuntimeRbacAuthorization` was modified

* `authorizationType()` was added

#### `models.ExecuteDataFlowActivity` was modified

* `type()` was added

#### `models.SqlServerLinkedService` was modified

* `type()` was added

#### `models.DynamicsEntityDataset` was modified

* `type()` was added

#### `models.DocumentDbCollectionSource` was modified

* `type()` was added

#### `models.AzureBlobFSSink` was modified

* `type()` was added

#### `models.AmazonRedshiftLinkedService` was modified

* `type()` was added

#### `models.CosmosDbSqlApiSource` was modified

* `type()` was added

#### `models.ResponsysLinkedService` was modified

* `type()` was added

#### `models.ImpalaLinkedService` was modified

* `type()` was added

#### `models.FileSystemSource` was modified

* `type()` was added

#### `models.FileServerLocation` was modified

* `type()` was added

#### `models.SftpServerLinkedService` was modified

* `type()` was added

#### `models.AzureKeyVaultSecretReference` was modified

* `type()` was added

#### `models.MySqlLinkedService` was modified

* `type()` was added

#### `models.MagentoSource` was modified

* `type()` was added

#### `models.LakeHouseTableSink` was modified

* `type()` was added

#### `models.CouchbaseSource` was modified

* `type()` was added

#### `models.HDInsightStreamingActivity` was modified

* `type()` was added

#### `models.PhoenixLinkedService` was modified

* `type()` was added

#### `models.Db2LinkedService` was modified

* `type()` was added

#### `models.DynamicsCrmLinkedService` was modified

* `type()` was added

#### `models.LookupActivity` was modified

* `type()` was added

#### `models.FtpServerLinkedService` was modified

* `type()` was added

#### `models.XmlDataset` was modified

* `type()` was added

#### `models.QuickBooksLinkedService` was modified

* `type()` was added

#### `models.GreenplumTableDataset` was modified

* `type()` was added

#### `models.SalesforceServiceCloudLinkedService` was modified

* `type()` was added

#### `models.SecureString` was modified

* `type()` was added

#### `models.AvroSource` was modified

* `type()` was added

#### `models.GoogleCloudStorageReadSettings` was modified

* `type()` was added

#### `models.SalesforceServiceCloudObjectDataset` was modified

* `type()` was added

#### `models.SalesforceV2Sink` was modified

* `type()` was added

#### `models.AzureDataExplorerLinkedService` was modified

* `type()` was added

#### `models.Db2Source` was modified

* `type()` was added

#### `models.HubspotSource` was modified

* `type()` was added

#### `models.CouchbaseLinkedService` was modified

* `type()` was added

#### `models.MagentoObjectDataset` was modified

* `type()` was added

#### `models.SharePointOnlineListLinkedService` was modified

* `type()` was added

#### `models.AppendVariableActivity` was modified

* `type()` was added

#### `models.SybaseLinkedService` was modified

* `type()` was added

#### `models.SsisEnvironment` was modified

* `type()` was added

#### `models.FactoryVstsConfiguration` was modified

* `type()` was added

#### `models.Flowlet` was modified

* `type()` was added

#### `models.HDInsightMapReduceActivity` was modified

* `type()` was added

#### `models.AmazonMwsLinkedService` was modified

* `type()` was added

#### `models.MongoDbV2Sink` was modified

* `type()` was added

#### `models.QuickBooksObjectDataset` was modified

* `type()` was added

#### `models.CosmosDbMongoDbApiLinkedService` was modified

* `type()` was added

#### `models.RelationalTableDataset` was modified

* `type()` was added

#### `models.GoogleBigQueryLinkedService` was modified

* `type()` was added

#### `models.AmazonRdsForOracleTableDataset` was modified

* `type()` was added

#### `models.SqlServerStoredProcedureActivity` was modified

* `type()` was added

#### `models.AzureSqlSource` was modified

* `type()` was added

#### `models.DataworldLinkedService` was modified

* `type()` was added

#### `models.ExcelSource` was modified

* `type()` was added

#### `models.AzureKeyVaultLinkedService` was modified

* `type()` was added

#### `models.ManagedIntegrationRuntimeStatus` was modified

* `type()` was added

#### `models.OracleCloudStorageReadSettings` was modified

* `type()` was added

#### `models.AzureSearchIndexSink` was modified

* `type()` was added

#### `models.AzureDataExplorerCommandActivity` was modified

* `type()` was added

#### `models.AzureSqlMILinkedService` was modified

* `type()` was added

#### `models.LinkedIntegrationRuntimeType` was modified

* `authorizationType()` was added

#### `models.ImpalaObjectDataset` was modified

* `type()` was added

#### `models.AmazonRdsForSqlServerTableDataset` was modified

* `type()` was added

#### `models.AzureDataExplorerSink` was modified

* `type()` was added

#### `models.AzureMySqlSink` was modified

* `type()` was added

#### `models.Office365LinkedService` was modified

* `type()` was added

#### `models.XmlReadSettings` was modified

* `type()` was added

#### `models.EloquaSource` was modified

* `type()` was added

#### `models.AzureDataLakeStoreSink` was modified

* `type()` was added

#### `models.AvroWriteSettings` was modified

* `type()` was added

#### `models.ChainingTrigger` was modified

* `type()` was added

#### `models.AmazonRdsForSqlServerLinkedService` was modified

* `type()` was added

#### `models.WebLinkedService` was modified

* `type()` was added

#### `models.ZendeskLinkedService` was modified

* `type()` was added

#### `models.MarketoLinkedService` was modified

* `type()` was added

#### `models.ShopifySource` was modified

* `type()` was added

#### `models.GoogleBigQueryV2ObjectDataset` was modified

* `type()` was added

#### `models.DynamicsAXSource` was modified

* `type()` was added

## 1.0.0-beta.27 (2024-03-14)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.PostgreSqlV2TableDataset` was added

* `models.ExpressionV2` was added

* `models.GoogleBigQueryV2Source` was added

* `models.PostgreSqlV2Source` was added

* `models.ExpressionV2Type` was added

* `models.ServiceNowV2LinkedService` was added

* `models.ServiceNowV2ObjectDataset` was added

* `models.ServiceNowV2Source` was added

* `models.GoogleBigQueryV2AuthenticationType` was added

* `models.GoogleBigQueryV2LinkedService` was added

* `models.PostgreSqlV2LinkedService` was added

* `models.ServiceNowV2AuthenticationType` was added

* `models.GoogleBigQueryV2ObjectDataset` was added

## 1.0.0-beta.26 (2024-01-29)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SalesforceV2Source` was modified

* `readBehavior()` was removed
* `withReadBehavior(java.lang.Object)` was removed

#### `models.SalesforceServiceCloudV2Source` was modified

* `readBehavior()` was removed
* `withReadBehavior(java.lang.Object)` was removed

### Features Added

#### `models.SalesforceV2Source` was modified

* `withIncludeDeletedObjects(java.lang.Object)` was added
* `includeDeletedObjects()` was added

#### `models.SalesforceServiceCloudV2LinkedService` was modified

* `authenticationType()` was added
* `withAuthenticationType(java.lang.Object)` was added

#### `models.SalesforceServiceCloudV2Source` was modified

* `withIncludeDeletedObjects(java.lang.Object)` was added
* `includeDeletedObjects()` was added

#### `models.SalesforceV2LinkedService` was modified

* `authenticationType()` was added
* `withAuthenticationType(java.lang.Object)` was added

## 1.0.0-beta.25 (2024-01-22)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.WebActivity` was modified

* `withHeaders(java.lang.Object)` was removed
* `java.lang.Object headers()` -> `java.util.Map headers()`

#### `models.WebhookActivity` was modified

* `withHeaders(java.lang.Object)` was removed
* `java.lang.Object headers()` -> `java.util.Map headers()`

#### `models.MariaDBLinkedService` was modified

* `withPwd(models.AzureKeyVaultSecretReference)` was removed
* `pwd()` was removed

#### `models.AzureFunctionActivity` was modified

* `withHeaders(java.lang.Object)` was removed
* `java.lang.Object headers()` -> `java.util.Map headers()`

### Features Added

* `models.WarehouseTableDataset` was added

* `models.SalesforceV2Source` was added

* `models.SnowflakeAuthenticationType` was added

* `models.SalesforceServiceCloudV2LinkedService` was added

* `models.WarehouseSource` was added

* `models.SalesforceServiceCloudV2ObjectDataset` was added

* `models.WarehouseSink` was added

* `models.SalesforceServiceCloudV2Source` was added

* `models.SalesforceV2SinkWriteBehavior` was added

* `models.SalesforceV2LinkedService` was added

* `models.SnowflakeV2Sink` was added

* `models.SalesforceV2ObjectDataset` was added

* `models.SnowflakeV2LinkedService` was added

* `models.SnowflakeV2Dataset` was added

* `models.WarehouseLinkedService` was added

* `models.SnowflakeV2Source` was added

* `models.SalesforceServiceCloudV2Sink` was added

* `models.SalesforceV2Sink` was added

#### `models.FileServerWriteSettings` was modified

* `withMetadata(java.util.List)` was added

#### `models.AzureFileStorageWriteSettings` was modified

* `withMetadata(java.util.List)` was added

#### `models.WebActivity` was modified

* `withHeaders(java.util.Map)` was added
* `turnOffAsync()` was added
* `httpRequestTimeout()` was added
* `withHttpRequestTimeout(java.lang.Object)` was added
* `withTurnOffAsync(java.lang.Boolean)` was added

#### `models.WebhookActivity` was modified

* `withHeaders(java.util.Map)` was added

#### `models.MariaDBLinkedService` was modified

* `withDatabase(java.lang.Object)` was added
* `withServer(java.lang.Object)` was added
* `port()` was added
* `driverVersion()` was added
* `password()` was added
* `withPassword(models.AzureKeyVaultSecretReference)` was added
* `withUsername(java.lang.Object)` was added
* `database()` was added
* `withDriverVersion(java.lang.Object)` was added
* `withPort(java.lang.Object)` was added
* `server()` was added
* `username()` was added

#### `models.LakeHouseWriteSettings` was modified

* `withMetadata(java.util.List)` was added

#### `models.AzureBlobFSWriteSettings` was modified

* `withMetadata(java.util.List)` was added

#### `models.SftpWriteSettings` was modified

* `withMetadata(java.util.List)` was added

#### `models.AzureDataLakeStoreWriteSettings` was modified

* `withMetadata(java.util.List)` was added

#### `models.StoreWriteSettings` was modified

* `withMetadata(java.util.List)` was added
* `metadata()` was added

#### `models.AzureBlobStorageWriteSettings` was modified

* `withMetadata(java.util.List)` was added

#### `models.AzureFunctionActivity` was modified

* `withHeaders(java.util.Map)` was added

#### `models.MySqlLinkedService` was modified

* `withUsername(java.lang.Object)` was added
* `withDriverVersion(java.lang.Object)` was added
* `withDatabase(java.lang.Object)` was added
* `withSslMode(java.lang.Object)` was added
* `withUseSystemTrustStore(java.lang.Object)` was added
* `server()` was added
* `withPort(java.lang.Object)` was added
* `username()` was added
* `withServer(java.lang.Object)` was added
* `port()` was added
* `sslMode()` was added
* `database()` was added
* `driverVersion()` was added
* `useSystemTrustStore()` was added

## 1.0.0-beta.24 (2023-11-22)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.LakeHouseTableDataset` was added

* `models.LakeHouseLinkedService` was added

* `models.LakeHouseWriteSettings` was added

* `models.LakeHouseReadSettings` was added

* `models.LakeHouseLocation` was added

* `models.LakeHouseTableSource` was added

* `models.ParquetReadSettings` was added

* `models.LakeHouseTableSink` was added

#### `models.WebhookActivity` was modified

* `policy()` was added
* `withPolicy(models.SecureInputOutputPolicy)` was added

#### `models.ParquetSource` was modified

* `formatSettings()` was added
* `withFormatSettings(models.ParquetReadSettings)` was added

#### `models.GoogleAdWordsLinkedService` was modified

* `googleAdsApiVersion()` was added
* `withSupportLegacyDataTypes(java.lang.Object)` was added
* `supportLegacyDataTypes()` was added
* `privateKey()` was added
* `withPrivateKey(models.SecretBase)` was added
* `withGoogleAdsApiVersion(java.lang.Object)` was added
* `withLoginCustomerId(java.lang.Object)` was added
* `loginCustomerId()` was added

## 1.0.0-beta.23 (2023-09-27)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.CosmosDbServicePrincipalCredentialType` was removed

* `models.SalesforceSourceReadBehavior` was removed

#### `models.SapEccLinkedService` was modified

* `withUsername(java.lang.String)` was removed
* `java.lang.String url()` -> `java.lang.Object url()`
* `java.lang.String username()` -> `java.lang.Object username()`
* `withUrl(java.lang.String)` was removed

#### `models.SmartsheetLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.HBaseLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.DrillLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SnowflakeLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SapOdpLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SapOpenHubLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SalesforceSource` was modified

* `withReadBehavior(models.SalesforceSourceReadBehavior)` was removed
* `models.SalesforceSourceReadBehavior readBehavior()` -> `java.lang.Object readBehavior()`

#### `models.SalesforceServiceCloudSource` was modified

* `withReadBehavior(models.SalesforceSourceReadBehavior)` was removed
* `models.SalesforceSourceReadBehavior readBehavior()` -> `java.lang.Object readBehavior()`

#### `models.VerticaLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.HDInsightOnDemandLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.FileServerLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureDataLakeStoreReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.AmazonS3LinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.NetezzaLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.MagentoLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.InformixLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.HttpReadSettings` was modified

* `enablePartitionDiscovery()` was removed
* `withPartitionRootPath(java.lang.Object)` was removed
* `partitionRootPath()` was removed
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.HttpLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.GreenplumLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.PostgreSqlLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.CommonDataServiceForAppsLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SquareLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureBlobFSReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.AzureBatchLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.MongoDbLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SapHanaLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.OracleCloudStorageLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.TeamDeskLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AsanaLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.ConcurLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SynapseNotebookActivity` was modified

* `java.lang.Integer numExecutors()` -> `java.lang.Object numExecutors()`
* `withNumExecutors(java.lang.Integer)` was removed

#### `models.MariaDBLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.XeroLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AzureFileStorageReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.AzureMLServiceLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.FtpReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `java.lang.Boolean useBinaryTransfer()` -> `java.lang.Object useBinaryTransfer()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed
* `withUseBinaryTransfer(java.lang.Boolean)` was removed

#### `models.DynamicsAXLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.FileServerReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.PrestoLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AmazonRdsForOracleLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SalesforceLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.RestResourceDataset` was modified

* `java.lang.Object paginationRules()` -> `java.util.Map paginationRules()`
* `java.lang.Object additionalHeaders()` -> `java.util.Map additionalHeaders()`
* `withAdditionalHeaders(java.lang.Object)` was removed
* `withPaginationRules(java.lang.Object)` was removed

#### `models.SapBWLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.ZohoLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SapTableLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.JiraLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AzureFileStorageLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureSqlDatabaseLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureSqlDWLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.ShopifyLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.TeradataLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SparkLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.OracleServiceCloudLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureBlobStorageLinkedService` was modified

* `withAccountKind(java.lang.String)` was removed
* `java.lang.String accountKind()` -> `java.lang.Object accountKind()`
* `withServiceEndpoint(java.lang.String)` was removed
* `java.lang.String serviceEndpoint()` -> `java.lang.Object serviceEndpoint()`

#### `models.AzureDataLakeStoreLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.ServiceNowLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureSearchLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AzureMySqlLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.HubspotLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AmazonS3CompatibleReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.DynamicsLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.CassandraLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SftpReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed
* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`

#### `models.ODataLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.GoogleAdWordsLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.HDInsightLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AzureBlobStorageReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed
* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`

#### `models.RestServiceLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.CosmosDbLinkedService` was modified

* `withServicePrincipalCredentialType(models.CosmosDbServicePrincipalCredentialType)` was removed
* `withEncryptedCredential(java.lang.Object)` was removed
* `models.CosmosDbServicePrincipalCredentialType servicePrincipalCredentialType()` -> `java.lang.Object servicePrincipalCredentialType()`
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureBlobFSLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AmazonS3ReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed
* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`

#### `models.AzureDataLakeAnalyticsLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.HiveLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureDatabricksDeltaLakeLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.GoogleSheetsLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.EloquaLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.HDInsightHiveActivity` was modified

* `java.util.List variables()` -> `java.util.Map variables()`
* `withVariables(java.util.List)` was removed

#### `models.SapCloudForCustomerLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.GoogleCloudStorageLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.OdbcLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.HdfsLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AzurePostgreSqlLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureMLLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.OracleLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AzureFunctionLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.HdfsReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.AmazonS3CompatibleLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.PaypalLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SalesforceMarketingCloudLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureDatabricksLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.QuickbaseLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.MicrosoftAccessLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AzureMariaDBLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SqlServerLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AmazonRedshiftLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.ResponsysLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.ImpalaLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SftpServerLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.MySqlLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.PhoenixLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.Db2LinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.DynamicsCrmLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.FtpServerLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.QuickBooksLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.SalesforceServiceCloudLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.GoogleCloudStorageReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.CouchbaseLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SharePointOnlineListLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.SybaseLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.AmazonMwsLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.GoogleBigQueryLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.DataworldLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.OracleCloudStorageReadSettings` was modified

* `java.lang.Boolean enablePartitionDiscovery()` -> `java.lang.Object enablePartitionDiscovery()`
* `withEnablePartitionDiscovery(java.lang.Boolean)` was removed

#### `models.AzureSqlMILinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.Office365LinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

#### `models.AmazonRdsForSqlServerLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.ZendeskLinkedService` was modified

* `withEncryptedCredential(java.lang.Object)` was removed
* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`

#### `models.MarketoLinkedService` was modified

* `java.lang.Object encryptedCredential()` -> `java.lang.String encryptedCredential()`
* `withEncryptedCredential(java.lang.Object)` was removed

### Features Added

* `models.MapperPolicy` was added

* `models.ChangeDataCaptureResource$Update` was added

* `models.SecureInputOutputPolicy` was added

* `models.ChangeDataCaptureFolder` was added

* `models.ActivityOnInactiveMarkAs` was added

* `models.ChangeDataCaptureListResponse` was added

* `models.ConnectionType` was added

* `models.MappingType` was added

* `models.MapperSourceConnectionsInfo` was added

* `models.ChangeDataCaptureResource$DefinitionStages` was added

* `models.ChangeDataCaptures` was added

* `models.FrequencyType` was added

* `models.ActivityState` was added

* `models.MapperDslConnectorProperties` was added

* `models.MapperConnectionReference` was added

* `models.IntegrationRuntimeDataFlowPropertiesCustomPropertiesItem` was added

* `models.ChangeDataCaptureResource$Definition` was added

* `models.ChangeDataCaptureResource$UpdateStages` was added

* `models.MapperAttributeMapping` was added

* `models.ChangeDataCaptureResource` was added

* `models.MapperTargetConnectionsInfo` was added

* `models.MapperAttributeMappings` was added

* `models.MapperAttributeReference` was added

* `models.MapperConnection` was added

* `models.MapperTable` was added

* `models.DataMapperMapping` was added

* `models.MapperTableSchema` was added

* `models.MapperPolicyRecurrence` was added

#### `models.SapEccLinkedService` was modified

* `withUsername(java.lang.Object)` was added
* `withUrl(java.lang.Object)` was added

#### `models.SmartsheetLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.Activity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `onInactiveMarkAs()` was added
* `state()` was added
* `withState(models.ActivityState)` was added

#### `models.HBaseLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.DrillLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SnowflakeLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SapOdpLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.MongoDbAtlasLinkedService` was modified

* `withDriverVersion(java.lang.Object)` was added
* `driverVersion()` was added

#### `models.SapOpenHubLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SalesforceSource` was modified

* `withReadBehavior(java.lang.Object)` was added

#### `models.SalesforceServiceCloudSource` was modified

* `withReadBehavior(java.lang.Object)` was added

#### `models.VerticaLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.HDInsightOnDemandLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.FileServerLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SqlMISource` was modified

* `withIsolationLevel(java.lang.Object)` was added
* `isolationLevel()` was added

#### `models.AzureDataLakeStoreReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.ScriptActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.HDInsightSparkActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.AmazonS3LinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.NetezzaLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.MagentoLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.InformixLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.WebActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.HttpReadSettings` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `additionalColumns()` was added

#### `models.GetMetadataActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `DataFactoryManager` was modified

* `changeDataCaptures()` was added

#### `models.FilterActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.HttpLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.GreenplumLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.PostgreSqlLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ForEachActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.CommonDataServiceForAppsLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ExecuteWranglingDataflowActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.SquareLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureBlobFSReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.AzureBatchLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.MongoDbLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.DatabricksNotebookActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.SapHanaLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.WaitActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.OracleCloudStorageLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.CopyActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.IfConditionActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.TeamDeskLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AsanaLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ExecutionActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.WebhookActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.ValidationActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.ConcurLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.CustomActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.SynapseNotebookActivity` was modified

* `targetSparkConfiguration()` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withNumExecutors(java.lang.Object)` was added
* `withState(models.ActivityState)` was added
* `withConfigurationType(models.ConfigurationType)` was added
* `configurationType()` was added
* `withSparkConfig(java.util.Map)` was added
* `sparkConfig()` was added
* `withTargetSparkConfiguration(models.SparkConfigurationParametrizationReference)` was added

#### `models.MariaDBLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.XeroLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureFileStorageReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.AzureMLServiceLinkedService` was modified

* `withAuthentication(java.lang.Object)` was added
* `authentication()` was added
* `withEncryptedCredential(java.lang.String)` was added

#### `models.FtpReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added
* `withUseBinaryTransfer(java.lang.Object)` was added

#### `models.IntegrationRuntimeDataFlowProperties` was modified

* `withCustomProperties(java.util.List)` was added
* `customProperties()` was added

#### `models.DynamicsAXLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.FileServerReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.PrestoLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureMLBatchExecutionActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.AmazonRdsForOracleLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SetVariableActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withSetSystemVariable(java.lang.Boolean)` was added
* `withState(models.ActivityState)` was added
* `policy()` was added
* `setSystemVariable()` was added
* `withPolicy(models.SecureInputOutputPolicy)` was added

#### `models.SalesforceLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.RestResourceDataset` was modified

* `withPaginationRules(java.util.Map)` was added
* `withAdditionalHeaders(java.util.Map)` was added

#### `models.SapBWLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ZohoLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SapTableLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.JiraLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureFileStorageLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.DataLakeAnalyticsUsqlActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.AmazonRdsForSqlServerSource` was modified

* `withIsolationLevel(java.lang.Object)` was added
* `isolationLevel()` was added

#### `models.AzureSqlDatabaseLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureSqlDWLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureMLExecutePipelineActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.DatabricksSparkJarActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.ShopifyLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.TeradataLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SparkLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.OracleServiceCloudLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureBlobStorageLinkedService` was modified

* `withAccountKind(java.lang.Object)` was added
* `withServiceEndpoint(java.lang.Object)` was added

#### `models.AzureDataLakeStoreLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ServiceNowLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureSearchLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ExecuteSsisPackageActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.UntilActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.DeleteActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.AzureMySqlLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SynapseSparkJobDefinitionActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.HubspotLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ControlActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.AmazonS3CompatibleReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.HDInsightPigActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.DynamicsLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.CassandraLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SftpReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.ODataLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.GoogleAdWordsLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.HDInsightLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureBlobStorageReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.RestServiceLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.CosmosDbLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added

#### `models.AzureBlobFSLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SelfHostedIntegrationRuntime` was modified

* `selfContainedInteractiveAuthoringEnabled()` was added
* `withSelfContainedInteractiveAuthoringEnabled(java.lang.Boolean)` was added

#### `models.AmazonS3ReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.SqlServerSource` was modified

* `withIsolationLevel(java.lang.Object)` was added
* `isolationLevel()` was added

#### `models.SwitchActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.AzureDataLakeAnalyticsLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.HiveLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureDatabricksDeltaLakeLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.GoogleSheetsLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.EloquaLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureMLUpdateResourceActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.SqlDWSource` was modified

* `withIsolationLevel(java.lang.Object)` was added
* `isolationLevel()` was added

#### `models.AzureFunctionActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.ExecutePipelineActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.HDInsightHiveActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withVariables(java.util.Map)` was added

#### `models.SapCloudForCustomerLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.GoogleCloudStorageLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.FailActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.OdbcLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.HdfsLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzurePostgreSqlLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureMLLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.OracleLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureFunctionLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.HdfsReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.AmazonS3CompatibleLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.PaypalLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SalesforceMarketingCloudLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AzureDatabricksLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SelfHostedIntegrationRuntimeStatus` was modified

* `selfContainedInteractiveAuthoringEnabled()` was added

#### `models.QuickbaseLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.MicrosoftAccessLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.DatabricksSparkPythonActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.AzureMariaDBLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ExecuteDataFlowActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.SqlServerLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AmazonRedshiftLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ResponsysLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ImpalaLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SftpServerLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.MySqlLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.HDInsightStreamingActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.PhoenixLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.Db2LinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.DynamicsCrmLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.LookupActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.FtpServerLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.QuickBooksLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SalesforceServiceCloudLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.GoogleCloudStorageReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.CouchbaseLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SharePointOnlineListLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AppendVariableActivity` was modified

* `withState(models.ActivityState)` was added
* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added

#### `models.SybaseLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.HDInsightMapReduceActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.AmazonMwsLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.GoogleBigQueryLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.SqlServerStoredProcedureActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.AzureSqlSource` was modified

* `isolationLevel()` was added
* `withIsolationLevel(java.lang.Object)` was added

#### `models.DataworldLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.OracleCloudStorageReadSettings` was modified

* `withEnablePartitionDiscovery(java.lang.Object)` was added

#### `models.AzureDataExplorerCommandActivity` was modified

* `withOnInactiveMarkAs(models.ActivityOnInactiveMarkAs)` was added
* `withState(models.ActivityState)` was added

#### `models.AzureSqlMILinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.PipelineExternalComputeScaleProperties` was modified

* `withNumberOfExternalNodes(java.lang.Integer)` was added
* `withNumberOfPipelineNodes(java.lang.Integer)` was added
* `numberOfPipelineNodes()` was added
* `numberOfExternalNodes()` was added

#### `models.Office365LinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.AmazonRdsForSqlServerLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.ZendeskLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

#### `models.MarketoLinkedService` was modified

* `withEncryptedCredential(java.lang.String)` was added

## 1.0.0-beta.22 (2023-03-13)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.AzureBlobFSLinkedService` was modified

* `withSasToken(models.SecretBase)` was added
* `withSasUri(java.lang.Object)` was added
* `sasUri()` was added
* `sasToken()` was added

## 1.0.0-beta.21 (2023-02-20)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CopyComputeScaleProperties` was added

* `models.AzureStorageAuthenticationType` was added

* `models.PipelineExternalComputeScaleProperties` was added

#### `models.IntegrationRuntimeComputeProperties` was modified

* `pipelineExternalComputeScaleProperties()` was added
* `withPipelineExternalComputeScaleProperties(models.PipelineExternalComputeScaleProperties)` was added
* `copyComputeScaleProperties()` was added
* `withCopyComputeScaleProperties(models.CopyComputeScaleProperties)` was added

#### `models.AzureBlobStorageLinkedService` was modified

* `authenticationType()` was added
* `containerUri()` was added
* `withAuthenticationType(models.AzureStorageAuthenticationType)` was added
* `withContainerUri(java.lang.Object)` was added

## 1.0.0-beta.20 (2023-01-18)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SynapseSparkJobDefinitionActivity` was modified

* `withNumExecutors(java.lang.Integer)` was removed
* `java.lang.Integer numExecutors()` -> `java.lang.Object numExecutors()`

### Features Added

* `models.ConfigurationType` was added

* `models.SparkConfigurationReferenceType` was added

* `models.SparkConfigurationParametrizationReference` was added

#### `models.SynapseSparkJobDefinitionActivity` was modified

* `withConfigurationType(models.ConfigurationType)` was added
* `targetSparkConfiguration()` was added
* `configurationType()` was added
* `withTargetSparkConfiguration(models.SparkConfigurationParametrizationReference)` was added
* `withNumExecutors(java.lang.Object)` was added
* `scanFolder()` was added
* `withSparkConfig(java.util.Map)` was added
* `sparkConfig()` was added
* `withScanFolder(java.lang.Object)` was added

## 1.0.0-beta.19 (2022-11-24)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CredentialOperations` was added

* `models.ManagedIdentityCredentialResource$Update` was added

* `models.ManagedIdentityCredentialResource$DefinitionStages` was added

* `models.ManagedIdentityCredentialResource$Definition` was added

* `models.ManagedIdentityCredentialResource` was added

* `models.ManagedIdentityCredentialResource$UpdateStages` was added

* `models.CredentialListResponse` was added

#### `models.ScriptActivity` was modified

* `scriptBlockExecutionTimeout()` was added
* `withScriptBlockExecutionTimeout(java.lang.Object)` was added

#### `DataFactoryManager` was modified

* `credentialOperations()` was added

## 1.0.0-beta.18 (2022-10-14)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.StoredProcedureParameterType` was removed

* `models.StoredProcedureParameter` was removed

#### `models.SqlMISink` was modified

* `withStoredProcedureParameters(java.util.Map)` was removed
* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`

#### `models.SqlServerSink` was modified

* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`
* `withStoredProcedureParameters(java.util.Map)` was removed

#### `models.SqlMISource` was modified

* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`
* `withStoredProcedureParameters(java.util.Map)` was removed

#### `models.SynapseSparkJobReference` was modified

* `withReferenceName(java.lang.String)` was removed
* `java.lang.String referenceName()` -> `java.lang.Object referenceName()`

#### `models.AmazonRdsForSqlServerSource` was modified

* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`
* `withStoredProcedureParameters(java.util.Map)` was removed

#### `models.SqlServerSource` was modified

* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`
* `withStoredProcedureParameters(java.util.Map)` was removed

#### `models.SqlSink` was modified

* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`
* `withStoredProcedureParameters(java.util.Map)` was removed

#### `models.AzureSqlSink` was modified

* `withStoredProcedureParameters(java.util.Map)` was removed
* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`

#### `models.SqlSource` was modified

* `withStoredProcedureParameters(java.util.Map)` was removed
* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`

#### `models.Factories` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AzureSqlSource` was modified

* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`
* `withStoredProcedureParameters(java.util.Map)` was removed

### Features Added

#### `models.SqlMISink` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.SqlServerSink` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.SqlMISource` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.FactoryRepoConfiguration` was modified

* `withDisablePublish(java.lang.Boolean)` was added
* `disablePublish()` was added

#### `models.SynapseSparkJobReference` was modified

* `withReferenceName(java.lang.Object)` was added

#### `models.AzureSynapseArtifactsLinkedService` was modified

* `withWorkspaceResourceId(java.lang.Object)` was added
* `workspaceResourceId()` was added

#### `models.AmazonRdsForSqlServerSource` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.FactoryGitHubConfiguration` was modified

* `withDisablePublish(java.lang.Boolean)` was added
* `withDisablePublish(java.lang.Boolean)` was added

#### `models.SynapseSparkJobDefinitionActivity` was modified

* `withFilesV2(java.util.List)` was added
* `filesV2()` was added
* `withPythonCodeReference(java.util.List)` was added
* `pythonCodeReference()` was added

#### `models.SqlServerSource` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.SqlSink` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.AzureSqlSink` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.SqlSource` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.Factories` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.FactoryVstsConfiguration` was modified

* `withDisablePublish(java.lang.Boolean)` was added
* `withDisablePublish(java.lang.Boolean)` was added

#### `models.AzureSqlSource` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

## 1.0.0-beta.17 (2022-09-13)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SynapseNotebookReference` was added

* `models.SynapseSparkJobReference` was added

* `models.AzureSynapseArtifactsLinkedService` was added

* `models.SynapseNotebookActivity` was added

* `models.BigDataPoolReferenceType` was added

* `models.BigDataPoolParametrizationReference` was added

* `models.SparkJobReferenceType` was added

* `models.SynapseSparkJobDefinitionActivity` was added

* `models.GoogleSheetsLinkedService` was added

* `models.NotebookParameterType` was added

* `models.NotebookParameter` was added

* `models.NotebookReferenceType` was added

## 1.0.0-beta.16 (2022-06-20)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SapOdpLinkedService` was added

* `models.PurviewConfiguration` was added

* `models.GlobalParameterResource` was added

* `models.GlobalParameters` was added

* `models.SapOdpResourceDataset` was added

* `models.SapOdpSource` was added

* `models.GlobalParameterResource$Update` was added

* `models.GlobalParameterResource$UpdateStages` was added

* `models.GlobalParameterListResponse` was added

* `models.GlobalParameterResource$Definition` was added

* `models.GlobalParameterResource$DefinitionStages` was added

#### `models.DataFlowResource` was modified

* `resourceGroupName()` was added

#### `DataFactoryManager` was modified

* `globalParameters()` was added

#### `models.ExecuteWranglingDataflowActivity` was modified

* `sourceStagingConcurrency()` was added
* `withSourceStagingConcurrency(java.lang.Object)` was added

#### `models.PrivateEndpointConnectionResource` was modified

* `resourceGroupName()` was added

#### `models.ManagedPrivateEndpointResource` was modified

* `resourceGroupName()` was added

#### `models.TriggerResource` was modified

* `resourceGroupName()` was added

#### `models.DatasetResource` was modified

* `resourceGroupName()` was added

#### `models.Factory$Definition` was modified

* `withPurviewConfiguration(models.PurviewConfiguration)` was added

#### `models.ManagedVirtualNetworkResource` was modified

* `resourceGroupName()` was added

#### `models.Factory` was modified

* `resourceGroupName()` was added
* `purviewConfiguration()` was added

#### `models.RestServiceLinkedService` was modified

* `withTokenEndpoint(java.lang.Object)` was added
* `resource()` was added
* `scope()` was added
* `tokenEndpoint()` was added
* `withClientId(java.lang.Object)` was added
* `withScope(java.lang.Object)` was added
* `clientSecret()` was added
* `clientId()` was added
* `withClientSecret(models.SecretBase)` was added
* `withResource(java.lang.Object)` was added

#### `models.IntegrationRuntimeResource` was modified

* `resourceGroupName()` was added

#### `models.ExecuteDataFlowActivity` was modified

* `withSourceStagingConcurrency(java.lang.Object)` was added
* `sourceStagingConcurrency()` was added

#### `models.LinkedServiceResource` was modified

* `resourceGroupName()` was added

#### `models.PipelineResource` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.15 (2022-05-10)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.PrivateEndpoint` was added

#### `models.PrivateLinkConnectionApprovalRequest` was modified

* `withPrivateEndpoint(models.PrivateEndpoint)` was added
* `privateEndpoint()` was added

#### `models.PowerQuerySink` was modified

* `withRejectedDataLinkedService(models.LinkedServiceReference)` was added
* `withRejectedDataLinkedService(models.LinkedServiceReference)` was added

#### `models.DataFlowSink` was modified

* `rejectedDataLinkedService()` was added
* `withRejectedDataLinkedService(models.LinkedServiceReference)` was added

## 1.0.0-beta.14 (2022-04-19)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DataFlowDebugSessionsExecuteCommandResponse` was removed

* `models.DataFlowDebugSessionsExecuteCommandHeaders` was removed

* `models.DataFlowDebugSessionsCreateHeaders` was removed

* `models.DataFlowDebugSessionsCreateResponse` was removed

#### `models.DataFlowReference` was modified

* `java.lang.String type()` -> `models.DataFlowReferenceType type()`
* `withType(java.lang.String)` was removed

#### `models.ManagedVirtualNetworkReference` was modified

* `withType(java.lang.String)` was removed
* `java.lang.String type()` -> `models.ManagedVirtualNetworkReferenceType type()`

#### `models.SqlServerStoredProcedureActivity` was modified

* `java.util.Map storedProcedureParameters()` -> `java.lang.Object storedProcedureParameters()`
* `withStoredProcedureParameters(java.util.Map)` was removed

#### `models.TriggerReference` was modified

* `withType(java.lang.String)` was removed
* `java.lang.String type()` -> `models.TriggerReferenceType type()`

#### `models.CredentialReference` was modified

* `withType(java.lang.String)` was removed
* `java.lang.String type()` -> `models.CredentialReferenceType type()`

### Features Added

* `models.TwilioLinkedService` was added

* `models.ExecutePipelineActivityPolicy` was added

* `models.DataFlowReferenceType` was added

* `models.TriggerReferenceType` was added

* `models.AppFiguresLinkedService` was added

* `models.AsanaLinkedService` was added

* `models.ManagedVirtualNetworkReferenceType` was added

* `models.CredentialReferenceType` was added

* `models.DataworldLinkedService` was added

#### `DataFactoryManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.DataFlowReference` was modified

* `withType(models.DataFlowReferenceType)` was added

#### `models.ManagedVirtualNetworkReference` was modified

* `withType(models.ManagedVirtualNetworkReferenceType)` was added

#### `DataFactoryManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.ExecutePipelineActivity` was modified

* `withPolicy(models.ExecutePipelineActivityPolicy)` was added
* `policy()` was added

#### `models.SqlServerStoredProcedureActivity` was modified

* `withStoredProcedureParameters(java.lang.Object)` was added

#### `models.TriggerReference` was modified

* `withType(models.TriggerReferenceType)` was added

#### `models.CredentialReference` was modified

* `withType(models.CredentialReferenceType)` was added

## 1.0.0-beta.13 (2022-03-24)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.WebActivity` was modified

* `withDisableCertValidation(java.lang.Boolean)` was added
* `disableCertValidation()` was added

## 1.0.0-beta.12 (2022-02-24)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SmartsheetLinkedService` was added

* `models.TeamDeskAuthenticationType` was added

* `models.TeamDeskLinkedService` was added

* `models.ZendeskAuthenticationType` was added

* `models.QuickbaseLinkedService` was added

* `models.ZendeskLinkedService` was added

## 1.0.0-beta.11 (2022-02-14)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ScriptActivityParameter` was added

* `models.ScriptActivityLogDestination` was added

* `models.ScriptActivity` was added

* `models.ScriptActivityParameterType` was added

* `models.ScriptActivityScriptBlock` was added

* `models.ScriptActivityParameterDirection` was added

* `models.ScriptActivityTypePropertiesLogSettings` was added

* `models.ScriptType` was added

## 1.0.0-beta.10 (2022-01-17)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.FailActivity` was added

#### `models.DynamicsLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

#### `models.CosmosDbLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

#### `models.AzureBlobFSLinkedService` was modified

* `servicePrincipalCredential()` was added
* `withServicePrincipalCredential(models.SecretBase)` was added
* `servicePrincipalCredentialType()` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added

#### `models.AzureDatabricksDeltaLakeLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `workspaceResourceId()` was added
* `credential()` was added
* `withWorkspaceResourceId(java.lang.Object)` was added

#### `models.LinkedIntegrationRuntimeRbacAuthorization` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

## 1.0.0-beta.9 (2021-12-20)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.GoogleAdWordsLinkedService` was modified

* `withConnectionProperties(java.lang.Object)` was added
* `connectionProperties()` was added

## 1.0.0-beta.8 (2021-11-29)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Flowlet` was modified

* `withAdditionalProperties(java.util.Map)` was removed
* `additionalPropertiesTypePropertiesAdditionalProperties()` was removed
* `additionalProperties()` was removed
* `withAdditionalPropertiesTypePropertiesAdditionalProperties(java.lang.Object)` was removed

### Features Added

* `models.ManagedIdentityCredential` was added

* `models.ServicePrincipalCredential` was added

#### `models.FtpReadSettings` was modified

* `withDisableChunking(java.lang.Object)` was added
* `disableChunking()` was added

#### `models.DataFlowReference` was modified

* `withParameters(java.util.Map)` was added
* `parameters()` was added

#### `models.SftpReadSettings` was modified

* `withDisableChunking(java.lang.Object)` was added
* `disableChunking()` was added

## 1.0.0-beta.7 (2021-11-10)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DatasetTarGZipCompression` was removed

* `models.DatasetTarCompression` was removed

* `models.DatasetGZipCompression` was removed

* `models.DatasetDeflateCompression` was removed

* `models.DatasetBZip2Compression` was removed

* `models.DatasetZipDeflateCompression` was removed

#### `models.DataFlowSink` was modified

* `dataset()` was removed
* `linkedService()` was removed

#### `models.DataFlowSource` was modified

* `linkedService()` was removed
* `dataset()` was removed

### Features Added

* `models.PowerQuerySinkMapping` was added

* `models.Flowlet` was added

#### `models.PowerQuerySource` was modified

* `withFlowlet(models.DataFlowReference)` was added
* `withFlowlet(models.DataFlowReference)` was added
* `withDataset(models.DatasetReference)` was added
* `withFlowlet(models.DataFlowReference)` was added
* `withLinkedService(models.LinkedServiceReference)` was added

#### `models.ExecuteWranglingDataflowActivity` was modified

* `queries()` was added
* `withQueries(java.util.List)` was added

#### `models.DataFlowDebugPackage` was modified

* `dataFlows()` was added
* `withDataFlows(java.util.List)` was added

#### `models.DatasetCompression` was modified

* `withType(java.lang.Object)` was added
* `level()` was added
* `withLevel(java.lang.Object)` was added
* `type()` was added

#### `models.PowerQuerySink` was modified

* `withFlowlet(models.DataFlowReference)` was added
* `withFlowlet(models.DataFlowReference)` was added
* `withFlowlet(models.DataFlowReference)` was added
* `withLinkedService(models.LinkedServiceReference)` was added
* `withDataset(models.DatasetReference)` was added

#### `models.WranglingDataFlow` was modified

* `documentLocale()` was added
* `withDocumentLocale(java.lang.String)` was added

#### `models.Transformation` was modified

* `flowlet()` was added
* `withLinkedService(models.LinkedServiceReference)` was added
* `dataset()` was added
* `withDataset(models.DatasetReference)` was added
* `withFlowlet(models.DataFlowReference)` was added
* `linkedService()` was added

#### `models.Factory$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.MappingDataFlow` was modified

* `withScriptLines(java.util.List)` was added
* `scriptLines()` was added

#### `models.FactoryUpdateParameters` was modified

* `publicNetworkAccess()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.DataFlowSink` was modified

* `withDataset(models.DatasetReference)` was added
* `withLinkedService(models.LinkedServiceReference)` was added
* `withFlowlet(models.DataFlowReference)` was added
* `withFlowlet(models.DataFlowReference)` was added

#### `models.DataFlowSource` was modified

* `withDataset(models.DatasetReference)` was added
* `withLinkedService(models.LinkedServiceReference)` was added
* `withFlowlet(models.DataFlowReference)` was added
* `withFlowlet(models.DataFlowReference)` was added

## 1.0.0-beta.6 (2021-09-10)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedIdentityCredential` was removed

* `models.ServicePrincipalCredential` was removed

### Features Added

* `models.IntegrationRuntimeCustomerVirtualNetwork` was added

* `models.AmazonRdsForOracleSource` was added

* `models.AmazonRdsForOracleLinkedService` was added

* `models.AmazonRdsForSqlServerSource` was added

* `models.AmazonRdsForOraclePartitionSettings` was added

* `models.AmazonRdsForOracleTableDataset` was added

* `models.AmazonRdsForSqlServerTableDataset` was added

* `models.AmazonRdsForSqlServerLinkedService` was added

#### `models.ManagedIntegrationRuntime` was modified

* `withCustomerVirtualNetwork(models.IntegrationRuntimeCustomerVirtualNetwork)` was added
* `customerVirtualNetwork()` was added

#### `models.SqlAlwaysEncryptedProperties` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

## 1.0.0-beta.5 (2021-08-25)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.4 (2021-08-16)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AdditionalColumns` was removed

#### `models.CosmosDbMongoDbApiSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.QuickBooksSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.GoogleBigQuerySource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.DelimitedTextSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.SalesforceSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SalesforceServiceCloudSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.NetezzaSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.MySqlSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SqlMISource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.PaypalSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ZohoSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SapOpenHubSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.HiveSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AmazonMwsSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AzureTableSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AzurePostgreSqlSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.PrestoSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SapCloudForCustomerSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.MicrosoftAccessSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AzureMariaDBSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.GoogleAdWordsSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.MariaDBSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.RestSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.SapEccSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.CassandraSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SapTableSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.CommonDataServiceForAppsSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.OracleSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.WebSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.DrillSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ServiceNowSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.MongoDbSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.InformixSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ParquetSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.ConcurSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.PhoenixSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SalesforceMarketingCloudSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.TeradataSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.GreenplumSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SapBwSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.OdbcSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.DynamicsCrmSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SapHanaSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SquareSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.DynamicsSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ImpalaSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SybaseSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.JiraSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ResponsysSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.JsonSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.SparkSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.HBaseSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.VerticaSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SqlServerSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AmazonRedshiftSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.MarketoSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.SqlDWSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.XeroSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AzureMySqlSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ODataSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.XmlSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.MongoDbAtlasSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.TabularSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.RelationalSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.PostgreSqlSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.MongoDbV2Source` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AzureDataExplorerSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.SqlSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.OracleServiceCloudSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.OrcSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.DocumentDbCollectionSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.CosmosDbSqlApiSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.FileSystemSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.MagentoSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.CouchbaseSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AvroSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`

#### `models.Db2Source` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.HubspotSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.AzureSqlSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ExcelSource` was modified

* `java.util.List additionalColumns()` -> `java.lang.Object additionalColumns()`
* `withAdditionalColumns(java.util.List)` was removed

#### `models.EloquaSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.ShopifySource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

#### `models.DynamicsAXSource` was modified

* `withAdditionalColumns(java.util.List)` was removed
* `withAdditionalColumns(java.util.List)` was removed

### Features Added

* `models.PowerQuerySource` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesEndpoint` was added

* `models.ExecuteWranglingDataflowActivity` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesEndpointDetails` was added

* `models.PowerQuerySink` was added

* `models.WranglingDataFlow` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesEndpointsResponse` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesCategoryEndpoint` was added

#### `models.CosmosDbMongoDbApiSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.QuickBooksSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.GoogleBigQuerySource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.DelimitedTextSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SalesforceSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SalesforceServiceCloudSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.NetezzaSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MySqlSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SqlMISource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.PaypalSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ZohoSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SapOpenHubSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.HiveSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AmazonMwsSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AzureTableSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AzurePostgreSqlSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.PrestoSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SapCloudForCustomerSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MicrosoftAccessSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AzureMariaDBSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.GoogleAdWordsSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MariaDBSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.RestSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SapEccSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.CassandraSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.IntegrationRuntimeDataFlowProperties` was modified

* `cleanup()` was added
* `withCleanup(java.lang.Boolean)` was added

#### `models.SapTableSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.CommonDataServiceForAppsSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.OracleSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.WebSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.DrillSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ServiceNowSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MongoDbSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.InformixSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ParquetSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ConcurSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.PhoenixSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SalesforceMarketingCloudSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.TeradataSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.GreenplumSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SapBwSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.OdbcSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.DynamicsCrmSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SapHanaSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SquareSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.DynamicsSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ImpalaSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SybaseSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.JiraSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ResponsysSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.IntegrationRuntimes` was modified

* `listOutboundNetworkDependenciesEndpointsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.JsonSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SparkSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.HBaseSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.VerticaSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SqlServerSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AmazonRedshiftSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MarketoSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SqlDWSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.XeroSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AzureMySqlSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.IntegrationRuntimeVNetProperties` was modified

* `withSubnetId(java.lang.String)` was added
* `subnetId()` was added

#### `models.ODataSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.XmlSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MongoDbAtlasSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.TabularSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.RelationalSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.PostgreSqlSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MongoDbV2Source` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AzureDataExplorerSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.SqlSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.OracleServiceCloudSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.OrcSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.DocumentDbCollectionSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.CosmosDbSqlApiSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.FileSystemSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.MagentoSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.CouchbaseSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.AvroSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.Db2Source` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.HubspotSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.CosmosDbMongoDbApiLinkedService` was modified

* `isServerVersionAbove32()` was added
* `withIsServerVersionAbove32(java.lang.Object)` was added

#### `models.AzureSqlSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ExcelSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added

#### `models.EloquaSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.ShopifySource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

#### `models.DynamicsAXSource` was modified

* `withAdditionalColumns(java.lang.Object)` was added
* `withAdditionalColumns(java.lang.Object)` was added

## 1.0.0-beta.3 (2021-07-29)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OrcCompressionCodec` was removed

* `models.AvroCompressionCodec` was removed

#### `models.IntegrationRuntimeSsisProperties` was modified

* `managedCredential()` was removed
* `withManagedCredential(models.EntityReference)` was removed

#### `models.AvroDataset` was modified

* `withAvroCompressionCodec(models.AvroCompressionCodec)` was removed
* `models.AvroCompressionCodec avroCompressionCodec()` -> `java.lang.Object avroCompressionCodec()`

#### `models.OrcDataset` was modified

* `models.OrcCompressionCodec orcCompressionCodec()` -> `java.lang.Object orcCompressionCodec()`
* `withOrcCompressionCodec(models.OrcCompressionCodec)` was removed

### Features Added

* `models.SqlUpsertSettings` was added

* `models.ManagedIdentityCredential` was added

* `models.ServicePrincipalCredential` was added

* `models.GitHubClientSecret` was added

* `models.Credential` was added

* `models.SqlDWUpsertSettings` was added

* `models.CredentialReference` was added

#### `models.SqlMISink` was modified

* `withSqlWriterUseTableLock(java.lang.Object)` was added
* `upsertSettings()` was added
* `withUpsertSettings(models.SqlUpsertSettings)` was added
* `withWriteBehavior(java.lang.Object)` was added
* `writeBehavior()` was added
* `sqlWriterUseTableLock()` was added

#### `models.SqlServerSink` was modified

* `sqlWriterUseTableLock()` was added
* `withSqlWriterUseTableLock(java.lang.Object)` was added
* `withUpsertSettings(models.SqlUpsertSettings)` was added
* `withWriteBehavior(java.lang.Object)` was added
* `writeBehavior()` was added
* `upsertSettings()` was added

#### `models.HDInsightOnDemandLinkedService` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.AzureBatchLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

#### `models.SqlDWSink` was modified

* `withSqlWriterUseTableLock(java.lang.Object)` was added
* `upsertSettings()` was added
* `sqlWriterUseTableLock()` was added
* `withUpsertSettings(models.SqlDWUpsertSettings)` was added
* `writeBehavior()` was added
* `withWriteBehavior(java.lang.Object)` was added

#### `models.PipelineRunInvokedBy` was modified

* `pipelineRunId()` was added
* `pipelineName()` was added

#### `models.IntegrationRuntimeSsisProperties` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.WebActivityAuthentication` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.AvroDataset` was modified

* `withAvroCompressionCodec(java.lang.Object)` was added

#### `models.FactoryGitHubConfiguration` was modified

* `withClientId(java.lang.String)` was added
* `clientSecret()` was added
* `clientId()` was added
* `withClientSecret(models.GitHubClientSecret)` was added

#### `models.AzureSqlDatabaseLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

#### `models.AzureSqlDWLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

#### `models.AzureBlobStorageLinkedService` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.AzureDataLakeStoreLinkedService` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.OrcDataset` was modified

* `withOrcCompressionCodec(java.lang.Object)` was added

#### `models.RestServiceLinkedService` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.AzureBlobFSLinkedService` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.AzureMLLinkedService` was modified

* `withAuthentication(java.lang.Object)` was added
* `authentication()` was added

#### `models.SqlSink` was modified

* `writeBehavior()` was added
* `upsertSettings()` was added
* `withWriteBehavior(java.lang.Object)` was added
* `sqlWriterUseTableLock()` was added
* `withUpsertSettings(models.SqlUpsertSettings)` was added
* `withSqlWriterUseTableLock(java.lang.Object)` was added

#### `models.AzureFunctionLinkedService` was modified

* `authentication()` was added
* `withAuthentication(java.lang.Object)` was added
* `withCredential(models.CredentialReference)` was added
* `credential()` was added
* `withResourceId(java.lang.Object)` was added
* `resourceId()` was added

#### `models.AzureSqlSink` was modified

* `upsertSettings()` was added
* `withWriteBehavior(java.lang.Object)` was added
* `withSqlWriterUseTableLock(java.lang.Object)` was added
* `writeBehavior()` was added
* `sqlWriterUseTableLock()` was added
* `withUpsertSettings(models.SqlUpsertSettings)` was added

#### `models.GitHubAccessTokenRequest` was modified

* `gitHubClientSecret()` was added
* `withGitHubClientSecret(models.GitHubClientSecret)` was added

#### `models.AzureDatabricksLinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

#### `models.AzureDataExplorerLinkedService` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.AzureKeyVaultLinkedService` was modified

* `credential()` was added
* `withCredential(models.CredentialReference)` was added

#### `models.AzureSqlMILinkedService` was modified

* `withCredential(models.CredentialReference)` was added
* `credential()` was added

## 1.0.0-beta.2 (2021-06-16)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.DynamicsDeploymentType` was removed

* `models.HdiNodeTypes` was removed

* `models.JsonFormatFilePattern` was removed

* `models.DynamicsAuthenticationType` was removed

* `models.DatasetCompressionLevel` was removed

* `models.CompressionCodec` was removed

* `models.JsonWriteFilePattern` was removed

* `models.DynamicsServicePrincipalCredentialType` was removed

#### `models.JsonFormat` was modified

* `models.JsonFormatFilePattern filePattern()` -> `java.lang.Object filePattern()`
* `withFilePattern(models.JsonFormatFilePattern)` was removed

#### `models.CommonDataServiceForAppsLinkedService` was modified

* `models.DynamicsDeploymentType deploymentType()` -> `java.lang.Object deploymentType()`
* `withServicePrincipalCredentialType(models.DynamicsServicePrincipalCredentialType)` was removed
* `models.DynamicsAuthenticationType authenticationType()` -> `java.lang.Object authenticationType()`
* `withDeploymentType(models.DynamicsDeploymentType)` was removed
* `withAuthenticationType(models.DynamicsAuthenticationType)` was removed
* `models.DynamicsServicePrincipalCredentialType servicePrincipalCredentialType()` -> `java.lang.Object servicePrincipalCredentialType()`

#### `models.DatasetTarGZipCompression` was modified

* `withLevel(models.DatasetCompressionLevel)` was removed
* `models.DatasetCompressionLevel level()` -> `java.lang.Object level()`

#### `models.JsonWriteSettings` was modified

* `models.JsonWriteFilePattern filePattern()` -> `java.lang.Object filePattern()`
* `withFilePattern(models.JsonWriteFilePattern)` was removed

#### `models.DatasetGZipCompression` was modified

* `models.DatasetCompressionLevel level()` -> `java.lang.Object level()`
* `withLevel(models.DatasetCompressionLevel)` was removed

#### `models.DatasetDeflateCompression` was modified

* `withLevel(models.DatasetCompressionLevel)` was removed
* `models.DatasetCompressionLevel level()` -> `java.lang.Object level()`

#### `models.ParquetDataset` was modified

* `withCompressionCodec(models.CompressionCodec)` was removed
* `models.CompressionCodec compressionCodec()` -> `java.lang.Object compressionCodec()`

#### `models.ScriptAction` was modified

* `withRoles(models.HdiNodeTypes)` was removed
* `models.HdiNodeTypes roles()` -> `java.lang.Object roles()`

#### `models.DynamicsLinkedService` was modified

* `models.DynamicsDeploymentType deploymentType()` -> `java.lang.Object deploymentType()`
* `models.DynamicsServicePrincipalCredentialType servicePrincipalCredentialType()` -> `java.lang.Object servicePrincipalCredentialType()`
* `withAuthenticationType(models.DynamicsAuthenticationType)` was removed
* `withDeploymentType(models.DynamicsDeploymentType)` was removed
* `models.DynamicsAuthenticationType authenticationType()` -> `java.lang.Object authenticationType()`
* `withServicePrincipalCredentialType(models.DynamicsServicePrincipalCredentialType)` was removed

#### `models.DelimitedTextDataset` was modified

* `models.CompressionCodec compressionCodec()` -> `java.lang.Object compressionCodec()`
* `models.DatasetCompressionLevel compressionLevel()` -> `java.lang.Object compressionLevel()`
* `withCompressionLevel(models.DatasetCompressionLevel)` was removed
* `withCompressionCodec(models.CompressionCodec)` was removed

#### `models.DynamicsCrmLinkedService` was modified

* `models.DynamicsAuthenticationType authenticationType()` -> `java.lang.Object authenticationType()`
* `models.DynamicsDeploymentType deploymentType()` -> `java.lang.Object deploymentType()`
* `withDeploymentType(models.DynamicsDeploymentType)` was removed
* `withServicePrincipalCredentialType(models.DynamicsServicePrincipalCredentialType)` was removed
* `models.DynamicsServicePrincipalCredentialType servicePrincipalCredentialType()` -> `java.lang.Object servicePrincipalCredentialType()`
* `withAuthenticationType(models.DynamicsAuthenticationType)` was removed

#### `models.DatasetZipDeflateCompression` was modified

* `models.DatasetCompressionLevel level()` -> `java.lang.Object level()`
* `withLevel(models.DatasetCompressionLevel)` was removed

### New Feature

* `models.MetadataItem` was added

#### `models.SqlMISink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CosmosDbMongoDbApiSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.QuickBooksSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.GoogleBigQuerySource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CopySink` was modified

* `disableMetricsCollection()` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DelimitedTextSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.FileServerWriteSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureFileStorageWriteSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SqlServerSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SalesforceSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CosmosDbSqlApiSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SalesforceServiceCloudSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.NetezzaSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureTableSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MySqlSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SqlMISource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureDataLakeStoreReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.JsonFormat` was modified

* `withFilePattern(java.lang.Object)` was added

#### `models.DocumentDbCollectionSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.PaypalSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ParquetSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.HttpReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ZohoSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.Office365Source` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SapOpenHubSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CommonDataServiceForAppsLinkedService` was modified

* `withDeploymentType(java.lang.Object)` was added
* `withAuthenticationType(java.lang.Object)` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added

#### `models.HttpSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DynamicsCrmSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.HiveSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureBlobFSReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.BinarySource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AmazonMwsSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SalesforceSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MicrosoftAccessSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.InformixSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureTableSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DatasetTarGZipCompression` was modified

* `withLevel(java.lang.Object)` was added

#### `models.SnowflakeSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzurePostgreSqlSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SqlDWSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.PrestoSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureDataLakeStoreSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SapCloudForCustomerSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MicrosoftAccessSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.BlobSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `metadata()` was added
* `withMetadata(java.util.List)` was added

#### `models.JsonWriteSettings` was modified

* `withFilePattern(java.lang.Object)` was added

#### `models.AvroSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureMariaDBSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.IntegrationRuntimeSsisProperties` was modified

* `withManagedCredential(models.EntityReference)` was added
* `managedCredential()` was added

#### `models.SnowflakeSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzurePostgreSqlSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.GoogleAdWordsSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OdbcSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MariaDBSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureFileStorageReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.RestSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SapEccSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.FtpReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CassandraSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SapTableSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CommonDataServiceForAppsSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OracleSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.FileServerReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureBlobFSWriteSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.HdfsSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CopySource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `disableMetricsCollection()` was added

#### `models.WebSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DrillSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ServiceNowSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MongoDbSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DatasetGZipCompression` was modified

* `withLevel(java.lang.Object)` was added

#### `models.InformixSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ParquetSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SftpWriteSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ConcurSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.PhoenixSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OrcSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CosmosDbMongoDbApiSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DatasetDeflateCompression` was modified

* `withLevel(java.lang.Object)` was added

#### `models.ParquetDataset` was modified

* `withCompressionCodec(java.lang.Object)` was added

#### `models.SalesforceMarketingCloudSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.TeradataSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.GreenplumSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SalesforceServiceCloudSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SapBwSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OdbcSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DelimitedTextSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DynamicsCrmSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SapHanaSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureDataLakeStoreWriteSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ScriptAction` was modified

* `withRoles(java.lang.Object)` was added

#### `models.SapCloudForCustomerSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureBlobFSSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SquareSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DynamicsSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DynamicsSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.StoreWriteSettings` was modified

* `disableMetricsCollection()` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ExcelDataset` was modified

* `withSheetIndex(java.lang.Object)` was added
* `sheetIndex()` was added

#### `models.ImpalaSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureQueueSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.JsonSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AmazonS3CompatibleReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureBlobStorageWriteSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SybaseSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.JiraSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DynamicsLinkedService` was modified

* `withServicePrincipalCredentialType(java.lang.Object)` was added
* `withAuthenticationType(java.lang.Object)` was added
* `withDeploymentType(java.lang.Object)` was added

#### `models.SftpReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ResponsysSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureBlobStorageReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.JsonSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SparkSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.RestSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MongoDbAtlasSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.HBaseSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.BinarySink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AmazonS3ReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.VerticaSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `DataFactoryManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.SqlServerSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.BlobSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.StoreReadSettings` was modified

* `disableMetricsCollection()` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureDatabricksDeltaLakeSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MarketoSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AmazonRedshiftSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SqlDWSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.XeroSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureMySqlSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ODataSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.XmlSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MongoDbAtlasSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.TabularSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.FileSystemSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.RelationalSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SharePointOnlineListSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OracleSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SqlSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureSqlSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.PostgreSqlSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.HdfsReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MongoDbV2Source` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DelimitedTextDataset` was modified

* `withCompressionCodec(java.lang.Object)` was added
* `withCompressionLevel(java.lang.Object)` was added

#### `models.AzureDataExplorerSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.SqlSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureDatabricksDeltaLakeSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CommonDataServiceForAppsSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OracleServiceCloudSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OrcSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DocumentDbCollectionSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureBlobFSSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `metadata()` was added
* `withMetadata(java.util.List)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CosmosDbSqlApiSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.FileSystemSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MagentoSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.CouchbaseSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DynamicsCrmLinkedService` was modified

* `withDeploymentType(java.lang.Object)` was added
* `withServicePrincipalCredentialType(java.lang.Object)` was added
* `withAuthenticationType(java.lang.Object)` was added

#### `models.AvroSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.GoogleCloudStorageReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.Db2Source` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.HubspotSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.MongoDbV2Sink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureSqlSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ExcelSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.OracleCloudStorageReadSettings` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureSearchIndexSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureDataExplorerSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.AzureMySqlSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.EloquaSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DatasetZipDeflateCompression` was modified

* `withLevel(java.lang.Object)` was added

#### `models.AzureDataLakeStoreSink` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.ShopifySource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

#### `models.DynamicsAXSource` was modified

* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added
* `withDisableMetricsCollection(java.lang.Object)` was added

## 1.0.0-beta.1 (2021-04-12)

- Azure Resource Manager DataFactory client library for Java. This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
