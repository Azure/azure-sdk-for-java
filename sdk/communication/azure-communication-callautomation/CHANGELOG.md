# Release History

## 1.1.2 (2024-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-communication-common` from `1.2.15` to version `1.3.0`.


## 1.1.1 (2023-12-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.14` to version `1.2.15`.
- Upgraded `azure-core` from `1.44.0` to version `1.45.1`.

## 1.1.0 (2023-11-23)

### Features Added

- Mid-Call actions support overriding callback uri
- Cancel adding Participant invitation
- Support transfer a participant in a group call to another participant
- Add Custom Context payload to Transfer and AddParticipant API

### Other Changes

- Dependency versions updated.

## 1.0.6 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-communication-common` from `1.2.13` to version `1.2.14`.

## 1.0.5 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-communication-common` from `1.2.12` to version `1.2.13`.

## 1.0.4 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-communication-common` from `1.2.11` to version `1.2.12`.

## 1.0.3 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 1.1.0-beta.1 (2023-08-17)

### Features Added

- Play and recognize supports TTS and SSML source prompts.
- Recognize supports choices and freeform speech.
- Start/Stop continuous DTMF recognition by subscribing/unsubscribing to tones.
- Send DTMF tones to a participant in the call.
- Mute participants in the call.

## 1.0.2 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-communication-common` from `1.2.9` to version `1.2.10`.


## 1.0.1 (2023-06-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-communication-common` from `1.2.8` to version `1.2.9`.

## 1.0.0 (2023-06-14)

### Features Added
- Outbound calls can now be created without providing a User Identifier. This value can be specified in the CallAutomationClientOption if desired.
- AnswerCall now accepts OperationContext.
- Calls can be answered by a specific communication identifier user.
- RemoveParticipant now sends success and failure events with the request.
- ParticipantsUpdated event now includes a sequence number to distinguish the ordering of events.
- CallConnectionProperties now includes CorrelationId.
- StartRecording now accepts ChannelAffinity.
- Added EventProcessor, an easy and powerful way to handle Call Automation events. See README for details.

### Breaking Changes
- AddParticipant and RemoveParticipant now only accept one participant at a time.
- CallSource has been flattened out.
- CallInvite model replaces previous models for handling outbound calls.

## 1.0.0-beta.1 (2022-11-07)
This is the first version of the restart of Azure Communication Service Call Automation. For more information, please see the [README][read_me].

- Name changed to Azure Communication Service Call Automation.
- Feature re-designed.
- Added interfaces from `com.azure.core.client.traits` to `CallAutomationClientBuilder`
- Added `retryOptions` to `CallAutomationClientBuilder`

### Features Added
- Create outbound call to an Azure Communication Service user or a phone number.
- Answer/Redirect/Reject incoming call from an Azure Communication Service user or a phone number.
- Hangup and terminate the existing call.
- Play audio in the call.
- Call recording.
- Get, add and remove participants from the call.
- Recording download apis.
- Optimized the logic for deserializing types derived from the `CommunicationIdentifier`.

### Breaking Changes
- Incompatible with previous version of service

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-callautomation/README.md
[DTMF]: https://en.wikipedia.org/wiki/Dual-tone_multi-frequency_signaling
