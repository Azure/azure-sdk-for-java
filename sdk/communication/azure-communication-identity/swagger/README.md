# Azure Communication Administration library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Administration library, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.1 --tag=identity
```

### Tag: identity

These settings apply only when `--tag=identity` is specified on the command line.

``` yaml $(tag) == 'identity'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/14bfbf5d0ff8f0dc1358e6e60362e99d0a649ba7/specification/communication/data-plane/Microsoft.CommunicationServicesIdentity/stable/2021-03-07/CommunicationIdentity.json
add-context-parameter: true
custom-types: CommunicationIdentityAccessToken,CommunicationIdentityTokenScope,CommunicationUserToken
custom-types-subpackage: models
models-subpackage: implementation.models
```

### Rename CommunicationIdentityAccessToken to CommunicationUserToken

``` yaml
directive:
    - rename-model:
        from: CommunicationIdentityAccessToken
        to: CommunicationUserToken
```

### Rename searchId to reservationId in CreateSearchResponse

``` yaml
directive:
  - from: swagger-document
    where: $.definitions.CreateSearchResponse.properties.searchId
    transform: >
      $["x-ms-client-name"] = "reservationId";
```

### Code generation settings

``` yaml
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.identity
generate-client-as-impl: true
custom-types-subpackage: models
sync-methods: all
context-client-method-parameter: true
```
