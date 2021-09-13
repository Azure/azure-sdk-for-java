# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
- Fixed a explored bug that ConfigurationClientCredential is already pacakge-private. Using connection String instead.

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
- Added logging when throwing `RutimeException`s.

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
