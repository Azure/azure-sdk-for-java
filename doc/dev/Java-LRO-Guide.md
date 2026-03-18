# Azure SDK for Java - Long-Running Operation (LRO) Behavior

## 1. Overview

In Azure, a long-running operation (LRO) is an operation pattern that cannot complete immediately within a single synchronous request-response interaction and therefore requires subsequent polling to observe status and determine the outcome. For the official introduction to LROs in the Java SDK, see [Long-running operations in the Azure SDK for Java](https://learn.microsoft.com/en-us/azure/developer/java/sdk/lro?view=azure-java-stable).

In the Azure SDK for Java, such operations typically do not return the final result directly. Instead, they return poller abstractions:

- Synchronous clients typically return `SyncPoller<T, U>`.
- Asynchronous clients typically return `PollerFlux<T, U>`.

These abstractions are provided by Azure Core to represent operation lifecycle, status polling, and final result access in a consistent way. According to the [Java SDK design guidance for methods invoking long-running operations](https://azure.github.io/azure-sdk/java_introduction.html#methods-invoking-long-running-operations), methods that initiate LROs typically use the `begin` prefix.

This document focuses on the behavior that Java developers actually observe at the SDK layer and explains how that behavior relates to REST API contracts and TypeSpec authoring.

---

## 2. LRO Behavior in the Java SDK

### 2.1 What an LRO Is in the Java SDK

In the Java SDK, an LRO is an in-progress operation surfaced through a poller. When a caller invokes `beginXxx()`, it does not receive the final business result immediately. Instead, it first receives a handle representing an operation that is still progressing, and can then use that handle to:

- Observe status changes.
- Wait until the operation reaches a terminal state.
- Retrieve the final result when the operation contract defines one.

As a result, an LRO in the Java SDK should first be understood as an operation lifecycle model, and only secondarily as a possible carrier of a final result. This distinction matters when similar-looking LRO APIs behave differently across services.

### 2.2 Execution Model: The Key Difference Between Sync and Async

The Azure SDK for Java provides both synchronous and asynchronous LRO interaction styles, but they must be distinguished clearly in one important respect: when the operation actually starts.

- For synchronous clients, once an API returning `SyncPoller` is called, the long-running operation starts immediately. This is consistent with the official documentation in [Synchronous long-running operations](https://learn.microsoft.com/en-us/azure/developer/java/sdk/lro?view=azure-java-stable#synchronous-long-running-operations).
- For asynchronous clients, calling an API returning `PollerFlux` first gives the caller an asynchronous poller stream. The long-running operation typically begins only after subscription occurs. This is consistent with the official documentation in [Asynchronous long-running operations](https://learn.microsoft.com/en-us/azure/developer/java/sdk/lro?view=azure-java-stable#asynchronous-long-running-operations) and with Reactor's lazy execution model.

Accordingly, synchronous LROs should be understood as starting when called, while asynchronous LROs should be understood as progressing after subscription.

Despite that distinction, both `SyncPoller` and `PollerFlux` share the same high-level role:

- They provide a uniform abstraction over the polling process.
- They allow callers to make decisions based on poll state.
- They provide access to the final result when the service defines one.

### 2.3 A Poller Does Not Mean There Is Always a Final Result

`SyncPoller<T, U>` and `PollerFlux<T, U>` include both an in-progress poll-state dimension and a terminal-result dimension. However, that does not mean every LRO necessarily exposes a separate, non-null, independently meaningful final result object.

For Java SDK users, a safer mental model is:

- A poller primarily represents an operation lifecycle.
- Whether a final result exists, what type it has, and whether it carries meaningful business semantics depend on the specific REST API contract.
- Whether the operation completed successfully should be determined primarily from poller state, not from the assumption that a result object must always exist.

This is why two methods that both follow the `beginXxx()` pattern can behave differently in terms of what is available after completion.

### 2.4 Commonality and Differences Between Management Plane and Data Plane

At the Java SDK surface, LROs from management-plane and data-plane libraries converge on the same poller model. At the REST level, however, the contracts those pollers reflect are not identical.

Data-plane LROs often emphasize a job, background task, or asynchronous business workflow. These patterns are commonly aligned with the [REST API Guidelines for long-running operations and jobs](https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md#long-running-operations--jobs), and typical characteristics include:

- An explicit status-monitor endpoint.
- An explicit operation resource or result resource.
- A business result object returned on completion.

Management-plane LROs are more commonly tied to Azure Resource Manager resource lifecycle transitions, such as create, update, delete, or asynchronous actions on a resource. In these cases, completion is usually determined through the [ARM asynchronous operation contract](https://github.com/cloud-and-ai-microsoft/resource-provider-contract/blob/master/v1.0/async-api-reference.md#asynchronous-operations), resource provisioning state, and the operation template in use.

Two points need to remain explicit here:

- It is not correct to treat data-plane LROs as always having a clear final result.
- It is also not correct to treat management-plane LROs as usually having only completion state and no final result.

Final-result behavior is determined by the specific operation contract, not by the management-plane or data-plane classification alone.

For example, in [TypeSpec authoring for ARM LROs](https://azure.github.io/typespec-azure/docs/howtos/arm/long-running-operations/):

- `PUT` or `PATCH` resource operations commonly use the resource itself as the final result.
- `DELETE` operations commonly use `void` as the final result.
- `POST` actions may return an explicit response type or may return no response content.

For Java SDK users, the most direct question is therefore not "which plane is this," but rather "what does this specific REST or TypeSpec contract define as the final result?"

---

## 3. The Relationship Between TypeSpec and Java SDK LROs

LROs in the Java SDK may be hand-designed, or they may be generated from OpenAPI or TypeSpec. From a consumer perspective, what matters is the Java SDK API that is exposed. From a service-definition perspective, what determines polling behavior and final-result semantics often lives in the API specification layer.

### 3.1 What Matters in Data-Plane TypeSpec Authoring

In data-plane TypeSpec, LRO authoring usually centers on the service's own asynchronous workflow. The main concerns include:

- How status-monitor or polling endpoints are defined.
- How completion is represented.
- Whether completion yields a distinct result resource or result payload.

These choices influence how the generated SDK polls, when it treats the operation as complete, and what it returns afterward. Relevant guidance is available directly in [TypeSpec authoring for data-plane LROs](https://azure.github.io/typespec-azure/docs/getstarted/azure-core/step05/).

### 3.2 What Matters in Management-Plane TypeSpec Authoring

In management-plane TypeSpec, LRO authoring usually follows ARM conventions. The focus here is not to invent a service-specific asynchronous protocol, but to correctly apply ARM-provided async operation templates and ensure that the final result matches the operation's actual response contract.

In particular, [`FinalResult` in ARM LRO templates](https://azure.github.io/typespec-azure/docs/howtos/arm/long-running-operations/) is not decorative metadata. It is an important input to eventual SDK behavior and should match the logical outcome of the operation.

For example:

- Resource create or update operations commonly use the resource type as `FinalResult`.
- Delete operations commonly use `void`.
- Async actions should use a `FinalResult` matching the actual action response type. If there is no response content, `void` should be used.

### 3.3 What This Means for Java SDK Users

Most Java SDK users do not need to understand TypeSpec syntax directly, but TypeSpec can strongly influence the observable behavior of the resulting Java API. Therefore:

- Java users typically do not need to understand TypeSpec syntax itself.
- When it is necessary to analyze why a poller's final result is a resource, a particular response type, or `void`, the API contract should be examined first rather than only the Java SDK surface.

---

## 4. Common LRO Issues and Cases in the Azure SDK for Java

This section preserves case information from internal Java SDK Teams discussions to illustrate recurring misunderstandings seen in real usage. The Teams links here are case sources. Normative conclusions should still be based on the public documentation and design guidance linked earlier in this document.

### 4.1 Guidance on Long-Running Operations for Java

**Source**  
[Language - Java Teams discussion](https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1746507490042?tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&parentMessageId=1746138132155&teamName=Azure%20SDK&channelName=Language%20-%20Java&createdTime=1746507490042)

**Issue Description**  
SDK authors and service teams ask how LROs should be modeled and exposed in the Java SDK, especially when the service itself does not natively define an LRO pattern.

**Root Cause**  
Java SDK LRO behavior is defined at the SDK abstraction layer. Even when the service does not explicitly define a single REST-level LRO endpoint, the SDK may still compose multiple REST calls and expose an LRO to provide a better user experience.

**SDK Behavior Explanation**  
The Java SDK treats an LRO as an SDK-level contract. This is consistent with the [Java SDK design guidance for methods invoking long-running operations](https://azure.github.io/azure-sdk/java_introduction.html#methods-invoking-long-running-operations).

**Resolution**  
Service teams should first decide whether Java users need a poller-based operation model. If the answer is yes, the next step is to determine whether that LRO should map directly to the service's native contract or be composed by the SDK without distorting semantics.

### 4.2 Issue in the Java SDK: LRO Response Not Directly Accessible

**Source**  
[Language - Java Teams discussion](https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1760413333133?tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&parentMessageId=1760413333133&teamName=Azure%20SDK&channelName=Language%20-%20Java&createdTime=1760413333133)

**Issue Description**  
After calling `beginXxx()`, users report that LRO response properties appear to be missing, unexpectedly nested, or not directly accessible as the target resource type.

**Root Cause**  
The Java SDK exposes an LRO based on the service's observable polling contract, not by extracting nested resources from undocumented response shapes. The SDK exposes operation lifecycle and final-result contract, not a field-by-field mirror of the REST payload.

**SDK Behavior Explanation**  
This is consistent with the poller model described in [Long-running operations in the Azure SDK for Java](https://learn.microsoft.com/en-us/azure/developer/java/sdk/lro?view=azure-java-stable) and with the SDK abstraction principles described in Section 2.

**Resolution**  
Consumers should not assume that the final polling response always maps directly to the target resource type. If the service nests the resource inside an operation payload, that behavior should be addressed primarily at the service-design layer rather than expecting the Java SDK to automatically flatten undocumented structure.

### 4.3 Question About Changing LRO Structure

**Source**  
[Language - Java Teams discussion](https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1760993584074?tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&parentMessageId=1760993584074&teamName=Azure%20SDK&channelName=Language%20-%20Java&createdTime=1760993584074)

**Issue Description**  
Service teams consider changing LRO method signatures to return simplified types, such as `boolean`, instead of resource models, to avoid `null` or ambiguous intermediate results.

**Root Cause**  
Changing the generic types of a poller changes the public API contract and can introduce breaking changes. In addition, if the result type during polling does not convey meaningful information, its value to SDK consumers is limited.

**SDK Behavior Explanation**  
The generic parameters of `SyncPoller<T, U>` and `PollerFlux<T, U>` are part of the API contract. This is consistent with the [Java SDK design guidance for methods invoking long-running operations](https://azure.github.io/azure-sdk/java_introduction.html#methods-invoking-long-running-operations) and with the conclusion in Section 2.3.

**Resolution**  
Changing poller result types should be avoided unless introduced as a new API. If intermediate results have no practical value, consumers should rely on poller state and final completion semantics first. If the issue originates in the result contract itself, the REST or TypeSpec contract should be revisited.

### 4.4 LRO Re-hydration in the Java SDK

**Source**  
[Language - Java Teams discussion](https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1733353184034?tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&parentMessageId=1733353184034&teamName=Azure%20SDK&channelName=Language%20-%20Java&createdTime=1733353184034)

**Issue Description**  
Users ask whether an LRO can be resumed by re-creating a poller from a previously stored operation ID or continuation token.

**Root Cause**  
Java SDK LROs are built around poller instances, and Azure Core does not provide a single fully uniform cross-SDK resume entry point that can be applied directly to every service. Whether resume is supported, and how it is supported, depends on the public API design of the specific library.

**SDK Behavior Explanation**  
On one hand, the [Java SDK design guidance](https://azure.github.io/azure-sdk/java_introduction.html#methods-invoking-long-running-operations) encourages libraries to provide a way to resume a poller from serialized state. On the other hand, this capability should not be assumed to exist by default in every Java SDK.

**Resolution**  
Unless explicit documentation and public API indicate otherwise, consumers should not assume that a given Java SDK supports LRO resume. At the same time, Java LROs should not be described categorically as process-only and never resumable. The specific library documentation and API should be treated as the source of truth.

### 4.5 API Behavior When Cancellation Is Invoked After LRO Completion

**Source**  
[Language - Java Teams discussion](https://teams.microsoft.com/l/message/19:5e673e41085f4a7eaaf20823b85b2b53@thread.skype/1715728130980?tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47&groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&parentMessageId=1715728130980&teamName=Azure%20SDK&channelName=Language%20-%20Java&createdTime=1715728130980)

**Issue Description**  
Questions arise about whether calling cancellation after an LRO has already completed can affect the final result.

**Root Cause**  
Once an LRO reaches a terminal state, the operation is already finalized at the service layer. Cancellation applies only while the operation is still actively in progress.

**SDK Behavior Explanation**  
This is consistent with the explanation of terminal state and final result in [Long-running operations in the Azure SDK for Java](https://learn.microsoft.com/en-us/azure/developer/java/sdk/lro?view=azure-java-stable) and with the lifecycle model described in Section 2.

**Resolution**  
Cancellation APIs should be treated as best-effort controls during execution. Invoking cancellation after the operation has completed does not change the result of an already finalized LRO.

---

## References

- [Long-running operations in the Azure SDK for Java](https://learn.microsoft.com/en-us/azure/developer/java/sdk/lro?view=azure-java-stable)
- [Java SDK design guidance: Methods invoking long-running operations](https://azure.github.io/azure-sdk/java_introduction.html#methods-invoking-long-running-operations)
- [Java SDK design guidance: Using Azure Core types](https://azure.github.io/azure-sdk/java_introduction.html#using-azure-core-types)
- [PollerFlux API reference](https://learn.microsoft.com/en-us/java/api/com.azure.core.util.polling.pollerflux?view=azure-java-stable)
- [SyncPoller API reference](https://learn.microsoft.com/en-us/java/api/com.azure.core.util.polling.syncpoller?view=azure-java-stable)
- [REST API Guidelines: Long-running operations / jobs](https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md#long-running-operations--jobs)
- [ARM asynchronous operations reference](https://github.com/cloud-and-ai-microsoft/resource-provider-contract/blob/master/v1.0/async-api-reference.md#asynchronous-operations)
- [TypeSpec authoring for data-plane LROs](https://azure.github.io/typespec-azure/docs/getstarted/azure-core/step05/)
- [TypeSpec authoring for ARM LROs](https://azure.github.io/typespec-azure/docs/howtos/arm/long-running-operations/)
