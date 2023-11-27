# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

- `WebPubSubClient` implements `Closeable` interface. User can use the "try-with-resources" statement on the client.

### Breaking Changes

- Renamed `WebPubSubDataType` to `WebPubSubDataFormat`.
- Removed `WebPubSubProtocol` class and its subclasses.
- Parameter type changed from `WebPubSubProtocol` to `WebPubSubProtocolType` on `WebPubSubClientBuilder` `protocol` method.
  Please call `.protocol(WebPubSubProtocolType.WEB_PUBSUB_JSON_PROTOCOL)` or `.protocol(WebPubSubProtocolType.WEB_PUBSUB_JSON_RELIABLE_PROTOCOL)`.
- Renamed `setNoEcho` method in `SendToGroupOptions` to `setEchoDisabled`.
- Renamed `isNoEcho` method in `SendToGroupOptions` to `isEchoDisabled`.

### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2023-04-17)

- Azure WebPubSub Client library for Java. One can use this library on your client side to manage the WebSocket client connections, join group, send message.
