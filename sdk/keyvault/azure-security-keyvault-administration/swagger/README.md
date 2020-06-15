# Azure Key Vault Administration for Java
> see https://aka.ms/autorest

### Setup
Increase max memory if you're using Autorest older than 3. Set the environment variable `NODE_OPTIONS` to `--max-old-space-size=8192`.

This is the AutoRest configuration file for KeyVaultAccessControlClient.
---
## Getting Started 
To build the SDK for KeyVaultAccessControlClient, simply [Install AutoRest](https://aka.ms/autorest/install) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Generation
There is one swagger for KeyVault Administration: rbac. It uses the following tag: `--tag=rbac-preview`.

```ps
cd <swagger-folder>
autorest --use=@microsoft.azure/autorest.java@3.0.4 --tag=${package} --version=2.0.4413 
```

e.g.
```ps
cd <swagger-folder>
autorest --use=@microsoft.azure/autorest.java@3.0.4 --tag=rbac-preview --version=2.0.4413  
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
generate-client-interfaces: false
sync-methods: all
add-context-parameter: true
```

### Tag: rbac-preview
These settings apply only when `--tag=package-2019-05-searchservice-preview` is specified on the command line.

``` yaml $(tag) == 'rbac-preview'
input-file: https://github.com/Azure/azure-rest-api-specs/blob/master/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.2/rbac.json
title: AccessControlClient
```
