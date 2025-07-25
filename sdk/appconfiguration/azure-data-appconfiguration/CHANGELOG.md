# Release History

## 1.9.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.8.1 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.
- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.

## 1.8.0 (2025-03-12)

### Features Added

- Added support for specifying the token credential's Microsoft Entra audience when creating a client.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.10` to version `1.15.11`.
- Upgraded `azure-json` from `1.4.0` to version `1.5.0`.
- Upgraded `azure-core` from `1.55.2` to version `1.55.3`.

## 1.7.4 (2025-02-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.10`.
- Upgraded `azure-json` from `1.3.0` to version `1.4.0`.
- Upgraded `azure-core` from `1.54.1` to version `1.55.2`.


## 1.7.3 (2024-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.53.0` to version `1.54.1`.
- Upgraded `azure-core-http-netty` from `1.15.5` to version `1.15.7`.


## 1.7.2 (2024-10-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.4` to version `1.15.5`.
- Upgraded `azure-core` from `1.52.0` to version `1.53.0`.

## 1.7.1 (2024-09-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-json` from `1.2.0` to version `1.3.0`.
- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.
- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.


## 1.7.0 (2024-08-07)

### Features Added

- Added a new service API support: `2023-11-01`.
- Added a new method `listLabels` to support listing labels capabilities.
- Added new class `SettingLabel` and `SettingLabelSelector`, and a new enum `SettingLabelFields`.
- Added a new property `tagsFilter` to `SettingSelector` to support filtering settings or revisions with tags filter.
- Added a new property `tags` to `ConfigurationSettingsFilter` to support filtering settings with tags filter for snapshot.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.
- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-json` from `1.1.0` to version `1.2.0`.

## 1.6.3 (2024-07-26)

### Bugs Fixed

Fixed `FeatureFlagConfigurationSetting`'s `setKey()` which should always add the feature flag prefix `.appconfig.featureflag/` before the input key. ([#33332](https://github.com/Azure/azure-sdk-for-java/issues/33332))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.
- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.

## 1.6.2 (2024-06-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.

## 1.6.1 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.

## 1.6.0 (2024-04-09)

### Features Added

- Added Page ETag support in listing configuration setting, which returns empty body and status code `304 not modified`
  if settings in the page and ETag of the page are not changed. If status code `200` returns in the page response,
  which means page's settings and ETag have changed. A full page of settings and a new page ETag will be returned in
  response.
  Use the new parameter `matchConditions` in the `SettingSelector` to assign the page ETags in the request to service.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.5.3 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 1.6.0-beta.1 (2024-03-04)

### Features Added

- Added Page ETag support in listing configuration setting, which returns empty body and status code `304 not modified` 
  if settings in the page and ETag of the page are not changed. If status code `200` returns in the page response, 
  which means page's settings and ETag have changed. A full page of settings and a new page ETag will be returned in
  response.
  Use the new parameter `matchConditions` in the `SettingSelector` to assign the page ETags in the request to service.


## 1.5.2 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.


## 1.5.1 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.5.0 (2023-11-08)

### Features Added

- Added support for the `2023-10-01` service version.

### Breaking Changes

Note: Below breaking changes only affect the version `1.5.0-beta.2`.

- Removed `azure-core-experimental` as a dependency and replaced usage of `PollResult` by `PollOperationDetails`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.

## 1.4.10 (2023-10-20)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.

## 1.5.0-beta.2 (2023-10-11)

### Features Added

- Added `SnapshotFields` enum to support fields selection for snapshot.
- Added a property `List<SnapshotFields> fields` in class `SnapshotSelector` to support fields selection for snapshot.

### Breaking Changes
Note: Below breaking changes only affect the version `1.5.0-beta.1`.

- Changed `listConfigurationSettingsForSnapshot` method's `SettingFields[] fields` parameter type to `List<SettingFields> fields`.
- Renamed `ConfigurationSettingSnapshot` to `ConfigurationSnapshot`.
- Renamed `ConfigurationSettingSnapshot`'s properties name,
  - `size` to `sizeInBytes`,
  - `compositionType` to `snapshotComposition`.
- Renamed `SnapshotSettingFilter` to `ConfigurationSettingsFilter`.
- Added `azure-core-experimental` as a dependency and replaced usage of `CreateSnapshotOperationDetail` by `PollResult` 
  and removed `CreateSnapshotOperationDetail` class.
- Replaced
   - `archiveSnapshotWithResponse(ConfigurationSettingsSnapshot, boolean)` by `archiveSnapshotWithResponse(String, MatchConditions)`
   - `recoverSnapshotWithResponse(ConfigurationSettingsSnapshot, boolean)` by `recoverSnapshotWithResponse(String, MatchConditions)`
   - `archiveSnapshotWithResponse(ConfigurationSettingsSnapshot, boolean, Context)` by `archiveSnapshotWithResponse(String, MatchConditions, Context)`
   - `recoverSnapshotWithResponse(ConfigurationSettingsSnapshot, boolean, Context)` by `recoverSnapshotWithResponse(String, MatchConditions, Context)`

### Bugs Fixed
- `FeatureFlagConfigurationSetting` and `SecretReferenceConfigurationSetting` will now retain custom attributes in the setting value.
  Previously, only attributes that were defined in the associated JSON schema were allowed and unknown attributes were discarded. ([#36725](https://github.com/Azure/azure-sdk-for-java/pull/36725))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.0`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.8`.

## 1.4.9 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 1.4.8 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 1.4.7 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.


## 1.5.0-beta.1 (2023-07-11)

### Features Added

- Added new feature, `Configuration Setting Snapshot` to the library. You can create, get,
  update(archive and recover) a snapshot, and list snapshots.
- Added new APIs to support listing configuration settings by given snapshot name and setting 
  fields.

### Other Changes

#### Dependency Updates

- Added a new dependency `azure-json`, version `1.0.1`.
- Upgraded `azure-core` from `1.40.0` to version `1.40.1`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.

## 1.4.6 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 1.4.5 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.4.4 (2023-04-18)

### Breaking Changes

- Fixed the bug that multiple authentications coexist per builder. App Configuration client builder should only
  support single authentication per builder instance.
- Moved the validation of authentication to client builder's `build()` method.

### Bugs Fixed

- Fixed a race condition of invalid signature issue by not sharing `Mac` instance in class-level per request operation.
  `Mac` isn't thread-safe, multiple threads could use it at once.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.

## 1.4.3 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 1.4.2 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.

## 1.4.1 (2023-01-17)

### Bugs Fixed
- Fixed shared `SyncTokenToken` and `RetryPolicy` policies across Configuration clients. 

## 1.4.0 (2023-01-11)

### Features Added
- Added `getEndpoint()` method to both App Configuration synchronous and asynchronous clients.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.35.0`.
- Updated `azure-core-http-netty` to `1.12.8`.

