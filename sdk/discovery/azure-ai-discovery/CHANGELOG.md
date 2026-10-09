# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2026-08-10)

Initial public preview (beta) release of the Azure AI Discovery client library for Java. This package targets the Microsoft Discovery data-plane API version `2026-06-01`.

### Features Added

- `WorkspaceClientBuilder` for building clients that work with Discovery workspaces: `ConversationsClient`, `InvestigationsClient`, `TasksClient`, and `ToolsClient` (each with an asynchronous variant).
- `BookshelfClient` and `BookshelfAsyncClient` for managing knowledge bases, including long-running create/update, indexing, and search operations.
