# Release History

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
