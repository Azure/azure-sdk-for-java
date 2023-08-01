# Azure Communication Call Automation client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Call Automation Client, simply Install AutoRest and in this folder, run:

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

### Code generation settings
``` yaml
tag: package-2023-01-15-preview
require:
    - https://github.com/williamzhao87/azure-rest-api-specs/blob/0a316f5346cf6dc37200403d6b20e2400157721c/specification/communication/data-plane/CallAutomation/readme.md
java: true
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.callautomation
custom-types: ToneValue,OperationStatus,CallRecordingState,CallConnectionState,EventSubscriptionType,MediaType,RecordingChannelType,RecordingContentType,RecordingFormatType
custom-types-subpackage: models
generate-client-as-impl: true
service-interface-as-public: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
title: Azure Communication Call Automation Service
directive:
- rename-model:
    from: CallParticipant
    to: CallParticipantInternal
- rename-model:
    from: AddParticipantRequest
    to: AddParticipantRequestInternal
- rename-model:
    from: AddParticipantResponse
    to: AddParticipantResponseInternal
- rename-model:
    from: CallConnectionProperties
    to: CallConnectionPropertiesInternal
- rename-model:
    from: CallingOperationResultDetails
    to: CallingOperationResultDetailsInternal
- rename-model:
    from: CallingOperationStatus
    to: CallingOperationStatusInternal
- rename-model:
    from: CommunicationCloudEnvironmentModel
    to: CommunicationCloudEnvironmentInternal
- rename-model:
    from: GetParticipantsResponse
    to: GetParticipantsResponseInternal
- rename-model:
    from: RemoveParticipantRequest
    to: RemoveParticipantRequestInternal
- rename-model:
    from: RemoveParticipantResponse
    to: RemoveParticipantResponseInternal
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
    from: RecordingStateResponse
    to: RecordingStateResponseInternal
- rename-model:
    from: PlayResponse
    to: PlayResponseInternal
- rename-model:
    from: PlaySource
    to: PlaySourceInternal
- rename-model:
    from: FileSource
    to: FileSourceInternal
- rename-model:
    from: TextSource
    to: TextSourceInternal
- rename-model:
    from: SsmlSource
    to: SsmlSourceInternal
- rename-model:
    from: PlayOptions
    to: PlayOptionsInternal
- rename-model:
    from: StartCallRecordingRequest
    to: StartCallRecordingRequestInternal
- rename-model:
    from: ContinuousDtmfRecognitionOptions
    to: ContinuousDtmfRecognitionOptionsInternal
- rename-model:
    from: SendDtmfOptions
    to: SendDtmfOptionsInternal
- rename-model:
    from: SendDtmfRequest
    to: SendDtmfRequestInternal
- rename-model:
    from: ChannelAffinity
    to: ChannelAffinityInternal
- rename-model:
    from: DtmfConfigurations
    to: DtmfConfigurationsInternal
- rename-model:
    from: RecognizeConfigurations
    to: RecognizeConfigurationsInternal
- rename-model:
    from: MediaStreamingConfiguration
    to: MediaStreamingConfigurationInternal
- rename-model:
    from: DtmfOptions
    to: DtmfOptionsInternal
- rename-model:
    from: SpeechOptions
    to: SpeechOptionsInternal
- rename-model:
    from: RecognizeOptions
    to: RecognizeOptionsInternal
- rename-model:
    from: Choice
    to: RecognizeChoiceInternal
- rename-model:
    from: MuteParticipantsRequest
    to: MuteParticipantsRequestInternal
- rename-model:
    from: MuteParticipantsResponse
    to: MuteParticipantsResponseInternal
- rename-model:
    from: UnmuteParticipantsRequest
    to: UnmuteParticipantsRequestInternal
- rename-model:
    from: UnmuteParticipantsResponse
    to: UnmuteParticipantsResponseInternal
- rename-model:
    from: CollectTonesResult
    to: CollectTonesResultInternal
- rename-model:
    from: ChoiceResult
    to: ChoiceResultInternal
- rename-model:
    from: SpeechResult
    to: SpeechResultInternal
- rename-model:
    from: ExternalStorage
    to: ExternalStorageInternal
- rename-model:
    from: BlobStorage
    to: BlobStorageInternal
- rename-model:
    from: ContinuousDtmfRecognitionRequest
    to: ContinuousDtmfRecognitionRequestInternal
- rename-model:
    from: SendDtmfRequest
    to: SendDtmfRequestInternal

# Remove models
- remove-model: AddParticipantFailed
- remove-model: AddParticipantSucceeded
- remove-model: CallConnected
- remove-model: CallDisconnected
- remove-model: CallTransferAccepted
- remove-model: CallTransferFailed
- remove-model: ParticipantsUpdated
- remove-model: RecordingStateChanged
- remove-model: PlayCompleted
- remove-model: PlayFailed
- remove-model: PlayCanceled
- remove-model: ResultInfo
- remove-model: RecognizeCompleted
- remove-model: RecognizeFailed
- remove-model: RecognizeCanceled
- remove-model: ContinuousDtmfRecognitionToneReceived
- remove-model: ToneInfo
- remove-model: ContinuousDtmfRecognitionToneFailed
- remove-model: ContinuousDtmfRecognitionStopped
- remove-model: SendDtmfCompleted
- remove-model: SendDtmfFailed
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

### Rename AcsEventType to AcsEventTypeInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.AcsEventType["x-ms-enum"]
  transform: >
    $.name = "AcsEventTypeInternal";
```

### Rename CallRejectReason to CallRejectReasonInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.CallRejectReason["x-ms-enum"]
  transform: >
    $.name = "CallRejectReasonInternal";
```

### Rename StopTones to StopTonesInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.StopTones["x-ms-enum"]
  transform: >
    $.name = "StopTonesInternal";
```


### Rename RecognizeInputType to RecognizeInputTypeInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.RecognizeInputType["x-ms-enum"]
  transform: >
    $.name = "RecognizeInputTypeInternal";
```

### Rename MediaStreamingAudioChannelType to MediaStreamingAudioChannelTypeInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.MediaStreamingAudioChannelType["x-ms-enum"]
  transform: >
    $.name = "MediaStreamingAudioChannelTypeInternal";
```

### Rename MediaStreamingContentType to MediaStreamingContentTypeInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.MediaStreamingContentType["x-ms-enum"]
  transform: >
    $.name = "MediaStreamingContentTypeInternal";
```

### Rename MediaStreamingTransportType to MediaStreamingTransportType
``` yaml
directive:
- from: swagger-document
  where: $.definitions.MediaStreamingTransportType["x-ms-enum"]
  transform: >
    $.name = "MediaStreamingTransportTypeInternal";
```

### Rename RecognitionType to RecognitionTypeInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.RecognitionType["x-ms-enum"]
  transform: >
    $.name = "RecognitionTypeInternal";
```

### Rename Tone to DtmfTone
``` yaml
directive:
- from: swagger-document
  where: $.definitions.Tone["x-ms-enum"]
  transform: >
    $.name = "DtmfToneInternal";
```

### Rename DtmfOptions to DtmfOptionsInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.DtmfOptions["x-ms-enum"]
  transform: >
    $.name = "DtmfOptionsInternal";
```

### Rename Choice to GenderType
``` yaml
directive:
- from: swagger-document
  where: $.definitions.Gender["x-ms-enum"]
  transform: >
    $.name = "GenderTypeInternal";
```

### Rename RecordingStorageType to RecordingStorageTypeInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions.RecordingStorageType["x-ms-enum"]
  transform: >
    $.name = "RecordingStorageTypeInternal";
```
