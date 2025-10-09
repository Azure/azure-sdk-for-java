# Release History

## 1.1.0-beta.4 (2025-10-09)

- Azure Resource Manager Nginx client library for Java. This package contains Microsoft Azure SDK for Nginx Management SDK.  Package api-version 2025-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.NginxConfigurationListResponse` was removed

#### `models.OperationResult` was removed

#### `models.NginxDeploymentApiKeyListResponse` was removed

#### `models.NginxCertificateListResponse` was removed

#### `models.OperationListResult` was removed

#### `models.NginxDeploymentListResponse` was removed

#### `models.UserIdentityProperties` was modified

* `validate()` was removed

#### `models.ScaleProfileCapacity` was modified

* `validate()` was removed

#### `models.NginxNetworkInterfaceConfiguration` was modified

* `validate()` was removed

#### `models.WebApplicationFirewallSettings` was modified

* `validate()` was removed

#### `models.NginxDeploymentScalingProperties` was modified

* `validate()` was removed

#### `models.AnalysisDiagnostic` was modified

* `withRule(java.lang.String)` was removed
* `float line()` -> `double line()`
* `withFile(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withId(java.lang.String)` was removed
* `withLine(float)` was removed
* `withMessage(java.lang.String)` was removed
* `withDirective(java.lang.String)` was removed

#### `models.NginxFrontendIpConfiguration` was modified

* `validate()` was removed

#### `models.NginxCertificateErrorResponseBody` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed
* `withDescription(java.lang.String)` was removed

#### `models.NginxDeploymentUserProfile` was modified

* `validate()` was removed

#### `models.DiagnosticItem` was modified

* `withLine(float)` was removed
* `withMessage(java.lang.String)` was removed
* `withRule(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withLevel(models.Level)` was removed
* `withDirective(java.lang.String)` was removed
* `withCategory(java.lang.String)` was removed
* `validate()` was removed
* `withFile(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `float line()` -> `double line()`

#### `models.NginxDeploymentUpdateProperties` was modified

* `validate()` was removed

#### `models.NginxLogging` was modified

* `validate()` was removed

#### `models.NginxDeploymentProperties` was modified

* `validate()` was removed

#### `models.NginxCertificateProperties` was modified

* `validate()` was removed

#### `models.NginxNetworkProfile` was modified

* `validate()` was removed

#### `models.NginxDeploymentApiKeyRequest` was modified

* `validate()` was removed

#### `models.ScaleProfile` was modified

* `validate()` was removed

#### `models.AnalysisResultData` was modified

* `validate()` was removed
* `withErrors(java.util.List)` was removed
* `withDiagnostics(java.util.List)` was removed

#### `models.NginxConfigurationRequestProperties` was modified

* `validate()` was removed

#### `models.AnalysisCreateConfig` was modified

* `validate()` was removed

#### `models.NginxConfigurationResponseProperties` was modified

* `withFiles(java.util.List)` was removed
* `withProtectedFiles(java.util.List)` was removed
* `validate()` was removed
* `withRootFile(java.lang.String)` was removed
* `withPackageProperty(models.NginxConfigurationPackage)` was removed

#### `models.IdentityProperties` was modified

* `validate()` was removed

#### `models.NginxConfigurationFile` was modified

* `validate()` was removed

#### `models.NginxConfigurationRequest` was modified

* `validate()` was removed

#### `models.NginxDeploymentApiKeyResponseProperties` was modified

* `validate()` was removed
* `withEndDateTime(java.time.OffsetDateTime)` was removed

#### `models.NginxDeploymentUpdateParameters` was modified

* `validate()` was removed

#### `models.NginxDeploymentPropertiesNginxAppProtect` was modified

* `validate()` was removed

#### `models.WebApplicationFirewallComponentVersions` was modified

* `validate()` was removed
* `withWafEngineVersion(java.lang.String)` was removed
* `withWafNginxVersion(java.lang.String)` was removed

#### `models.NginxConfigurationProtectedFileRequest` was modified

* `validate()` was removed

#### `models.WebApplicationFirewallStatus` was modified

* `validate()` was removed

#### `models.NginxPublicIpAddress` was modified

* `validate()` was removed

#### `models.NginxConfigurationPackage` was modified

* `validate()` was removed

#### `models.ResourceSku` was modified

* `validate()` was removed

#### `models.WebApplicationFirewallPackage` was modified

* `withVersion(java.lang.String)` was removed
* `withRevisionDatetime(java.time.OffsetDateTime)` was removed
* `validate()` was removed

#### `models.AutoUpgradeProfile` was modified

* `validate()` was removed

#### `models.NginxStorageAccount` was modified

* `validate()` was removed

#### `models.NginxDeploymentUpdatePropertiesNginxAppProtect` was modified

* `validate()` was removed

#### `models.AnalysisCreate` was modified

* `validate()` was removed

#### `models.NginxConfigurationProtectedFileResponse` was modified

* `validate()` was removed
* `withContentHash(java.lang.String)` was removed
* `withVirtualPath(java.lang.String)` was removed

#### `models.NginxDeploymentApiKeyRequestProperties` was modified

* `validate()` was removed

#### `models.NginxPrivateIpAddress` was modified

* `validate()` was removed

### Features Added

* `models.NginxDeploymentDefaultWafPolicyListResponse` was added

* `models.NginxDeploymentWafPolicy$Definition` was added

* `models.WafPolicies` was added

* `models.ActionType` was added

* `models.NginxDeploymentWafPolicyMetadataProperties` was added

* `models.NginxDeploymentWafPolicyApplyingStatusCode` was added

* `models.NginxDeploymentWafPolicy` was added

* `models.NginxDeploymentDefaultWafPolicyProperties` was added

* `models.NginxDeploymentWafPolicyCompilingStatus` was added

* `models.NginxDeploymentWafPolicyProperties` was added

* `models.NginxDeploymentWafPolicyCompilingStatusCode` was added

* `models.Origin` was added

* `models.NginxDeploymentWafPolicyMetadata` was added

* `models.Operation` was added

* `models.NginxDeploymentWafPolicy$DefinitionStages` was added

* `models.NginxDeploymentWafPolicyApplyingStatus` was added

* `models.DefaultWafPolicies` was added

#### `models.NginxDeploymentApiKeyRequest` was modified

* `systemData()` was added

#### `models.NginxDeploymentApiKeyResponse` was modified

* `systemData()` was added

#### `NginxManager` was modified

* `defaultWafPolicies()` was added
* `wafPolicies()` was added

#### `models.WebApplicationFirewallStatus` was modified

* `wafRelease()` was added

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