## 1.3.9 (2022-11-09)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.34.0`.
- Updated `azure-core-http-netty` to `1.12.7`.

## 1.3.8 (2022-10-12)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.33.0`.
- Updated `azure-core-http-netty` to `1.12.6`.

## 1.3.7 (2022-09-06)

### Bugs Fixed
- Fixed the issue of sharing the same default http pipeline instance between App Configuration clients.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.32.0`.
- Updated `azure-core-http-netty` to `1.12.5`.

## 1.3.6 (2022-08-12)

### Features Added
- Integrate synchronous workflow for sync clients so that they do not block on async client APIs.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.31.0`.
- Updated `azure-core-http-netty` to `1.12.4`.

## 1.3.5 (2022-07-08)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.30.0`.
- Updated `azure-core-http-netty` to `1.12.3`.

## 1.3.4 (2022-06-08)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.29.1`.
- Updated `azure-core-http-netty` to `1.12.2`.

## 1.3.3 (2022-05-11)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.28.0`.
- Updated `azure-core-http-netty` to `1.12.0`.

## 1.3.2 (2022-04-08)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.27.0`.
- Updated `azure-core-http-netty` to `1.11.9`.

## 1.3.1 (2022-03-09)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.26.0`.
- Updated `azure-core-http-netty` to `1.11.8`.

## 1.3.0 (2022-02-10)

