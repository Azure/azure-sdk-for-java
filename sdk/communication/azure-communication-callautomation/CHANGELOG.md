# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
