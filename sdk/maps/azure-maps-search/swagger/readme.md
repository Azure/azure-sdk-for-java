# Azure Maps Search for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Maps Search.
---
## Getting Started

To build the SDK for Maps Search, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

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

### Basic Information

These are the global settings for Search Client.

``` yaml
directive:
  - from: swagger-document
    where: "$"
    transform: >
      $["securityDefinitions"] = {};
      $["security"] = [];
  
  - rename-model:
      from: Address
      to: MapsSearchAddress
  - rename-model:
      from: SearchAddressBatchResult
      to: SearchAddressBatchResultPrivate
  - rename-model:
      from: SearchAddressBatchItem
      to: SearchAddressBatchItemPrivate
  - rename-model:
      from: ReverseSearchAddressBatchProcessResult
      to: ReverseSearchAddressBatchResultPrivate
  - rename-model:
      from: ReverseSearchAddressBatchItem
      to: ReverseSearchAddressBatchItemPrivate

title: SearchClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Search/stable/2023-06-01/search.json
namespace: com.azure.maps.search
java: true
use: '@autorest/java@4.1.29'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
payload-flattening-threshold: 0
add-context-parameter: true
context-client-method-parameter: true
client-logger: true
generate-client-as-impl: true
sync-methods: all
generate-sync-async-clients: false
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: MapsSearchAddress,Boundary
customization-class: src/main/java/SearchCustomization.java
generic-response-type: true
no-custom-headers: true
```
