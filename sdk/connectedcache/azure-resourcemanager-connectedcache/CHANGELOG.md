# Release History

## 1.0.0-beta.2 (2025-12-24)

- Azure Resource Manager Connected Cache client library for Java. This package contains Microsoft Azure SDK for Connected Cache Management SDK. Microsoft Connected Cache Rest Api version 2023-05-01-preview. Package api-version 2024-11-30-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.EnterpriseCustomerOperations` was removed

#### `models.CacheNodePreviewResource$DefinitionStages` was removed

#### `models.CacheNodesOperations` was removed

#### `models.CycleType` was removed

#### `models.EnterprisePreviewResource$Update` was removed

#### `models.EnterprisePreviewResource$UpdateStages` was removed

#### `models.CacheNodePreviewResource$UpdateStages` was removed

#### `models.EnterprisePreviewResource` was removed

#### `models.CacheNodePreviewResource` was removed

#### `models.CacheNodeOldResponse` was removed

#### `models.EnterprisePreviewResource$Definition` was removed

#### `models.CacheNodePreviewResource$Definition` was removed

#### `models.CacheNodePreviewResource$Update` was removed

#### `models.EnterprisePreviewResource$DefinitionStages` was removed

#### `models.AdditionalCustomerProperties` was modified

* `validate()` was removed
* `peeringDbLastUpdateTime()` was removed

#### `models.CacheNodeInstallProperties` was modified

* `validate()` was removed

#### `models.BgpCidrsConfiguration` was modified

* `validate()` was removed

#### `models.BgpConfiguration` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `ConnectedCacheManager` was modified

* `enterpriseCustomerOperations()` was removed
* `cacheNodesOperations()` was removed

#### `models.ConnectedCachePatchResource` was modified

* `validate()` was removed

#### `models.CacheNodeDriveConfiguration` was modified

* `validate()` was removed

#### `models.CacheNodeProperty` was modified

* `validate()` was removed

#### `models.CacheNodeEntity` was modified

* `validate()` was removed

#### `models.ProxyUrlConfiguration` was modified

* `validate()` was removed

#### `models.CustomerEntity` was modified

* `validate()` was removed

#### `models.CustomerProperty` was modified

* `validate()` was removed

#### `models.AdditionalCacheNodeProperties` was modified

* `validate()` was removed
* `withProxyUrl(java.lang.String)` was removed
* `updateCycleType()` was removed
* `withUpdateCycleType(models.CycleType)` was removed
* `proxyUrl()` was removed

### Features Added

* `models.MccCacheNodeTlsCertificate` was added

* `models.MccIssue` was added

* `models.MccCacheNodeAutoUpdateInfo` was added

* `models.MccCacheNodeAutoUpdateHistoryProperties` was added

* `models.MccCacheNodeIssueHistory` was added

* `models.MccCacheNodeAutoUpdateHistory` was added

* `models.MccCacheNodeTlsCertificateHistory` was added

* `models.MccCacheNodeIssueHistoryProperties` was added

* `models.MccCacheNodeTlsCertificateProperties` was added

#### `models.CacheNodeInstallProperties` was modified

* `driveConfiguration()` was added
* `proxyUrlConfiguration()` was added
* `tlsCertificateProvisioningKey()` was added

#### `models.IspCacheNodeResource` was modified

* `getCacheNodeMccIssueDetailsHistory()` was added
* `getCacheNodeAutoUpdateHistory()` was added
* `getCacheNodeAutoUpdateHistoryWithResponse(com.azure.core.util.Context)` was added
* `getCacheNodeMccIssueDetailsHistoryWithResponse(com.azure.core.util.Context)` was added

#### `models.IspCacheNodesOperations` was modified

* `getCacheNodeMccIssueDetailsHistory(java.lang.String,java.lang.String,java.lang.String)` was added
* `getCacheNodeAutoUpdateHistory(java.lang.String,java.lang.String,java.lang.String)` was added
* `getCacheNodeMccIssueDetailsHistoryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getCacheNodeAutoUpdateHistoryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.EnterpriseMccCacheNodesOperations` was modified

* `getCacheNodeAutoUpdateHistory(java.lang.String,java.lang.String,java.lang.String)` was added
* `getCacheNodeMccIssueDetailsHistory(java.lang.String,java.lang.String,java.lang.String)` was added
* `getCacheNodeTlsCertificateHistory(java.lang.String,java.lang.String,java.lang.String)` was added
* `getCacheNodeAutoUpdateHistoryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getCacheNodeTlsCertificateHistoryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getCacheNodeMccIssueDetailsHistoryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AdditionalCacheNodeProperties` was modified

* `tlsStatus()` was added
* `creationMethod()` was added
* `currentTlsCertificate()` was added
* `withCreationMethod(java.lang.Integer)` was added
* `lastAutoUpdateInfo()` was added
* `issuesCount()` was added
* `issuesList()` was added

#### `models.EnterpriseMccCacheNodeResource` was modified

* `getCacheNodeAutoUpdateHistoryWithResponse(com.azure.core.util.Context)` was added
* `getCacheNodeTlsCertificateHistory()` was added
* `getCacheNodeMccIssueDetailsHistory()` was added
* `getCacheNodeAutoUpdateHistory()` was added
* `getCacheNodeMccIssueDetailsHistoryWithResponse(com.azure.core.util.Context)` was added
* `getCacheNodeTlsCertificateHistoryWithResponse(com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2024-11-21)

- Azure Resource Manager Connected Cache client library for Java. This package contains Microsoft Azure SDK for Connected Cache Management SDK. Microsoft Connected Cache Rest Api version 2023-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-connectedcache Java SDK.
