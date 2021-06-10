# Azure Communication Calling Service client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Server Calling Client, simply Install AutoRest and in this folder, run:

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

There is one swagger for Calling management APIs.

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.22
```

## Update generated files for server calling service
To update generated files for calling service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.22

### Code generation settings
``` yaml
tag: package-callingserver-2021-05-18
input-file: https://github.com/Azure/azure-rest-api-specs/blob/37acfb43a99ac90f6cb986f227a34bcfbccd6c5b/specification/communication/data-plane/CallingServer/preview/2021-06-15-preview/communicationservicescallingserver.json
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.callingserver
custom-types: ToneValue,ToneInfo,ResultInfo,JoinCallResult,CancelAllMediaOperationsResult,PlayAudioResult,OperationStatus,StartCallRecordingResult,CallRecordingStateResult,CallRecordingState,CallConnectionState,CreateCallResult,EventSubscriptionType,CallModality
custom-types-subpackage: models
generate-client-as-impl: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
title: Azure Communication CallingServer Service 
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    delete  $["CallConnectionStateChangedEvent"];
    delete  $["CallRecordingStateChangeEvent"];
    delete  $["InviteParticipantsResultEvent"];
    delete  $["PlayAudioResultEvent"];
    delete  $["ToneReceivedEvent"]; 
- rename-model:
    from: CreateCallRequest
    to: CreateCallRequestInternal
- rename-model:
    from: ParticipantsUpdatedEvent
    to: ParticipantsUpdatedEventInternal
- rename-model:
    from: CommunicationParticipant
    to: CommunicationParticipantInternal
- rename-model:
    from: GetCallRecordingStateResponse
    to: CallRecordingStateResult
- rename-model:
    from: StartCallRecordingResponse
    to: StartCallRecordingResult
- rename-model:
    from: CancelAllMediaOperationsResponse
    to: CancelAllMediaOperationsResult
- rename-model:
    from: JoinCallResponse
    to: JoinCallResult
- rename-model:
    from: CreateCallResponse
    to: CreateCallResult 
- rename-model:
    from: PlayAudioResponse
    to: PlayAudioResult
```
