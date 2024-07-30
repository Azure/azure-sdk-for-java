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
use: '@autorest/java@4.1.24'
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
partial-update: true
```

### Tag: rbac
These settings apply only when `--tag=rbac` is specified on the command line.

``` yaml $(tag) == 'rbac'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/8af9817c15d688c941cda106758045b5deb9a069/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.6-preview.1/rbac.json
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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/8af9817c15d688c941cda106758045b5deb9a069/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.6-preview.1/backuprestore.json
title: KeyVaultBackupClient
customization-class: src/main/java/BackupRestoreCustomizations.java
```

### Tag: settings
These settings apply only when `--tag=settings` is specified on the command line.

``` yaml $(tag) == 'settings'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/8af9817c15d688c941cda106758045b5deb9a069/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.6-preview.1/settings.json
title: KeyVaultSettingsClient
custom-types: KeyVaultSettingType
customization-class: src/main/java/SettingsCustomizations.java
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
