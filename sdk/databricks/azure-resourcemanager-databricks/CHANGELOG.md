# Release History

## 1.1.0 (2026-05-08)

- Azure Resource Manager Azure Databricks client library for Java. This package contains Microsoft Azure SDK for Azure Databricks Management SDK. ARM Databricks. Package api-version 2026-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.VirtualNetworkPeeringList` was removed

#### `models.WorkspaceListResult` was removed

#### `models.AccessConnectorListResult` was removed

#### `models.PrivateLinkResourcesList` was removed

#### `models.PrivateEndpointConnectionsList` was removed

#### `models.Workspace$DefinitionStages` was modified

* `withManagedResourceGroupId(java.lang.String)` was removed in stage 3

#### `models.AccessConnectorUpdate` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.ManagedDiskEncryption` was modified

* `validate()` was removed

#### `models.CreatedBy` was modified

* `java.util.UUID applicationId()` -> `java.lang.String applicationId()`
* `validate()` was removed
* `java.util.UUID oid()` -> `java.lang.String oid()`

#### `models.AddressSpace` was modified

* `validate()` was removed

#### `models.ManagedServiceIdentity` was modified

* `validate()` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed
* `withDescription(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.EncryptionV2` was modified

* `validate()` was removed

#### `models.GroupIdInformationProperties` was modified

* `GroupIdInformationProperties()` was changed to private access
* `validate()` was removed
* `withRequiredMembers(java.util.List)` was removed
* `withGroupId(java.lang.String)` was removed
* `withRequiredZoneNames(java.util.List)` was removed

#### `models.WorkspaceCustomBooleanParameter` was modified

* `validate()` was removed

#### `models.WorkspaceEncryptionParameter` was modified

* `validate()` was removed

#### `models.EndpointDetail` was modified

* `EndpointDetail()` was changed to private access
* `withPort(java.lang.Integer)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withIsAccessible(java.lang.Boolean)` was removed
* `validate()` was removed
* `withLatency(java.lang.Double)` was removed

#### `models.EncryptionEntitiesDefinition` was modified

* `validate()` was removed

#### `models.WorkspaceCustomObjectParameter` was modified

* `WorkspaceCustomObjectParameter()` was changed to private access
* `withValue(java.lang.Object)` was removed
* `validate()` was removed

#### `models.VirtualNetworkPeeringPropertiesFormatDatabricksVirtualNetwork` was modified

* `validate()` was removed

#### `models.WorkspacePropertiesEncryption` was modified

* `validate()` was removed

#### `models.EndpointDependency` was modified

* `EndpointDependency()` was changed to private access
* `validate()` was removed
* `withDomainName(java.lang.String)` was removed
* `withEndpointDetails(java.util.List)` was removed

#### `models.WorkspaceUpdate` was modified

* `validate()` was removed

#### `models.WorkspaceCustomStringParameter` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.VirtualNetworkPeeringPropertiesFormatRemoteVirtualNetwork` was modified

* `validate()` was removed

#### `models.WorkspaceCustomParameters` was modified

* `models.WorkspaceCustomBooleanParameter enableNoPublicIp()` -> `models.WorkspaceNoPublicIpBooleanParameter enableNoPublicIp()`
* `validate()` was removed
* `withEnableNoPublicIp(models.WorkspaceCustomBooleanParameter)` was removed

#### `models.PrivateEndpointConnectionProperties` was modified

* `validate()` was removed

#### `models.Encryption` was modified

* `validate()` was removed

#### `models.ManagedIdentityConfiguration` was modified

* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `validate()` was removed

#### `models.ErrorInfo` was modified

* `ErrorInfo()` was changed to private access
* `validate()` was removed

#### `models.Workspaces` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WorkspaceProviderAuthorization` was modified

* `java.util.UUID roleDefinitionId()` -> `java.lang.String roleDefinitionId()`
* `validate()` was removed
* `withRoleDefinitionId(java.util.UUID)` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `withPrincipalId(java.util.UUID)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `validate()` was removed

#### `models.AccessConnectorProperties` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.ManagedDiskEncryptionKeyVaultProperties` was modified

* `validate()` was removed

#### `models.EncryptionV2KeyVaultProperties` was modified

* `validate()` was removed

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

* `models.ComputeMode` was added

* `models.WorkspacePropertiesAccessConnector` was added

* `models.InitialType` was added

* `models.AutomaticClusterUpdateDefinition` was added

* `models.DefaultCatalogProperties` was added

#### `models.Workspace$Definition` was modified

* `withComputeMode(models.ComputeMode)` was added
* `withDefaultCatalog(models.DefaultCatalogProperties)` was added
* `withEnhancedSecurityCompliance(models.EnhancedSecurityComplianceDefinition)` was added
* `withDefaultStorageFirewall(models.DefaultStorageFirewall)` was added
* `withAccessConnector(models.WorkspacePropertiesAccessConnector)` was added

#### `models.VirtualNetworkPeering` was modified

* `systemData()` was added

#### `models.GroupIdInformation` was modified

* `systemData()` was added

#### `models.Workspace` was modified

* `accessConnector()` was added
* `defaultCatalog()` was added
* `defaultStorageFirewall()` was added
* `computeMode()` was added
* `isUcEnabled()` was added
* `enhancedSecurityCompliance()` was added

#### `models.WorkspaceCustomBooleanParameter` was modified

* `withType(models.CustomParameterType)` was added

#### `models.WorkspaceEncryptionParameter` was modified

* `withType(models.CustomParameterType)` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

#### `models.WorkspaceCustomStringParameter` was modified

* `withType(models.CustomParameterType)` was added

#### `models.WorkspaceCustomParameters` was modified

* `withEnableNoPublicIp(models.WorkspaceNoPublicIpBooleanParameter)` was added

#### `models.Workspaces` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.WorkspaceProviderAuthorization` was modified

* `withPrincipalId(java.lang.String)` was added
* `withRoleDefinitionId(java.lang.String)` was added

#### `models.AccessConnectorProperties` was modified

* `referedBy()` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager AzureDatabricks client library for Java. This package contains Microsoft Azure SDK for AzureDatabricks Management SDK. The Microsoft Azure management APIs allow end users to operate on Azure Databricks Workspace / Access Connector resources. Package tag package-2023-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager AzureDatabricks client library for Java.

## 1.0.0-beta.5 (2024-10-28)

- Azure Resource Manager AzureDatabricks client library for Java. This package contains Microsoft Azure SDK for AzureDatabricks Management SDK. The Microsoft Azure management APIs allow end users to operate on Azure Databricks Workspace / Access Connector resources. Package tag package-2023-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.AccessConnectorUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedDiskEncryption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkPeeringList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CreatedBy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AddressSpace` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedServiceIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionV2` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GroupIdInformationProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceCustomBooleanParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceEncryptionParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccessConnectorListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EndpointDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionEntitiesDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceCustomObjectParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResourcesList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualNetworkPeeringPropertiesFormatDatabricksVirtualNetwork` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspacePropertiesEncryption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EndpointDependency` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkspaceUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceCustomStringParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkPeeringPropertiesFormatRemoteVirtualNetwork` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkspaceCustomParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Encryption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedIdentityConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ErrorInfo` was modified

* `getAdditionalInfo()` was added
* `getMessage()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `getDetails()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `getTarget()` was added
* `getCode()` was added

#### `models.WorkspaceProviderAuthorization` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccessConnectorProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

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
