# Release History

## 1.1.0-beta.1 (Unreleased)


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
