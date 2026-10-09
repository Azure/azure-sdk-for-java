# AGENTS.md — Azure Storage Queue SDK for Java

This file provides Queue-specific guidance for AI agents working in the `sdk/storage/azure-storage-queue` subtree.

For shared storage rules (generated code, magic strings, cross-service consistency, common patterns), see <a>../AGENTS.md</a>.
For repo-wide guidance, see the <a>root AGENTS.md</a>.

## Queue SDK Overview

The `azure-storage-queue` module contains the Azure Queue Storage client library for Java.

Primary client types include:
- `QueueServiceClient` / `QueueServiceAsyncClient`
- `QueueClient` / `QueueAsyncClient`

Service version is controlled via `com.azure.storage.queue.QueueServiceVersion`.

## Queue-Specific Rules

### 1. Preserve Service vs Queue Responsibility

Queue-level operations (send, receive, delete, peek messages) belong on `QueueClient` / `QueueAsyncClient`.
Service-level operations (create/delete queues, list queues, service properties) belong on `QueueServiceClient` / `QueueServiceAsyncClient`.

Do not flatten service-level operations onto queue clients or vice versa.

### 2. Keep Visibility Timeout Behavior Explicit

Visibility timeout is a core Queue semantics concept — it controls when a dequeued message becomes visible again to other consumers. When working with receive/update message APIs:
- Do not hide or default visibility timeout in ways that change observable message lifecycle behavior.
- Keep timeout values explicit in option shapes rather than absorbing them into convenience overloads that obscure the behavior.

### 3. Respect Message Encoding

Queue messages are encoded via `com.azure.storage.queue.QueueMessageEncoding`. When adding or modifying send/receive APIs, do not bypass or hardcode encoding behavior — route through the existing encoding configuration on the client.

### 4. Keep Sync and Async Tests Separate

When adding or modifying a sync or async client surface, ensure that **both** sync and async test coverage exist for the changed behaviour. Sync and async paths are separate code paths — changes to one can silently break the other without a dedicated test catching it.

## Queue Service Semantics

- Messages have a configurable **visibility timeout** — a dequeued message is not deleted until explicitly deleted; if not deleted within the visibility window it becomes visible again.
- Messages have a configurable **time-to-live (TTL)** — messages that exceed TTL are automatically deleted by the service.
- A queue can hold a maximum of approximately 500 TB of messages; individual message size is limited to 64 KB.
- Message ordering is best-effort FIFO — do not assume strict ordering guarantees.
- Peek operations do not affect visibility timeout; receive operations do.

## Build and Test

```bash
# Build this module
mvn -f sdk/storage/azure-storage-queue/pom.xml clean install -DskipTests

# Run this module's tests (playback mode)
mvn -f sdk/storage/azure-storage-queue/pom.xml test

# Run live tests (requires Azure resources)
AZURE_TEST_MODE=LIVE mvn -f sdk/storage/azure-storage-queue/pom.xml test
```

For local Azurite setup, run `npx azurite` before invoking Maven. In CI, Azurite is managed via <a>`sdk/storage/tests-install-azurite.yml`</a> as an Azure Pipelines template — this is not directly invocable locally.

See <a>`../CONTRIBUTING.md`</a> for storage test setup and live test guidance.
