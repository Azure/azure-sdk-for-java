# Release History

## 4.0.0-beta.3 (Unreleased)

### Features Added
- Support StorageQueueMessageConverter as a bean to support customize ObjectMapper.
### Breaking Changes
- Remove `StorageQueueOperation`.
- Remove configuration of checkpoint mode for StorageQueueTemplate, and support only MANUAL mode.
- Remove auto creating Storage Queue when send/receive messages via `StorageQueueTemplate`.
- Add the parameter of visibility timeout for StorageQueueTemplate#receiveAsync
### Bugs Fixed

### Other Changes

## 4.0.0-beta.2 (2021-11-22)

Please refer to [CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/430fdbfae956667b1576a8e6b609810b9441442c/sdk/spring/CHANGELOG.md) for more details.