### Features Added
- Added interfaces from `com.azure.core.client.traits` to `ConfigurationClientBuilder`.
- Added a new method `retryOptions` to `ConfigurationClientBuilder`.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.25.0`.
- Updated `azure-core-http-netty` to `1.11.7`.

## 1.2.5 (2022-01-13)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.22.0` to `1.24.1`.
- Updated `azure-core-http-netty` from `1.11.2` to `1.11.6`.

## 1.2.4 (2021-11-11)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.21.0` to `1.22.0`.
- Updated `azure-core-http-netty` from `1.11.1` to `1.11.2`.

## 1.2.3 (2021-10-05)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.20.0` to `1.21.0`.
- Updated `azure-core-http-netty` from `1.11.0` to `1.11.1`.

## 1.2.2 (2021-09-09)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.19.0` to `1.20.0`.
- Updated `azure-core-http-netty` from `1.10.2` to `1.11.0`.

## 1.2.1 (2021-08-11)
### Dependency Updates
- Updated `azure-core` from `1.18.0` to `1.19.0`.
- Updated `azure-core-http-netty` from `1.10.1` to `1.10.2`.

## 1.2.0 (2021-07-08)
### Bugs Fixed
- `SecretReferenceConfigurationSetting` and `FeatureFlagConfigurationSetting` are able to update the strongly-type
  properties of setting when changing the setting's `value`, vice versa.

## 1.1.12 (2021-05-18)
### Dependency Updates
- Updated `azure-core` from `1.15.0` to `1.16.0`.
- Updated `azure-core-http-netty` from `1.9.1` to `1.9.2`.

## 1.2.0-beta.1 (2021-04-09)
### New Features
- Added updateSyncToken() to be able to provide external synchronization tokens to both
  `ConfigurationAsyncClient` and `ConfigurationClient` clients.
- Added new `SecretReferenceConfigurationSetting` and `FeatureFlagConfigurationSetting`
  types to represent configuration settings that references KeyVault Secret reference and
  feature flag respectively.
- Added new convenience overload APIs that take `ConfigurationSetting`:
  `addConfigurationSetting(ConfigurationSetting setting)`
  `getConfigurationSetting(ConfigurationSetting setting)`
  `setConfigurationSetting(ConfigurationSetting setting)`
  `deleteConfigurationSetting(ConfigurationSetting setting)`
  `setReadOnly(ConfigurationSetting setting, boolean isReadOnly)`
- Added a new method that accepts `ClientOptions` in `ConfigurationClientBuilder`.

## 1.1.10 (2021-03-09)
### Dependency updates
- Update dependency version, `azure-core` to 1.14.0 and `azure-core-http-netty` to 1.9.0.

## 1.1.9 (2021-02-10)
### Dependency updates
- Update dependency version, `azure-core` to 1.13.0 and `azure-core-http-netty` to 1.8.0.

## 1.1.8 (2021-01-14)
### Dependency updates
- Update dependency version, `azure-core` to 1.12.0 and `azure-core-http-netty` to 1.7.1.

## 1.1.7 (2020-11-12)
### Dependency updates
- Update dependency version, `azure-core` to 1.10.0 and `azure-core-http-netty` to 1.6.3.

## 1.1.6 (2020-10-06)
### Dependency updates
- Update dependency version, `azure-core` to 1.9.0 and `azure-core-http-netty` to 1.6.2.

## 1.1.5 (2020-09-10)
### Dependency updates
- Update dependency version, `azure-core` to 1.8.1 and `azure-core-http-netty` to 1.6.1.

## 1.1.4 (2020-08-11)
### Dependency updates
- Update dependency version, `azure-core` to 1.7.0 and `azure-core-http-netty` to 1.5.4.

## 1.1.3 (2020-07-07)
### Dependency updates
- Update dependency version, `azure-core` to 1.6.0 and `azure-core-http-netty` to 1.5.3.

## 1.1.2 (2020-06-09)
### Dependency updates
- Update dependency version, `azure-core` to 1.5.1 and `azure-core-http-netty` to 1.5.2.

## 1.1.1 (2020-04-06)
### Dependency updates
- Update dependency version, `azure-core` to 1.4.0 and `azure-core-http-netty` to 1.5.0.

