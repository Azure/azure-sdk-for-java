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

This package uses both the DFS endpoint and the Blob endpoint; the SDK routes certain operations to the Blob endpoint when supported for compatibility/performance, so endpoint selection should follow existing client behavior rather than assuming DFS-only paths.

## Data Lake-Specific Rules

### 1. Preserve the FileSystem/Path Model

Keep file system and path semantics explicit:
- file system operations on `DataLakeFileSystemClient`
- file operations on `DataLakeFileClient`
- directory operations on `DataLakeDirectoryClient`

Do not flatten them into a single generic path abstraction.

### 2. Keep Rename and ACL Operations First-Class

Rename/move and ACL/permission behavior are core Data Lake semantics. Keep those flows explicit and aligned with existing options/models.

### 3. Prefer Blob Endpoint When Applicable; Use DFS for Data Lake-Specific Operations

For azure-storage-file-datalake, prefer Blob endpoint-backed operations where supported by existing SDK patterns, and use DFS endpoint handling for Data Lake-specific operations. Preserve Data Lake hierarchical namespace (HNS) behavioral expectations across both paths, and do not introduce new endpoint-selection logic that diverges from established client behavior.

Endpoint and URL handling must preserve Data Lake path hierarchy semantics and follow existing SDK routing behavior across both dfs.core.windows.net and blob.core.windows.net endpoints. Do not assume DFS-only URL logic; use the same endpoint-selection patterns already used by the client/operation.

## Data Lake Service Semantics

- Requires hierarchical namespace (HNS)-enabled accounts; this library is designed for and is only fully supported for HNS-enabled accounts.
- Supports POSIX-like access control semantics on files and directories.
- Rename is a primary, first-class operation — not a copy-then-delete convenience alias.
- File and directory are distinct client types with different operations; do not treat them as interchangeable.
- The file system / path terminology maps to blob container / blob, but Data Lake-specific semantics must be preserved, not erased for convenience.

## Build and Test

```bash
# Build this module
mvn -f sdk/storage/azure-storage-file-datalake/pom.xml clean install -DskipTests

# Run this module's tests (playback mode)
mvn -f sdk/storage/azure-storage-file-datalake/pom.xml test
```

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for storage test setup, Azurite configuration, and live test guidance.
