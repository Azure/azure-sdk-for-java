# AGENTS.md — Azure Storage Data Lake SDK for Java

This file provides Data Lake-specific guidance for AI agents working in the `sdk/storage/azure-storage-file-datalake` subtree.

For shared storage rules (generated code, magic strings, cross-service consistency, common patterns), see [../AGENTS.md](../AGENTS.md).
For repo-wide guidance, see the [root AGENTS.md](../../../AGENTS.md).

## Data Lake SDK Overview

The `azure-storage-file-datalake` module contains the Azure Data Lake Storage Gen2 client library for Java.

Primary client types include:
- `DataLakeServiceClient` / `DataLakeServiceAsyncClient`
- `DataLakeFileSystemClient` / `DataLakeFileSystemAsyncClient`
- `DataLakeFileClient` / `DataLakeFileAsyncClient`
- `DataLakeDirectoryClient` / `DataLakeDirectoryAsyncClient`

This module targets hierarchical namespace (HNS)-enabled accounts and uses the `dfs.core.windows.net` endpoint shape.

## Data Lake-Specific Rules

### 1. Preserve the FileSystem/Path Model

Keep file system and path semantics explicit:
- file system operations on `DataLakeFileSystemClient`
- file operations on `DataLakeFileClient`
- directory operations on `DataLakeDirectoryClient`

Do not flatten them into a single generic path abstraction.

### 2. Keep Rename and ACL Operations First-Class

Rename/move and ACL/permission behavior are core Data Lake semantics. Keep those flows explicit and aligned with existing options/models.

### 3. Respect DFS Endpoint Semantics

Endpoint and URL handling must preserve Data Lake `dfs.core.windows.net` endpoint and path hierarchy expectations, not blob-specific URL logic.

## Data Lake Service Semantics

- Requires hierarchical namespace (HNS)-enabled accounts; this library does not support HNS-disabled accounts.
- Supports POSIX-like access control semantics on files and directories.
- Rename is a primary, first-class operation — not a copy-then-delete convenience alias.
- File and directory are distinct client types with different operations; do not treat them as interchangeable.
- The file system / path terminology maps to blob container / blob, but Data Lake-specific semantics must be preserved, not erased for convenience.

## Build and Test

```bash
# Build this module
mvn -f sdk/storage/azure-storage-file-datalake/pom.xml -Dgpg.skip clean install

# Run this module's tests (playback mode)
mvn -f sdk/storage/azure-storage-file-datalake/pom.xml test
```

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for storage test setup, Azurite configuration, and live test guidance.
