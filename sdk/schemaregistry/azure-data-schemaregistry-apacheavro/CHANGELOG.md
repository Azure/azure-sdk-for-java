# Release History

## 1.0.0-beta.8 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.7 (2021-11-12)

### Other Changes

#### Dependency Updates

- Update `azure-data-schemaregistry` dependency to `1.0.0`.
- Update `azure-core-serializer-avro-apache` dependency to `1.0.0-beta.16`.

## 1.0.0-beta.6 (2021-10-08)

### Breaking Changes

- Renamed package from `azure-data-schemaregistry-avro` to `azure-data-schemaregistry-apacheavro`.
- Renamed serializer to `SchemaRegistryApacheAvroSerializer`.
- Renamed builder to `SchemaRegistryApacheAvroSerializerBuilder`.

### Other Changes

#### Dependency Updates

- Update `azure-data-schemaregistry` dependency to `1.0.0-beta.6`.
- Update `azure-core-serializer-avro-apache` dependency to `1.0.0-beta.15`.

## 1.0.0-beta.5 (2021-08-17)

### Other Changes

#### Dependency Updates

- Update `azure-data-schemaregistry` dependency to `1.0.0-beta.5`.
- Update `azure-core-serializer-avro-apache` dependency to `1.0.0-beta.13`.

## 1.0.0-beta.4 (2020-09-21)
- Added 4-byte format identifier prefix to serialized payload
- Updated `module-info` to require `azure-data-schemaregistry` module transitively

## 1.0.0-beta.3 (2020-09-10)
- Updated to use `azure-core` for `ObjectSerializer`.
- Removed `AvroSchemaRegistrySerializer` and `AvroSchemaRegistryCodec`
- `SchemaRegistryAvroSerializerBuilder` now takes `SchemaRegistryAsyncClient` as input to build the
 `SchemaRegistryAvroSerializer` instance

## 1.0.0-beta.2 (2020-06-19)
- Fix null max schema map size parameter behavior

## 1.0.0-beta.1 (2020-06-04)
- Initial add
