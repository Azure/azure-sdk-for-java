# Azure Mixed Reality Authentication Service client library for Java

> see https://aka.ms/autorest

## Generation

You can update the codegen by either running the following commands or by running the `update.ps1` script in PowerShell:

```bash
cd sdk/mixedreality/azure-mixedreality-authentication/swagger
autorest autorest.md --java --v4 --use=@autorest/java@4.0.16
```

### Code generation settings

``` yaml
title: MixedRealityStsRestClient
namespace: com.azure.mixedreality.authentication
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/aa19725fe79aea2a9dc580f3c66f77f89cc34563/specification/mixedreality/data-plane/Microsoft.MixedReality/preview/2019-02-28-preview/mr-sts.json
java: true
output-folder: ..\
generate-client-as-impl: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
context-client-method-parameter: true
```
