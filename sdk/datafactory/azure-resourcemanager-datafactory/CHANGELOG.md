# Release History

## 1.0.0-beta.23 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
