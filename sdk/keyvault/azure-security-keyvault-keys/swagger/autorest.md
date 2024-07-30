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
use: '@autorest/java@4.1.22'
output-folder: ../
java: true
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/8af9817c15d688c941cda106758045b5deb9a069/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.6-preview.1/keys.json
title: KeyClient
namespace: com.azure.security.keyvault.keys
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: KeyCurveName,KeyExportEncryptionAlgorithm,KeyOperation,KeyRotationPolicyAction,KeyType,ReleaseKeyResult
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
directive:
    - rename-model:
        from: KeyReleaseResult
        to: ReleaseKeyResult
```

### Rename expandable string enum models

```yaml
directive:
  - from: "keys.json"
    where: $.definitions
    transform: >
      $.KeyType = $.JsonWebKey.properties.kty;
      $.KeyType["x-ms-enum"].name = "KeyType";
      $.JsonWebKey.properties.kty = { "$ref": "#/definitions/KeyType" };
      $.KeyProperties.properties.kty = { "$ref": "#/definitions/KeyType" };
      $.KeyCreateParameters.properties.kty = { "$ref": "#/definitions/KeyType" };

      $.KeyCurveName = $.JsonWebKey.properties.crv;
      $.KeyCurveName.description = "Elliptic curve name.";
      $.KeyCurveName["x-ms-enum"].name = "KeyCurveName";
      $.JsonWebKey.properties.crv = { "$ref": "#/definitions/KeyCurveName" };
      $.KeyProperties.properties.crv = { "$ref": "#/definitions/KeyCurveName" };
      $.KeyCreateParameters.properties.crv = { "$ref": "#/definitions/KeyCurveName" };

      $.KeyExportEncryptionAlgorithm = $.KeyExportParameters.properties.enc;
      $.KeyExportEncryptionAlgorithm["x-ms-enum"].name = "KeyExportEncryptionAlgorithm";
      $.KeyExportParameters.properties.enc = { "$ref": "#/definitions/KeyExportEncryptionAlgorithm" };
      $.KeyReleaseParameters.properties.enc = { "$ref": "#/definitions/KeyExportEncryptionAlgorithm" };

      $.KeyOperation = $.KeyCreateParameters.properties.key_ops.items;
      $.KeyOperation.enum = $.KeyOperation.enum.filter(item => item !== "export");
      $.KeyOperation["x-ms-enum"].name = "KeyOperation";
      $.JsonWebKey.properties.key_ops.items = { "$ref": "#/definitions/KeyOperation" };
      $.KeyCreateParameters.properties.key_ops.items = { "$ref": "#/definitions/KeyOperation" };
      $.KeyUpdateParameters.properties.key_ops.items = { "$ref": "#/definitions/KeyOperation" };
```
