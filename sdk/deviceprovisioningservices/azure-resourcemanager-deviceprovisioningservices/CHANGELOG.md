# Release History

## 1.2.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.1 (2025-10-20)

- Azure Resource Manager IotDps client library for Java. This package contains Microsoft Azure SDK for IotDps Management SDK. API for using the Azure IoT Hub Device Provisioning Service features. Package api-version 2025-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ProvisioningServiceDescriptionListResult` was removed

#### `models.PrivateLinkResources` was removed

#### `models.OperationListResult` was removed

#### `models.CertificateListDescription` was removed

#### `models.IotDpsSkuDefinitionListResult` was removed

#### `models.SharedAccessSignatureAuthorizationRuleListResult` was removed

#### `models.ProvisioningServiceDescription$DefinitionStages` was modified

* `withExistingResourceGroup(java.lang.String)` was removed in stage 2

#### `models.TagsResource` was modified

* `validate()` was removed

#### `models.IotHubDefinitionDescription` was modified

* `validate()` was removed

#### `models.IotDpsSkuInfo` was modified

* `validate()` was removed

#### `models.GroupIdInformationProperties` was modified

* `GroupIdInformationProperties()` was changed to private access
* `withRequiredZoneNames(java.util.List)` was removed
* `withGroupId(java.lang.String)` was removed
* `validate()` was removed
* `withRequiredMembers(java.util.List)` was removed

#### `models.IpFilterRule` was modified

* `validate()` was removed

#### `models.ErrorDetails` was modified

* `ErrorDetails()` was changed to private access
* `validate()` was removed

#### `models.IotDpsPropertiesDescription` was modified

* `validate()` was removed

#### `models.CertificateProperties` was modified

* `validate()` was removed

#### `models.VerificationCodeResponseProperties` was modified

* `VerificationCodeResponseProperties()` was changed to private access
* `withUpdated(java.lang.String)` was removed
* `validate()` was removed
* `withExpiry(java.lang.String)` was removed
* `withIsVerified(java.lang.Boolean)` was removed
* `withCertificate(byte[])` was removed
* `withThumbprint(java.lang.String)` was removed
* `withSubject(java.lang.String)` was removed
* `withCreated(java.lang.String)` was removed
* `withVerificationCode(java.lang.String)` was removed

#### `models.PrivateEndpointConnectionProperties` was modified

* `validate()` was removed

#### `models.OperationInputs` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.DpsCertificates` was modified

* `listWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.CertificateListDescription list(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable list(java.lang.String,java.lang.String)`

#### `models.ProvisioningServiceDescription$Definition` was modified

* `withExistingResourceGroup(java.lang.String)` was removed

#### `models.VerificationCodeRequest` was modified

* `validate()` was removed

#### `models.IotDpsResources` was modified

* `listPrivateLinkResourcesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.PrivateLinkResources listPrivateLinkResources(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listPrivateLinkResources(java.lang.String,java.lang.String)`

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.ErrorMessage` was modified

* `ErrorMessage()` was changed to private access
* `withDetails(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `validate()` was removed
* `withMessage(java.lang.String)` was removed

#### `IotDpsManager` was modified

* `fluent.IotDpsClient serviceClient()` -> `fluent.IotDpsManagementClient serviceClient()`

### Features Added

* `models.DeviceRegistryNamespaceDescription` was added

* `models.UserAssignedIdentity` was added

* `models.ManagedServiceIdentity` was added

* `models.ManagedServiceIdentityType` was added

* `models.DeviceRegistryNamespaceAuthenticationType` was added

#### `models.GroupIdInformation` was modified

* `systemData()` was added

#### `models.IotDpsPropertiesDescription` was modified

* `deviceRegistryNamespace()` was added
* `portalOperationsHostName()` was added
* `withDeviceRegistryNamespace(models.DeviceRegistryNamespaceDescription)` was added
* `withPortalOperationsHostName(java.lang.String)` was added

#### `models.ProvisioningServiceDescription` was modified

* `subscriptionid()` was added
* `resourceGroup()` was added
* `identity()` was added

#### `models.DpsCertificates` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ProvisioningServiceDescription$Definition` was modified

* `withSubscriptionid(java.lang.String)` was added
* `withExistingResourceGroup(java.lang.String)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withResourceGroup(java.lang.String)` was added

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
