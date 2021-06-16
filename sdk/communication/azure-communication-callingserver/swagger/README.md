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
autorest README.md --java --v4 --use=@autorest/java@4.0.20 --use=@autorest/modelerfour@4.15.442
```

## Update generated files for server calling service
To update generated files for calling service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.20 --use=@autorest/modelerfour@4.15.442

### Code generation settings
``` yaml
tag: package-2021-06-15-preview
require:
    - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/74575fadb83fcd08a70d1168c8d04a6ca5e90715/specification/communication/data-plane/CallingServer/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.callingserver
custom-types: ToneValue,OperationStatus,CallRecordingState,CallConnectionState,EventSubscriptionType,CallModality
custom-types-subpackage: models
generate-client-as-impl: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
title: Azure Communication CallingServer Service 
directive:
- rename-model:
    from: CallRecordingStateChangeEvent
    to: CallRecordingStateChangeEventInternal    
- rename-model:
    from: InviteParticipantsResultEvent
    to: InviteParticipantsResultEventInternal    
- rename-model:
    from: PlayAudioResultEvent
    to: PlayAudioResultEventInternal   
- rename-model:
    from: ToneReceivedEvent
    to: ToneReceivedEventInternal      
- rename-model:
    from: CallConnectionStateChangedEvent
    to: CallConnectionStateChangedEventInternal
- rename-model:
    from: ParticipantsUpdatedEvent
    to: ParticipantsUpdatedEventInternal
- rename-model:
    from: CommunicationParticipant
    to: CommunicationParticipantInternal
- rename-model:
    from: JoinCallResult
    to: JoinCallResultInternal
- rename-model:
    from: PlayAudioResult
    to: PlayAudioResultInternal
- rename-model:
    from: CallRecordingStateResult
    to: CallRecordingStateResultInternal
- rename-model:
    from: StartCallRecordingResult
    to: StartCallRecordingResultInternal
- rename-model:
    from: CreateCallResult
    to: CreateCallResultInternal
- rename-model:
    from: CancelAllMediaOperationsResult
    to: CancelAllMediaOperationsResultInternal
- rename-model:
    from: ResultInfo
    to: ResultInfoInternal
- rename-model:
    from: ToneInfo
    to: ToneInfoInternal                        
```
