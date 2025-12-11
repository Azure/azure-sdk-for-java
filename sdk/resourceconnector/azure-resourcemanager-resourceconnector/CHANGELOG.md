# Release History

## 1.2.0-beta.1 (2025-12-11)

- Azure Resource Manager Resource Connector client library for Java. This package contains Microsoft Azure SDK for Resource Connector Management SDK. The appliances Rest API spec. Package api-version 2025-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ApplianceListResult` was removed

#### `models.ApplianceOperationsList` was removed

#### `models.ArtifactProfile` was modified

* `ArtifactProfile()` was changed to private access
* `validate()` was removed

#### `models.PatchableAppliance` was modified

* `validate()` was removed

#### `models.SupportedVersion` was modified

* `SupportedVersion()` was changed to private access
* `validate()` was removed

#### `models.UpgradeGraphProperties` was modified

* `UpgradeGraphProperties()` was changed to private access
* `validate()` was removed

#### `models.SupportedVersionCatalogVersionData` was modified

* `SupportedVersionCatalogVersionData()` was changed to private access
* `validate()` was removed

#### `models.SshKey` was modified

* `SshKey()` was changed to private access
* `validate()` was removed

#### `ResourceConnectorManager` was modified

* `fluent.ResourceConnector serviceClient()` -> `fluent.ResourceConnectorManagementClient serviceClient()`

#### `models.HybridConnectionConfig` was modified

* `HybridConnectionConfig()` was changed to private access
* `validate()` was removed

#### `models.ApplianceCredentialKubeconfig` was modified

* `ApplianceCredentialKubeconfig()` was changed to private access
* `validate()` was removed

#### `models.AppliancePropertiesInfrastructureConfig` was modified

* `validate()` was removed

#### `models.SupportedVersionCatalogVersion` was modified

* `SupportedVersionCatalogVersion()` was changed to private access
* `validate()` was removed

#### `models.SupportedVersionMetadata` was modified

* `SupportedVersionMetadata()` was changed to private access
* `validate()` was removed

#### `models.Identity` was modified

* `validate()` was removed

### Features Added

* `models.ProxyConfiguration` was added

* `models.NetworkProfile` was added

* `models.Event` was added

* `models.GatewayConfiguration` was added

* `models.DnsConfiguration` was added

#### `models.Appliance` was modified

* `events()` was added
* `networkProfile()` was added

#### `models.Appliance$Definition` was modified

* `withNetworkProfile(models.NetworkProfile)` was added

## 1.1.0 (2024-12-11)

- Azure Resource Manager Resource Connector client library for Java. This package contains Microsoft Azure SDK for Resource Connector Management SDK. The appliances Rest API spec. Package tag package-2022-10-27. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0 (2023-08-23)

- Azure Resource Manager Resource Connector client library for Java. This package contains Microsoft Azure SDK for Resource Connector Management SDK. The appliances Rest API spec. Package tag package-2022-10-27. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `AppliancesManager$Configurable` was removed

* `AppliancesManager` was removed

#### `models.Appliances` was modified

* `listKeysWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Appliance` was modified

* `listKeysWithResponse(com.azure.core.util.Context)` was removed

### Features Added

* `ResourceConnectorManager$Configurable` was added

* `ResourceConnectorManager` was added

#### `models.Appliances` was modified

* `listKeysWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Appliance` was modified

* `listKeysWithResponse(java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.2 (2023-04-26)

- Azure Resource Manager Appliances client library for Java. This package contains Microsoft Azure SDK for Appliances Management SDK. The appliances Rest API spec. Package tag package-2022-10-27. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ApplianceListClusterCustomerUserCredentialResults` was removed

#### `models.Appliances` was modified

* `listClusterCustomerUserCredential(java.lang.String,java.lang.String)` was removed
* `listClusterCustomerUserCredentialWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Appliance` was modified

* `listClusterCustomerUserCredentialWithResponse(com.azure.core.util.Context)` was removed
* `listClusterCustomerUserCredential()` was removed

#### `models.SshKey` was modified

* `withPrivateKey(java.lang.String)` was removed
* `withPublicKey(java.lang.String)` was removed

### Features Added

* `models.ArtifactProfile` was added

* `models.ApplianceListKeysResults` was added

* `models.ApplianceGetTelemetryConfigResult` was added

#### `models.Appliances` was modified

* `getTelemetryConfigWithResponse(com.azure.core.util.Context)` was added
* `getTelemetryConfig()` was added
* `listKeysWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listKeys(java.lang.String,java.lang.String)` was added

#### `models.Appliance` was modified

* `listKeysWithResponse(com.azure.core.util.Context)` was added
* `listKeys()` was added

#### `models.SshKey` was modified

* `certificate()` was added
* `creationTimestamp()` was added
* `expirationTimestamp()` was added

## 1.0.0-beta.1 (2022-07-08)

- Azure Resource Manager Appliances client library for Java. This package contains Microsoft Azure SDK for Appliances Management SDK. The appliances Rest API spec. Package tag package-2022-04-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
