# Release History

## 1.1.0-beta.1 (2026-03-06)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package api-version 2025-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PrivateLinkResourceList` was removed

#### `models.CustomCertificateList` was removed

#### `models.SignalRUsageList` was removed

#### `models.CustomDomainList` was removed

#### `models.PrivateEndpointConnectionList` was removed

#### `models.SignalRResourceList` was removed

#### `models.SharedPrivateLinkResourceList` was removed

#### `models.OperationList` was removed

#### `models.ManagedIdentitySettings` was modified

* `validate()` was removed

#### `models.SignalRCorsSettings` was modified

* `validate()` was removed

#### `models.NameAvailabilityParameters` was modified

* `validate()` was removed

#### `models.ResourceLogConfiguration` was modified

* `validate()` was removed

#### `models.ResourceReference` was modified

* `validate()` was removed

#### `models.ServerlessSettings` was modified

* `validate()` was removed

#### `models.SignalRUsageName` was modified

* `SignalRUsageName()` was changed to private access
* `withValue(java.lang.String)` was removed
* `validate()` was removed
* `withLocalizedValue(java.lang.String)` was removed

#### `models.RegenerateKeyParameters` was modified

* `validate()` was removed

#### `models.SignalRFeature` was modified

* `validate()` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `withMetricSpecifications(java.util.List)` was removed
* `withLogSpecifications(java.util.List)` was removed
* `validate()` was removed

#### `models.UpstreamAuthSettings` was modified

* `validate()` was removed

#### `models.MetricSpecification` was modified

* `MetricSpecification()` was changed to private access
* `withAggregationType(java.lang.String)` was removed
* `withCategory(java.lang.String)` was removed
* `withFillGapWithZero(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withDimensions(java.util.List)` was removed
* `withUnit(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.LiveTraceConfiguration` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `Sku()` was changed to private access
* `validate()` was removed

#### `models.SkuCapacity` was modified

* `SkuCapacity()` was changed to private access
* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.UpstreamTemplate` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.Dimension` was modified

