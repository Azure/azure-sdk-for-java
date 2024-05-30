# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

