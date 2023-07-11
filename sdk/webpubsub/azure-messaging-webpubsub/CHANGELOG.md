# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.5 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.

## 1.2.4 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.2.3 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `nimbus-jose-jwt` from `9.22` to version `9.31`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.

## 1.2.2 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 1.2.1 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.

## 1.2.0 (2023-01-11)

### Bugs Fixed

- Fixed incorrect "audience" from `getClientAccessToken` method in `WebPubSubServiceAsyncClient`. ([#24741](https://github.com/Azure/azure-sdk-for-java/issues/24741))
- Fixed bug of getting incorrect token in `getClientAccessToken` method from Azure token credential. 

### Features Added

- Added method `removeConnectionFromAllGroupsWithResponse` in `WebPubSubServiceClient` and `WebPubSubServiceAsyncClient` to remove the connection from all the groups it is in.
- Added a `webpubsub.group` option in `GetClientAccessTokenOptions`, to enable connections join initial groups once it is connected.
- Added a `filter` parameter when sending messages to connections in a hub/group/user to filter out the connections recieving message, details about `filter` syntax please see [OData filter syntax for Azure Web PubSub](https://aka.ms/awps/filter-syntax).

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-11-01`.
- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.

## 1.1.8 (2022-11-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.

## 1.1.7 (2022-10-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 1.1.6 (2022-09-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.

## 1.1.5 (2022-08-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.4`.

## 1.1.4 (2022-07-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-core-http-netty` from `1.12.2` to version `1.12.3`.

## 1.1.3 (2022-06-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.

## 1.1.2 (2022-05-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.
- Upgraded `nimbus-jose-jwt` from `9.10.1` to version `9.22`.

## 1.1.1 (2022-04-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.

## 1.1.0 (2022-03-08)

### Features Added

- Added interfaces from `com.azure.core.client.traits` to `WebPubSubServiceClientBuilder`.
- Added `retryOptions` to `WebPubSubServiceClientBuilder`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to `1.26.0`.
- Upgraded `azure-core-http-netty` from `1.11.7` to `1.11.8`.

## 1.0.2 (2022-02-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to `1.11.7`.

## 1.0.1 (2022-01-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.23.0` to `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.3` to `1.11.6`.

## 1.0.0 (2021-11-23)

### Breaking Changes

- `setErrorOptions` is removed from `RequestOptions` in `azure-core`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.23.0`.

## 1.0.0-beta.6 (2021-11-11)

### Breaking Changes

- Merged the `Context` parameter into the `RequestOptions` parameter in methods of `WebPubSubServiceClient`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0-beta.1` to `1.22.0`.

## 1.0.0-beta.5 (2021-10-26)

### Features Added

- Added support for method `closeUserConnectionsWithResponse`, `closeGroupConnectionsWithResponse`, `closeAllConnectionsWithResponse` in `WebPubSubServiceClient` and `WebPubSubServiceAsyncClient`.

### Breaking Changes

- Renamed method `getAuthenticationToken` to `getClientAccessToken` in `WebPubSubServiceClient` and `WebPubSubServiceAsyncClient`.
- Type changed from `String` to `WebPubSubPermission` in method `checkPermissionWithResponse`, `grantPermissionWithResponse`, `revokePermissionWithResponse`, in `WebPubSubServiceClient` and `WebPubSubServiceAsyncClient`.
- Renamed class `WebPubSubAuthenticationToken` to `WebPubSubClientAccessToken`.
- Renamed method `getAuthToken` to `getToken` in `WebPubSubClientAccessToken`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-10-01`.

## 1.0.0-beta.4 (2021-09-08)

### Features Added
- Added support for [Azure Active Directory](https://docs.microsoft.com/azure/active-directory/authentication/) based authentication.
- Added support for API management by configuring `reverseProxyEndpoint` on the client builder. 
  https://github.com/Azure/azure-webpubsub/issues/194 describes how to integrate with the API Management service.

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
