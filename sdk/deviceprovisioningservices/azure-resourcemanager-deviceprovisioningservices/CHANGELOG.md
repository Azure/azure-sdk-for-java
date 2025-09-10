# Release History

## 1.2.0-beta.1 (2025-09-10)

- Azure Resource Manager DeviceProvisioningServices client library for Java. This package contains Microsoft Azure SDK for DeviceProvisioningServices Management SDK. API for using the Azure IoT Hub Device Provisioning Service features. Package api-version 2025-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ProvisioningServiceDescriptionListResult` was removed

#### `models.PrivateLinkResources` was removed

#### `models.OperationListResult` was removed

#### `models.CertificateListDescription` was removed

#### `models.IotDpsSkuDefinitionListResult` was removed

#### `models.SharedAccessSignatureAuthorizationRuleListResult` was removed

#### `IotDpsManager` was removed

#### `IotDpsManager$Configurable` was removed

#### `models.SharedAccessSignatureAuthorizationRule` was removed

#### `models.GroupIdInformationProperties` was modified

* `withRequiredZoneNames(java.util.List)` was removed
* `withRequiredMembers(java.util.List)` was removed
* `withGroupId(java.lang.String)` was removed

#### `models.IotDpsPropertiesDescription` was modified

* `deviceProvisioningHostname()` was removed
* `serviceOperationsHostname()` was removed

#### `models.VerificationCodeResponseProperties` was modified

* `withSubject(java.lang.String)` was removed
* `withExpiry(java.lang.String)` was removed
* `withThumbprint(java.lang.String)` was removed
* `withVerificationCode(java.lang.String)` was removed
* `withUpdated(java.lang.String)` was removed
* `withCertificate(byte[])` was removed
* `withIsVerified(java.lang.Boolean)` was removed
* `withCreated(java.lang.String)` was removed

#### `models.DpsCertificates` was modified

* `models.CertificateListDescription list(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable list(java.lang.String,java.lang.String)`
* `listWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.IotDpsResources` was modified

* `listPrivateLinkResourcesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.SharedAccessSignatureAuthorizationRule listKeysForKeyName(java.lang.String,java.lang.String,java.lang.String)` -> `models.SharedAccessSignatureAuthorizationRuleAccessRightsDescription listKeysForKeyName(java.lang.String,java.lang.String,java.lang.String)`
* `models.PrivateLinkResources listPrivateLinkResources(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listPrivateLinkResources(java.lang.String,java.lang.String)`

#### `models.ErrorMessage` was modified

* `withMessage(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `withDetails(java.lang.String)` was removed

### Features Added

* `models.DeviceRegistryNamespaceDescription` was added

* `models.UserAssignedIdentity` was added

* `models.SharedAccessSignatureAuthorizationRuleAccessRightsDescription` was added

* `models.ManagedServiceIdentity` was added

* `DeviceProvisioningServicesManager` was added

* `models.ManagedServiceIdentityType` was added

* `models.DeviceRegistryNamespaceAuthenticationType` was added

* `DeviceProvisioningServicesManager$Configurable` was added

#### `models.GroupIdInformation` was modified

* `systemData()` was added

#### `models.IotDpsPropertiesDescription` was modified

* `deviceRegistryNamespace()` was added
* `withPortalOperationsHostName(java.lang.String)` was added
* `withDeviceRegistryNamespace(models.DeviceRegistryNamespaceDescription)` was added
* `portalOperationsHostName()` was added
* `serviceOperationsHostName()` was added
* `deviceProvisioningHostName()` was added

#### `models.ProvisioningServiceDescription` was modified

* `subscriptionid()` was added
* `resourcegroup()` was added
* `identity()` was added

#### `models.DpsCertificates` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ProvisioningServiceDescription$Definition` was modified

* `withSubscriptionid(java.lang.String)` was added
* `withResourcegroup(java.lang.String)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.IotDpsResources` was modified

* `listPrivateLinkResources(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.1.0 (2025-01-03)

- Azure Resource Manager IotDps client library for Java. This package contains Microsoft Azure SDK for IotDps Management SDK. API for using the Azure IoT Hub Device Provisioning Service features. Package tag package-2022-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.ErrorMesssage` was removed

#### `models.CertificateBodyDescription` was removed

#### `models.AsyncOperationResult` was modified

* `models.ErrorMesssage error()` -> `models.ErrorMessage error()`

#### `models.CertificateResponse$Update` was modified

* `withCertificate(java.lang.String)` was removed

#### `models.CertificateResponse$Definition` was modified

* `withCertificate(java.lang.String)` was removed

### Features Added

* `models.ErrorMessage` was added

#### `models.ErrorDetails` was modified

* `getTarget()` was added
* `getCode()` was added
* `getMessage()` was added
* `getAdditionalInfo()` was added
* `getDetails()` was added

#### `models.IotDpsPropertiesDescription` was modified

* `withEnableDataResidency(java.lang.Boolean)` was added
* `enableDataResidency()` was added

#### `models.CertificateProperties` was modified

* `withCertificate(byte[])` was added
* `withIsVerified(java.lang.Boolean)` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added
* `systemData()` was added

#### `models.CertificateResponse$Update` was modified

* `withProperties(models.CertificateProperties)` was added

#### `models.ProvisioningServiceDescription` was modified

* `resourceGroupName()` was added
* `systemData()` was added

#### `models.CertificateResponse$Definition` was modified

* `withProperties(models.CertificateProperties)` was added

#### `models.CertificateResponse` was modified

* `resourceGroupName()` was added
* `systemData()` was added

#### `IotDpsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `IotDpsManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.1.0-beta.2 (2022-07-12)

- Azure Resource Manager IotDps client library for Java. This package contains Microsoft Azure SDK for IotDps Management SDK. API for using the Azure IoT Hub Device Provisioning Service features. Package tag package-2022-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorMesssage` was removed

* `models.CertificateBodyDescription` was removed

#### `models.AsyncOperationResult` was modified

* `models.ErrorMesssage error()` -> `models.ErrorMessage error()`

#### `models.CertificateResponse$Update` was modified

* `withCertificate(java.lang.String)` was removed
* `withIsVerified(java.lang.Boolean)` was removed

#### `models.CertificateResponse$Definition` was modified

* `withCertificate(java.lang.String)` was removed
* `withIsVerified(java.lang.Boolean)` was removed

### Features Added

* `models.ErrorMessage` was added

#### `models.CertificateProperties` was modified

* `withCertificate(byte[])` was added
* `withIsVerified(java.lang.Boolean)` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `models.CertificateResponse$Update` was modified

* `withProperties(models.CertificateProperties)` was added

#### `models.ProvisioningServiceDescription` was modified

* `resourceGroupName()` was added

#### `models.CertificateResponse$Definition` was modified

* `withProperties(models.CertificateProperties)` was added

#### `models.CertificateResponse` was modified

* `resourceGroupName()` was added

#### `IotDpsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `IotDpsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.1.0-beta.1 (2022-01-26)

- Azure Resource Manager IotDps client library for Java. This package contains Microsoft Azure SDK for IotDps Management SDK. API for using the Azure IoT Hub Device Provisioning Service features. Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.IotDpsPropertiesDescription` was modified

* `enableDataResidency()` was added
* `withEnableDataResidency(java.lang.Boolean)` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

#### `models.CertificateResponse$Update` was modified

* `withIsVerified(java.lang.Boolean)` was added

#### `models.ProvisioningServiceDescription` was modified

* `systemData()` was added

#### `models.CertificateResponse$Definition` was modified

* `withIsVerified(java.lang.Boolean)` was added

#### `models.CertificateResponse` was modified

* `systemData()` was added

#### `models.CertificateBodyDescription` was modified

* `isVerified()` was added
* `withIsVerified(java.lang.Boolean)` was added

#### `IotDpsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0 (2021-05-28)

Initial release of the Java Resource Management SDK for Device Provisioning Service.
