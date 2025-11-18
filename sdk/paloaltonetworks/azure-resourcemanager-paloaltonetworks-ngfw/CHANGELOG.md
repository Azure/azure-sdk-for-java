# Release History

## 1.4.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.3.0 (2025-10-30)

- Azure Resource Manager PaloAltoNetworks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAltoNetworks Ngfw Management SDK.  Package api-version 2025-10-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CertificateObjectGlobalRulestackResourceListResult` was removed

#### `models.FqdnListGlobalRulestackResourceListResult` was removed

#### `models.GlobalRulestackResourceListResult` was removed

#### `models.PrefixListResourceListResult` was removed

#### `models.FirewallStatusResourceListResult` was removed

#### `models.LocalRulestackResourceListResult` was removed

#### `models.OperationListResult` was removed

#### `models.FirewallResourceListResult` was removed

#### `models.PreRulesResourceListResult` was removed

#### `models.PrefixListGlobalRulestackResourceListResult` was removed

#### `models.CertificateObjectLocalRulestackResourceListResult` was removed

#### `models.FqdnListLocalRulestackResourceListResult` was removed

#### `models.PostRulesResourceListResult` was removed

#### `models.LocalRulesResourceListResult` was removed

#### `models.LocalRulestacks` was modified

* `models.CountriesResponse listCountries(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listCountries(java.lang.String,java.lang.String)`
* `listAppIdsWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `models.ListAppIdResponse listAppIds(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listAppIds(java.lang.String,java.lang.String)`
* `models.PredefinedUrlCategoriesResponse listPredefinedUrlCategories(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listPredefinedUrlCategories(java.lang.String,java.lang.String)`
* `listCountriesWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `listPredefinedUrlCategoriesWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed

#### `models.MarketplaceDetails` was modified

* `validate()` was removed

#### `models.PredefinedUrlCategory` was modified

* `PredefinedUrlCategory()` was removed
* `java.lang.String name()` -> `java.lang.String name()`
* `withAction(java.lang.String)` was removed
* `java.lang.String action()` -> `java.lang.String action()`
* `withName(java.lang.String)` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `validate()` was removed

#### `models.AdvSecurityObjectModel` was modified

* `AdvSecurityObjectModel()` was changed to private access
* `withEntry(java.util.List)` was removed
* `validate()` was removed
* `withType(java.lang.String)` was removed

#### `models.IpAddressSpace` was modified

* `validate()` was removed

#### `models.GlobalRulestackResourceUpdateProperties` was modified

* `validate()` was removed

#### `models.AzureResourceManagerManagedIdentityProperties` was modified

* `validate()` was removed

#### `models.MonitorLog` was modified

* `validate()` was removed

#### `models.DnsSettings` was modified

* `validate()` was removed

#### `models.FirewallResourceUpdate` was modified

* `validate()` was removed

#### `models.NameDescriptionObject` was modified

* `NameDescriptionObject()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed

#### `models.AppSeenInfo` was modified

* `AppSeenInfo()` was changed to private access
* `withCategory(java.lang.String)` was removed
* `withRisk(java.lang.String)` was removed
* `withStandardPorts(java.lang.String)` was removed
* `withTitle(java.lang.String)` was removed
* `withSubCategory(java.lang.String)` was removed
* `withTag(java.lang.String)` was removed
* `validate()` was removed
* `withTechnology(java.lang.String)` was removed

#### `models.PlanData` was modified

* `validate()` was removed

#### `models.EndpointConfiguration` was modified

* `validate()` was removed

#### `models.Category` was modified

* `validate()` was removed

#### `PaloAltoNetworksNgfwManager` was modified

* `fluent.PaloAltoNetworksCloudngfw serviceClient()` -> `fluent.PaloAltoNetworksNgfwManagementClient serviceClient()`

#### `models.DestinationAddr` was modified

* `validate()` was removed

#### `models.IpAddress` was modified

* `validate()` was removed

#### `models.FrontendSetting` was modified

* `validate()` was removed

#### `models.StorageAccount` was modified

* `validate()` was removed

#### `models.LocalRulestackResourceUpdate` was modified

* `validate()` was removed

#### `models.SecurityServices` was modified

* `validate()` was removed

#### `models.SecurityServicesTypeList` was modified

* `SecurityServicesTypeList()` was changed to private access
* `withEntry(java.util.List)` was removed
* `validate()` was removed
* `withType(java.lang.String)` was removed

#### `models.RulestackDetails` was modified

* `validate()` was removed

#### `models.VwanConfiguration` was modified

* `validate()` was removed

#### `models.LocalRulestackResourceUpdateProperties` was modified

* `validate()` was removed

#### `models.EventHub` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.LogDestination` was modified

* `validate()` was removed

#### `models.GlobalRulestackResourceUpdate` was modified

* `validate()` was removed

#### `models.LocalRulestackResource` was modified

* `listPredefinedUrlCategoriesWithResponse(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `listCountriesWithResponse(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `models.CountriesResponse listCountries()` -> `com.azure.core.http.rest.PagedIterable listCountries()`
* `listAppIdsWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `models.ListAppIdResponse listAppIds()` -> `com.azure.core.http.rest.PagedIterable listAppIds()`
* `models.PredefinedUrlCategoriesResponse listPredefinedUrlCategories()` -> `com.azure.core.http.rest.PagedIterable listPredefinedUrlCategories()`

#### `models.SourceAddr` was modified

* `validate()` was removed

#### `models.AzureResourceManagerUserAssignedIdentity` was modified

* `validate()` was removed

#### `models.TagInfo` was modified

* `validate()` was removed

#### `models.FirewallResourceUpdateProperties` was modified

* `validate()` was removed

#### `models.AppSeenData` was modified

* `AppSeenData()` was changed to private access
* `validate()` was removed
* `withAppSeenList(java.util.List)` was removed
* `withCount(int)` was removed

#### `models.PanoramaStatus` was modified

* `PanoramaStatus()` was changed to private access
* `validate()` was removed

#### `models.VnetConfiguration` was modified

* `validate()` was removed

#### `models.NetworkProfile` was modified

* `validate()` was removed

#### `models.Country` was modified

* `Country()` was removed
* `fromJson(com.azure.json.JsonReader)` was removed
* `withCode(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `java.lang.String code()` -> `java.lang.String code()`
* `java.lang.String description()` -> `java.lang.String description()`
* `toJson(com.azure.json.JsonWriter)` was removed
* `validate()` was removed

#### `models.ApplicationInsights` was modified

* `validate()` was removed

#### `models.PanoramaConfig` was modified

* `hostname()` was removed
* `validate()` was removed

### Features Added

* `models.CloudManagerTenantList` was added

* `models.EnableStatus` was added

* `models.SupportInfoModel` was added

* `models.PaloAltoNetworksCloudngfwOperations` was added

* `models.RegistrationStatus` was added

* `models.ProductSerialStatusValues` was added

* `models.StrataCloudManagerConfig` was added

* `models.ProductSerialNumberStatus` was added

* `models.ProductSerialNumberRequestStatus` was added

* `models.StrataCloudManagerInfo` was added

* `models.MetricsObjectFirewalls` was added

* `models.MetricsObjectFirewallResource` was added

#### `models.LocalRulestacks` was modified

* `listAppIds(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `listPredefinedUrlCategories(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `listCountries(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.PredefinedUrlCategory` was modified

* `innerModel()` was added

#### `models.FirewallResource$Definition` was modified

* `withIsStrataCloudManaged(models.BooleanEnum)` was added
* `withStrataCloudManagerConfig(models.StrataCloudManagerConfig)` was added

#### `PaloAltoNetworksNgfwManager` was modified

* `metricsObjectFirewalls()` was added
* `paloAltoNetworksCloudngfwOperations()` was added

#### `models.FirewallStatusResource` was modified

* `strataCloudManagerInfo()` was added
* `isStrataCloudManaged()` was added

#### `models.FirewallResource` was modified

* `isStrataCloudManaged()` was added
* `strataCloudManagerConfig()` was added

#### `models.LocalRulestackResource` was modified

* `listAppIds(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `listPredefinedUrlCategories(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `listCountries(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.FirewallResourceUpdateProperties` was modified

* `withStrataCloudManagerConfig(models.StrataCloudManagerConfig)` was added
* `strataCloudManagerConfig()` was added
* `isStrataCloudManaged()` was added
* `withIsStrataCloudManaged(models.BooleanEnum)` was added

#### `models.NetworkProfile` was modified

* `privateSourceNatRulesDestination()` was added
* `withPrivateSourceNatRulesDestination(java.util.List)` was added

#### `models.Country` was modified

* `innerModel()` was added

#### `models.PanoramaConfig` was modified

* `hostName()` was added

## 1.2.0 (2025-01-06)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.1.0 (2023-11-15)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.NetworkProfile` was modified

* `trustedRanges()` was added
* `withTrustedRanges(java.util.List)` was added

## 1.0.0 (2023-07-14)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2022-08-29. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-05-04)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2022-08-29-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
