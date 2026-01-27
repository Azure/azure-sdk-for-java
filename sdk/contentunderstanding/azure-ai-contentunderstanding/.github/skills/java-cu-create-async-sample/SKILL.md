---
name: java-cu-create-async-sample
description: |
  Creates or updates async samples for Content Understanding SDK with reactive patterns.
  
  This skill helps you:
  - Convert sync samples to async versions
  - Apply reactive patterns (Mono/Flux, flatMap, doOnNext, subscribe)
  - Ensure 100% functionality parity between sync and async
  - Report any non-portable code
  
  Trigger phrases: "create async sample", "convert to async", "生成异步 Sample", "sync to async"
---

# Java CU Create Async Sample

This skill creates or updates async samples for the Content Understanding SDK, ensuring they use proper reactive patterns and maintain 100% functionality parity with their sync counterparts.

## Workflow

### Step 1: Enumerate Sync Samples

1. List all sync samples in `src/samples/java/com/azure/ai/contentunderstanding/samples/`
2. Filter for files matching pattern: `Sample*.java` (excluding `*Async.java`)
3. For each sync sample, identify the corresponding async sample (if exists):
   - Sync: `SampleXX_Name.java`
   - Async: `SampleXX_NameAsync.java`

### Step 2: Read Reference Documentation

Before converting, read the async-patterns.md reference document in the `references/` directory for:
- Reactive programming concepts (Mono, Flux, flatMap, doOnNext, subscribe)
- Conversion patterns and examples
- Common pitfalls to avoid

### Step 3: Convert Each Sample

For each sync sample:

1. **Read the sync sample** to understand its functionality
2. **Check if async version exists**:
   - If exists: Read and compare with sync version
   - If missing: Create new async version
3. **Identify conversion points**:
   - Client: `ContentUnderstandingClient` → `ContentUnderstandingAsyncClient`
   - Methods: Direct calls → Reactive chains
   - Return types: Direct values → `Mono<T>` or `Flux<T>`
   - PollerFlux: Use reactive pattern (`.last().flatMap().subscribe()`)
4. **Apply reactive patterns** (see the async-patterns.md reference document):
   - Use `flatMap()` for sequential async operations
   - Use `doOnNext()` for side effects (printing)
   - Use `subscribe()` to start execution
   - Add `TimeUnit.sleep()` to prevent premature exit
5. **Verify functionality parity**:
   - Same operations in same order
   - Same output messages
   - Same error handling
   - Same helper methods

### Step 4: Report Issues

If something cannot be ported:
1. **Document the issue** clearly
2. **Explain why** it cannot be ported
3. **Ask the user** for guidance if needed

## Conversion Patterns

### Pattern 1: Simple Operations (Mono)

**Sync:**
```java
ContentUnderstandingDefaults defaults = client.getDefaults();
System.out.println("Defaults: " + defaults);
```

**Async:**
```java
client.getDefaults()
    .doOnNext(defaults -> System.out.println("Defaults: " + defaults))
    .subscribe();
```

### Pattern 2: Sequential Operations

**Sync:**
```java
ContentUnderstandingDefaults current = client.getDefaults();
ContentUnderstandingDefaults updated = client.updateDefaults(map);
ContentUnderstandingDefaults verified = client.getDefaults();
```

**Async:**
```java
client.getDefaults()
    .flatMap(current -> client.updateDefaults(map))
    .flatMap(updated -> client.getDefaults())
    .doOnNext(verified -> System.out.println("Verified: " + verified))
    .subscribe();
```

### Pattern 3: PollerFlux Operations

**Sync:**
```java
SyncPoller<Status, Result> poller = client.beginAnalyze(...);
Result result = poller.getFinalResult();
```

**Async:**
```java
PollerFlux<Status, Result> poller = client.beginAnalyze(...);
poller.last()
    .flatMap(pollResponse -> {
        if (pollResponse.getStatus().isComplete()) {
            return pollResponse.getFinalResult();
        } else {
            return Mono.error(new RuntimeException("Operation failed"));
        }
    })
    .subscribe(result -> {
        // Process result
    });
```

### Pattern 4: Error Handling

**Sync:**
```java
try {
    ContentUnderstandingDefaults defaults = client.getDefaults();
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
}
```

**Async:**
```java
client.getDefaults()
    .doOnError(error -> System.err.println("Error: " + error.getMessage()))
    .subscribe(
        result -> { /* success */ },
        error -> System.exit(1)
    );
```

## Naming Conventions

- Sync sample: `SampleXX_Name.java`
- Async sample: `SampleXX_NameAsync.java`
- Package: `com.azure.ai.contentunderstanding.samples`
- Class name matches file name

## Required Imports for Async Samples

```java
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeUnit;
```

## Validation Checklist

Before finalizing an async sample, verify:

- [ ] Uses `ContentUnderstandingAsyncClient` (not sync client)
- [ ] No `.block()` calls (except in retry loops if necessary)
- [ ] Uses reactive chaining (`flatMap`, `then`) for sequential operations
- [ ] Uses `doOnNext()` for side effects (printing)
- [ ] Uses `subscribe()` to start execution
- [ ] Includes `TimeUnit.sleep()` to prevent premature exit
- [ ] Same functionality as sync version (100% parity)
- [ ] Same output messages and formatting
- [ ] Same helper methods (if any)
- [ ] Same error handling behavior
- [ ] Same comments and documentation

## Non-Portable Patterns

Report these as issues:

1. **Blocking operations in loops**: May need special handling
2. **Synchronous file I/O**: May need to wrap in `Mono.fromCallable()`
3. **Thread.sleep()**: Should use `TimeUnit.sleep()` in reactive context
4. **Complex state management**: May need refactoring for reactive patterns

## Questions to Ask

If encountering non-portable code, ask:

1. "How should we handle [specific blocking operation] in the async version?"
2. "Should [specific pattern] be refactored for reactive programming?"
3. "Is [specific functionality] required to be synchronous, or can it be async?"

## Output

After processing all samples, provide:

1. **Summary**: Total sync samples, async samples created/updated
2. **Issues**: List of any non-portable code with explanations
3. **Questions**: Any questions that need user input
