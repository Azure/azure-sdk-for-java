# Azure Communication Service room client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Rooms Client, simply Install AutoRest and in this folder, run the below:

### Generation

There is one swagger for Rooms management APIs.

```ps
cd <swagger-folder>
autorest README.md
```

## Update generated files for rooms service
To update generated files for rooms service, run the following command

> autorest README.md

### Code generation settings
``` yaml
require:
    - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/e30976f6ccb058a36cd2f9d5160e1fd51f6c5d95/specification/communication/data-plane/Rooms/readme.md
use: '@autorest/java@4.0.59'
java: true
title: AzureCommunicationRoomService
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.rooms
generate-client-as-impl: true
custom-types: RoomJoinPolicy
custom-types-subpackage: models
models-subpackage: implementation.models
generate-client-interfaces: false
service-interface-as-public: true
generate-sync-async-clients: false
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: true
custom-strongly-typed-header-deserialization: true
generic-response-type: true
```
