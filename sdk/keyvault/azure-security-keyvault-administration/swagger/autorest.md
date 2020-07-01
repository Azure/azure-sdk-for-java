# Azure Key Vault Administration for Java
> see https://aka.ms/autorest

### Setup
Increase max memory if you're using Autorest older than 3. Set the environment variable `NODE_OPTIONS` to `--max-old-space-size=8192`.

This is the AutoRest configuration file for KeyVaultBackupClient.
---
## Getting Started 
To build the SDK for KeyVaultBackupClient, simply [Install AutoRest](https://aka.ms/autorest/install) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Generation
There is one swagger for KeyVault Administration: backuprestore. It uses the following tag: `--tag=backuprestore-preview`.

```ps
cd <swagger-folder>
autorest --use=@microsoft.azure/autorest.java@4.0.0 --tag=${package} 
```

e.g.
```ps
cd <swagger-folder>
autorest --use=@microsoft.azure/autorest.java@4.0.0 --tag=backuprestore-preview
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

### Tag: backuprestore-preview
These settings apply only when `--tag=backuprestore-preview` is specified on the command line.

``` yaml $(tag) == 'backuprestore-preview'
input-file: https://github.com/Azure/azure-rest-api-specs/blob/master/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.2-preview/backuprestore.json
title: KeyVaultBackupClient
```
