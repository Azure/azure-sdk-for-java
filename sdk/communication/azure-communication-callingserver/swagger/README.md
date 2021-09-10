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
tag: package-2021-08-30-preview
require:
    - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/3893616381e816729ef9cdd768e87fb2845e189d/specification/communication/data-plane/CallingServer/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.callingserver
custom-types: ToneValue,OperationStatus,CallRecordingState,CallConnectionState,EventSubscriptionType,MediaType,RecordingChannelType,RecordingContentType,RecordingFormatType
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
    from: AddParticipantResultEvent
    to: AddParticipantResultEventInternal    
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
    from: CallParticipant
    to: CallParticipantInternal
- rename-model:
    from: JoinCallResult
    to: JoinCallResultInternal
- rename-model:
    from: PlayAudioResult
    to: PlayAudioResultInternal
- rename-model:
    from: CallRecordingProperties
    to: CallRecordingPropertiesInternal
- rename-model:
    from: StartCallRecordingResult
    to: StartCallRecordingResultInternal
- rename-model:
    from: CreateCallResult
    to: CreateCallResultInternal
- rename-model:
    from: AddParticipantResult
    to: AddParticipantResultInternal    
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
