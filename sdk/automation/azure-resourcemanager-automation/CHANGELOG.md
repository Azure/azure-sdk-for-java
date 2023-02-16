# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-08-12)

- Azure Resource Manager Automation client library for Java. This package contains Microsoft Azure SDK for Automation Management SDK. Automation Client. Package tag package-2022-02-22. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.RunbookDraftsReplaceContentResponse` was removed

* `models.HybridRunbookWorkerGroupUpdateParameters` was removed

* `models.RunbooksPublishHeaders` was removed

* `models.RunbookDraftsReplaceContentHeaders` was removed

* `models.RunbooksPublishResponse` was removed

#### `models.DscNode` was modified

* `namePropertiesNodeConfigurationName()` was removed

#### `models.HybridRunbookWorker` was modified

* `java.lang.String ip()` -> `java.lang.String ip()`
* `validate()` was removed
* `withLastSeenDateTime(java.time.OffsetDateTime)` was removed
* `java.lang.String name()` -> `java.lang.String name()`
* `java.time.OffsetDateTime lastSeenDateTime()` -> `java.time.OffsetDateTime lastSeenDateTime()`
* `withRegistrationTime(java.time.OffsetDateTime)` was removed
* `withName(java.lang.String)` was removed
* `registrationTime()` was removed
* `withIp(java.lang.String)` was removed

#### `models.HybridRunbookWorkerGroup` was modified

* `hybridRunbookWorkers()` was removed

#### `models.HybridRunbookWorkerGroups` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,models.HybridRunbookWorkerGroupUpdateParameters)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,models.HybridRunbookWorkerGroupUpdateParameters,com.azure.core.util.Context)` was removed

#### `models.RunbookDrafts` was modified

* `java.lang.String getContent(java.lang.String,java.lang.String,java.lang.String)` -> `reactor.core.publisher.Flux getContent(java.lang.String,java.lang.String,java.lang.String)`
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `void undoEdit(java.lang.String,java.lang.String,java.lang.String)` -> `models.RunbookDraftUndoEditResult undoEdit(java.lang.String,java.lang.String,java.lang.String)`
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Runbooks` was modified

* `java.lang.String getContent(java.lang.String,java.lang.String,java.lang.String)` -> `reactor.core.publisher.Flux getContent(java.lang.String,java.lang.String,java.lang.String)`

#### `models.DscConfigurations` was modified

* `update(java.lang.String,java.lang.String,java.lang.String)` was removed

### Features Added

* `models.PrivateEndpointConnectionListResult` was added

* `models.EncryptionProperties` was added

* `models.Identity` was added

* `models.DeletedAutomationAccount` was added

* `models.GraphicalRunbookContent` was added

* `models.RunbookProperties` was added

* `models.HybridRunbookWorkerGroup$DefinitionStages` was added

* `models.HybridRunbookWorkerMoveParameters` was added

* `models.IdentityUserAssignedIdentities` was added

* `models.PrivateEndpointProperty` was added

* `models.KeyVaultProperties` was added

* `models.RunbookDraftUndoEditResult` was added

* `models.ResourceProviders` was added

* `models.HybridRunbookWorker$Definition` was added

* `models.RawGraphicalRunbookContent` was added

* `models.PrivateLinkServiceConnectionStateProperty` was added

* `models.EncryptionKeySourceType` was added

* `models.PrivateLinkResourceListResult` was added

* `models.WorkerType` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.HybridRunbookWorker$DefinitionStages` was added

* `models.HybridRunbookWorkersListResult` was added

* `models.HybridRunbookWorkerGroupCreateOrUpdateParameters` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.HybridRunbookWorkerGroup$Definition` was added

* `models.HybridRunbookWorkers` was added

* `models.ResourceIdentityType` was added

* `models.PrivateEndpointConnection` was added

* `models.GraphRunbookType` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.DeletedAutomationAccountListResult` was added

* `models.PrivateLinkResource` was added

