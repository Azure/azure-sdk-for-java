# Release History

## 1.4.8 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.4.7 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.


## 1.4.6 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.


## 1.4.5 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.4.4 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 1.4.3 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.


## 1.4.2 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.4.1 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.


## 1.4.0 (2023-10-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.

## 1.3.10 (2023-09-19)

### Features Added

- Add support for protobuf schema format.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 1.3.9 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 1.3.8 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.


## 1.3.7 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 1.3.6 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.3.5 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 1.3.4 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 1.3.3 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.

## 1.4.0-beta.2 (2023-02-13)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.36.0`.
- Update `azure-core-http-netty` dependency to `1.13.0`.

## 1.4.0-beta.1 (2023-01-26)

### Features Added

- Added additional `SchemaFormat`s, `SchemaFormat.JSON`, and `SchemaFormat.CUSTOM`.
- Added new service version, `SchemaRegistryVersion.V2022_10`.
- Updated the latest service version to `SchemaRegistryVersion.V2022_10`.

## 1.3.2 (2023-01-18)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.35.0`.
- Update `azure-core-http-netty` dependency to `1.12.8`.

## 1.3.1 (2022-11-16)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.34.0`.
- Update `azure-core-http-netty` dependency to `1.12.7`.

## 1.3.0 (2022-10-11)

### Features Added

- Added `getVersion` to `SchemaProperties`.
- Added the following methods in `SchemaRegistryAsyncClient`:
  - `Mono<SchemaRegistrySchema> getSchema(String groupName, String schemaName, int schemaVersion)`
  - `Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String groupName, String schemaName, int schemaVersion)`
- Added the following methods in `SchemaRegistryClient`:
    - `SchemaRegistrySchema getSchema(String groupName, String schemaName, int schemaVersion)`
    - `Response<SchemaRegistrySchema> getSchemaWithResponse(String groupName, String schemaName, int schemaVersion, Context context)`

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.33.0`.
- Update `azure-core-http-netty` dependency to `1.12.6`.

## 1.2.4 (2022-09-12)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.32.0`.
- Update `azure-core-http-netty` dependency to `1.12.5`.

## 1.2.3 (2022-08-10)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.31.0`.
- Update `azure-core-http-netty` dependency to `1.12.4`.

## 1.2.2 (2022-07-12)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.30.0`.
- Update `azure-core-http-netty` dependency to `1.12.3`.

## 1.2.1 (2022-06-10)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.29.1`.
- Update `azure-core-http-netty` dependency to `1.12.2`.

## 1.2.0 (2022-05-13)

### Features Added

- Added `SchemaProperties.getGroupName()`.
- Added `SchemaProperties.getName()`.

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.28.0`.
- Update `azure-core-http-netty` dependency to `1.12.0`.

## 1.1.1 (2022-04-07)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.27.0`.
- Update `azure-core-http-netty` dependency to `1.11.9`.

## 1.1.0 (2022-03-15)

### Features Added

- Added interfaces from `com.azure.core.client.traits` to `SchemaRegistryClientBuilder`.
- Added `retryOptions` to `SchemaRegistryClientBuilder`.

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.26.0`.
- Update `azure-core-http-netty` dependency to `1.11.8`.

## 1.0.2 (2022-02-12)

### Other Changes

#### Dependency Updates

- Update `azure-core` dependency to `1.25.0`.

## 1.0.1 (2022-01-18)

### Bugs Fixed

- Fixed double serialization of JSON when publishing/reading schemas. #25789

## 1.0.0 (2021-11-12)

### Breaking Changes

- Removed preview `SchemaRegistryVersion.2017_04`.
- Renamed `SchemaRegistrySchema.getSchemaDefinition` to `SchemaRegistrySchema.getDefinition`.

### Bugs Fixed

- Added correct User Agent string for client.

### Other Changes

- Regenerated REST API based off 2021-10 swagger.
- An `HttpResponseException` with status code 415 is returned if an invalid `SchemaFormat` is passed for `registerSchema` or `getSchemaById` calls.

## 1.0.0-beta.6 (2021-10-08)

### Features added

- Added `SchemaRegistrySchema`.
- Added `SchemaRegistryVersion`.
- Added back `Response<T>` overloads for `getSchema`, `getSchemaProperties`.

### Breaking changes

- Removed client-side caching.
- Changed `getSchemaId` to `getSchemaProperties`.
- Moved `SchemaProperties.getSchema()` to `SchemaRegistrySchema.getSchemaDefinition()`.
- Changed `getSchema()` to return `String` instead of `byte[]`.
- Changed `SerializationType` to `SchemaFormat`.

### Other Changes

#### Dependency Updates

- Update `azure-core-serializer-avro-apache` dependency to `1.0.0-beta.13`.

## 1.0.0-beta.5 (2021-08-17)

### Features added
- Caching schemas based on schemaId.

### Breaking changes
- 4xx responses return their respective `HttpResponseException` rather than `IllegalStateException`
- Removed `Response<T>` overloads for getSchema and getSchemaId because response could be cached.
- `SchemaRegistryClientBuilder.maxCacheSize` is package-private.

### Other Changes

#### Dependency Updates

- Update `azure-core-serializer-avro-apache` dependency to `1.0.0-beta.13`.

## 1.0.0-beta.4 (2020-09-21)
- Minor code cleanup and refactoring

## 1.0.0-beta.3 (2020-09-10)
- Removed `SchemaRegistryCodec` and `SchemaRegistrySerializer`
- Updated Schema Registry client APIs to use `SchemaProperties`

## 1.0.0-beta.2 (2020-06-19)
- Fix 4xx HTTP response handling

## 1.0.0-beta.1 (2020-06-04)
- Initial add
