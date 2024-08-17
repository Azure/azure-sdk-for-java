# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2024-05-22)

### Bugs Fixed

- Fixed a bug that caused `long` properties on models to be deserialized incorrectly.

## 1.0.0-beta.1 (2024-05-16)

- Azure Batch client library for Java. This package contains the Microsoft Azure Batch client library.

### Features Added

- Unified Clients: Consolidated multiple smaller clients into two clients: `BatchClient` for synchronous methods and `BatchAsyncClient` for asynchronous methods.
- Refactored Options: Instead of listing each optional parameter separately in method signatures, a single options object is now used. This object encapsulates all optional parameters.
- Bulk Task Creation: Added `createTasks` method for bulk task creation (adding multiple tasks to a job at once) to both clients.

### Bugs Fixed

- Fixed various typos, misspellings, and unclear operation names for improved clarity and consistency.

### Other Changes

- Removal of Ocp Date Header: The `ocp-date` header has been removed from all SDK operations.
