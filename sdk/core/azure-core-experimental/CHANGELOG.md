# Release History

## 1.0.0-beta.11 (Unreleased)


## 1.0.0-beta.10 (2021-02-05)

### New Features

- Added challenge based authentication support via `BearerTokenAuthenticationChallengePolicy` and `AccessTokenCache` classes.

## 1.0.0-beta.9 (2021-01-11)

### Breaking Changes

- Moved `BinaryData` to `azure-core`.

## 1.0.0-beta.8 (2020-10-29)

### New Features

- Added `Object` based APIs to `BinaryData` that use a default `JsonSerializer`.

### Breaking Changes

- Moved `JsonPatchDocument` into `azure-core`.
- Removed `BinaryData.fromString(String, Charset)`.

## 1.0.0-beta.7 (2020-10-08)

- Added API `fromObject()` in `BinaryData` which uses `JsonSerializer` present in the classpath.
- Added APIs to `JsonPatchDocument` which accept pre-serialized JSON.
- Updated `azure-core` dependency to released version. 

## 1.0.0-beta.6 (2020-10-06)

- Added `BinaryData` abstraction to represent binary data and supports serialization through `ObjectSerializer`.

## 1.0.0-beta.5 (2020-10-01)

- Added `JsonPatchDocument` to support JSON Patch functionality.

## 1.0.0-beta.4 (2020-09-08)

- Updated `azure-core` version to pickup bug fix.

## 1.0.0-beta.3 (2020-09-03)

- Added `AvroSerializer` interface containing Avro specific serializer APIs.
- Added `AvroSerializerProvider` interface as a service provider for `AvroSerializer`.

## 1.0.0-beta.2 (2020-08-07)

- Moved `ObjectSerializer` and some implementation of `JsonSerializer` into `azure-core`.
- Created sub-interface of `JsonSerializer` in `azure-core` to include APIs that weren't moved.

## 1.0.0-beta.1 (2020-07-02)

- Added `ObjectSerializer` interface containing generic serializer APIs.
- Added `JsonSerializer` interface containing JSON specific serializer APIs.
- Added `JsonNode`, and subclasses, which are strongly type representations of a JSON tree.
- Added GeoJSON classes and serializers.
