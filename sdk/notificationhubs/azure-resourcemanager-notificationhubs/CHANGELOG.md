# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2024-03-15)

- Azure Resource Manager NotificationHubs client library for Java. This package contains Microsoft Azure SDK for NotificationHubs Management SDK. Microsoft Notification Hubs Resource Provider REST API. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NamespaceCreateOrUpdateParameters` was removed

* `models.PolicykeyResource` was removed

* `models.SharedAccessAuthorizationRuleCreateOrUpdateParameters` was removed

* `models.NotificationHubCreateOrUpdateParameters` was removed

#### `models.NamespaceResource$DefinitionStages` was modified

* `withLocation(java.lang.String)` was removed in stage 1
* `withLocation(com.azure.core.management.Region)` was removed in stage 1
* Stage 3 was added

#### `models.SharedAccessAuthorizationRuleResource$DefinitionStages` was modified

* `withExistingNamespace(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was removed
* `withExistingNamespace(java.lang.String,java.lang.String)` was removed

#### `models.NotificationHubResource` was modified

* `debugSendWithResponse(java.lang.Object,com.azure.core.util.Context)` was removed

#### `models.SharedAccessAuthorizationRuleListResult` was modified

* `withValue(java.util.List)` was removed
* `withNextLink(java.lang.String)` was removed

#### `models.DebugSendResponse` was modified

* `java.lang.Float failure()` -> `java.lang.Long failure()`
* `java.lang.Object results()` -> `java.util.List results()`
* `sku()` was removed
* `java.lang.Float success()` -> `java.lang.Long success()`

#### `models.PnsCredentialsResource` was modified

* `sku()` was removed

#### `models.NotificationHubResource$Update` was modified

* `withAuthorizationRules(java.util.List)` was removed
* `withNamePropertiesName(java.lang.String)` was removed

#### `models.NotificationHubListResult` was modified

* `withValue(java.util.List)` was removed
* `withNextLink(java.lang.String)` was removed

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was removed

#### `models.Namespaces` was modified

* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `defineAuthorizationRule(java.lang.String)` was removed
* `deleteAuthorizationRuleById(java.lang.String)` was removed
* `getAuthorizationRuleById(java.lang.String)` was removed
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `list(com.azure.core.util.Context)` was removed
* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource)` was removed
* `getAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AccessRights` was modified

* `toString()` was removed
* `models.AccessRights[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.NotificationHubPatchParameters` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withNamePropertiesName(java.lang.String)` was removed
* `withAuthorizationRules(java.util.List)` was removed
* `namePropertiesName()` was removed

#### `models.NotificationHubResource$Definition` was modified

* `withAuthorizationRules(java.util.List)` was removed

#### `NotificationHubsManager` was modified

* `fluent.NotificationHubsManagementClient serviceClient()` -> `fluent.NotificationHubsRPClient serviceClient()`

#### `models.NotificationHubs` was modified

* `createOrUpdateAuthorizationRule(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SharedAccessAuthorizationRuleCreateOrUpdateParameters)` was removed
* `createOrUpdateAuthorizationRuleWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SharedAccessAuthorizationRuleCreateOrUpdateParameters,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `debugSendWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Object,com.azure.core.util.Context)` was removed
* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource)` was removed
* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource,com.azure.core.util.Context)` was removed

#### `models.NamespaceListResult` was modified

* `withNextLink(java.lang.String)` was removed
* `withValue(java.util.List)` was removed

#### `models.NamespaceType` was modified

* `toString()` was removed
* `models.NamespaceType[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.SharedAccessAuthorizationRuleResource` was modified

* `sku()` was removed
* `regenerateKeys(models.PolicykeyResource)` was removed
* `java.lang.String modifiedTime()` -> `java.time.OffsetDateTime modifiedTime()`
* `java.lang.String createdTime()` -> `java.time.OffsetDateTime createdTime()`
* `regenerateKeysWithResponse(models.PolicykeyResource,com.azure.core.util.Context)` was removed

#### `models.NamespaceResource$Definition` was modified

* `withServiceBusEndpoint(java.lang.String)` was removed
* `withNamePropertiesName(java.lang.String)` was removed
* `withLocation(java.lang.String)` was removed
* `withCritical(java.lang.Boolean)` was removed
* `withRegion(java.lang.String)` was removed
* `withSubscriptionId(java.lang.String)` was removed
* `withUpdatedAt(java.time.OffsetDateTime)` was removed
* `withProvisioningState(java.lang.String)` was removed
* `withEnabled(java.lang.Boolean)` was removed
* `withStatus(java.lang.String)` was removed
* `withCreatedAt(java.time.OffsetDateTime)` was removed
* `withLocation(com.azure.core.management.Region)` was removed

#### `models.NamespaceResource` was modified

* `java.lang.String status()` -> `models.NamespaceStatus status()`
* `java.lang.String provisioningState()` -> `models.OperationProvisioningState provisioningState()`

### Features Added

* `models.OperationProperties` was added

* `models.PrivateLinkConnectionStatus` was added

* `models.PolicyKeyResource` was added

* `models.ReplicationRegion` was added

* `models.Availability` was added

* `models.RemotePrivateEndpointConnection` was added

* `models.NamespaceStatus` was added

* `models.OperationProvisioningState` was added

* `models.PrivateEndpointConnections` was added

* `models.NetworkAcls` was added

* `models.RegistrationResult` was added

* `models.BrowserCredential` was added

* `models.PrivateEndpointConnectionProperties` was added

* `models.FcmV1Credential` was added

* `models.PrivateLinkResourceListResult` was added

* `models.PrivateEndpointConnectionResource` was added

* `models.LogSpecification` was added

* `models.ZoneRedundancyPreference` was added

* `models.IpRule` was added

* `models.PrivateLinkResourceProperties` was added

* `models.ServiceSpecification` was added

* `models.RemotePrivateLinkServiceConnectionState` was added

* `models.XiaomiCredential` was added

* `models.PublicNetworkAccess` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpointConnectionResourceListResult` was added

* `models.MetricSpecification` was added

* `models.PublicInternetAuthorizationRule` was added

* `models.PolicyKeyType` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withRegion(java.lang.String)` was added
* `withExistingNotificationHub(java.lang.String,java.lang.String,java.lang.String)` was added
* `withRights(java.util.List)` was added
* `withPrimaryKey(java.lang.String)` was added
* `withSecondaryKey(java.lang.String)` was added
* `withRegion(com.azure.core.management.Region)` was added
* `withTags(java.util.Map)` was added

#### `models.NotificationHubResource` was modified

* `dailyMaxActiveDevices()` was added
* `fcmV1Credential()` was added
* `resourceGroupName()` was added
* `xiaomiCredential()` was added
* `systemData()` was added
* `debugSendWithResponse(com.azure.core.util.Context)` was added
* `browserCredential()` was added

#### `models.OperationDisplay` was modified

* `description()` was added

#### `models.NamespaceResource$Update` was modified

* `withProperties(fluent.models.NamespaceProperties)` was added

#### `models.DebugSendResponse` was modified

* `systemData()` was added

#### `models.NamespacePatchParameters` was modified

* `withProperties(fluent.models.NamespaceProperties)` was added
* `properties()` was added

#### `models.PnsCredentialsResource` was modified

* `browserCredential()` was added
* `systemData()` was added
* `xiaomiCredential()` was added
* `fcmV1Credential()` was added

#### `models.NotificationHubResource$Update` was modified

* `withBrowserCredential(models.BrowserCredential)` was added
* `withXiaomiCredential(models.XiaomiCredential)` was added
* `withFcmV1Credential(models.FcmV1Credential)` was added

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withRights(java.util.List)` was added
* `withSecondaryKey(java.lang.String)` was added
* `withTags(java.util.Map)` was added
* `withPrimaryKey(java.lang.String)` was added

#### `models.Namespaces` was modified

* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource)` was added
* `getPnsCredentials(java.lang.String,java.lang.String)` was added
* `createOrUpdateAuthorizationRuleWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.SharedAccessAuthorizationRuleResourceInner,com.azure.core.util.Context)` was added
* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource,com.azure.core.util.Context)` was added
* `createOrUpdateAuthorizationRule(java.lang.String,java.lang.String,java.lang.String,fluent.models.SharedAccessAuthorizationRuleResourceInner)` was added
* `getPnsCredentialsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.NotificationHubPatchParameters` was modified

