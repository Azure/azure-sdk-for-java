# Java SDK LRO (Generated SDK)

> **Purpose**
>
> This document quickly summarizes the **definition, typical usage, and common pitfalls of LROs (Long‑Running Operations) in generated Azure Java SDKs**, and includes a few real cases from Teams threads for troubleshooting and expectation alignment.

---

## 1. Definitions

### 1.1 What an LRO is (service async)
An LRO is a **service-side asynchronous operation**: the service accepts a request, starts a long-running process, and the client must **poll** until completion.

### 1.2 What an LRO is *not* (client async I/O)
Client-side async I/O (e.g., reactive APIs or futures) changes **how the client waits** (non-blocking vs blocking), but **does not change how long the service takes**.

> **Key takeaway**
>
> **Async client APIs do not make an LRO complete faster**; they only change whether the client thread blocks while waiting.

Extra clarification: if one request/response returns the final result (or a clear failure), that is a “sync operation”; if the server continues processing after the initial response and the client must poll for terminal state and (optionally) fetch the final result, that is typically an LRO.

---

## 2. How generated Java SDKs model and use LROs

Azure Java SDK commonly represents LROs with pollers:

- **Sync**: `SyncPoller<TPollResult, TFinalResult>`
- **Async**: `PollerFlux<TPollResult, TFinalResult>`

Typical usage patterns:

- The begin operation returns a poller.
- The poller repeatedly calls the **polling endpoint** until a terminal state.
- When complete, the poller produces a **final result**.

> Note: The exact polling protocol (headers / status endpoints) depends on the service and spec.

Minimal usage examples:

```java
// sync
SyncPoller<TPollResult, TFinalResult> poller = client.beginXxx(...);
poller.waitForCompletion();
TFinalResult result = poller.getFinalResult();

// async
PollerFlux<TPollResult, TFinalResult> pollerFlux = asyncClient.beginXxx(...);
pollerFlux.getFinalResult().subscribe(resultAsync -> { /* ... */ });
```

## 3. Notes (common conventions and pitfalls)

- **Poller-returning methods are commonly prefixed with `begin`**
  - Pattern: `begin<OperationName>(...)`

- **Sync clients often use sync-over-async (transparent to callers)**
  - The sync `beginXxx(...)` you call is often a wrapper: it internally calls an async begin and returns a `SyncPoller`.

- **Avoid `void` as the terminal result type**
  - Otherwise it becomes harder for users to inspect terminal state/result details.

- **The final polling response body may not be the “real” resource**
  - Some services return a generic `OperationStatus` wrapper, and the real resource is nested under `properties` (or similar). Generated SDKs typically won’t “guess and extract” a nested resource.

- **Rehydration using only an `operationId` is not supported by default**
  - To “resume later”, you usually need SDK support such as serialized state (depends on the library/implementation), not just a stored operationId.

---

## 4. Teams cases (real-world issues)

This section captures two recurring issues in Java LRO discussions:

1) Final LRO response shape and deserialization
2) LRO rehydration / resume support

### 4.1 Final LRO response shape may not match the “real” resource
A real-world case showed:

- The polling **final 200 OK** response returned a generic `OperationStatus`.
- The actual resource (`SolutionVersion`) was nested under `properties` (even nested `properties.properties`).
- The SDK could not directly parse expected properties on the top-level model.

**Implication**
- This “resource embedded under a property of the final polling response” pattern is often difficult to model reliably with generator tooling.
- Without service/spec changes or handwritten customization, generated SDKs may not be able to auto-extract the nested resource.

**Reference**

- https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1760413333133

### 4.2 Rehydration (resume a poller later) is not supported by default
A discussion asked whether Java can rehydrate a poller using an `operationId` or continuation token (like .NET).

**Conclusion**
- Java does **not** currently support rehydrating LRO operations using only an operation id.
- If needed, file an issue to drive a consistent cross-library solution.

**Reference**

- https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1733353184034

---

## 5. Sources & references

### Teams threads
- LRO final response is `OperationStatus` and resource nested under `properties` (reviewSolutionVersion):
  - https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1760413333133

- LRO rehydration question (SyncPoller / PollerFlux):
  - https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1733353184034

### Further reading (optional)
- Azure Java SDK: Long-running operations (LRO)
  - https://learn.microsoft.com/en-us/azure/developer/java/sdk/lro

- Java SDK Guidelines: Methods Invoking Long‑Running Operations
  - https://azure.github.io/azure-sdk/java_introduction.html#methods-invoking-long-running-operations

- TypeSpec Azure: Long-running (Asynchronous) Operations
  - https://azure.github.io/typespec-azure/docs/howtos/azure-core/long-running-operations/

---
