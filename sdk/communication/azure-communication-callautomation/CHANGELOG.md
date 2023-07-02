# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added
- Start/Stop continuous DTMF recognition by subscribing/unsubscribing to tones.
- Send DTMF tones to a participant in the call

### Breaking Changes

### Bugs Fixed

### Other Changes

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