* `models.RunbookCreateOrUpdateProperties` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.EncryptionPropertiesIdentity` was added

* `models.PrivateEndpointConnections` was added

* `models.HybridRunbookWorkerGroup$UpdateStages` was added

* `models.DeletedAutomationAccounts` was added

* `models.HybridRunbookWorkerGroup$Update` was added

* `models.HybridRunbookWorkerCreateParameters` was added

#### `models.DscNodeConfiguration` was modified

* `resourceGroupName()` was added

#### `models.Variable` was modified

* `resourceGroupName()` was added

#### `models.Webhook` was modified

* `resourceGroupName()` was added

#### `models.DscConfiguration` was modified

* `resourceGroupName()` was added

#### `models.DscNode` was modified

* `namePropertiesName()` was added

#### `models.ConnectionType` was modified

* `resourceGroupName()` was added

#### `models.AutomationAccountUpdateParameters` was modified

* `publicNetworkAccess()` was added
* `disableLocalAuth()` was added
* `identity()` was added
* `encryption()` was added
* `withEncryption(models.EncryptionProperties)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.Identity)` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added

#### `models.Certificate` was modified

* `resourceGroupName()` was added

#### `models.AutomationAccount$Update` was modified

* `withEncryption(models.EncryptionProperties)` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added
* `withIdentity(models.Identity)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.Watcher` was modified

* `stop()` was added
* `startWithResponse(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `start()` was added
* `stopWithResponse(com.azure.core.util.Context)` was added

#### `models.AutomationAccountCreateOrUpdateParameters` was modified

* `withIdentity(models.Identity)` was added
* `encryption()` was added
* `identity()` was added
* `withEncryption(models.EncryptionProperties)` was added
* `disableLocalAuth()` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added
* `publicNetworkAccess()` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.Connection` was modified

* `resourceGroupName()` was added

#### `models.HybridRunbookWorker` was modified

* `refresh(com.azure.core.util.Context)` was added
* `workerName()` was added
* `registeredDateTime()` was added
* `moveWithResponse(models.HybridRunbookWorkerMoveParameters,com.azure.core.util.Context)` was added
* `vmResourceId()` was added
* `refresh()` was added
* `workerType()` was added
* `innerModel()` was added
* `id()` was added
* `systemData()` was added
* `move(models.HybridRunbookWorkerMoveParameters)` was added
* `type()` was added

#### `models.Runbook` was modified

* `publish(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `publish()` was added

#### `models.AutomationAccount` was modified

* `systemData()` was added
* `automationHybridServiceUrl()` was added
* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added
* `disableLocalAuth()` was added
* `resourceGroupName()` was added
* `encryption()` was added
* `identity()` was added

#### `AutomationManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.HybridRunbookWorkerGroup` was modified

* `type()` was added
* `systemData()` was added
* `resourceGroupName()` was added
* `refresh(com.azure.core.util.Context)` was added
* `update()` was added
* `refresh()` was added

#### `models.HybridRunbookWorkerGroups` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `AutomationManager` was modified

* `hybridRunbookWorkers()` was added
* `resourceProviders()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `deletedAutomationAccounts()` was added
* `privateLinkResources()` was added
* `privateEndpointConnections()` was added

#### `models.RunbookDrafts` was modified

* `replaceContentWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long,com.azure.core.util.Context)` was added
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long)` was added
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long)` was added
* `replaceContentWithResponse(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long,com.azure.core.util.Context)` was added

#### `models.Job` was modified

* `stop()` was added
* `resume()` was added
* `resumeWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `suspend()` was added
* `suspendWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `stopWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.Schedule` was modified

* `resourceGroupName()` was added

#### `models.Credential` was modified

* `resourceGroupName()` was added

#### `models.AutomationAccount$Definition` was modified

* `withEncryption(models.EncryptionProperties)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.Identity)` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added

#### `models.SourceControl` was modified

* `resourceGroupName()` was added

#### `models.Module` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-09)

- Azure Resource Manager Automation client library for Java. This package contains Microsoft Azure SDK for Automation Management SDK. Automation Client. Package tag package-2019-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
