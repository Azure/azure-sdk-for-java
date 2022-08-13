# Release History

## 1.0.0-alpha.20220805.1 (2022-08-05)
This is the first version of the restart of Azure Communication Service CallingServer service and named to CallAutomation service. For more information, please see the [README][read_me].

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
