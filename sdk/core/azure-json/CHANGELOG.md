# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

- Fixed a bug when buffering JSON where text fields weren't maintaining proper encoding. ([#33406](https://github.com/Azure/azure-sdk-for-java/pull/33406))

### Other Changes

## 1.0.0-beta.2 (2023-02-01)

### Features Added

- Add `JsonReader.readRemainingFieldsAsJsonObject` to support reading the remainder of a JSON object from a field starting
  point.

### Bugs Fixed

- Fixed a bug where the default `JsonReader` would throw an exception if the starting `JsonToken` was a field name
  not followed by a start object when calling `bufferObject`. Buffering now supports field starting points.

## 1.0.0-beta.1 (2022-09-22)

- Initial release. Please see the README for more information.
