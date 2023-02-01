# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

- Add `JsonReader.readRemainingFieldsAsJsonObject` to support reading the remainder of a JSON object from a field starting
  point.

### Breaking Changes

### Bugs Fixed

- Fixed a bug where the default `JsonReader` would throw an exception if the starting `JsonToken` was a field name
  not followed by a start object when calling `bufferObject`. Buffering now supports field starting points.

### Other Changes

## 1.0.0-beta.1 (2022-09-22)

- Initial release. Please see the README for more information.
