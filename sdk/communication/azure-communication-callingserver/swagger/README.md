# Azure Communication Calling Service client library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Chat Client, simply Install AutoRest and in this folder, run:

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

## Update generated files for chat service
To update generated files for calling service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.22

### Code generation settings
``` yaml
tag: package-callingserver-2021-05-18
input-file: https://github.com/Azure/azure-rest-api-specs/blob/9550e58c98dc0af9474d896493335bf0543b2b4d/specification/communication/data-plane/CallingServer/preview/2021-04-15-preview1/communicationservicescallingserver.json
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.callingserver
generate-client-as-impl: true
models-subpackage: implementation.models
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
title: Azure Communication CallingServer Service
directive:
    - rename-model:
        from: CreateCallRequest
        to: CreateCallRequestInternal
    - rename-model:
        from: PlayAudioRequest
        to: PlayAudioRequestInternal
    - rename-model:
        from: InviteParticipantsRequest
        to: InviteParticipantsRequestInternal
    - rename-model:
        from: StartCallRecordingRequest
        to: StartCallRecordingRequestInternal
    - rename-model:
        from: ResultInfo
        to: ResultInfoInternal
    - rename-model:
        from: JoinCallRequest
        to: JoinCallRequestInternal
```
