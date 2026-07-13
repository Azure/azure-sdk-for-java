# AGENTS.md — Azure Storage File Share SDK for Java

This file provides File Share-specific guidance for AI agents working in the `sdk/storage/azure-storage-file-share` subtree.

For shared storage rules (generated code, magic strings, cross-service consistency, common patterns), see [../AGENTS.md](../AGENTS.md).
For repo-wide guidance, see the [root AGENTS.md](../../../AGENTS.md).

## File Share SDK Overview

The `azure-storage-file-share` module contains the Azure File Share client library for Java.

Primary client types include:
- `ShareServiceClient` / `ShareServiceAsyncClient`
- `ShareClient` / `ShareAsyncClient`
- `ShareDirectoryClient` / `ShareDirectoryAsyncClient`
- `ShareFileClient` / `ShareFileAsyncClient`

-Azure Files supports both SMB and NFS protocols; feature support, permission models, and API behavior differ between them. Do not treat file-share APIs as protocol-agnostic — verify whether a given operation or option applies to SMB, NFS, or both.

## File Share-Specific Rules

### 1. Preserve Service/Share/Directory/File Boundaries

Do not flatten resource layers:
- service operations on `ShareServiceClient`
- share operations on `ShareClient`
- directory operations on `ShareDirectoryClient`
- file operations on `ShareFileClient`

### 2. Keep SMB/File Concepts Explicit

SMB-oriented properties and file/share permission semantics should remain explicit in APIs and models, not hidden behind generic metadata abstractions.

### 3. Keep Share Snapshot Behavior Share-Scoped

Snapshot operations belong to share semantics and should remain modeled at share scope.

## File Share Service Semantics

- Share, directory, and file are distinct resource types with different operations.
- Naming behavior differs from blobs: file and directory names are case-preserving but case-insensitive.
- File range/content update semantics are file-specific and should not be generalized from blob patterns.

## Build and Test

```bash
# Build this module
mvn -f sdk/storage/azure-storage-file-share/pom.xml clean install -DskipTests

# Run this module's tests (playback mode)
mvn -f sdk/storage/azure-storage-file-share/pom.xml test
```

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for storage test setup, Azurite configuration, and live test guidance.
