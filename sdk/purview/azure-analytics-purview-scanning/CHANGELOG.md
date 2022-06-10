# Release History

## 1.0.0-beta.3 (Unreleased)

### Breaking Changes

- Merged the `Context` parameter into the `RequestOptions` parameter in methods of clients.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.23.0`.

## 1.0.0-beta.2 (2021-10-15)

### Breaking Changes

This is a new version of LLC SDK. Changes are

- A sync client and an async client for each operation group.
- Add `RequestOptions` and `Context` to client method parameters.
- Return type of client method is always `Response` except for paging and long-running operations.
- Allow users to set `ServiceVersion` in client builder.

## 1.0.0-beta.1 (2021-05-11)

- Initial beta release for Purview Scanning client library.
