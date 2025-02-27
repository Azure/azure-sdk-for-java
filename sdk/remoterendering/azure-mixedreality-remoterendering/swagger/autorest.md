# Azure App Configuration Tutorial for Java

> see https://aka.ms/autorest

## Generation

You can update the codegen by running the following commands:

```bash
cd sdk/remoterendering/azure-mixedreality-remoterendering/swagger
autorest autorest.md
```

### Code generation settings
``` yaml
input-file: "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/2a65b0a2bbd9113b91c889f187d8778c2725c0b9/specification/mixedreality/data-plane/Microsoft.MixedReality/stable/2021-01-01/mr-arr.json"
java: true
use: '@autorest/java@4.1.42'
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.mixedreality.remoterendering
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
models-subpackage: implementation.models
artifact-id: azure-mixedreality-remoterendering
credential-types: tokencredential
required-fields-as-ctor-args: true
```
