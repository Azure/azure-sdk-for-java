# Azure Key Vault Certificates for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for Key Vault Certificates.

---
## Getting Started
To build the SDK for Key Vault Certificates, simply [Install Autorest](https://aka.ms/autorest) and
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
use: '@autorest/java@4.1.21'
output-folder: ../../generated-keyvault-certificates
java: true
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/6a2e3c7617314fe4ea7e5706da5437214e8a602b/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.5-preview.1/certificates.json
title: CertificateClient
namespace: com.azure.security.keyvault.certificates
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
