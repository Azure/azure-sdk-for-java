# Azure Key Vault Administration for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for KeyVault Administration.

---
## Getting Started
To build the SDK for KeyVault Administration, simply [Install Autorest](https://aka.ms/autorest) and 
in this folder, run:

> `autorest --tag={swagger specification}`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

There are three swagger specifications for KeyVault Administration: `rbac`, `backuprestore` and `settings`. 
They use the following tags respectively: `--tag=rbac`, `--tag=backuprestore`, `--tag=settings`.

```ps
cd <swagger-folder>
autorest --tag={swagger specification}
```

e.g.
```ps
cd <swagger-folder>
autorest --tag=rbac
autorest --tag=backuprestore
autorest --tag=settings
```

## Configuration
```yaml
use: '@autorest/java@4.1.16'
output-folder: ../
java: true
namespace: com.azure.security.keyvault.administration
models-subpackage: implementation.models
custom-types-subpackage: models
enable-sync-stack: true
generate-client-interfaces: false
generate-client-as-impl: true
service-interface-as-public: true
license-header: MICROSOFT_MIT_SMALL
disable-client-builder: true
add-context-parameter: true
context-client-method-parameter: true
generic-response-type: true
stream-style-serialization: true
required-fields-as-ctor-args: true
include-read-only-in-constructor-args: true
custom-package-info-descriptions:
  com.azure.security.keyvault.administration.implementation.models: 'Package containing the data models for KeyVaultAccessControlClient, KeyVaultBackupClient, and KeyVaultSettingsClient. The key vault client performs cryptographic key operations and vault operations against the Key Vault service.'
  com.azure.security.keyvault.administration.implementation: 'Package containing the implementations for KeyVaultAccessControlClient, KeyVaultBackupClient, and KeyVaultSettingsClient. The key vault client performs cryptographic key operations and vault operations against the Key Vault service.'
  com.azure.security.keyvault.administration.models: 'Package containing classes used by {@link com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient} and {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} to perform access control operations on Azure Key Vault resources, as well as classes used by {@link com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient} and {@link com.azure.security.keyvault.administration.KeyVaultBackupClient} to perform backup and restore operations on Azure Key Vault keys.'
  com.azure.security.keyvault.administration: 'Package containing classes for creating clients {@link com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient} and {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} that perform access control operations on Azure Key Vault resources, as well as clients {@link com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient} and {@link com.azure.security.keyvault.administration.KeyVaultBackupClient} that perform backup and restore operations on Azure Key Vault keys.'
```

### Tag: rbac
These settings apply only when `--tag=rbac` is specified on the command line.

``` yaml $(tag) == 'rbac'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/551275acb80e1f8b39036b79dfc35a8f63b601a7/specification/keyvault/data-plane/Microsoft.KeyVault/stable/7.4/rbac.json
title: KeyVaultAccessControlClient
custom-types: KeyVaultDataAction,KeyVaultRoleDefinitionType,KeyVaultRoleScope,KeyVaultRoleType
customization-class: src/main/java/RbacCustomizations.java
```

#### Enum Renames
``` yaml $(tag) == 'rbac'
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.DataAction["x-ms-enum"].name = "KeyVaultDataAction";
      $.RoleDefinitionProperties.properties.type["x-ms-enum"].name = "KeyVaultRoleType";
      $.RoleScope["x-ms-enum"].name = "KeyVaultRoleScope";
      $.RoleDefinition.properties.type["x-ms-enum"].name = "KeyVaultRoleDefinitionType";
```

### Tag: backuprestore
These settings apply only when `--tag=backuprestore` is specified on the command line.

``` yaml $(tag) == 'backuprestore'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/551275acb80e1f8b39036b79dfc35a8f63b601a7/specification/keyvault/data-plane/Microsoft.KeyVault/stable/7.4/backuprestore.json
title: KeyVaultBackupClient
```

### Tag: settings
These settings apply only when `--tag=settings` is specified on the command line.

``` yaml $(tag) == 'settings'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/551275acb80e1f8b39036b79dfc35a8f63b601a7/specification/keyvault/data-plane/Microsoft.KeyVault/stable/7.4/settings.json
title: KeyVaultSettingsClient
custom-types: KeyVaultSettingType
```

#### Rename SettingTypeEnum to KeyVaultSettingType
``` yaml $(tag) == 'settings'
directive:
  - from: swagger-document
    where: $.definitions.Setting
    transform: >
      $.properties.type["x-ms-enum"].name = "KeyVaultSettingType";
```

#### Bug in Autorest Java for required properties that are flattened in operation definition
``` yaml $(tag) == 'settings'
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      delete $.UpdateSettingRequest.required;
```
