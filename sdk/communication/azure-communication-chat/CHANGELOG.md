# Release History

## 1.0.0-beta.4 (2021-02-09)
### Breaking Changes
- 'ChatClient' is split into 'ChatClient' and 'ChatThreadClient'.
- Renamed 'Member' and 'ThreadMember' to 'Participant'.
- Removed 'Priority' in 'ChatMessage'.
- 'ChatMessage' properties are now all required.
- 'ChatMessage' type property is no longer a 'String', but an extendable Enum type, 'ChatMessageType'.
- 'ChatMessage' content property is no longer a 'String', but an object of 'ChatMessageContent'.
- All 'OffsetDateTime' properties are now in RFC3339 format instead of ISO8601 format.

### Added
- Support for 'ChatMessageType' in 'ChatMessage'.
- Support optional user agent http header in chat clients.
- Added CommunicationError.
- Added CommunicationErrorResponse.
- Added CommunicationErrorResponseException.
- Added ChatMessageContent.
- Added ChatMessageType.
- Added AddChatParticipantsErrors.
- Added AddChatParticipantsResult.
- Added ChatMessageType.

## 1.0.0-beta.3 (2020-11-16)
Updated `azure-communication-chat` version

## 1.0.0-beta.2 (2020-10-06)
Updated `azure-communication-chat` version

## 1.0.0-beta.1 (2020-09-22)
This is the initial release of Azure Communication Services for chat. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/communication/azure-communication-chat/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/chat/get-started?pivots=programming-language-java
