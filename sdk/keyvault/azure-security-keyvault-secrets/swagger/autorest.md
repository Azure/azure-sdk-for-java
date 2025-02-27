# Azure Key Vault Secrets for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for KeyVault Secrets.

---
## Getting Started
To build the SDK for KeyVault Secrets, simply [Install Autorest](https://aka.ms/autorest) and
in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest
```

## Configuration

```yaml
use: '@autorest/java@4.1.42'
output-folder: ../
java: true
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/8af9817c15d688c941cda106758045b5deb9a069/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.6-preview.1/secrets.json
title: SecretClient
namespace: com.azure.security.keyvault.secrets
models-subpackage: implementation.models
custom-types-subpackage: models
enable-sync-stack: true
generate-client-as-impl: true
license-header: MICROSOFT_MIT_SMALL
disable-client-builder: true
```

### Rename SecretSetParameters.contentType and SecretUpdateParameters.contentType to secretContentType

This solves an issue with generators after 4.1.29 (uncertain of which version as the update went from 4.1.29 to 4.1.42)
where in the generated APIs using these types as parameters the previous constant for the request content type of
`String contentType = "application/json"` was removed and replaced with the `contentType` value for `SecretSetParameters`
or `SecretUpdateParameters`. Obtusely, this change causes the interface method to no longer add `@HeaderParam("Content-Type")`
using the `contentType` from `SecretSetParameters` or `SecretUpdateParameters` as the value, but fixes the issue as the
`@BodyParam` will set the content type to `application/json` as expected.

```yaml
directive:
  - from: secrets.json
    where: $.definitions
    transform: >
      $.SecretSetParameters.properties.contentType["x-ms-client-name"] = "secretContentType";
      $.SecretUpdateParameters.properties.contentType["x-ms-client-name"] = "secretContentType";
```
