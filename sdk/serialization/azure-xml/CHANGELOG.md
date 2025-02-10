# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0 (2025-01-27)

### Features Added

- Added APIs to `XmlReader` that may reduce `QName`, and other allocations, depending on the XML implementation.
  `XmlElementConsumer` and usage with `XmlReader.processNextElement(XmlElementConsumer)` allow for processing an element
  without using `QName`. `XmlReader.elementNameMathes(String)` and `XmlReader.elementNameMatches(String, String)` allow
  for checking the current element name without using `QName`. `XmlReader.getElementLocalName()` and 
  `XmlReader.getElementNamespaceUri()` allow for inspecting the current element name and namespace without using 
  `QName`. Depending on the implementation, using these methods may reduce allocations and improve performance.

### Other Changes

- Shaded Aalto XML 1.3.3 into `azure-xml` and updated `XmlReader` and `XmlWriter` to use that implementation by default
  if the `XMLInputFactory` or `XMLOutputFactory` `newInstance` returned the default JDK implementation.

## 1.1.0 (2024-07-26)

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
