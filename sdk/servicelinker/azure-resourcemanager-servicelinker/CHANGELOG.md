# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
