# Release History

## 1.0.0-beta.5 (2024-09-27)

- Azure Resource Manager AzureDatabricks client library for Java. This package contains Microsoft Azure SDK for AzureDatabricks Management SDK. The Microsoft Azure management APIs allow end users to operate on Azure Databricks Workspace / Access Connector resources. Package tag package-2024-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.WorkspaceCustomParameters` was modified

* `models.WorkspaceCustomBooleanParameter enableNoPublicIp()` -> `models.WorkspaceNoPublicIpBooleanParameter enableNoPublicIp()`
* `withEnableNoPublicIp(models.WorkspaceCustomBooleanParameter)` was removed

#### `models.Workspaces` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.EnhancedSecurityMonitoringDefinition` was added

* `models.IdentityType` was added

* `models.EnhancedSecurityMonitoringValue` was added

* `models.DefaultStorageFirewall` was added

* `models.AutomaticClusterUpdateValue` was added

* `models.WorkspaceNoPublicIpBooleanParameter` was added

* `models.ComplianceSecurityProfileValue` was added

* `models.ComplianceSecurityProfileDefinition` was added

* `models.EnhancedSecurityComplianceDefinition` was added

* `models.ComplianceStandard` was added

* `models.WorkspacePropertiesAccessConnector` was added

* `models.InitialType` was added

* `models.AutomaticClusterUpdateDefinition` was added

* `models.DefaultCatalogProperties` was added

#### `models.AccessConnectorUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedDiskEncryption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkPeeringList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Workspace$Definition` was modified

* `withEnhancedSecurityCompliance(models.EnhancedSecurityComplianceDefinition)` was added
* `withDefaultStorageFirewall(models.DefaultStorageFirewall)` was added
* `withAccessConnector(models.WorkspacePropertiesAccessConnector)` was added
* `withDefaultCatalog(models.DefaultCatalogProperties)` was added

#### `models.CreatedBy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AddressSpace` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedServiceIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionV2` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GroupIdInformationProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Workspace` was modified

* `accessConnector()` was added
* `defaultCatalog()` was added
* `defaultStorageFirewall()` was added
* `isUcEnabled()` was added
* `enhancedSecurityCompliance()` was added

#### `models.WorkspaceCustomBooleanParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceEncryptionParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AccessConnectorListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EndpointDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionEntitiesDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceCustomObjectParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResourcesList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkPeeringPropertiesFormatDatabricksVirtualNetwork` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspacePropertiesEncryption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EndpointDependency` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceCustomStringParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkPeeringPropertiesFormatRemoteVirtualNetwork` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceCustomParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withEnableNoPublicIp(models.WorkspaceNoPublicIpBooleanParameter)` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Encryption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIdentityConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ErrorInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `getCode()` was added
* `getDetails()` was added
* `getMessage()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `getAdditionalInfo()` was added
* `getTarget()` was added

#### `models.Workspaces` was modified

* `delete(java.lang.String,java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.WorkspaceProviderAuthorization` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionsList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AccessConnectorProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `referedBy()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedDiskEncryptionKeyVaultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EncryptionV2KeyVaultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.4 (2023-06-08)

- Azure Resource Manager AzureDatabricks client library for Java. This package contains Microsoft Azure SDK for AzureDatabricks Management SDK. The Microsoft Azure management APIs allow end users to operate on Azure Databricks Workspace / Access Connector resources. Package tag package-2023-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PrivateLinkServiceConnectionState` was modified

* `withActionRequired(java.lang.String)` was removed
* `actionRequired()` was removed

### Features Added

* `models.AccessConnectorUpdate` was added

* `models.ManagedDiskEncryption` was added

* `models.AccessConnector$Definition` was added

* `models.ManagedServiceIdentity` was added

* `models.AccessConnectorListResult` was added

* `models.AccessConnectors` was added

* `models.ManagedServiceIdentityType` was added

* `models.AccessConnector$UpdateStages` was added

* `models.AccessConnector$Update` was added

* `models.AccessConnector` was added

* `models.UserAssignedIdentity` was added

* `models.AccessConnectorProperties` was added

* `models.ManagedDiskEncryptionKeyVaultProperties` was added

* `models.AccessConnector$DefinitionStages` was added

#### `models.Workspace$Definition` was modified

* `withManagedDiskIdentity(models.ManagedIdentityConfiguration)` was added

#### `AzureDatabricksManager` was modified

* `accessConnectors()` was added

#### `models.OperationDisplay` was modified

* `description()` was added
* `withDescription(java.lang.String)` was added

#### `models.Workspace` was modified

* `managedDiskIdentity()` was added
* `diskEncryptionSetId()` was added

#### `models.EncryptionEntitiesDefinition` was modified

* `withManagedDisk(models.ManagedDiskEncryption)` was added
* `managedDisk()` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `withGroupIds(java.util.List)` was added
* `groupIds()` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `actionsRequired()` was added
* `withActionsRequired(java.lang.String)` was added

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
