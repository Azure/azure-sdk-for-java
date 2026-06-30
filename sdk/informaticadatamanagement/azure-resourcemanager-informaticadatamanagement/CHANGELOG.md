# Release History

## 1.1.0 (2026-06-30)

- Azure Resource Manager Informatica DataManagement client library for Java. This package contains Microsoft Azure SDK for Informatica DataManagement Management SDK.  Package api-version 2025-11-27. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.InformaticaOrganizationResourceListResult` was removed

#### `models.InformaticaServerlessRuntimeResourceListResult` was removed

#### `models.InfaRuntimeResourceFetchMetadata` was removed

#### `models.MarketplaceDetails` was modified

* `validate()` was removed

#### `models.RegionsMetadata` was modified

* `validate()` was removed

#### `models.InformaticaProperties` was modified

* `validate()` was removed

#### `models.ServerlessRuntimeUserContextProperties` was modified

* `validate()` was removed

#### `models.NetworkInterfaceConfiguration` was modified

* `validate()` was removed

#### `models.LinkOrganization` was modified

* `validate()` was removed

#### `models.InformaticaServerlessRuntimeResourceUpdate` was modified

* `validate()` was removed

#### `models.ServerlessRuntimeTag` was modified

* `validate()` was removed

#### `models.ServerlessRuntimeNetworkProfile` was modified

* `validate()` was removed

#### `models.MarketplaceDetailsUpdate` was modified

* `validate()` was removed

#### `models.InfaServerlessFetchConfigProperties` was modified

* `validate()` was removed

#### `models.CompanyDetailsUpdate` was modified

* `validate()` was removed

#### `models.OfferDetailsUpdate` was modified

* `validate()` was removed

#### `models.ServerlessRuntimeUserContextPropertiesUpdate` was modified

* `validate()` was removed

#### `models.ServerlessConfigProperties` was modified

* `validate()` was removed

#### `models.ServerlessRuntimeConfigProperties` was modified

* `validate()` was removed

#### `models.OrganizationPropertiesCustomUpdate` was modified

* `validate()` was removed

#### `models.ApplicationTypeMetadata` was modified

* `validate()` was removed

#### `models.CompanyDetails` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `InformaticaDataManagementManager` was modified

* `fluent.InformaticaDataManagement serviceClient()` -> `fluent.InformaticaDataManagementClient serviceClient()`

#### `models.ServerlessRuntimeConfigPropertiesUpdate` was modified

* `validate()` was removed

#### `models.UserDetails` was modified

* `validate()` was removed

#### `models.CdiConfigProps` was modified

* `validate()` was removed

#### `models.InformaticaOrganizationResourceUpdate` was modified

* `validate()` was removed

#### `models.UserDetailsUpdate` was modified

* `validate()` was removed

#### `models.InformaticaServerlessRuntimeProperties` was modified

* `validate()` was removed

#### `models.ServerlessRuntimeNetworkProfileUpdate` was modified

* `validate()` was removed

#### `models.OrganizationProperties` was modified

* `validate()` was removed

#### `models.ServerlessRuntimeDependency` was modified

* `validate()` was removed

#### `models.ApplicationConfigs` was modified

* `validate()` was removed

#### `models.NetworkInterfaceConfigurationUpdate` was modified

* `validate()` was removed

#### `models.ComputeUnitsMetadata` was modified

* `validate()` was removed

#### `models.OfferDetails` was modified

* `validate()` was removed

#### `models.AdvancedCustomProperties` was modified

* `validate()` was removed

#### `models.ServerlessRuntimePropertiesCustomUpdate` was modified

* `validate()` was removed

### Features Added

* `models.ManagedServiceIdentity` was added

* `models.ServerlessRuntimeDataDisk` was added

* `models.ManagedServiceIdentityType` was added

* `models.MarketplaceSubscriptionStatus` was added

* `models.InfaRuntimeResourceFetchMetaData` was added

* `models.UserAssignedIdentity` was added

#### `models.MarketplaceDetails` was modified

* `marketplaceSubscriptionStatus()` was added

#### `models.InformaticaServerlessRuntimeResource` was modified

* `identity()` was added

#### `models.InformaticaServerlessRuntimeResourceUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

#### `models.InformaticaOrganizationResource` was modified

* `identity()` was added

#### `models.MarketplaceDetailsUpdate` was modified

* `marketplaceSubscriptionStatus()` was added

#### `models.InfaServerlessFetchConfigProperties` was modified

* `serverlessRuntimeDataDisks()` was added

#### `models.InformaticaServerlessRuntimeResource$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.InformaticaOrganizationResourceUpdate` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

#### `models.InformaticaOrganizationResource$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.InformaticaOrganizationResource$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.InformaticaServerlessRuntimeProperties` was modified

* `withServerlessRuntimeDataDisks(java.util.List)` was added
* `serverlessRuntimeDataDisks()` was added

#### `models.InformaticaServerlessRuntimeResource$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.ServerlessRuntimePropertiesCustomUpdate` was modified

* `serverlessRuntimeDataDisks()` was added
* `withServerlessRuntimeDataDisks(java.util.List)` was added

## 1.0.0 (2024-07-15)

- Azure Resource Manager Informatica DataManagement client library for Java. This package contains Microsoft Azure SDK for Informatica DataManagement Management SDK.  Package tag package-2024-05-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `implementation.models.InformaticaOrganizationResourceListResult` was removed

* `implementation.models.PagedOperation` was removed

* `models.InfaRuntimeResourceFetchMetaData` was removed

* `implementation.models.InformaticaServerlessRuntimeResourceListResult` was removed

#### `InformaticaDataManagementManager` was modified

* `fluent.DataManagementClient serviceClient()` -> `fluent.InformaticaDataManagement serviceClient()`

### Features Added

* `models.OperationListResult` was added

* `models.InformaticaOrganizationResourceListResult` was added

* `models.InformaticaServerlessRuntimeResourceListResult` was added

* `models.InfaRuntimeResourceFetchMetadata` was added

#### `models.MarketplaceDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegionsMetadata` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InformaticaProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerlessRuntimeUserContextProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkInterfaceConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkOrganization` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InformaticaServerlessRuntimeResourceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServerlessRuntimeTag` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerlessRuntimeNetworkProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketplaceDetailsUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InfaServerlessFetchConfigProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CompanyDetailsUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OfferDetailsUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServerlessRuntimeUserContextPropertiesUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerlessConfigProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServerlessRuntimeConfigProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OrganizationPropertiesCustomUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ApplicationTypeMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CompanyDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerlessRuntimeConfigPropertiesUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CdiConfigProps` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InformaticaOrganizationResourceUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserDetailsUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InformaticaServerlessRuntimeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerlessRuntimeNetworkProfileUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OrganizationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServerlessRuntimeDependency` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplicationConfigs` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkInterfaceConfigurationUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComputeUnitsMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OfferDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AdvancedCustomProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServerlessRuntimePropertiesCustomUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.1 (2024-06-03)

- Azure Resource Manager Informatica DataManagement client library for Java. This package contains Microsoft Azure SDK for Informatica DataManagement Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

