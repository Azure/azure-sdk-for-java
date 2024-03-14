# Release History

## 1.0.0-beta.4 (2024-03-14)

- Azure Resource Manager NotificationHubs client library for Java. This package contains Microsoft Azure SDK for NotificationHubs Management SDK. Microsoft Notification Hubs Resource Provider REST API. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.NamespaceCreateOrUpdateParameters` was removed

* `models.PolicykeyResource` was removed

* `models.SharedAccessAuthorizationRuleCreateOrUpdateParameters` was removed

* `models.NotificationHubCreateOrUpdateParameters` was removed

#### `models.NamespaceResource$DefinitionStages` was modified

* `withLocation(com.azure.core.management.Region)` was removed in stage 1
* `withLocation(java.lang.String)` was removed in stage 1
* Stage 3 was added

#### `models.SharedAccessAuthorizationRuleResource$DefinitionStages` was modified

* `withExistingNamespace(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withExistingNamespace(java.lang.String,java.lang.String)` was removed
* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was removed

#### `models.BaiduCredential` was modified

* `baiduEndPoint()` was removed
* `baiduSecretKey()` was removed
* `withBaiduApiKey(java.lang.String)` was removed
* `withBaiduSecretKey(java.lang.String)` was removed
* `baiduApiKey()` was removed
* `withBaiduEndPoint(java.lang.String)` was removed

#### `models.GcmCredential` was modified

* `gcmEndpoint()` was removed
* `googleApiKey()` was removed
* `withGoogleApiKey(java.lang.String)` was removed
* `withGcmEndpoint(java.lang.String)` was removed

#### `models.NotificationHubResource` was modified

* `baiduCredential()` was removed
* `gcmCredential()` was removed
* `mpnsCredential()` was removed
* `admCredential()` was removed
* `namePropertiesName()` was removed
* `authorizationRules()` was removed
* `registrationTtl()` was removed
* `apnsCredential()` was removed
* `wnsCredential()` was removed
* `debugSendWithResponse(java.lang.Object,com.azure.core.util.Context)` was removed

#### `models.SharedAccessAuthorizationRuleListResult` was modified

* `withValue(java.util.List)` was removed
* `withNextLink(java.lang.String)` was removed

#### `models.MpnsCredential` was modified

* `thumbprint()` was removed
* `certificateKey()` was removed
* `mpnsCertificate()` was removed
* `withThumbprint(java.lang.String)` was removed
* `withCertificateKey(java.lang.String)` was removed
* `withMpnsCertificate(java.lang.String)` was removed

#### `models.DebugSendResponse` was modified

* `results()` was removed
* `sku()` was removed
* `failure()` was removed
* `success()` was removed

#### `models.PnsCredentialsResource` was modified

* `admCredential()` was removed
* `sku()` was removed
* `apnsCredential()` was removed
* `baiduCredential()` was removed
* `mpnsCredential()` was removed
* `wnsCredential()` was removed
* `gcmCredential()` was removed

#### `models.NotificationHubResource$Update` was modified

* `withAuthorizationRules(java.util.List)` was removed
* `withAdmCredential(models.AdmCredential)` was removed
* `withMpnsCredential(models.MpnsCredential)` was removed
* `withApnsCredential(models.ApnsCredential)` was removed
* `withBaiduCredential(models.BaiduCredential)` was removed
* `withWnsCredential(models.WnsCredential)` was removed
* `withGcmCredential(models.GcmCredential)` was removed
* `withRegistrationTtl(java.lang.String)` was removed
* `withNamePropertiesName(java.lang.String)` was removed

#### `models.NotificationHubListResult` was modified

* `withNextLink(java.lang.String)` was removed
* `withValue(java.util.List)` was removed

#### `models.ApnsCredential` was modified

* `thumbprint()` was removed
* `appId()` was removed
* `token()` was removed
* `withKeyId(java.lang.String)` was removed
* `endpoint()` was removed
* `withAppId(java.lang.String)` was removed
* `withEndpoint(java.lang.String)` was removed
* `apnsCertificate()` was removed
* `withAppName(java.lang.String)` was removed
* `appName()` was removed
* `withCertificateKey(java.lang.String)` was removed
* `withThumbprint(java.lang.String)` was removed
* `withApnsCertificate(java.lang.String)` was removed
* `withToken(java.lang.String)` was removed
* `keyId()` was removed
* `certificateKey()` was removed

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was removed

#### `models.Namespaces` was modified

* `deleteAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `defineAuthorizationRule(java.lang.String)` was removed
* `deleteAuthorizationRuleById(java.lang.String)` was removed
* `getAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getAuthorizationRuleById(java.lang.String)` was removed
* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource,com.azure.core.util.Context)` was removed
* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource)` was removed