* `fcmV1Credential()` was added
* `withFcmV1Credential(models.FcmV1Credential)` was added
* `browserCredential()` was added
* `dailyMaxActiveDevices()` was added
* `name()` was added
* `tags()` was added
* `withXiaomiCredential(models.XiaomiCredential)` was added
* `xiaomiCredential()` was added
* `withBrowserCredential(models.BrowserCredential)` was added
* `withName(java.lang.String)` was added

#### `models.WnsCredential` was modified

* `certificateKey()` was added
* `withCertificateKey(java.lang.String)` was added
* `withWnsCertificate(java.lang.String)` was added
* `wnsCertificate()` was added

#### `models.NotificationHubResource$Definition` was modified

* `withXiaomiCredential(models.XiaomiCredential)` was added
* `withBrowserCredential(models.BrowserCredential)` was added
* `withFcmV1Credential(models.FcmV1Credential)` was added

#### `models.Operation` was modified

* `properties()` was added
* `isDataAction()` was added

#### `NotificationHubsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `privateEndpointConnections()` was added

#### `models.NotificationHubs` was modified

* `getAuthorizationRuleById(java.lang.String)` was added
* `defineAuthorizationRule(java.lang.String)` was added
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `debugSendWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteAuthorizationRuleById(java.lang.String)` was added
* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource)` was added
* `getAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource,com.azure.core.util.Context)` was added
* `deleteAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.CheckAvailabilityResult` was modified

* `systemData()` was added

#### `models.SharedAccessAuthorizationRuleResource` was modified

* `regenerateKeysWithResponse(models.PolicyKeyResource,com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `regenerateKeys(models.PolicyKeyResource)` was added
* `systemData()` was added

#### `NotificationHubsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.NamespaceResource$Definition` was modified

* `withRegion(java.lang.String)` was added
* `withRegion(com.azure.core.management.Region)` was added
* `withZoneRedundancy(models.ZoneRedundancyPreference)` was added
* `withNetworkAcls(models.NetworkAcls)` was added
* `withPnsCredentials(fluent.models.PnsCredentials)` was added
* `withReplicationRegion(models.ReplicationRegion)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withStatus(models.NamespaceStatus)` was added
* `withProvisioningState(models.OperationProvisioningState)` was added

#### `models.NamespaceResource` was modified

* `systemData()` was added
* `publicNetworkAccess()` was added
* `zoneRedundancy()` was added
* `privateEndpointConnections()` was added
* `getPnsCredentials()` was added
* `getPnsCredentialsWithResponse(com.azure.core.util.Context)` was added
* `networkAcls()` was added
* `replicationRegion()` was added
* `resourceGroupName()` was added
* `pnsCredentials()` was added

## 1.0.0-beta.3 (2022-01-04)

- Azure Resource Manager NotificationHubs client library for Java. This package contains Microsoft Azure SDK for NotificationHubs Management SDK. Azure NotificationHub client. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SharedAccessAuthorizationRuleListResult` was modified

* `innerModel()` was removed
* `java.lang.String nextLink()` -> `java.lang.String nextLink()`
* `java.util.List value()` -> `java.util.List value()`

#### `models.Namespaces` was modified

* `models.SharedAccessAuthorizationRuleListResult listKeys(java.lang.String,java.lang.String,java.lang.String)` -> `models.ResourceListKeys listKeys(java.lang.String,java.lang.String,java.lang.String)`

#### `models.SharedAccessAuthorizationRuleResource` was modified

* `models.SharedAccessAuthorizationRuleListResult listKeys()` -> `models.ResourceListKeys listKeys()`

### Features Added

#### `models.SharedAccessAuthorizationRuleListResult` was modified

* `validate()` was added
* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added

## 1.0.0-beta.2 (2021-11-03)

- Azure Resource Manager NotificationHubs client library for Java. This package contains Microsoft Azure SDK for NotificationHubs Management SDK. Azure NotificationHub client. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SharedAccessAuthorizationRuleProperties` was removed

#### `models.SharedAccessAuthorizationRuleResource$DefinitionStages` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed in stage 2

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed

#### `models.SharedAccessAuthorizationRuleCreateOrUpdateParameters` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed
* `models.SharedAccessAuthorizationRuleProperties properties()` -> `fluent.models.SharedAccessAuthorizationRuleProperties properties()`

### Features Added

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was added

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was added

#### `NotificationHubsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.SharedAccessAuthorizationRuleCreateOrUpdateParameters` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was added

## 1.0.0-beta.1 (2021-04-20)

- Azure Resource Manager NotificationHubs client library for Java. This package contains Microsoft Azure SDK for NotificationHubs Management SDK. Azure NotificationHub client. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
