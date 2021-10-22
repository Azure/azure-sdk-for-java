# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.8 (2021-10-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.20.0` to `1.21.0`.
- Upgraded Jackson from `2.12.4` to `2.12.5`.

## 1.2.7 (2021-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.19.0` to `1.20.0`.

## 1.2.6 (2021-08-06)

### Dependency Updates

- Updated `azure-core` from `1.18.0` to `1.19.0`.
- Updated Jackson from `2.12.3` to `2.12.4`.

## 1.2.5 (2021-07-01)

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 1.2.4 (2021-06-07)

### Dependency Updates

- Updated `azure-core` from `1.16.0` to `1.17.0`.
- Updated Jackson from `2.12.2` to `2.12.3`.

## 1.2.3 (2021-05-07)

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.

## 1.2.2 (2021-04-02)

### Dependency Updates

- Upgraded `azure-core` from `1.14.1` to `1.15.0`.

## 1.2.1 (2021-03-19)

### Bug Fixes

- Fix a bug where `ClassNotFoundException` or `MethodNotFoundException` was thrown when Jackson 2.11 is resolved
  instead of Jackson 2.12. [#19897](https://github.com/Azure/azure-sdk-for-java/issues/19897)

## 1.2.0 (2021-03-08)

### Dependency Updates

- Updated `azure-core` from `1.13.0` to `1.14.0`.
- Updated Jackson from `2.11.3` to `2.12.1`.

## 1.1.2 (2021-02-05)

### Dependency Updates

- Updated `azure-core` from `1.12.0` to `1.13.0`.

## 1.1.1 (2021-01-11)

### Dependency Updates

- Updated `azure-core` from `1.10.0` to `1.12.0`.

## 1.1.0 (2020-10-28)

### Breaking Changes

- Changed default `ObjectMapper` configuration used by `JacksonJsonSerializerBuilder`.

## 1.0.2 (2020-10-01)

- Updated `azure-core` version.

## 1.0.1 (2020-09-08)

- Updated `azure-core` version to pickup bug fix.

## 1.0.0 (2020-09-03)

- Initial GA.

## 1.0.0-beta.3 (2020-08-12)

- Changed `JacksonJsonSerializer` to implement `azure-core`'s `JsonSerialzer` instead of `azure-core-experimental`'s.
- Removed JSON tree models and APIs.
- `JacksonJsonSerializer` now implements the interface `MemberNameConverter`.

## 1.0.0-beta.2 (2020-07-16)

- `JacksonJsonSerializer` implements `JsonSerializer` instead of `ObjectSerializer`.
- Added implementations for `JsonNode` and its subclasses.

## 1.0.0-beta.1 (2020-05-04)

- Initial release. Please see the README and wiki for information on the new design.
