# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2024-10-09)

- Azure Resource Manager ServiceLinker client library for Java. This package contains Microsoft Azure SDK for ServiceLinker Management SDK. Microsoft.ServiceLinker provider. Package tag package-2024-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.LinkerList` was removed

* `models.SourceConfigurationResult` was removed

#### `models.LinkerResource` was modified

* `models.SourceConfigurationResult listConfigurations()` -> `models.ConfigurationResult listConfigurations()`

#### `models.Linkers` was modified

* `models.SourceConfigurationResult listConfigurations(java.lang.String,java.lang.String)` -> `models.ConfigurationResult listConfigurations(java.lang.String,java.lang.String)`

### Features Added

* `models.AccessKeyInfoBase` was added

* `models.ConfigurationNameItem` was added

* `models.ConfigurationNamesOperations` was added

* `models.CreateOrUpdateDryrunParameters` was added

* `models.DryrunPrerequisiteResultType` was added

* `models.ConfigurationName` was added

* `models.DryrunResource$Update` was added

* `models.SelfHostedServer` was added

* `models.FirewallRules` was added

* `models.ConfigurationInfo` was added

* `models.DatabaseAadAuthInfo` was added

* `models.DryrunParameters` was added

* `models.DryrunList` was added

* `models.UserAccountAuthInfo` was added

* `models.DeleteOrUpdateBehavior` was added

* `models.DryrunPatch` was added

* `models.AllowType` was added

* `models.DryrunPrerequisiteResult` was added

* `models.PermissionsMissingDryrunPrerequisiteResult` was added

* `models.DaprMetadataRequired` was added

* `models.ConfigurationStore` was added

* `models.EasyAuthMicrosoftEntraIdAuthInfo` was added

* `models.AzureAppConfigProperties` was added

* `models.Connectors` was added

* `models.DaprMetadata` was added

* `models.DryrunResource$DefinitionStages` was added

* `models.SecretSourceType` was added

* `models.DryrunOperationPreview` was added

* `models.ConfigurationResult` was added

* `models.DryrunResource` was added

* `models.LinkerConfigurationType` was added

* `models.ResourceList` was added

* `models.LinkersOperations` was added

* `models.PublicNetworkSolution` was added

* `models.AuthMode` was added

* `models.DaprConfigurationResource` was added

* `models.DaprBindingComponentDirection` was added

* `models.ConfigurationNameResult` was added

* `models.DryrunActionName` was added

* `models.DryrunResource$Definition` was added

* `models.DaprConfigurationList` was added

* `models.AccessKeyPermissions` was added

* `models.BasicErrorDryrunPrerequisiteResult` was added

* `models.FabricPlatform` was added

* `models.DryrunResource$UpdateStages` was added

* `models.DaprProperties` was added

* `models.DryrunPreviewOperationType` was added

#### `ServiceLinkerManager` was modified

* `connectors()` was added
* `linkersOperations()` was added
* `configurationNamesOperations()` was added

#### `models.LinkerResource$Definition` was modified

* `withConfigurationInfo(models.ConfigurationInfo)` was added
* `withPublicNetworkSolution(models.PublicNetworkSolution)` was added

#### `models.LinkerResource` was modified

* `configurationInfo()` was added
* `publicNetworkSolution()` was added

#### `models.LinkerResource$Update` was modified

* `withConfigurationInfo(models.ConfigurationInfo)` was added
* `withPublicNetworkSolution(models.PublicNetworkSolution)` was added

#### `models.VNetSolution` was modified

* `deleteOrUpdateBehavior()` was added
* `withDeleteOrUpdateBehavior(models.DeleteOrUpdateBehavior)` was added

#### `models.LinkerPatch` was modified

* `configurationInfo()` was added
* `publicNetworkSolution()` was added
* `withConfigurationInfo(models.ConfigurationInfo)` was added
* `withPublicNetworkSolution(models.PublicNetworkSolution)` was added

#### `models.ServicePrincipalCertificateAuthInfo` was modified

* `withRoles(java.util.List)` was added
* `withDeleteOrUpdateBehavior(models.DeleteOrUpdateBehavior)` was added
* `withAuthMode(models.AuthMode)` was added
* `roles()` was added
* `deleteOrUpdateBehavior()` was added

#### `models.SourceConfiguration` was modified

* `withDescription(java.lang.String)` was added
* `keyVaultReferenceIdentity()` was added
* `withKeyVaultReferenceIdentity(java.lang.String)` was added
* `configType()` was added
* `description()` was added

#### `models.ServicePrincipalSecretAuthInfo` was modified

* `withRoles(java.util.List)` was added
* `deleteOrUpdateBehavior()` was added
* `withAuthMode(models.AuthMode)` was added
* `withUsername(java.lang.String)` was added
* `withDeleteOrUpdateBehavior(models.DeleteOrUpdateBehavior)` was added
* `roles()` was added
* `username()` was added

#### `models.UserAssignedIdentityAuthInfo` was modified

* `username()` was added
* `withAuthMode(models.AuthMode)` was added
* `withRoles(java.util.List)` was added
* `withUsername(java.lang.String)` was added
* `deleteOrUpdateBehavior()` was added
* `roles()` was added
* `withDeleteOrUpdateBehavior(models.DeleteOrUpdateBehavior)` was added

#### `models.SecretStore` was modified

* `withKeyVaultSecretName(java.lang.String)` was added
* `keyVaultSecretName()` was added

#### `models.SystemAssignedIdentityAuthInfo` was modified

* `withAuthMode(models.AuthMode)` was added
* `roles()` was added
* `withDeleteOrUpdateBehavior(models.DeleteOrUpdateBehavior)` was added
* `withUsername(java.lang.String)` was added
* `username()` was added
* `withRoles(java.util.List)` was added
* `deleteOrUpdateBehavior()` was added

#### `models.SecretAuthInfo` was modified

* `withAuthMode(models.AuthMode)` was added

#### `models.AuthInfoBase` was modified

* `authMode()` was added
* `withAuthMode(models.AuthMode)` was added

## 1.0.0-beta.3 (2024-10-09)

- Azure Resource Manager ServiceLinker client library for Java. This package contains Microsoft Azure SDK for ServiceLinker Management SDK. Microsoft.ServiceLinker provider. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.KeyVaultSecretUriSecretInfo` was modified

