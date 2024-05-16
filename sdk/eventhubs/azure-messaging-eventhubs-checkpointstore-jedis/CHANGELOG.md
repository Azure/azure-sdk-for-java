# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-09-22)

### Bugs Fixed

- Fixes bug where errors claiming ownership were not propagated.
- Fixes bug where error not returned when creating partition ownerships.

### Other Changes

#### Dependency Updates

- Upgraded `azure-messaging-eventhubs` from `5.15.2` to version `5.16.0`.

## 1.0.0-beta.1 (2023-02-13)

### Features Added

- Added implementation of `CheckpointStore` with `redis.clients.Jedis`

### Other Changes

#### Dependency Updates

- Add `azure-messaging-eventhubs` dependency to `5.15.2`.
- Add `jedis` dependency `4.3.1`.