## 1.1.0 (2020-03-11)
- Updated javadoc to support the changes that App Configuration service no longer support `*a` and `*a*` suffix and full text search.
  For more information: see [Filtering](https://github.com/Azure/AppConfiguration/blob/d7837982445b4692448c246f7b45334df1a8c89b/docs/REST/kv.md#filtering).

## 1.0.1 (2020-01-07)
- Added support for setting `x-ms-client-request-id`, `x-ms-correlation-request-id` and `correlation-context` http header values.
- Fixed `UserAgent` unknown name and unknown version bug.
- Fixed `connectionString()`, it throws `IllegalArgumentException` error when `connectionString` is an empty string, 
  the secret contained within the connection string is invalid or the HMAC-SHA256 MAC algorithm cannot be instantiated.
- No longer set `TokenCredential` to null when `connectionString` is given, or visa versa.

### Breaking changes
- SettingSelector takes a filter instead of taking a list of strings. Supported `SettingSelector` literal special character and wild card functions. 

## 1.0.0-beta.7 (2019-11-26)
- Added support for Azure Activity Directory authentication.
- Added service API version support

### Breaking Changes
- Removed clearReadOnly API, updated setReadOnly API to support setting and clearing read only based on the flag passed.
- Removed Range class, SettingSelector no longer supports Range.

## 1.0.0-preview.6 (2019-10-31)
- Renamed addSetting, getSetting, deleteSetting, setSetting, listSettings, listSettingRevisions to
  addConfigurationSetting, getConfigurationSetting, deleteConfigurationSetting, setConfigurationSetting,
  listConfigurationSettings, listRevisions for consistency naming across languages.
- Ensured exceptions are consistent for certain operations (c.f. other languages).
- Renamed asOfDayTime to acceptDateTime, and lock to isReadOnly.
- ConfigurationCredentialsPolicy no longer explored to public and moved to implementation folder.
- Fixed AzConfig Revisions Range Returns 416 Status Code
- Added ConfigurationServiceVersion class for version
- Added more samples including conditional request, setReadOnly, clearReadOnly, listRevisions, etc.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 

## 1.0.0-preview.5 (2019-10-11)
- Fixed a explored bug that ConfigurationClientCredential is already package-private. Using connection String instead.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## 1.0.0-preview.4 (2019-10-08)
- Updated addSetting, getSetting, deleteSetting, setSetting to support conditional request.
- Removed UpdateSetting.
- Allowed user to define custom equality of configuration setting.
- No public ConfigurationClientCredential.
- Removed credential and CredentialPolicy package.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## 1.0.0-preview.3 (2019-09-10)
- Removed dependency on Netty.
- Added logging when throwing `RuntimeException`s.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/4375acbb70d4b85db238d6b5147b697d9355f45e/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## 1.0.0-preview.2 (2019-08-06)
- Merged ConfigurationClientBuilder and ConfigurationAsyncClientBuilder into ConfigurationClientBuilder. Method to build each client were added.
- ConfigurationClientBuilder was made instantiable, static builder method removed from ConfigurationClient and ConfigurationAsyncClient.
- Builder method credentials renamed to credential and serviceEndpoint to endpoint.
- Listing operations return PagedFlux and PagedIterable in their respective clients.
- Asynchronous calls check subscriberContext for tracing context.
- Synchronous calls support passing tracing context in maximal overloads.

**Breaking changes: New API Design**
- Simplified API to return model types directly on non-maximal overloads. Maximal overloads return `Response<T>` and suffixed with WithResponse.

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-data-appconfiguration_1.0.0-preview.2/sdk/appconfiguration/azure-data-appconfiguration/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-data-appconfiguration_1.0.0-preview.2/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration) 
demonstrate the new API.

## 1.0.0-preview.1 (2019-06-28)
Version 1.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic 
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide 
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-data-appconfiguration_1.0.0-preview.1/appconfiguration/client/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-data-appconfiguration_1.0.0-preview.1/appconfiguration/client/src/samples/java) 
demonstrate the new API.

- Initial release. Please see the README and wiki for information on the new design.