* `Dimension()` was changed to private access
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withToBeExportedForShoebox(java.lang.Boolean)` was removed
* `withInternalName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.ResourceLogCategory` was modified

* `validate()` was removed

#### `models.LiveTraceCategory` was modified

* `validate()` was removed

#### `models.ResourceSku` was modified

* `validate()` was removed

#### `models.NetworkAcl` was modified

* `validate()` was removed

#### `models.ManagedIdentity` was modified

* `validate()` was removed

#### `models.PrivateEndpointAcl` was modified

* `validate()` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed

#### `models.SignalRNetworkACLs` was modified

* `validate()` was removed

#### `models.UserAssignedIdentityProperty` was modified

* `validate()` was removed

#### `models.ShareablePrivateLinkResourceType` was modified

* `ShareablePrivateLinkResourceType()` was changed to private access
* `withName(java.lang.String)` was removed
* `validate()` was removed
* `withProperties(models.ShareablePrivateLinkResourceProperties)` was removed

#### `models.OperationProperties` was modified

* `OperationProperties()` was changed to private access
* `validate()` was removed
* `withServiceSpecification(models.ServiceSpecification)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withOperation(java.lang.String)` was removed

#### `models.ServerlessUpstreamSettings` was modified

* `validate()` was removed

#### `models.ShareablePrivateLinkResourceProperties` was modified

* `ShareablePrivateLinkResourceProperties()` was changed to private access
* `validate()` was removed
* `withGroupId(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.SignalRTlsSettings` was modified

* `validate()` was removed

### Features Added

* `models.IPRule` was added

* `models.ClientConnectionCountRuleDiscriminator` was added

* `models.ClientTrafficControlRuleDiscriminator` was added

* `models.SignalRReplicas` was added

* `models.SignalRReplicaSharedPrivateLinkResources` was added

* `models.TrafficThrottleByJwtCustomClaimRule` was added

* `models.TrafficThrottleByJwtSignatureRule` was added

* `models.ThrottleByUserIdRule` was added

* `models.Replica` was added

* `models.Replica$Update` was added

* `models.ClientTrafficControlRule` was added

* `models.ThrottleByJwtSignatureRule` was added

* `models.TrafficThrottleByUserIdRule` was added

* `models.ApplicationFirewallSettings` was added

* `models.Replica$DefinitionStages` was added

* `models.Replica$UpdateStages` was added

* `models.ThrottleByJwtCustomClaimRule` was added

* `models.Replica$Definition` was added

* `models.ClientConnectionCountRule` was added

* `models.RouteSettings` was added

#### `models.ServerlessSettings` was modified

* `withKeepAliveIntervalInSeconds(java.lang.Integer)` was added
* `keepAliveIntervalInSeconds()` was added

#### `models.SignalRs` was modified

* `listReplicaSkus(java.lang.String,java.lang.String,java.lang.String)` was added
* `listReplicaSkusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SharedPrivateLinkResource` was modified

* `fqdns()` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.SharedPrivateLinkResource$Definition` was modified

* `withFqdns(java.util.List)` was added

#### `models.SignalRNetworkACLs` was modified

* `ipRules()` was added
* `withIpRules(java.util.List)` was added

#### `models.SignalRResource$Definition` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRouteSettings(models.RouteSettings)` was added
* `withApplicationFirewall(models.ApplicationFirewallSettings)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

#### `models.SignalRResource` was modified

* `applicationFirewall()` was added
* `resourceStopped()` was added
* `regionEndpointEnabled()` was added
* `routeSettings()` was added

#### `models.SignalRResource$Update` was modified

* `withApplicationFirewall(models.ApplicationFirewallSettings)` was added
* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added
* `withRouteSettings(models.RouteSettings)` was added

#### `models.SharedPrivateLinkResource$Update` was modified

* `withFqdns(java.util.List)` was added

#### `SignalRManager` was modified

* `signalRReplicaSharedPrivateLinkResources()` was added
* `signalRReplicas()` was added

## 1.0.0 (2024-12-24)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2023-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager SignalR client library for Java.

## 1.0.0-beta.9 (2024-10-31)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2023-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ManagedIdentitySettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SignalRCorsSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NameAvailabilityParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomCertificateList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceLogConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SignalRUsageList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerlessSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SignalRUsageName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegenerateKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SignalRFeature` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpstreamAuthSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MetricSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomDomainList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LiveTraceConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuCapacity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpstreamTemplate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Dimension` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceLogCategory` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnectionList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IpRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LiveTraceCategory` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceSku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkAcl` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SignalRResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointAcl` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SharedPrivateLinkResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SignalRNetworkACLs` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentityProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ShareablePrivateLinkResourceType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReplicaList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerlessUpstreamSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ShareablePrivateLinkResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SignalRTlsSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.8 (2023-10-23)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2023-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.IpRule` was added

#### `models.CustomDomain` was modified

* `systemData()` was added

#### `models.SharedPrivateLinkResource` was modified

* `systemData()` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.Replica` was modified

* `resourceStopped()` was added
* `regionEndpointEnabled()` was added

#### `models.Replica$Update` was modified

* `withRegionEndpointEnabled(java.lang.String)` was added
* `withResourceStopped(java.lang.String)` was added

#### `models.SignalRNetworkACLs` was modified

* `ipRules()` was added
* `withIpRules(java.util.List)` was added

#### `models.SignalRResource$Definition` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

#### `models.CustomCertificate` was modified

* `systemData()` was added

#### `models.SignalRResource` was modified

* `regionEndpointEnabled()` was added
* `resourceStopped()` was added

#### `models.SignalRResource$Update` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

#### `models.Replica$Definition` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

## 1.0.0-beta.7 (2023-09-14)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2023-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CustomDomain` was modified

* `systemData()` was removed

#### `models.SharedPrivateLinkResource` was modified

* `systemData()` was removed

#### `models.CustomCertificate` was modified

* `systemData()` was removed

### Features Added

* `models.SignalRReplicas` was added

* `models.Replica` was added

* `models.Replica$Update` was added

* `models.ReplicaList` was added

* `models.Replica$DefinitionStages` was added

* `models.Replica$UpdateStages` was added

* `models.Replica$Definition` was added

#### `models.SignalRs` was modified

* `listReplicaSkusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listReplicaSkus(java.lang.String,java.lang.String,java.lang.String)` was added

#### `SignalRManager` was modified

* `signalRReplicas()` was added

## 1.0.0-beta.6 (2023-03-20)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2023-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.5 (2022-11-21)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2022-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ServerlessSettings` was added

#### `models.CustomDomain` was modified

* `resourceGroupName()` was added

#### `models.SharedPrivateLinkResource` was modified

* `resourceGroupName()` was added

#### `models.SignalRResource$Definition` was modified

* `withServerless(models.ServerlessSettings)` was added

#### `models.CustomCertificate` was modified

* `resourceGroupName()` was added

#### `models.SignalRResource` was modified

* `resourceGroupName()` was added
* `serverless()` was added

#### `models.SignalRResource$Update` was modified

* `withServerless(models.ServerlessSettings)` was added

## 1.0.0-beta.4 (2022-04-11)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2022-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CustomCertificate$DefinitionStages` was added

* `models.CustomCertificateList` was added

* `models.ResourceReference` was added

* `models.CustomDomain$Definition` was added

* `models.SignalRCustomCertificates` was added

* `models.CustomDomain` was added

* `models.CustomCertificate$Definition` was added

* `models.CustomCertificate$UpdateStages` was added

* `models.CustomDomainList` was added

* `models.LiveTraceConfiguration` was added

* `models.SignalRCustomDomains` was added

* `models.CustomCertificate$Update` was added

* `models.LiveTraceCategory` was added

* `models.CustomDomain$Update` was added

* `models.CustomDomain$UpdateStages` was added

* `models.CustomDomain$DefinitionStages` was added

* `models.CustomCertificate` was added

#### `SignalRManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.SignalRResource$Definition` was modified

* `withLiveTraceConfiguration(models.LiveTraceConfiguration)` was added

#### `models.SignalRResource` was modified

* `liveTraceConfiguration()` was added

#### `models.SignalRResource$Update` was modified

* `withLiveTraceConfiguration(models.LiveTraceConfiguration)` was added

#### `SignalRManager` was modified

* `signalRCustomDomains()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `signalRCustomCertificates()` was added

## 1.0.0-beta.3 (2021-11-11)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ResourceLogConfiguration` was added

* `models.Sku` was added

* `models.SkuCapacity` was added

* `models.ResourceLogCategory` was added

* `models.SkuList` was added

* `models.ScaleType` was added

#### `models.SignalRs` was modified

* `listSkusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSkus(java.lang.String,java.lang.String)` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `models.SignalRResource$Definition` was modified

* `withResourceLogConfiguration(models.ResourceLogConfiguration)` was added

#### `models.SignalRResource` was modified

* `hostnamePrefix()` was added
* `resourceLogConfiguration()` was added

#### `models.SignalRResource$Update` was modified

* `withResourceLogConfiguration(models.ResourceLogConfiguration)` was added

## 1.0.0-beta.2 (2021-07-09)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2021-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `SignalRManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.SignalRResource$Definition` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withPublicNetworkAccess(java.lang.String)` was added
* `withDisableAadAuth(java.lang.Boolean)` was added

#### `models.SignalRResource` was modified

* `disableLocalAuth()` was added
* `publicNetworkAccess()` was added
* `disableAadAuth()` was added

#### `models.SignalRResource$Update` was modified

* `withDisableAadAuth(java.lang.Boolean)` was added
* `withPublicNetworkAccess(java.lang.String)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

## 1.0.0-beta.1 (2021-04-14)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2021-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
