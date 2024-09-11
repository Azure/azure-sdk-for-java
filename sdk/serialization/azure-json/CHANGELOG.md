# Release History

## 1.3.0 (2024-09-11)

### Features Added

- Added the ability to configure JSONC parsing with `JsonOptions.setJsoncSupported`.

### Other Changes

- Updated shaded Jackson Core code to 2.17.2.

## 1.2.0 (2024-07-26)

### Features Added

- `JsonArray`, `JsonBoolean`, `JsonElement`, `JsonNull`, `JsonNumber`, `JsonObject`, and `JsonString` have been added
  to represent a JSON structure with an object-oriented approach. `JsonElement` can be used to generically deserialize
  JSON content and retain JSON typing information. All classes support serialization to JSON, either at the root or as
  a portion of the overall JSON structure.
- Convenience APIs have been added to `JsonSerializable` to support deserialization directly from `String`, `byte[]`,
  `InputStream`, and `Reader` sources without needing to use `JsonProviders` to create a `JsonReader` instance and to
  support serialization directly to `String`, `byte[]`, `OutputStream`, and `Writer` targets without needing to use
  `JsonProviders` to create a `JsonWriter` instance.
- Overloads to `JsonWriter`'s `writeArray`, `writeArrayField`, `writeMap` and `writeMapField`  methods have been added
  that can control how null elements in the array or null key-value pair values are handled in serialization.

### Bugs Fixed

- A bug was fixed in `JsonWriter`'s `writeArray`, `writeArrayField`, `writeMap`, and `writeMapField` methods when handling
  null element values or null key-value pair values in serialization. Previously, handling of null values was left to
  the `WriteValueCallback` supplied to the methods, and if the callback method being used skipped writing null values it
  would result in JSON arrays dropping elements and an exception in `Map` serialization. Now, null values are always
  written in the basic overload and in the new overloads that accept `boolean skipNullElements` / `boolean skipNullValues`,
  null values are skipped if the corresponding boolean is set to `true`. The basic overload will have a difference in
  JSON produced, but it will be the correct representation of the data.

## 1.1.0 (2023-08-04)

### Features Added
- Added `getRawText` method to `JsonReader`

## 1.0.1 (2023-05-04)

### Other Changes

- Changed `UncheckedIOException` to `IOException` in `JsonReader.reset` method as it's checked with `IOException`.
- Changed max nesting depth in `JsonReader.readUntyped` from 1000 to 999.

## 1.0.0 (2023-04-07)

- Initial GA. Please see the README for more information.

### Breaking Changes

- Renamed `JsonReader.resetSupported` to `JsonReader.isResetSupported`.
- Removed all overloads in `JsonProvider` that didn't accept `JsonOptions`.

## 1.0.0-beta.3 (2023-03-02)

### Bugs Fixed

- Fixed a bug when buffering JSON where text fields weren't maintaining proper encoding. ([#33406](https://github.com/Azure/azure-sdk-for-java/pull/33406))

## 1.0.0-beta.2 (2023-02-01)

### Features Added

- Add `JsonReader.readRemainingFieldsAsJsonObject` to support reading the remainder of a JSON object from a field starting
  point.

### Bugs Fixed

- Fixed a bug where the default `JsonReader` would throw an exception if the starting `JsonToken` was a field name
  not followed by a start object when calling `bufferObject`. Buffering now supports field starting points.

## 1.0.0-beta.1 (2022-09-22)

- Initial release. Please see the README for more information.
