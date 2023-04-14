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
    - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/af0e925f435eed808cfa0168057405a43991c7ab/specification/communication/data-plane/Rooms/readme.md
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
url-as-string: true
context-client-method-parameter: true
required-parameter-client-methods: true
custom-strongly-typed-header-deserialization: true
generic-response-type: true
```
 
### Rename Role to ParticipantRole
```yaml
directive:
  - from: swagger-document
    where: $.definitions.Role["x-ms-enum"]
    transform: >
      $.name = "ParticipantRole";
```

```yaml
directive:
- from: swagger-document
  where: $["paths"]["/rooms/{roomId}/participants"]
  transform: >
    $["patch"] = {
      "tags": [
        "Participants"
      ],
      "summary": "Update participants in a room.",
      "operationId": "Participants_Update",
      "consumes": [
        "application/merge-patch+json"
      ],
      "produces": [
        "application/json"
      ],
      "parameters": [
        {
          "in": "path",
          "name": "roomId",
          "description": "The id of the room to update the participants in.",
          "required": true,
          "type": "string"
        },
        {
          "$ref": "#/parameters/ApiVersionParameter"
        },
        {
          "in": "body",
          "name": "updateParticipantsRequest",
          "description": "An updated set of participants of the room.",
          "required": true,
          "type": "string"
        }
      ],
      "responses": {
        "200": {
          "description": "The participants were successfully updated.",
          "schema": {
            "$ref": "#/definitions/UpdateParticipantsResult"
          }
        },
        "default": {
          "description": "Error response",
          "schema": {
            "$ref": "#/definitions/CommunicationErrorResponse"
          },
          "headers": {
            "x-ms-error-code": {
              "x-ms-client-name": "ErrorCode",
              "type": "string"
            }
          }
        }
      },
      "x-ms-examples": {
        "Update participants": {
          "$ref": "./examples/Participants_Update.json"
        }
      },
      "headers": {
        "x-ms-error-code": {
          "x-ms-client-name": "ErrorCode",
          "type": "string"
        }
      },
      "x-ms-examples": {
        "Update participants": {
          "$ref": "./examples/Participants_Update.json"
        }
      }
    }
```
