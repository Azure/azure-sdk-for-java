# Azure Key Vault Administration for Java
> see https://aka.ms/autorest

### Setup
Increase max memory if you're using Autorest older than 3. Set the environment variable `NODE_OPTIONS` to `--max-old-space-size=8192`.

This is the AutoRest configuration file for the KeyVaultAccessControlClient and KeyVaultBackupClient.
---
## Getting Started
To build the SDK for either client, simply [Install AutoRest](https://github.com/Azure/autorest/blob/master/docs/install/readme.md) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Generation
There are two swagger specifications for KeyVault Administration: rbac and backuprestore. They use the following tags: `--tag=rbac` & `--tag=backuprestore`.

```ps
cd <swagger-folder>
autorest --use=@microsoft.azure/autorest.java@4.0.0 --tag=${package}
```

e.g.
```ps
cd <swagger-folder>
autorest --use=@microsoft.azure/autorest.java@4.0.0 --tag=rbac
```

```ps
cd <swagger-folder>
autorest --use=@microsoft.azure/autorest.java@4.0.0 --tag=backuprestore
```

## Code generation settings
``` yaml
java: true
output-folder: ../
namespace: com.azure.security.keyvault.administration
license-header: MICROSOFT_MIT_SMALL
models-subpackage: implementation.models
custom-types-subpackage: models
generate-client-as-impl: true
sync-methods: none
add-context-parameter: true
context-client-method-parameter: true
```

### Tag: rbac
These settings apply only when `--tag=rbac` is specified on the command line.

``` yaml $(tag) == 'rbac'
input-file: https://github.com/Azure/azure-rest-api-specs/blob/master/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.3-preview/rbac.json
title: KeyVaultAccessControlClient
```

### Tag: backuprestore
These settings apply only when `--tag=backuprestore` is specified on the command line.

``` yaml $(tag) == 'backuprestore'
input-file: https://github.com/Azure/azure-rest-api-specs/blob/master/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.3-preview/backuprestore.json
title: KeyVaultBackupClient
```
