# Release History

## 1.0.0-beta.3 (2022-01-26)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-preview-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2021-10-09)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-preview-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OperationResultsDescription` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpointConnections` was added

* `models.OperationResultStatus` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpoint` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.PrivateLinkResourceBase` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.MsaAppType` was added

* `models.OperationResults` was added

* `models.PrivateLinkResourceListResult` was added

#### `models.BotProperties` was modified

* `msaAppMsiResourceId()` was added
* `privateEndpointConnections()` was added
* `withMsaAppMsiResourceId(java.lang.String)` was added
* `disableLocalAuth()` was added
* `msaAppTenantId()` was added
* `withMsaAppType(models.MsaAppType)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `msaAppType()` was added
* `withMsaAppTenantId(java.lang.String)` was added

#### `BotServiceManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.SlackChannelProperties` was modified

* `scopes()` was added
* `withScopes(java.lang.String)` was added

#### `BotServiceManager` was modified

* `privateEndpointConnections()` was added
* `operationResults()` was added
* `privateLinkResources()` was added

## 1.0.0-beta.1 (2021-05-14)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-2021-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
