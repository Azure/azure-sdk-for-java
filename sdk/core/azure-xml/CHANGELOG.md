# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-06-02)

### Features Added

- Added new APIs to `XmlSerializable` to support cases where the object could be used with different root XML element
  names. ([#35225](https://github.com/Azure/azure-sdk-for-java/pull/35225))

### Bugs Fixed

- Fixed a bug in reading the XML element value when it took multiple reads to fully consume it where null would 
  accidentally be appended to the final result for each unused segment tracker.

## 1.0.0-beta.1 (2022-09-22)

- Initial release. Please see the README for more information.