#### `models.AccessRights` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.AccessRights[] values()` -> `java.util.Collection values()`

#### `models.NotificationHubPatchParameters` was modified

* `namePropertiesName()` was removed
* `withAdmCredential(models.AdmCredential)` was removed
* `withLocation(java.lang.String)` was removed
* `withMpnsCredential(models.MpnsCredential)` was removed
* `authorizationRules()` was removed
* `gcmCredential()` was removed
* `withApnsCredential(models.ApnsCredential)` was removed
* `mpnsCredential()` was removed
* `baiduCredential()` was removed
* `registrationTtl()` was removed
* `apnsCredential()` was removed
* `withGcmCredential(models.GcmCredential)` was removed
* `admCredential()` was removed
* `wnsCredential()` was removed
* `withWnsCredential(models.WnsCredential)` was removed
* `withNamePropertiesName(java.lang.String)` was removed
* `withAuthorizationRules(java.util.List)` was removed
* `withBaiduCredential(models.BaiduCredential)` was removed
* `withTags(java.util.Map)` was removed
* `withRegistrationTtl(java.lang.String)` was removed

#### `models.WnsCredential` was modified

* `packageSid()` was removed
* `withSecretKey(java.lang.String)` was removed
* `withPackageSid(java.lang.String)` was removed
* `secretKey()` was removed
* `withWindowsLiveEndpoint(java.lang.String)` was removed
* `windowsLiveEndpoint()` was removed

#### `models.NotificationHubResource$Definition` was modified

* `withBaiduCredential(models.BaiduCredential)` was removed
* `withRegistrationTtl(java.lang.String)` was removed
* `withAdmCredential(models.AdmCredential)` was removed
* `withMpnsCredential(models.MpnsCredential)` was removed
* `withNamePropertiesName(java.lang.String)` was removed
* `withWnsCredential(models.WnsCredential)` was removed
* `withAuthorizationRules(java.util.List)` was removed
* `withApnsCredential(models.ApnsCredential)` was removed
* `withGcmCredential(models.GcmCredential)` was removed

#### `NotificationHubsManager` was modified

* `fluent.NotificationHubsManagementClient serviceClient()` -> `fluent.NotificationHubsRPClient serviceClient()`

#### `models.NotificationHubs` was modified

* `createOrUpdateAuthorizationRule(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SharedAccessAuthorizationRuleCreateOrUpdateParameters)` was removed
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource,com.azure.core.util.Context)` was removed
* `createOrUpdateAuthorizationRuleWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SharedAccessAuthorizationRuleCreateOrUpdateParameters,com.azure.core.util.Context)` was removed
* `debugSendWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Object,com.azure.core.util.Context)` was removed
* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicykeyResource)` was removed

#### `models.NamespaceListResult` was modified

* `withValue(java.util.List)` was removed
* `withNextLink(java.lang.String)` was removed

#### `models.AdmCredential` was modified

* `authTokenUrl()` was removed
* `clientSecret()` was removed
* `withClientId(java.lang.String)` was removed
* `clientId()` was removed
* `withAuthTokenUrl(java.lang.String)` was removed
* `withClientSecret(java.lang.String)` was removed

#### `models.NamespaceType` was modified

