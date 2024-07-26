# Release History

## 1.1.1 (2024-07-26)

### Other Changes

- `XmlSerializable.toXml(XmlWriter)` is now a `default` method in the interface. This will call 
  `XmlSerializable.toXml(XmlWriter, String)` with a null string as the second argument, as the `String` argument was
  always considered optional.

## 1.0.0 (2024-04-04)

### Features Added

- Added `XmlReader` and `XmlWriter` factory methods to create instances of `XmlReader` and `XmlWriter`.

### Breaking Changes

- Removed `XmlProvider` and `XmlProviders`, use the factory methods on `XmlReader` and `XmlWriter` instead.
- Made `XmlReader` and `XmlWriter` final classes instead of abstract classes.
- Renamed `ReadValueCallback` to `XmlReadValueCallback`.
- Removed checked `IOException` from `XmlReadValueCallback`.

### Bugs Fixed

- Fixed a bug where the get element methods on `XmlReader` weren't idempotent.

## 1.0.0-beta.3 (2024-02-29)

### Breaking Changes

- Changed default encoding from `utf-8` to `UTF-8`.

## 1.0.0-beta.2 (2023-06-02)

### Features Added

- Added new APIs to `XmlSerializable` to support cases where the object could be used with different root XML element
  names. ([#35225](https://github.com/Azure/azure-sdk-for-java/pull/35225))

### Bugs Fixed

- Fixed a bug in reading the XML element value when it took multiple reads to fully consume it where null would 
  accidentally be appended to the final result for each unused segment tracker.

## 1.0.0-beta.1 (2022-09-22)

- Initial release. Please see the README for more information.
