# Release History

## 1.0.0-beta.4 (Unreleased)


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
