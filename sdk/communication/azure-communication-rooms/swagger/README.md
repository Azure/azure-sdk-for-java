# Azure Communication Rooms Service client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Rooms Client, simply Install AutoRest and in this folder, run:

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

There is one swagger for Azure Communication Service Rooms APIs.

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.1
```

## Update generated files for Rooms service
To update generated files for Rooms service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.1

### Code generation settings
``` yaml
tag: package-rooms-2022-02-01-preview
input-file:
    -  $(this-folder)/rooms.json
add-context-parameter: true
custom-types: CommunicationRoom
custom-types-subpackage: models
models-subpackage: implementation.models
```

### Code generation settings

``` yaml
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.rooms
generate-client-as-impl: true
custom-types-subpackage: models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
```
