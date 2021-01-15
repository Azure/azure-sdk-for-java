# Release History

## 2.2.0-beta.1 (Unreleased)


## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Supported `enableByNameAndVersion` and `disableByNameAndVersion` method in `Secrets`.
- Added `enabled` method in `Secret`.
- Renamed `getAttributes`, `getTags`, `isManaged` method to `attributes`, `tags`, `managed` in `Key`.
- Updated `list` method in `Keys` and `Secrets`. It will no longer retrieve key and secret value. Key can be retrieved via `getJsonWebKey`. Secret value can be retrieved via `getValue`.

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
