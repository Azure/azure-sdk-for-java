# AGENTS.md — Azure Storage SDK for Java

This file provides storage-specific guidance for AI agents working in the `sdk/storage` subtree.
For general repo-wide guidance, see the [root AGENTS.md](../../AGENTS.md).

## Storage SDK Overview

The `sdk/storage` directory contains Java client libraries for Azure Storage services:

| Module | Service |
|---|---|
| `azure-storage-blob` | Azure Blob Storage |
| `azure-storage-blob-batch` | Blob batch operations |
| `azure-storage-blob-changefeed` | Blob Change Feed |
| `azure-storage-blob-cryptography` | Client-side encryption for Blobs |
| `azure-storage-blob-nio` | `java.nio.file` provider backed by Blob Storage |
| `azure-storage-file-share` | Azure Files (SMB/REST) |
| `azure-storage-file-datalake` | Azure Data Lake Storage Gen2 |
| `azure-storage-queue` | Azure Queue Storage |
| `azure-storage-common` | Shared primitives used by all storage libraries |
| `azure-storage-internal-avro` | Internal Avro support (not public API) |

## Rules for AI Agents

### 1. Do Not Edit Generated Code

Several source files are auto-generated from REST specs and **must not be edited by hand**. Edits will be overwritten on the next codegen run.

Generated files are typically found in paths matching:
- `src/main/java/**/implementation/` — generated implementation classes
- `src/main/java/**/models/` — generated model classes

If a bug exists in generated code, the fix must be made upstream (in the TypeSpec/OpenAPI spec or the AutoRest/TypeSpec-Java emitter configuration), not in the generated file itself.

### 2. Preserve API Consistency Across Blobs, Queues, Files, and Data Lake

Azure Storage services share a large set of common concepts (authentication, retry, error handling, pipeline configuration, SAS tokens, etc.). When adding or modifying APIs:

- Match parameter names, types, and ordering to the equivalent method in sibling services. For example, if `BlobClient.downloadToFile(String filePath, ...)` takes parameters in a certain order, `ShareFileClient.downloadToFile(...)` must match.
- Use the same option bag class names and field names where the concept is the same (e.g., `DownloadRetryOptions`, `ParallelTransferOptions`).
- Use the same return types and exception types for analogous operations.
- If you are adding a feature to one service, check whether it also applies to sibling services and file a follow-up if so.

### 3. Prefer Existing Patterns Over New Abstractions

The storage SDK has well-established patterns. Before introducing a new abstraction, verify the pattern does not already exist:

- **Retry**: use `RequestRetryOptions` / `RequestRetryPolicy` from `azure-storage-common`
- **Parallel upload/download**: use `ParallelTransferOptions`
- **Paging**: use `PagedIterable` / `PagedFlux` from `azure-core`
- **Async clients**: mirror the sync client surface using Project Reactor (`Mono`, `Flux`)
- **Client builders**: extend or follow `StorageImplUtils` and the existing `*ClientBuilder` pattern

Do not introduce new utility classes or helper abstractions unless there is a clear gap that existing patterns cannot fill. Prefer extending existing classes.

### 4. No Magic Strings

All string constants that represent service concepts must be defined as named constants, not inlined as literals:

- HTTP header names → use `HttpHeaderName` constants from `azure-core`
- Storage-specific header/query param names → define in `Constants` or a service-specific `*Constants` class (e.g., `BlobConstants`, `FileConstants`)
- Error codes → use the existing error code enums/constants; do not compare against raw string literals like `"BlobNotFound"`
- Service version strings → use the `BlobServiceVersion`, `QueueServiceVersion`, etc. enums

## Storage Service Semantics

Do not assume uniform behavior across services.

- Blob specifics:
  - AppendBlob is not overwrite-safe.
  - BlockBlob upload flow includes commit semantics.
- Queue specifics:
  - Visibility timeout drives message lifecycle behavior.
- File share specifics:
  - Hierarchical directory semantics differ from blobs.

## Build and Test

```bash
# Build all storage modules
mvn -f sdk/storage/pom.xml clean install -DskipTests

# Run tests for a specific module (playback mode)
mvn -f sdk/storage/azure-storage-blob/pom.xml test

# Run live tests (requires Azure resources)
AZURE_TEST_MODE=LIVE mvn -f sdk/storage/azure-storage-blob/pom.xml test

# Start Azurite (local storage emulator) for tests
npx azurite
```

See [`CONTRIBUTING.md`](CONTRIBUTING.md) in this directory for detailed storage-specific contribution guidance, including how to provision test resources and record/playback test sessions.
