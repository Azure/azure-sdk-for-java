# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2023-01-16)

- Azure Resource Manager AzureDatabricks client library for Java. This package contains Microsoft Azure SDK for AzureDatabricks Management SDK. The Microsoft Azure management APIs allow end users to operate on Azure Databricks Workspace resources. Package tag package-2021-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OutboundNetworkDependenciesEndpoints` was added

* `models.EndpointDetail` was added

* `models.OutboundEnvironmentEndpoint` was added

* `models.EndpointDependency` was added

#### `models.VirtualNetworkPeering` was modified

* `resourceGroupName()` was added

#### `AzureDatabricksManager` was modified

* `outboundNetworkDependenciesEndpoints()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Workspace` was modified

* `resourceGroupName()` was added

#### `AzureDatabricksManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.2 (2021-06-21)

- Azure Resource Manager AzureDatabricks client library for Java. This package contains Microsoft Azure SDK for AzureDatabricks Management SDK. The Microsoft Azure management APIs allow end users to operate on Azure Databricks Workspace resources. Package tag package-2021-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `DatabricksManager` was removed

* `DatabricksManager$Configurable` was removed

### New Feature

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateEndpoint` was added

* `AzureDatabricksManager` was added

* `models.GroupIdInformation` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.EncryptionV2` was added

* `models.RequiredNsgRules` was added

* `models.GroupIdInformationProperties` was added

* `models.PrivateLinkServiceConnectionStatus` was added

* `models.EncryptionEntitiesDefinition` was added

* `models.WorkspaceCustomObjectParameter` was added

* `models.PublicNetworkAccess` was added

* `models.WorkspacePropertiesEncryption` was added

* `models.PrivateLinkResourcesList` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateLinkResources` was added

* `models.EncryptionKeySource` was added

* `models.PrivateEndpointConnectionProperties` was added

* `AzureDatabricksManager$Configurable` was added

* `models.PrivateEndpointConnectionsList` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.PrivateEndpointConnections` was added

* `models.EncryptionV2KeyVaultProperties` was added

#### `models.Workspace$Definition` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withEncryption(models.WorkspacePropertiesEncryption)` was added
* `withRequiredNsgRules(models.RequiredNsgRules)` was added

#### `models.Workspace` was modified

* `encryption()` was added
* `requiredNsgRules()` was added
* `privateEndpointConnections()` was added
* `systemData()` was added
* `publicNetworkAccess()` was added

#### `models.WorkspaceCustomParameters` was modified

* `storageAccountName()` was added
* `withNatGatewayName(models.WorkspaceCustomStringParameter)` was added
* `loadBalancerBackendPoolName()` was added
* `natGatewayName()` was added
* `loadBalancerId()` was added
* `vnetAddressPrefix()` was added
* `withStorageAccountName(models.WorkspaceCustomStringParameter)` was added
* `publicIpName()` was added
* `withLoadBalancerId(models.WorkspaceCustomStringParameter)` was added
* `withVnetAddressPrefix(models.WorkspaceCustomStringParameter)` was added
* `withStorageAccountSkuName(models.WorkspaceCustomStringParameter)` was added
* `withPublicIpName(models.WorkspaceCustomStringParameter)` was added
* `resourceTags()` was added
* `storageAccountSkuName()` was added
* `withLoadBalancerBackendPoolName(models.WorkspaceCustomStringParameter)` was added

## 1.0.0-beta.1 (2021-04-08)

- Azure Resource Manager Databricks client library for Java. This package contains Microsoft Azure SDK for Databricks Management SDK. ARM Databricks. Package tag package-2018-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
