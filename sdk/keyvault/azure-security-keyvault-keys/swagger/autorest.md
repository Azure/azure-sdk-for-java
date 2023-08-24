# Azure Key Vault Keys for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for KeyVault Keys.

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
use: '@autorest/java@4.1.19'
output-folder: ../
java: true
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/551275acb80e1f8b39036b79dfc35a8f63b601a7/specification/keyvault/data-plane/Microsoft.KeyVault/stable/7.4/keys.json
title: KeyClient
namespace: com.azure.security.keyvault.keys
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: KeyCurveName,KeyExportEncryptionAlgorithm,KeyOperation,KeyRotationPolicyAction,KeyType
customization-class: src/main/java/KeysCustomizations.java
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

### Rename expandable string enum models

```yaml
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.KeyType = $["JsonWebKey"]["properties"]["kty"];
      $.KeyType["x-ms-enum"].name = "KeyType";
      $["JsonWebKey"]["properties"]["kty"] = { "$ref": "#/definitions/KeyType" };
```

```yaml
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.KeyProperties.properties.kty = { "$ref": "#/definitions/KeyType" };
      $.KeyCreateParameters.properties.kty = { "$ref": "#/definitions/KeyType" };

      $.KeyCurveName = $["JsonWebKey"]["properties"]["crv"];
      $.KeyCurveName.description = "Elliptic curve name.";
      $.KeyCurveName["x-ms-enum"].name = "KeyCurveName";
      $["JsonWebKey"]["properties"]["crv"] = { "$ref": "#/definitions/KeyCurveName" };
      $.KeyProperties.properties.crv = { "$ref": "#/definitions/KeyCurveName" };
      $.KeyCreateParameters.properties.crv = { "$ref": "#/definitions/KeyCurveName" };
```

```yaml
directive:
  - from: swagger-document
    where: $.definitions.KeyExportParameters
    transform: >
      $.properties.enc["x-ms-enum"].name = "KeyExportEncryptionAlgorithm";
```

```yaml
directive:
  - from: swagger-document
    where: $.definitions.KeyCreateParameters
    transform: >
      $.properties["key_ops"].items["x-ms-enum"].name = "KeyOperation";
```
