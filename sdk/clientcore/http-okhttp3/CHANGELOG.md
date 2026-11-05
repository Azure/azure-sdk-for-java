# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

- Fixed timeout resolution to prefer explicit builder values, then builder-specific configuration, then global
  configuration, and finally SDK defaults. Nonpositive call timeouts now disable the timeout instead of rejecting
  negative values.
- Prevented proxy authentication retries for HTTPS endpoint 407 responses while preserving CONNECT and cleartext HTTP proxy authentication.
- Fixed configured default connection, read, and write timeouts being ignored when no explicit builder timeout is set.

### Other Changes

## 1.0.0-beta.2 (2025-03-12)

### Other Changes

- Updated `core` dependency version to `1.0.0-beta.7`.

## 1.0.0-beta.1 (2025-03-10)

### Features Added

- Initial release. Please see the README and wiki for more information.
  This package contains core types for building Java client libraries for accessing web services.

