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

## Related Modules

The following modules have a direct dependency on `azure-storage-blob` and may be affected by API changes here:

- **`azure-storage-blob-batch`** — uses blob client internals to submit batch requests; changes to request pipeline construction or URL building may require updates here.
- **`azure-storage-blob-changefeed`** — consumes blob client APIs to read change feed segments stored as blobs; changes to download or listing APIs may require updates here.
- **`azure-storage-blob-cryptography`** — wraps blob clients to add client-side encryption; changes to upload/download option shapes or client construction patterns are likely to have downstream impact here.
- **`azure-storage-blob-nio`** — implements `java.nio.file` on top of blob APIs; changes to container/blob read, write, or metadata operations may require updates here.

When modifying a public API in `azure-storage-blob`, check each of these modules for call sites that use the changed API and update them accordingly, or confirm that no change is required.

## Blob-Specific Rules

### 1. Preserve Generic vs Specialized Client Boundaries

When changing APIs, keep semantics on the right client:
- block blob behavior on `BlockBlobClient`
- append behavior on `AppendBlobClient`
- page/range behavior on `PageBlobClient`

Do not flatten type-specific semantics onto generic `BlobClient` APIs unless that pattern already exists.

### 2. Keep Transfer and Conditions Behavior Consistent

For upload/download and conditional operations, align with existing blob patterns and avoid introducing alternate option shapes for established workflows.

Note that `azure-storage-blob` contains its own `com.azure.storage.blob.models.ParallelTransferOptions` 
alongside the shared `com.azure.storage.common.ParallelTransferOptions`. Prefer the blob-specific one 
(`com.azure.storage.blob.models.ParallelTransferOptions`) for blob upload/download APIs; the common one 
is used in cross-service contexts (e.g. Data Lake's `ReadToFileOptions`). Do not conflate the two.

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
mvn -f sdk/storage/azure-storage-blob/pom.xml clean install -DskipTests

# Run this module's tests (playback mode)
mvn -f sdk/storage/azure-storage-blob/pom.xml test
```

See [`../CONTRIBUTING.md`](../CONTRIBUTING.md) for storage test setup, Azurite configuration, and live test guidance.
