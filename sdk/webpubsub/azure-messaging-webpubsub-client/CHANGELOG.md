# Release History

## 1.0.4 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `netty-codec-http` from `4.1.108.Final` to version `4.1.110.Final`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.


## 1.0.3 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.


## 1.0.2 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `netty-codec-http` from `4.1.101.Final` to version `4.1.108.Final`.


## 1.0.1 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.47.0`.


## 1.0.0 (2024-01-18)

### Features Added

- `WebPubSubClient` implements `Closeable` interface. User can use the "try-with-resources" statement on the client.

### Breaking Changes

- Renamed `WebPubSubDataType` to `WebPubSubDataFormat`.
- Removed `WebPubSubProtocol` class and its subclasses.
- Parameter type changed from `WebPubSubProtocol` to `WebPubSubProtocolType` on `WebPubSubClientBuilder` `protocol` method.
  Please call `.protocol(WebPubSubProtocolType.JSON_PROTOCOL)` or `.protocol(WebPubSubProtocolType.JSON_RELIABLE_PROTOCOL)`.
- Parameter type change from `Mono<String>` to `Supplier<String>` on constructor of `WebPubSubClientCredential`.
  Please call e.g. `new WebPubSubClientCredential(() -> serviceClient.getClientAccessToken(...).getUrl())`.
- Renamed `setNoEcho` method in `SendToGroupOptions` to `setEchoDisabled`.
- Renamed `isNoEcho` method in `SendToGroupOptions` to `isEchoDisabled`.

### Other Changes

- Upgraded `azure-core` from `1.38.0` to version `1.45.1`.
- Upgraded `netty-codec-http` from `4.1.89.Final` to version `4.1.101.Final`.

## 1.0.0-beta.1 (2023-04-17)

- Azure WebPubSub Client library for Java. One can use this library on your client side to manage the WebSocket client connections, join group, send message.