* `toString()` was removed
* `models.NamespaceType[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.SharedAccessAuthorizationRuleResource` was modified

* `createdTime()` was removed
* `claimType()` was removed
* `sku()` was removed
* `keyName()` was removed
* `claimValue()` was removed
* `revision()` was removed
* `rights()` was removed
* `secondaryKey()` was removed
* `modifiedTime()` was removed
* `regenerateKeysWithResponse(models.PolicykeyResource,com.azure.core.util.Context)` was removed
* `regenerateKeys(models.PolicykeyResource)` was removed
* `primaryKey()` was removed

#### `models.NamespaceResource$Definition` was modified

* `withCritical(java.lang.Boolean)` was removed
* `withEnabled(java.lang.Boolean)` was removed
* `withScaleUnit(java.lang.String)` was removed
* `withLocation(com.azure.core.management.Region)` was removed
* `withServiceBusEndpoint(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withProvisioningState(java.lang.String)` was removed
* `withRegion(java.lang.String)` was removed
* `withSubscriptionId(java.lang.String)` was removed
* `withUpdatedAt(java.time.OffsetDateTime)` was removed
* `withCreatedAt(java.time.OffsetDateTime)` was removed
* `withLocation(java.lang.String)` was removed
* `withNamespaceType(models.NamespaceType)` was removed
* `withNamePropertiesName(java.lang.String)` was removed
* `withDataCenter(java.lang.String)` was removed

#### `models.NamespaceResource` was modified

* `updatedAt()` was removed
* `createdAt()` was removed
* `enabled()` was removed
* `metricId()` was removed
* `dataCenter()` was removed
* `critical()` was removed
* `java.lang.String region()` -> `com.azure.core.management.Region region()`
* `namespaceType()` was removed
* `status()` was removed
* `scaleUnit()` was removed
* `namePropertiesName()` was removed
* `serviceBusEndpoint()` was removed
* `provisioningState()` was removed
* `subscriptionId()` was removed

### Features Added

* `models.OperationProperties` was added

* `models.PrivateLinkConnectionStatus` was added

* `models.PolicyKeyResource` was added

* `models.ReplicationRegion` was added

* `models.XiaomiCredentialProperties` was added

* `models.Availability` was added

* `models.RemotePrivateEndpointConnection` was added

* `models.GcmCredentialProperties` was added

* `models.NamespaceStatus` was added

* `models.OperationProvisioningState` was added

* `models.PrivateEndpointConnections` was added

* `models.NetworkAcls` was added

* `models.RegistrationResult` was added

* `models.BrowserCredential` was added

* `models.PrivateEndpointConnectionProperties` was added

* `models.FcmV1Credential` was added

* `models.ApnsCredentialProperties` was added

* `models.NamespaceProperties` was added

* `models.PrivateLinkResourceListResult` was added

* `models.PrivateEndpointConnectionResource` was added

* `models.LogSpecification` was added

* `models.ZoneRedundancyPreference` was added

* `models.FcmV1CredentialProperties` was added

* `models.IpRule` was added

* `models.PnsCredentials` was added

* `models.PrivateLinkResourceProperties` was added

* `models.SharedAccessAuthorizationRuleProperties` was added

* `models.NotificationHubProperties` was added

* `models.ServiceSpecification` was added

* `models.RemotePrivateLinkServiceConnectionState` was added

* `models.WnsCredentialProperties` was added

* `models.XiaomiCredential` was added

* `models.DebugSendResult` was added

* `models.AdmCredentialProperties` was added

* `models.MpnsCredentialProperties` was added

* `models.BaiduCredentialProperties` was added

* `models.PublicNetworkAccess` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpointConnectionResourceListResult` was added

* `models.MetricSpecification` was added

* `models.BrowserCredentialProperties` was added

* `models.PublicInternetAuthorizationRule` was added

* `models.PolicyKeyType` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withRegion(java.lang.String)` was added
* `withRegion(com.azure.core.management.Region)` was added
* `withExistingNotificationHub(java.lang.String,java.lang.String,java.lang.String)` was added
* `withTags(java.util.Map)` was added
* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was added

