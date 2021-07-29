# Release History

## 1.0.0-beta.4 (Unreleased)


## 1.0.0-beta.3 (2021-07-29)

### Dependency Updates
- Upgraded `azure-core` to `1.19.0-beta.1`.
- Upgraded `azure-core-http-netty` to `1.10.1`.

### Breaking Changes
- Changed sync and async clients to use protocol methods that uses `RequestOptions` and `BinaryData` to create HTTP 
  request.

## 1.0.0-beta.2 (2021-04-27)

### Bug Fixes
- Fixed issue with generating token that included only the last role in the input list.

## 1.0.0-beta.1 (2021-04-22)
Version 1.0.0-beta.1 is a preview of our efforts in creating a client library for Azure Web PubSub that is 
developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as 
possible. The principles that guide our efforts can be found in the 
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

- Initial release. Please see the README and wiki for information on using the new library.
