# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2025-07-21)

- Azure Resource Manager Healthbot client library for Java. This package contains Microsoft Azure SDK for Healthbot Management SDK. Azure Health Bot is a cloud platform that empowers developers in Healthcare organizations to build and deploy their compliant, AI-powered virtual health assistants and health bots, that help them improve processes and reduce costs. Package tag package-2025-05-25. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.UserAssignedIdentity` was added

* `models.HealthBotKey` was added

* `models.ResourceIdentityType` was added

* `models.Identity` was added

* `models.KeyVaultProperties` was added

* `models.HealthBotKeysResponse` was added

#### `models.HealthBotUpdateParameters` was modified

* `withProperties(models.HealthBotProperties)` was added
* `identity()` was added
* `location()` was added
* `withIdentity(models.Identity)` was added
* `properties()` was added
* `withLocation(java.lang.String)` was added

#### `models.HealthBot$Definition` was modified

* `withIdentity(models.Identity)` was added

#### `models.HealthBotProperties` was modified

* `withKeyVaultProperties(models.KeyVaultProperties)` was added
* `keyVaultProperties()` was added
* `accessControlMethod()` was added

#### `models.HealthBot` was modified

* `regenerateApiJwtSecretWithResponse(com.azure.core.util.Context)` was added
* `listSecrets()` was added
* `identity()` was added
* `listSecretsWithResponse(com.azure.core.util.Context)` was added
* `regenerateApiJwtSecret()` was added

#### `models.Bots` was modified

* `listSecrets(java.lang.String,java.lang.String)` was added
* `listSecretsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `regenerateApiJwtSecretWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `regenerateApiJwtSecret(java.lang.String,java.lang.String)` was added

#### `models.HealthBot$Update` was modified

* `withIdentity(models.Identity)` was added
* `withProperties(models.HealthBotProperties)` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager Healthbot client library for Java. This package contains Microsoft Azure SDK for Healthbot Management SDK. Microsoft Healthcare Bot is a cloud platform that empowers developers in Healthcare organizations to build and deploy their compliant, AI-powered virtual health assistants and health bots, that help them improve processes and reduce costs. Package tag package-2020-12-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager Healthbot client library for Java.

## 1.0.0-beta.3 (2024-10-10)

- Azure Resource Manager Healthbot client library for Java. This package contains Microsoft Azure SDK for Healthbot Management SDK. Microsoft Healthcare Bot is a cloud platform that empowers developers in Healthcare organizations to build and deploy their compliant, AI-powered virtual health assistants and health bots, that help them improve processes and reduce costs. Package tag package-2020-12-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.HealthBotUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BotResponseList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HealthBotProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HealthBot` was modified

* `systemData()` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableOperations` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2022-07-19)

- Azure Resource Manager Healthbot client library for Java. This package contains Microsoft Azure SDK for Healthbot Management SDK. Microsoft Healthcare Bot is a cloud platform that empowers developers in Healthcare organizations to build and deploy their compliant, AI-powered virtual health assistants and health bots, that help them improve processes and reduce costs. Package tag package-2020-12-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SystemData` was removed

* `models.IdentityType` was removed

* `models.ErrorAdditionalInfo` was removed

### Features Added

#### `HealthbotManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `HealthbotManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.HealthBot` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-01-07)

- Azure Resource Manager Healthbot client library for Java. This package contains Microsoft Azure SDK for Healthbot Management SDK. Microsoft Healthcare Bot is a cloud platform that empowers developers in Healthcare organizations to build and deploy their compliant, AI-powered virtual health assistants and health bots, that help them improve processes and reduce costs. Package tag package-2020-12-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
