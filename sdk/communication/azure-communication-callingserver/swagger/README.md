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
tag: package-2021-11-15-preview
require:
    - https://raw.githubusercontent.com/navali-msft/azure-rest-api-specs/c16d5c3b668207b9ec101294a9f05a20e7281083/specification/communication/data-plane/CallingServer/readme.md
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.callingserver
custom-types: AudioRoutingMode,CallRejectReason,CallingOperationStatus,CallRecordingState,CallConnectionState,CallingEventSubscriptionType,CallMediaType,RecordingChannelType,RecordingContentType,RecordingFormatType,ToneValue
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
    from: CreateAudioRoutingGroupResult
    to: CreateAudioRoutingGroupResultInternal
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
    from: CallingOperationResultDetails
    to: CallingOperationResultDetailsInternal
- rename-model:
    from: ToneInfo
    to: ToneInfoInternal
- rename-model:
    from: AnswerCallResult
    to: AnswerCallResultInternal
- rename-model:
    from: CallConnectionProperties
    to: CallConnectionPropertiesInternal
```

### Rename RecordingChannelType to RecordingChannel
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.RecordingChannelType
    transform: >
      $["x-ms-enum"].name = "RecordingChannel";
```

### Rename RecordingContentType to RecordingContent
``` yaml
directive:
  - from: swagger-document
    where: $.definitions.RecordingContentType
    transform: >
      $["x-ms-enum"].name = "RecordingContent";
```

### Rename RecordingFormatType to RecordingFormat
``` yaml
directive:
- from: swagger-document
  where: $.definitions.RecordingFormatType["x-ms-enum"]
  transform: >
    $.name = "RecordingFormat";
```
