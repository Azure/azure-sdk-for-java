# Release History

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
