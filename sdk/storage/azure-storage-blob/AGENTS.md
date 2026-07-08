# AGENTS.md — Azure Storage Blob SDK for Java

This file provides blob-specific guidance for AI agents working in the `sdk/storage/azure-storage-blob` subtree.

For shared storage rules (generated code, magic strings, cross-service consistency, common patterns), see [../AGENTS.md](../AGENTS.md).
For repo-wide guidance, see the [root AGENTS.md](../../../AGENTS.md).

## Blob SDK Overview

The `azure-storage-blob` module contains the Azure Blob Storage client library for Java.

Primary client types include:
- `BlobServiceClient` / `BlobServiceAsyncClient`
- `BlobContainerClient` / `BlobContainerAsyncClient`
- `BlobClient` / `BlobAsyncClient`
- `BlockBlobClient`, `AppendBlobClient`, `PageBlobClient`

## Blob-Specific Rules

### 1. Preserve Generic vs Specialized Client Boundaries

When changing APIs, keep semantics on the right client:
- block blob behavior on `BlockBlobClient`
- append behavior on `AppendBlobClient`
- page/range behavior on `PageBlobClient`

Do not flatten type-specific semantics onto generic `BlobClient` APIs unless that pattern already exists.

### 2. Keep Transfer and Conditions Behavior Consistent

For upload/download and conditional operations, align with existing blob patterns and avoid introducing alternate option shapes for established workflows.

### 3. Respect Container vs Blob Responsibility

Container-level operations should stay on container clients; blob-level operations should stay on blob clients.

## Blob Service Semantics

- **Block blobs**
  - Upload flow includes staging/commit semantics.
- **Append blobs**
  - Append operations are additive and not overwrite-safe in the same way as block blob uploads.
- **Page blobs**
  - Page and range semantics differ from block/append blobs.
- **Snapshots and versions**
  - Base blobs, snapshots, and versions are distinct resource identities and should not be collapsed.

## Build and Test

```bash
# Build this module
mvn -f sdk/storage/azure-storage-blob/pom.xml -Dgpg.skip clean install

# Run this module's tests (playback mode)
mvn -f sdk/storage/azure-storage-blob/pom.xml test
```

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for storage test setup, Azurite configuration, and live test guidance.
