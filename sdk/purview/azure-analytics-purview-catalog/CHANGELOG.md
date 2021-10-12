# Release History

## 1.0.0-beta.2 (unreleased)
### Features Added
- Adopt new API version 2021-09-01. It adds some new operations.

### Breaking Changes
This is a new version of LLC SDK. Changes are
- A sync client and an async client for each operation group.
- Add `RequestOptions` and `Context` to client method parameters.
- Return type of client method is always `Response` except paging and long-running operations.
- Allow users to set `ServiceVersion` in client builder.

## 1.0.0-beta.1 (2021-05-11)

- Initial beta release for Purview Catalog client library.
