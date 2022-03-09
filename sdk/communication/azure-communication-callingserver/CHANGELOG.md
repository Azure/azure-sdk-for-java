# Release History

## 1.0.0-beta.5 (Unreleased)
- Added interfaces from `com.azure.core.client.traits` to `CallingServerClientBuilder`
- Added `retryOptions` to `CallingServerClientBuilder`

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2021-10-06)
### Features Added
- Add support for TokenCredential authentication with CallingServerClientBuilder.
- Added support for custom options(Recordingcontenttype, Recordingchanneltype, Recordingformattype) for Mixed Audio feature

### Bugs Fixed
- Using released version of azure-communication-common.

### Other Changes
#### Dependency updates
- Upgraded `azure-communication-common` to 1.0.4

## 1.0.0-beta.3 (2021-07-26)
### Features Added
- Added RedirectPolicy as a new HttpPolicy to redirect requests based on the HttpResponse.

## 1.0.0-beta.2 (2021-06-25)
- Updated sdk and apis documentation.

### Bug Fixes
- Fixed bug with AddParticipant api.

## 1.0.0-beta.1 (2021-06-24)
This is the first release of Azure Communication Service Calling Server. For more information, please see the [README][read_me].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

### Features Added
- Create outbound call to an Azure Communication Service user or a phone number.
- Hangup and delete the existing call.
- Play audio in the call.
- Out-call apis for call recording including start, pause, resume stop and get state.
- Subscribe to and receive [DTMF][DTMF] tones via events.
- Add and remove participants from the call.
- Recording download apis.

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-callingserver/README.md
[DTMF]: https://en.wikipedia.org/wiki/Dual-tone_multi-frequency_signaling
