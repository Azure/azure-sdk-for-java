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
use: '@autorest/java@4.1.22'
output-folder: ../
java: true
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/a2f6f742d088dcc712e67cb2745d8271eaa370ff/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.5-preview.1/secrets.json
title: SecretClient
namespace: com.azure.security.keyvault.secrets
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
```
