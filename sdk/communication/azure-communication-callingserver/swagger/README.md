# Azure Communication Calling Service client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Server Calling Client, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation

There is one swagger for Calling management APIs.

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.20 --use=@autorest/modelerfour@4.15.442
```

## Update generated files for server calling service
To update generated files for calling service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.20 --use=@autorest/modelerfour@4.15.442

### Code generation settings
``` yaml
tag: package-2022-04-07-preview
require:
    - https://github.com/richardcho-msft/azure-rest-api-specs/blob/dev-communication-CallingServer-2022-04-07-preview/specification/communication/data-plane/CallingServer/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.callingserver
custom-types: ToneValue,OperationStatus,CallRecordingState,CallConnectionState,EventSubscriptionType,MediaType,RecordingChannelType,RecordingContentType,RecordingFormatType
custom-types-subpackage: models
generate-client-as-impl: true
service-interface-as-public: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
title: Azure Communication CallingServer Service 
directive:
- rename-model:
    from: AcsCallParticipantDto
    to: AcsCallParticipantInternal    
- rename-model:
    from: AddParticipantsRequest
    to: AddParticipantsRequestInternal    
- rename-model:
    from: AddParticipantsResponse
    to: AddParticipantsResponseInternal
- rename-model:
    from: CallConnectionPropertiesDto
    to: CallConnectionPropertiesInternal     
- rename-model:
    from: CallingOperationResultDetailsDto
    to: CallingOperationResultDetailsInternal
- rename-model:
    from: CallingOperationStatusDto
    to: CallingOperationStatusInternal
- rename-model:
    from: CallSourceDto
    to: CallSourceInternal
- rename-model:
    from: CommunicationCloudEnvironmentModel
    to: CommunicationCloudEnvironmentInternal
- rename-model:
    from: GetParticipantsResponse
    to: GetParticipantsResponseInternal
- rename-model:
    from: RemoveParticipantsRequest
    to: RemoveParticipantsRequestInternal
- rename-model:
    from: RemoveParticipantsResponse
    to: RemoveParticipantsResponseInternal
- rename-model:
    from: TransferCallResponse
    to: TransferCallResponseInternal
- rename-model:
    from: TransferToParticipantRequest
    to: TransferToParticipantRequestInternal
- rename-model:
    from: CreateCallRequest
    to: CreateCallRequestInternal
- rename-model:
    from: AnswerCallRequest
    to: AnswerCallRequestInternal
- rename-model:
    from: RedirectCallRequest
    to: RedirectCallRequestInternal
- rename-model:
    from: RejectCallRequest
    to: RejectCallRequestInternal
- rename-model:
    from: CallLocator
    to: CallLocatorInternal
- rename-model:
    from: RecordingIdResponse
    to: RecordingIdResponseInternal
- rename-model:
    from: RecordingStatusResponse
    to: RecordingStatusResponseInternal
- rename-model:
    from: PlayResponse
    to: PlayResponseInternal
- rename-model:
    from: PlaySource
    to: PlaySourceInternal
- rename-model:
    from: FileSource
    to: FileSourceInternal
```

### Rename RecordingChannelType to RecordingChannelInternal
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.RecordingChannelType
    transform: >
      $["x-ms-enum"].name = "RecordingChannelInternal";
```

### Rename RecordingContentType to RecordingContentInternal
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.RecordingContentType
    transform: >
      $["x-ms-enum"].name = "RecordingContentInternal";
```

### Rename RecordingFormatType to RecordingFormatInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.RecordingFormatType["x-ms-enum"]
  transform: >
    $.name = "RecordingFormatInternal";
```

### Rename RecordingState to RecordingStateInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.RecordingState["x-ms-enum"]
  transform: >
    $.name = "RecordingStateInternal";
```

### Rename PlaySourceType to PlaySourceTypeInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.PlaySourceType["x-ms-enum"]
  transform: >
    $.name = "PlaySourceTypeInternal";
```

### Rename CallLocatorKind to CallLocatorKindInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.CallLocatorKind["x-ms-enum"]
  transform: >
    $.name = "CallLocatorKindInternal";
```

### Rename CallConnectionStateModel to CallConnectionStateModelInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.CallConnectionStateModel["x-ms-enum"]
  transform: >
    $.name = "CallConnectionStateModelInternal";
```

### Rename RecordingStatus to RecordingStatusInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.RecordingStatus["x-ms-enum"]
  transform: >
    $.name = "RecordingStatusInternal";
```