* `secretType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LinkerList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LinkerPatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServicePrincipalCertificateAuthInfo` was modified

* `authType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureResourcePropertiesBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.KeyVaultSecretReferenceSecretInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `secretType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServicePrincipalSecretAuthInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `authType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentityAuthInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `authType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConfluentBootstrapServer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecretStore` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValueSecretInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `secretType()` was added

#### `models.SystemAssignedIdentityAuthInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `authType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConfluentSchemaRegistry` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureKeyVaultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecretAuthInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `authType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecretInfoBase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `secretType()` was added

#### `models.TargetServiceBase` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthInfoBase` was modified

* `authType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VNetSolution` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidationResultItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2022-05-19)

- Azure Resource Manager ServiceLinker client library for Java. This package contains Microsoft Azure SDK for ServiceLinker Management SDK. Microsoft.ServiceLinker provider. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Type` was removed

* `models.ValidateResult` was removed

#### `models.Linkers` was modified

* `models.ValidateResult validate(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.ValidateOperationResult validate(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.ValidateResult validate(java.lang.String,java.lang.String)` -> `models.ValidateOperationResult validate(java.lang.String,java.lang.String)`

#### `models.LinkerResource` was modified

* `models.ValidateResult validate()` -> `models.ValidateOperationResult validate()`
* `models.ValidateResult validate(com.azure.core.util.Context)` -> `models.ValidateOperationResult validate(com.azure.core.util.Context)`

### Features Added

* `models.ValidateOperationResult` was added

* `models.TargetServiceType` was added

* `models.AzureResourceType` was added

## 1.0.0-beta.1 (2022-04-15)

- Azure Resource Manager ServiceLinker client library for Java. This package contains Microsoft Azure SDK for ServiceLinker Management SDK. Microsoft.ServiceLinker provider. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
