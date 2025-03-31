# Release History

## 1.1.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.3 (2025-02-26)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2024-11-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NginxConfiguration` was removed

#### `models.NginxConfiguration$Update` was removed

#### `models.NginxConfiguration$Definition` was removed

#### `models.NginxConfiguration$DefinitionStages` was removed

#### `models.NginxConfiguration$UpdateStages` was removed

#### `models.NginxConfigurationProperties` was removed

#### `models.NginxDeploymentProperties` was modified

* `managedResourceGroup()` was removed
* `withManagedResourceGroup(java.lang.String)` was removed

#### `models.Configurations` was modified

* `models.NginxConfiguration getById(java.lang.String)` -> `models.NginxConfigurationResponse getById(java.lang.String)`
* `models.NginxConfiguration$DefinitionStages$Blank define(java.lang.String)` -> `models.NginxConfigurationResponse$DefinitionStages$Blank define(java.lang.String)`
* `models.NginxConfiguration get(java.lang.String,java.lang.String,java.lang.String)` -> `models.NginxConfigurationResponse get(java.lang.String,java.lang.String,java.lang.String)`

### Features Added

* `models.NginxConfigurationResponse$Update` was added

* `models.WebApplicationFirewallSettings` was added

* `models.NginxDeploymentApiKeyResponse$Definition` was added

* `models.NginxDeploymentApiKeyResponse$DefinitionStages` was added

* `models.NginxConfigurationResponse$UpdateStages` was added

* `models.NginxConfigurationResponse$Definition` was added

* `models.Level` was added

* `models.DiagnosticItem` was added

* `models.ActivationState` was added

* `models.NginxDeploymentApiKeyListResponse` was added

* `models.NginxDeploymentApiKeyResponse$Update` was added

* `models.NginxDeploymentApiKeyRequest` was added

* `models.NginxConfigurationRequestProperties` was added

* `models.NginxDeploymentApiKeyResponse` was added

* `models.NginxConfigurationResponseProperties` was added

* `models.NginxConfigurationRequest` was added

* `models.NginxDeploymentApiKeyResponseProperties` was added

* `models.NginxDeploymentPropertiesNginxAppProtect` was added

* `models.WebApplicationFirewallComponentVersions` was added

* `models.NginxConfigurationProtectedFileRequest` was added

* `models.WebApplicationFirewallStatus` was added

* `models.NginxConfigurationResponse$DefinitionStages` was added

* `models.NginxDeploymentApiKeyResponse$UpdateStages` was added

* `models.WebApplicationFirewallPackage` was added

* `models.ApiKeys` was added

* `models.NginxDeploymentUpdatePropertiesNginxAppProtect` was added

* `models.NginxConfigurationResponse` was added

* `models.NginxConfigurationProtectedFileResponse` was added

* `models.NginxDeploymentApiKeyRequestProperties` was added

#### `models.NginxDeploymentUpdateProperties` was modified

* `withNginxAppProtect(models.NginxDeploymentUpdatePropertiesNginxAppProtect)` was added
* `nginxAppProtect()` was added
* `networkProfile()` was added
* `withNetworkProfile(models.NginxNetworkProfile)` was added

#### `models.NginxDeploymentProperties` was modified

* `withNginxAppProtect(models.NginxDeploymentPropertiesNginxAppProtect)` was added
* `nginxAppProtect()` was added
* `dataplaneApiEndpoint()` was added

#### `models.AnalysisResultData` was modified

* `diagnostics()` was added
* `withDiagnostics(java.util.List)` was added

#### `NginxManager` was modified

* `apiKeys()` was added

## 1.1.0-beta.2 (2024-12-04)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2024-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.1.0-beta.1 (2024-05-20)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2024-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AnalysisCreateConfig` was added

* `models.ScaleProfileCapacity` was added

* `models.AnalysisDiagnostic` was added

* `models.NginxCertificateErrorResponseBody` was added

* `models.AutoUpgradeProfile` was added

* `models.AnalysisCreate` was added

* `models.ScaleProfile` was added

* `models.AnalysisResultData` was added

* `models.AnalysisResult` was added

#### `models.NginxDeploymentScalingProperties` was modified

* `withProfiles(java.util.List)` was added
* `profiles()` was added

#### `models.Configurations` was modified

* `analysis(java.lang.String,java.lang.String,java.lang.String)` was added
* `analysisWithResponse(java.lang.String,java.lang.String,java.lang.String,models.AnalysisCreate,com.azure.core.util.Context)` was added

#### `models.NginxDeploymentUpdateProperties` was modified

* `autoUpgradeProfile()` was added
* `withAutoUpgradeProfile(models.AutoUpgradeProfile)` was added

#### `models.NginxDeploymentProperties` was modified

* `withAutoUpgradeProfile(models.AutoUpgradeProfile)` was added
* `autoUpgradeProfile()` was added

#### `models.NginxCertificateProperties` was modified

* `withCertificateError(models.NginxCertificateErrorResponseBody)` was added
* `sha1Thumbprint()` was added
* `keyVaultSecretCreated()` was added
* `keyVaultSecretVersion()` was added
* `certificateError()` was added

#### `models.NginxConfiguration` was modified

* `analysisWithResponse(models.AnalysisCreate,com.azure.core.util.Context)` was added
* `analysis()` was added

## 1.0.0 (2023-11-17)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2023-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NginxCertificate$Definition` was modified

* `withTags(java.util.Map)` was removed

#### `models.NginxCertificate` was modified

* `tags()` was removed

#### `models.NginxConfiguration$Definition` was modified

* `withTags(java.util.Map)` was removed

#### `models.NginxCertificate$Update` was modified

* `withTags(java.util.Map)` was removed

#### `models.NginxConfiguration` was modified

* `tags()` was removed

#### `models.NginxConfiguration$Update` was modified

* `withTags(java.util.Map)` was removed

### Features Added

* `models.NginxDeploymentScalingProperties` was added

* `models.NginxDeploymentUserProfile` was added

#### `models.NginxConfigurationPackage` was modified

* `protectedFiles()` was added
* `withProtectedFiles(java.util.List)` was added

#### `models.NginxDeploymentUpdateProperties` was modified

* `withScalingProperties(models.NginxDeploymentScalingProperties)` was added
* `withUserProfile(models.NginxDeploymentUserProfile)` was added
* `scalingProperties()` was added
* `userProfile()` was added

#### `models.NginxDeploymentProperties` was modified

* `withUserProfile(models.NginxDeploymentUserProfile)` was added
* `scalingProperties()` was added
* `withScalingProperties(models.NginxDeploymentScalingProperties)` was added
* `userProfile()` was added

## 1.0.0-beta.2 (2022-10-13)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2022-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NginxDeploymentProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.NginxCertificateProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.NginxConfigurationProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

### Features Added

* `models.NginxCertificate$UpdateStages` was added

* `models.NginxCertificate$Update` was added

#### `models.NginxCertificate` was modified

* `resourceGroupName()` was added
* `update()` was added

#### `models.NginxConfigurationProperties` was modified

* `withProtectedFiles(java.util.List)` was added
* `protectedFiles()` was added

## 1.0.0-beta.1 (2022-08-30)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package tag package-2022-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
