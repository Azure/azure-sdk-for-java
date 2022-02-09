# Azure App Configuration Tutorial for Java

> see https://aka.ms/autorest

## Generation

You can update the codegen by running the following commands:

```bash
cd sdk/remoterendering/azure-mixedreality-remoterendering/swagger
autorest autorest.md --java --use=@autorest/java@4.0.3
```

### Code generation settings
``` yaml
input-file: "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/2a65b0a2bbd9113b91c889f187d8778c2725c0b9/specification/mixedreality/data-plane/Microsoft.MixedReality/stable/2021-01-01/mr-arr.json"
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.mixedreality.remoterendering
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
context-client-method-parameter: true
artifact-id: azure-mixedreality-remoterendering
credential-types: tokencredential
required-fields-as-ctor-args: true
```
