# Release History

## 1.1.6 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.16.0` to version `1.16.1`.
- Upgraded `azure-core` from `1.56.0` to version `1.56.1`.


## 1.1.5 (2025-08-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.5` to version `1.56.0`.
- Upgraded `azure-core-http-netty` from `1.15.13` to version `1.16.0`.


## 1.1.4 (2025-07-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.4` to version `1.55.5`.
- Upgraded `azure-core-http-netty` from `1.15.12` to version `1.15.13`.


## 1.1.3 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.
- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.


## 1.1.2 (2025-03-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to version `1.55.3`.
- Upgraded `azure-core-http-netty` from `1.15.10` to version `1.15.11`.


## 1.1.1 (2025-02-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.10`.
- Upgraded `azure-core` from `1.54.1` to version `1.55.2`.


## 1.1.0 (2024-11-15)

### Other Changes

- Removed `JsonCreator` and `JsonProperty` annotation and replaced them with new methods `toJson` and `fromJson` using stream-style serialization.

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.54.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.7`.

## 1.0.0 (2024-05-21)

### Features Added

- Added support for AAD authentication for Text Translation endpoints.
- Ability to translate strings directly without a need to create instances of objects.
- Options pattern used for methods with too many parameters.

### Breaking Changes

- Method `getSentLen` renamed to `getSentencesLengths`.
- `String` can be used for translate, transliterate, find sentence boundaries methods instead of `InputTextItem`.
- `GetLanguages*` classes and definitions were renamed to `GetSupportedLanguages*`.
- `getProj` method renamed to `getProjections`.
- `Translation` class renamed to `TranslationText`.
- `getScore` method renamed to `getConfidence`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.15.0`.

## 1.0.0-beta.1 (2023-04-18)

- Azure Text Translation client library for Java. This package contains Microsoft Azure Text Translation client library.

