# Azure Communication Service room client library for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Communication Rooms.

---
## Getting Started
To build the SDK for Communication Rooms, simply [Install AutoRest](https://aka.ms/autorest) and
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

### Code generation settings
``` yaml
require:
    - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/91813ca7a287fe944262e992413ce4d51d987276/specification/communication/data-plane/Rooms/readme.md
use: '@autorest/java@4.1.42'
java: true
title: AzureCommunicationRoomService
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.rooms
generate-client-as-impl: true
custom-types: RoomJoinPolicy
custom-types-subpackage: models
models-subpackage: implementation.models
sync-methods: all
required-parameter-client-methods: true
enable-sync-stack: true
```

### Rename Role to ParticipantRole
```yaml
directive:
  - from: swagger-document
    where: $.definitions.Role["x-ms-enum"]
    transform: >
      $.name = "ParticipantRole";
```

### Change UpdateParticipantRequest to String
```yaml
directive:
  - from: swagger-document
    where: $.paths["/rooms/{roomId}/participants"].patch.parameters[2]
    transform: >
      $.schema = {
        "type": "string",
        "description": "An updated set of participants of the room."
      };
```
