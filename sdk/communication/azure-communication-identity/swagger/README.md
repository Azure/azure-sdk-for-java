# Azure Communication Identity library for Java

> see https://aka.ms/autorest

## Getting Started

To build the SDK for Communication Identity library, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:
> `autorest --java`

To see additional help and options, run:
> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest --java
```

### Code generation settings
``` yaml
java: true
output-folder: ..\
use: '@autorest/java@4.1.42'
tag: package-2023-10
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/5797d78f04cd8ca773be82d2c99a3294009b3f0a/specification/communication/data-plane/Identity/readme.md
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.identity
custom-types: CommunicationTokenScope,GetTokenForTeamsUserOptions
custom-types-subpackage: models
models-subpackage: implementation.models
customization-class: src/main/java/TeamsUserExchangeTokenRequestCustomization.java
sync-methods: all
disable-client-builder: true
generate-client-as-impl: true
enable-sync-stack: true
```

### Rename CommunicationIdentityTokenScope to CommunicationTokenScope
```yaml
directive:
  - from: swagger-document
    where: $.definitions.CommunicationIdentityTokenScope
    transform: >
      $["x-ms-enum"].name = "CommunicationTokenScope";
```

### Directive to change TeamsUserExchangeTokenRequest to GetTokenForTeamsUserOptions
```yaml
directive:
  - from: swagger-document
    where: $.definitions.TeamsUserExchangeTokenRequest
    transform: >
      $["x-ms-client-name"] = "GetTokenForTeamsUserOptions";
      $.properties.token["x-ms-client-name"] = "teamsUserAadToken";
      $.properties.appId["x-ms-client-name"] = "clientId";
      $.properties.userId["x-ms-client-name"] = "userObjectId";
```
