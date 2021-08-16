# Release History

## 1.0.0-beta.16 (Unreleased)

### Features Added

- Added `TokenRequestContenxtExperimental` class that allows to configure TenantId Challenges.

### Dependency Updates

- Upgraded `azure-core` from `1.18.0` to `1.19.0`.

## 1.0.0-beta.15 (2021-07-01)

### Features Added

- Added `HttpAuthorization` which supports configuring a generic `Authorization` header on a request.

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 1.0.0-beta.14 (2021-06-07)

### Breaking Changes

- Removed `geojson` package and GeoJSON classes.

### Dependency Updates

- Upgraded `azure-core` from `1.16.0` to `1.17.0`.

## 1.0.0-beta.13 (2021-05-07)

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.

## 1.0.0-beta.12 (2021-04-02)

### New Features

- Added positional coordinate getter to `GeoBoundingBox`.
- Overloaded `toString` for `GeoBoundingBox` and `GeoPosition`.
- Added `getOuterRing` to `GeoPolygon`.
- Added `DynamicRequest` and `DynamicResponse` to enable making REST API calls without a service client.

### Breaking Changes

- Removed `GeoArray` from public API.
- Changed `GeoBoundingBox` constructor to use `double` instead of `Double` when including altitude values.
- Renamed `GeoLine` to `GeoLineString` and `GeoLineCollection` to `GeoLineStringCollection`.
- Changed `getCoordinates` of `GeoLineString` and `GeoLinearRing` to return `List` instead of `GeoArray`.
- Removed `getCoordinates` from `GeoLineStringCollection`, `GeoPointCollection`, `GeoPolygon`, and `GeoPolygonCollection`.

### Dependency Updates

- Upgraded `azure-core` from `1.14.0` to `1.15.0`.

## 1.0.0-beta.11 (2021-03-08)

### New Features

- Added `ARMChallengeAuthenticationPolicy` as an implementation of `BearerTokenAuthenticationChallengePolicy`.

### Breaking Changes

- Modified implementations of `onBeforeRequest` and `onChallenge` in `BearerTokenAuthenticationChallengePolicy`.

### Dependency Updates

- Upgraded `azure-core` from `1.13.0` to `1.14.0`.

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
