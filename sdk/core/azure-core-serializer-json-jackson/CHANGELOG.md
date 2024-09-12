# Release History

## 1.5.1 (2024-09-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to `1.52.0`.

## 1.5.0 (2024-07-31)

### Features Added

- Added implementations of `azure-json`'s `JsonElement` that wrap `JsonArray` and `JsonObject` to simplify combining
  Jackson functionality with `azure-json` functionality.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.50.0` to `1.51.0`.
- Upgraded Jackson from `2.13.5` to `2.17.2`.

## 1.4.14 (2024-07-12)
### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to `1.50.0`.

## 1.4.13 (2024-06-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.

## 1.4.12 (2024-05-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.

## 1.4.11 (2024-04-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.
- 
## 1.4.10 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.

## 1.4.9 (2024-02-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.

## 1.4.7 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.

## 1.4.6 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.

## 1.4.5 (2023-10-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 1.4.4 (2023-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 1.4.3 (2023-08-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0`.

## 1.4.2 (2023-07-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 1.4.1 (2023-06-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 1.4.0 (2023-05-04)

### Features Added

- Implement `azure-json` service provider interface with a Jackson-based implementation.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 1.3.1 (2023-04-07)

### Other Changes

- Added dependency on `azure-json` and added support for `JsonSeriliazable` in `JacksonAdapter`.

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to `1.38.0`.

## 1.3.0 (2023-03-02)

### Features Added

- Added replica of `JacksonAdapter` from `azure-core`. The `JacksonAdapter` in this library doesn't expose the internal
  `ObjectMapper`s used to prevent global mutations or support for running privileged actions to override 
  `SecurityManager` policies as `SecurityManager` is deprecated for removal. This `JacksonAdapter` will serve as the
  long term replacement to the `JacksonAdapter` in `azure-core`. ([#33401](https://github.com/Azure/azure-sdk-for-java/pull/33401))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to `1.37.0`.
- Upgraded Jackson to the latest releases `2.13.5`.

## 1.2.25 (2023-02-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to `1.36.0`.

## 1.2.24 (2023-01-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to `1.35.0`.

## 1.2.23 (2022-11-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to `1.34.0`.

## 1.2.22 (2022-10-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to `1.33.0`.
- Upgraded Jackson from `2.13.3` to `2.13.4`.

## 1.2.21 (2022-09-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to `1.32.0`.

## 1.2.20 (2022-08-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to `1.31.0`.

## 1.2.19 (2022-06-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to `1.30.0`.
- Upgraded Jackson from `2.13.2` to `2.13.3`.

## 1.2.18 (2022-06-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.0` to `1.29.1`.

## 1.2.17 (2022-06-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to `1.29.0`.
- 
## 1.2.16 (2022-05-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to `1.28.0`.

## 1.2.15 (2022-04-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to `1.27.0`.

## 1.2.14 (2022-03-04)

### Other Changes

- Updated all `ClientLogger`s to be static constants instead of instance variables. ([#27339](https://github.com/Azure/azure-sdk-for-java/pull/27339))

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to `1.26.0`.

## 1.2.13 (2022-02-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to `1.25.0`.

## 1.2.12 (2022-01-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.0` to `1.24.1`.

## 1.2.11 (2022-01-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.23.1` to `1.24.0`.
- Upgraded Jackson from `2.13.0` to `2.13.1`.

## 1.2.10 (2021-12-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to `1.23.1`.
- Upgraded Jackson from `2.12.5` to `2.13.0`.

## 1.2.9 (2021-11-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to `1.22.0`.

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
