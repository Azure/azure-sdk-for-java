# Release History

## 1.1.0 (2026-03-06)

- Azure Resource Manager Peering client library for Java. This package contains Microsoft Azure SDK for Peering Management SDK. Peering Client. Package api-version 2025-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PeeringListResult` was removed

#### `models.PeeringRegisteredAsnListResult` was removed

#### `models.PeeringRegisteredPrefixListResult` was removed

#### `models.PeeringServiceProviderListResult` was removed

#### `models.Enum0` was removed

#### `models.PeeringLocationListResult` was removed

#### `models.CdnPeeringPrefixListResult` was removed

#### `models.PeeringReceivedRouteListResult` was removed

#### `models.PeeringServiceListResult` was removed

#### `models.PeeringServicePrefixListResult` was removed

#### `models.PeerAsnListResult` was removed

#### `models.PeeringServiceLocationListResult` was removed

#### `models.PeeringServiceCountryListResult` was removed

#### `models.OperationListResult` was removed

#### `models.ContactDetail` was modified

* `validate()` was removed

#### `models.OperationDisplayInfo` was modified

* `OperationDisplayInfo()` was changed to private access
* `validate()` was removed

#### `models.PeeringLocationPropertiesExchange` was modified

* `PeeringLocationPropertiesExchange()` was changed to private access
* `withPeeringFacilities(java.util.List)` was removed
* `validate()` was removed

#### `models.ResourceTags` was modified

* `validate()` was removed

#### `models.PeeringServiceSku` was modified

* `validate()` was removed

#### `models.PeeringPropertiesDirect` was modified

* `validate()` was removed

#### `models.PeerAsn$Update` was modified

* `withValidationState(models.ValidationState)` was removed

#### `models.BgpSession` was modified

* `validate()` was removed

#### `models.PeeringPropertiesExchange` was modified

* `validate()` was removed

#### `models.PeeringBandwidthOffer` was modified

* `PeeringBandwidthOffer()` was changed to private access
* `validate()` was removed
* `withValueInMbps(java.lang.Integer)` was removed
* `withOfferName(java.lang.String)` was removed

#### `models.LegacyPeerings` was modified

* `list(java.lang.String,models.LegacyPeeringsKind,java.lang.Integer,com.azure.core.util.Context)` was removed

#### `models.ExchangePeeringFacility` was modified

* `ExchangePeeringFacility()` was changed to private access
* `withFacilityIPv4Prefix(java.lang.String)` was removed
* `withMicrosoftIPv6Address(java.lang.String)` was removed
* `withPeeringDBFacilityId(java.lang.Integer)` was removed
* `withFacilityIPv6Prefix(java.lang.String)` was removed
* `withBandwidthInMbps(java.lang.Integer)` was removed
* `withMicrosoftIPv4Address(java.lang.String)` was removed
* `withPeeringDBFacilityLink(java.lang.String)` was removed
* `validate()` was removed
* `withExchangeName(java.lang.String)` was removed

#### `models.CheckServiceProviderAvailabilityInput` was modified

* `validate()` was removed

#### `models.ResourceProviders` was modified

* `models.Enum0 checkServiceProviderAvailability(models.CheckServiceProviderAvailabilityInput)` -> `models.CheckServiceProviderAvailabilityResponse checkServiceProviderAvailability(models.CheckServiceProviderAvailabilityInput)`

#### `models.PeeringSku` was modified

* `withFamily(models.Family)` was removed
* `withSize(models.Size)` was removed
* `validate()` was removed
* `withTier(models.Tier)` was removed

#### `models.PeeringServicePrefixEvent` was modified

* `PeeringServicePrefixEvent()` was changed to private access
* `validate()` was removed

#### `models.PeerAsn$Definition` was modified

* `withValidationState(models.ValidationState)` was removed

#### `models.ExchangeConnection` was modified

* `validate()` was removed

#### `models.DirectConnection` was modified

* `validate()` was removed

#### `models.DirectPeeringFacility` was modified

* `DirectPeeringFacility()` was changed to private access
* `withDirectPeeringType(models.DirectPeeringType)` was removed
* `validate()` was removed
* `withPeeringDBFacilityId(java.lang.Integer)` was removed
* `withAddress(java.lang.String)` was removed
* `withPeeringDBFacilityLink(java.lang.String)` was removed

#### `models.PeeringLocationPropertiesDirect` was modified

* `PeeringLocationPropertiesDirect()` was changed to private access
* `withBandwidthOffers(java.util.List)` was removed
* `validate()` was removed
* `withPeeringFacilities(java.util.List)` was removed

### Features Added

* `models.ServiceSpecification` was added

* `models.Protocol` was added

* `models.ConnectionMonitorTest$Update` was added

* `models.LogAnalyticsWorkspaceProperties` was added

* `models.RpUnbilledPrefixes` was added

* `models.ConnectionMonitorTest` was added

* `models.ConnectionMonitorTest$UpdateStages` was added

* `models.ConnectivityProbe` was added

* `models.Command` was added

* `models.ConnectionMonitorTest$DefinitionStages` was added

* `models.LookingGlassOutput` was added

* `models.CheckServiceProviderAvailabilityResponse` was added

* `models.LookingGlass` was added

* `models.ConnectionMonitorTest$Definition` was added

* `models.LookingGlassCommand` was added

* `models.ConnectionMonitorTests` was added

* `models.RpUnbilledPrefix` was added

* `models.LookingGlassSourceType` was added

* `models.MetricDimension` was added

* `models.MetricSpecification` was added

#### `models.PeerAsn` was modified

* `systemData()` was added

#### `models.Operation` was modified

* `serviceSpecification()` was added

#### `models.ProvisioningState` was modified

* `CANCELED` was added

#### `models.PeeringServicePrefix` was modified

* `systemData()` was added

#### `models.PeeringServiceCountry` was modified

* `systemData()` was added

#### `models.PeeringServiceLocation` was modified

* `systemData()` was added

#### `models.DirectPeeringType` was modified

* `EDGE_ZONE_FOR_OPERATORS` was added
* `PEER_PROP` was added

#### `models.PeeringServiceProvider` was modified

* `systemData()` was added

#### `models.CdnPeeringPrefix` was modified

* `systemData()` was added

#### `models.PeeringRegisteredPrefix` was modified

* `validate()` was added
* `validateWithResponse(com.azure.core.util.Context)` was added
* `systemData()` was added

#### `models.Peering` was modified

* `systemData()` was added
* `connectivityProbes()` was added

#### `models.LegacyPeerings` was modified

* `list(java.lang.String,models.LegacyPeeringsKind,java.lang.Integer,models.DirectPeeringType,com.azure.core.util.Context)` was added

#### `models.PeeringServices` was modified

* `initializeConnectionMonitorWithResponse(com.azure.core.util.Context)` was added
* `initializeConnectionMonitor()` was added

#### `models.PeeringLocationsDirectPeeringType` was modified

* `EDGE_ZONE_FOR_OPERATORS` was added
* `PEER_PROP` was added

#### `models.PeeringService$Definition` was modified

* `withLogAnalyticsWorkspaceProperties(models.LogAnalyticsWorkspaceProperties)` was added

#### `models.PeeringService` was modified

* `logAnalyticsWorkspaceProperties()` was added
* `systemData()` was added

#### `PeeringManager` was modified

* `lookingGlass()` was added
* `connectionMonitorTests()` was added
* `rpUnbilledPrefixes()` was added

#### `models.Peering$Definition` was modified

* `withConnectivityProbes(java.util.List)` was added

#### `models.RegisteredPrefixes` was modified

* `validate(java.lang.String,java.lang.String,java.lang.String)` was added
* `validateWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ConnectionState` was modified

* `EXTERNAL_BLOCKER` was added
* `TYPE_CHANGE_IN_PROGRESS` was added
* `TYPE_CHANGE_REQUESTED` was added

#### `models.PeeringRegisteredAsn` was modified

* `systemData()` was added

#### `models.PeeringLocation` was modified

* `systemData()` was added

## 1.0.0 (2024-12-25)

- Azure Resource Manager Peering client library for Java. This package contains Microsoft Azure SDK for Peering Management SDK. Peering Client. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager Peering client library for Java.

## 1.0.0-beta.3 (2024-10-17)

- Azure Resource Manager Peering client library for Java. This package contains Microsoft Azure SDK for Peering Management SDK. Peering Client. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.PeeringListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContactDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeeringRegisteredAsnListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplayInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringLocationPropertiesExchange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceTags` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeeringServiceSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringPropertiesDirect` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringRegisteredPrefixListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeeringServiceProviderListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BgpSession` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringLocationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringPropertiesExchange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CdnPeeringPrefixListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringReceivedRouteListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeeringServiceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeeringBandwidthOffer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringServicePrefixListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExchangePeeringFacility` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CheckServiceProviderAvailabilityInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringSku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeeringServicePrefixEvent` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PeerAsnListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExchangeConnection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringServiceLocationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DirectConnection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringServiceCountryListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DirectPeeringFacility` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PeeringLocationPropertiesDirect` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager Peering client library for Java. This package contains Microsoft Azure SDK for Peering Management SDK. Peering Client. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Peerings` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PeeringServices` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.PeeringServicePrefix` was modified

* `resourceGroupName()` was added

#### `models.PeeringRegisteredPrefix` was modified

* `resourceGroupName()` was added

#### `models.Peering` was modified

* `resourceGroupName()` was added

#### `models.Peerings` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PeeringServices` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `PeeringManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.PeeringService` was modified

* `resourceGroupName()` was added

#### `PeeringManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.PeeringRegisteredAsn` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-19)

- Azure Resource Manager Peering client library for Java. This package contains Microsoft Azure SDK for Peering Management SDK. Peering Client. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
