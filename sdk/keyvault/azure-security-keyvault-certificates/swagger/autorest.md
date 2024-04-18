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
use: '@autorest/java@4.1.22'
output-folder: ../
java: true
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/8af9817c15d688c941cda106758045b5deb9a069/specification/keyvault/data-plane/Microsoft.KeyVault/preview/7.6-preview.1/certificates.json
title: CertificateClient
namespace: com.azure.security.keyvault.certificates
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: AdministratorContact,CertificateContact,CertificateKeyCurveName,CertificateKeyType,CertificateKeyUsage,CertificateOperationError,CertificatePolicyAction,SubjectAlternativeNames
customization-class: src/main/java/CertificatesCustomizations.java
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
        from: AdministratorDetails
        to: AdministratorContact
    - rename-model:
        from: Contact
        to: CertificateContact
```

### AdministratorContact EmailAddress -> Email
```yaml
directive:
  - from: swagger-document
    where: $.definitions.AdministratorContact
    transform: >
      delete $.properties.email["x-ms-client-name"];
```

### CertificateContact EmailAddress -> Email
```yaml
directive:
  - from: swagger-document
    where: $.definitions.CertificateContact
    transform: >
      delete $.properties.email["x-ms-client-name"];
```

### Rename KeyUsageType to CertificateKeyUsage and fix enum value name
```yaml
directive:
  - from: "certificates.json"
    where: $.definitions
    transform: >
      $.CertificateKeyUsage = $.X509CertificateProperties.properties["key_usage"].items;
      delete $.X509CertificateProperties.properties["key_usage"].items;
      $.X509CertificateProperties.properties["key_usage"].items = { "$ref": "#/definitions/CertificateKeyUsage" };
      $.CertificateKeyUsage["x-ms-enum"].name = "CertificateKeyUsage";
      $.CertificateKeyUsage["x-ms-enum"].values = $.CertificateKeyUsage.enum.map(item => {
        if (item === "cRLSign") {
          return {
            "name": "CrlSign",
            "value": "cRLSign"
          }
        }
        
        return {
          "name": item,
          "value": item
        }
      });
```

### Change CertificatePolicyAction to an extensible enum
```yaml
directive:
  - from: swagger-document
    where: $.definitions.Action
    transform: >
      $.properties["action_type"]["x-ms-enum"].modelAsString = true;
```

### Rename SubjectAlternativeNames upns to userPrincipalNames
```yaml
directive:
  - from: swagger-document
    where: $.definitions.SubjectAlternativeNames.properties
    transform: >
      $.upns["x-ms-client-name"] = "userPrincipalNames";
```

### Rename Error to CertificateOperationError
```yaml
directive:
  - from: "common.json"
    where: $.definitions
    transform: >
      $.CertificateOperationError = $.Error;
      delete $.Error;
      
      $.CertificateOperationError.properties.innererror["$ref"] = "#/definitions/CertificateOperationError";
      $.KeyVaultError.properties.error["$ref"] = "#/definitions/CertificateOperationError";
```

```yaml
directive:
  - from: swagger-document
    where: $.definitions.CertificateOperation
    transform: >
      $.properties.error["$ref"] = "common.json#/definitions/CertificateOperationError";
```

### Rename JsonWebKeyType to CertificateKeyType
```yaml
directive:
  - from: "keys.json"
    where: $.definitions
    transform: >
      $.CertificateKeyType = $.JsonWebKey.properties.kty;
      $.CertificateKeyType.enum = $.CertificateKeyType.enum.filter(item => item != "oct" && item != "oct-HSM");
      $.CertificateKeyType["x-ms-enum"].name = "CertificateKeyType";
      $.CertificateKeyType["x-ms-enum"].values = $.CertificateKeyType["x-ms-enum"].values
          .filter(item => item.value != "oct" && item.value != "oct-HSM")
          .map(item => {
            if (item.value === "EC-HSM") {
              item.name = "EcHsm";
            } else if (item.value === "RSA-HSM") {
              item.name = "RsaHsm";
            }
            
            return item;
          });
      
      delete $.JsonWebKey.properties.kty;
      $.JsonWebKey.properties.kty = { "$ref": "#/definitions/CertificateKeyType" };
        
      delete $.KeyProperties.properties.kty;
      $.KeyProperties.properties.kty = { "$ref": "#/definitions/CertificateKeyType" };

      delete $.KeyCreateParameters.properties.kty;
      $.KeyCreateParameters.properties.kty = { "$ref": "#/definitions/CertificateKeyType" };
```

### Rename JsonWebKeyCurveName to CertificateKeyCurveName
```yaml
directive:
  - from: "keys.json"
    where: $.definitions
    transform: >
      $.CertificateKeyCurveName = $.JsonWebKey.properties.crv;
      $.CertificateKeyCurveName["x-ms-enum"].name = "CertificateKeyCurveName";
      
      delete $.JsonWebKey.properties.crv;
      $.JsonWebKey.properties.crv = { "$ref": "#/definitions/CertificateKeyCurveName" };
        
      delete $.KeyProperties.properties.crv;
      $.KeyProperties.properties.crv = { "$ref": "#/definitions/CertificateKeyCurveName" };

      delete $.KeyCreateParameters.properties.crv;
      $.KeyCreateParameters.properties.crv = { "$ref": "#/definitions/CertificateKeyCurveName" };
```
