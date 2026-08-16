# AGENTS.md — Azure Storage Blob Batch SDK for Java

This file provides Blob Batch-specific guidance for AI agents working in the `sdk/storage/azure-storage-blob-batch` subtree.

For shared storage rules (generated code, magic strings, cross-service consistency, common patterns), see <a>../AGENTS.md</a>.
For blob-specific rules and semantics that this module depends on, see <a>../azure-storage-blob/AGENTS.md</a>.
For repo-wide guidance, see the <a>root AGENTS.md</a>.

## Blob Batch SDK Overview

The `azure-storage-blob-batch` module contains the Azure Blob Batch client library for Java. It enables submitting multiple blob operations (delete, set access tier) in a single HTTP request.

This module has a **direct dependency on `azure-storage-blob`**. It wraps blob client internals to construct and submit batch requests. Changes to `azure-storage-blob` public APIs — particularly around request pipeline construction, URL building, or per-blob operation shapes — may have downstream impact here. Always check this module when modifying those areas in `azure-storage-blob`.

Primary client types include:
- `BlobBatchClient` / `BlobBatchAsyncClient`
- `BlobBatch` — represents a single batch request; operations are added to it before submission
- `BlobBatchClientBuilder` — constructs batch clients from an existing `BlobServiceClient` or `BlobServiceAsyncClient`

## Blob Batch-Specific Rules

### 1. Do Not Bypass the Batch Request Model

All operations submitted in a batch must go through `BlobBatch`. Do not construct raw HTTP multipart batch requests manually — use the existing `BlobBatch` / `BlobBatchOperation` model.

### 2. Respect the Homogeneous Batch Constraint

A single `BlobBatch` can only contain one type of operation (all deletes or all set-tier). Do not modify batch submission logic in a way that would allow mixing operation types in a single batch — this is a service-level constraint enforced by `BlobBatchType`.

### 3. Keep Batch Client Construction Blob-Client-Rooted

`BlobBatchClient` is constructed from a `BlobServiceClient` or `BlobServiceAsyncClient`, not independently. Do not introduce alternate construction paths that bypass this — the batch client inherits pipeline configuration (auth, retry, pipeline policies) from the blob service client.

### 4. Keep Sync and Async Tests Separate

When adding or modifying a sync or async client surface, ensure that **both** sync and async test coverage exist for the changed behaviour. Sync and async paths are separate code paths — changes to one can silently break the other without a dedicated test catching it.

## Blob Batch Service Semantics

- A batch request is a single HTTP call containing multiple sub-requests; the service processes each sub-request independently and returns per-operation results.
- Batch failures are per-operation — a batch response may contain a mix of successes and failures. Use `BlobBatchStorageException` to inspect per-operation errors.
- Supported batch operations are limited to: **delete blob** and **set blob access tier**. Do not attempt to add new operation types without verifying service-level batch support.
- Batch requests are scoped to a single storage account.

## Build and Test

```bash
# Build this module
mvn -f sdk/storage/azure-storage-blob-batch/pom.xml -Dgpg.skip clean install

# Run this module's tests (playback mode)
mvn -f sdk/storage/azure-storage-blob-batch/pom.xml test

# Run live tests (requires Azure resources)
AZURE_TEST_MODE=LIVE mvn -f sdk/storage/azure-storage-blob-batch/pom.xml test
```

For local Azurite setup, run `npx azurite` before invoking Maven. In CI, Azurite is managed via <a>`sdk/storage/tests-install-azurite.yml`</a> as an Azure Pipelines template — this is not directly invocable locally.

See <a>`../CONTRIBUTING.md`</a> for storage test setup and live test guidance.