#### `models.BaiduCredential` was modified

* `withProperties(models.BaiduCredentialProperties)` was added
* `properties()` was added

#### `models.GcmCredential` was modified

* `withProperties(models.GcmCredentialProperties)` was added
* `properties()` was added

#### `models.NotificationHubResource` was modified

* `properties()` was added
* `debugSendWithResponse(com.azure.core.util.Context)` was added
* `systemData()` was added
* `resourceGroupName()` was added

#### `models.OperationDisplay` was modified

* `description()` was added

#### `models.MpnsCredential` was modified

* `properties()` was added
* `withProperties(models.MpnsCredentialProperties)` was added

#### `models.NamespaceResource$Update` was modified

* `withProperties(models.NamespaceProperties)` was added

#### `models.DebugSendResponse` was modified

* `systemData()` was added
* `properties()` was added

#### `models.NamespacePatchParameters` was modified

* `withProperties(models.NamespaceProperties)` was added
* `properties()` was added

#### `models.PnsCredentialsResource` was modified

* `systemData()` was added
* `properties()` was added

#### `models.NotificationHubResource$Update` was modified

* `withProperties(models.NotificationHubProperties)` was added

#### `models.ApnsCredential` was modified

* `withProperties(models.ApnsCredentialProperties)` was added
* `properties()` was added

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was added
* `withTags(java.util.Map)` was added

#### `models.Namespaces` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource,com.azure.core.util.Context)` was added
* `getPnsCredentials(java.lang.String,java.lang.String)` was added
* `getPnsCredentialsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdateAuthorizationRule(java.lang.String,java.lang.String,java.lang.String,fluent.models.SharedAccessAuthorizationRuleResourceInner)` was added
* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource)` was added
* `list(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `createOrUpdateAuthorizationRuleWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.SharedAccessAuthorizationRuleResourceInner,com.azure.core.util.Context)` was added

#### `models.NotificationHubPatchParameters` was modified

* `properties()` was added
* `withProperties(models.NotificationHubProperties)` was added
* `tags()` was added

#### `models.WnsCredential` was modified

* `properties()` was added
* `withProperties(models.WnsCredentialProperties)` was added

#### `models.NotificationHubResource$Definition` was modified

* `withProperties(models.NotificationHubProperties)` was added

#### `models.Operation` was modified

* `isDataAction()` was added
* `properties()` was added

#### `NotificationHubsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `privateEndpointConnections()` was added

#### `models.NotificationHubs` was modified

* `deleteAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getAuthorizationRuleById(java.lang.String)` was added
* `regenerateKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource,com.azure.core.util.Context)` was added
* `defineAuthorizationRule(java.lang.String)` was added
* `regenerateKeys(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PolicyKeyResource)` was added
* `deleteAuthorizationRuleById(java.lang.String)` was added
* `debugSendWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `getAuthorizationRuleByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.CheckAvailabilityResult` was modified

* `systemData()` was added

#### `models.AdmCredential` was modified

* `withProperties(models.AdmCredentialProperties)` was added
* `properties()` was added

#### `models.SharedAccessAuthorizationRuleResource` was modified

* `regenerateKeysWithResponse(models.PolicyKeyResource,com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `systemData()` was added
* `regenerateKeys(models.PolicyKeyResource)` was added
* `properties()` was added

#### `NotificationHubsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.NamespaceResource$Definition` was modified

* `withProperties(models.NamespaceProperties)` was added
* `withRegion(com.azure.core.management.Region)` was added
* `withRegion(java.lang.String)` was added

#### `models.NamespaceResource` was modified

* `resourceGroupName()` was added
* `getPnsCredentials()` was added
* `systemData()` was added
* `properties()` was added
* `regionName()` was added
* `getPnsCredentialsWithResponse(com.azure.core.util.Context)` was added

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
