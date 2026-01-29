# Async Programming Reference for Content Understanding SDK

This document provides reactive programming concepts and patterns for creating async samples in the Content Understanding SDK.

## Introduction to Reactive Programming in Java

### What is Reactive Programming?

Reactive programming is a programming paradigm that focuses on asynchronous data streams and the propagation of change. In Java, reactive programming is implemented using **Project Reactor**, which provides `Mono` and `Flux` types for handling asynchronous operations.

Unlike traditional synchronous programming where operations block threads waiting for results, reactive programming allows operations to be non-blocking and asynchronous. This means your code can start multiple operations and handle their results as they complete, rather than waiting for each one sequentially.

### Why is Reactive Programming Required?

1. **Non-Blocking Operations**: Reactive programming allows your application to handle multiple operations concurrently without blocking threads. This is especially important for I/O-bound operations like API calls, database queries, or file operations.

2. **Better Resource Utilization**: Instead of blocking threads waiting for responses, reactive programming allows threads to be freed up to handle other tasks. This leads to better scalability and resource efficiency.

3. **Composability**: Reactive streams can be easily composed, transformed, and chained together, making complex asynchronous workflows more readable and maintainable.

4. **Backpressure Handling**: Reactive streams can handle backpressure (when a producer is faster than a consumer) automatically, preventing memory issues.

5. **Azure SDK Standard**: Azure SDK for Java uses reactive programming for all async operations, providing a consistent pattern across all services.

### How is Reactive Programming Commonly Used?

In Azure SDK for Java and Content Understanding SDK specifically:

- **API Calls**: All async client methods return `Mono<T>` (for single results) or `Flux<T>` (for collections/streams)
- **Long-Running Operations**: Operations like document analysis use `PollerFlux` which emits status updates over time
- **Chaining Operations**: Sequential operations are chained using `flatMap()` to ensure proper ordering
- **Error Handling**: Errors are propagated through the reactive stream and handled with `doOnError()` or error callbacks
- **Side Effects**: Operations like printing or logging are done with `doOnNext()` without blocking the stream

### For .NET Developers: Java Reactive vs .NET async/await

If you're coming from a .NET background, here's how Java reactive programming compares to .NET's async/await pattern:

**Similarities:**

- Both are designed for non-blocking, asynchronous operations
- Both handle I/O-bound operations efficiently
- Both allow composing multiple async operations

**Key Differences:**

| .NET async/await | Java Reactive (Project Reactor) |
|------------------|----------------------------------|
| `Task<T>` / `Task` | `Mono<T>` / `Mono<Void>` |
| `IAsyncEnumerable<T>` | `Flux<T>` |
| `await` keyword | `.block()` (avoid in async samples) or `.subscribe()` |
| Sequential: `var result = await operation()` | Sequential: `.flatMap(result -> nextOperation())` |
| `async` method modifier | No modifier needed - methods return `Mono`/`Flux` |
| `try/catch` for errors | `.doOnError()` or error callback in `.subscribe()` |
| `await Task.WhenAll()` | `Flux.merge()` or `Mono.zip()` |

**Example Comparison:**

**.NET (async/await):**

```csharp
var current = await client.GetDefaultsAsync();
var updated = await client.UpdateDefaultsAsync(map);
var verified = await client.GetDefaultsAsync();
Console.WriteLine($"Verified: {verified}");
```

**Java (Reactive):**

```java
client.getDefaults()
    .flatMap(current -> client.updateDefaults(map))
    .flatMap(updated -> client.getDefaults())
    .doOnNext(verified -> System.out.println("Verified: " + verified))
    .subscribe();
```

**Key Takeaway:** In .NET, you use `await` to get values from async operations. In Java reactive, you chain operations with `flatMap()` and handle values in callbacks (`doOnNext()`, `subscribe()`). The Java approach is more functional and composable, but requires thinking in terms of streams and transformations rather than sequential await statements.

### Key Principles

1. **Lazy Execution**: Reactive streams don't execute until you call `subscribe()` - this allows you to build up complex chains before execution
2. **Immutable**: Each operator returns a new stream, keeping the original unchanged
3. **Non-Blocking**: Operations never block threads - they return immediately and process results asynchronously
4. **Composable**: Streams can be combined, filtered, transformed, and chained together

### Example: Sync vs Reactive

**Synchronous (Blocking):**

```java
// Each call blocks the thread until complete
ContentUnderstandingDefaults current = client.getDefaults();  // Blocks here
ContentUnderstandingDefaults updated = client.updateDefaults(map);  // Blocks here
System.out.println("Done");
```

**Reactive (Non-Blocking):**

```java
// Operations are chained and execute asynchronously
client.getDefaults()
    .flatMap(current -> client.updateDefaults(map))
    .doOnNext(updated -> System.out.println("Done"))
    .subscribe();  // Starts execution, doesn't block
```

The reactive version allows the thread to handle other work while waiting for API responses, making your application more efficient and scalable.

## Core Concepts

### Mono vs Flux

**Mono<T>**: Represents 0 or 1 value

- Use for: Single API calls, get operations, update operations
- Example: `Mono<ContentUnderstandingDefaults> getDefaults()`

**Flux<T>**: Represents 0 to N values

- Use for: Collections, streams, PollerFlux
- Example: `Flux<ContentAnalyzer> listAnalyzers()`

### Key Operators

#### subscribe() - Start Execution

**Purpose**: Subscribes to a Mono/Flux and executes callbacks.

**Example:**

```java
client.getDefaults()
    .subscribe(
        result -> System.out.println("Got: " + result),  // onNext
        error -> System.err.println("Error: " + error),  // onError
        () -> System.out.println("Done!")                // onComplete
    );
```

**Important**: Without `subscribe()`, nothing happens - reactive chains are lazy.

#### doOnNext() - Side Effects

**Purpose**: Perform side effects (like printing) without changing the value.

**Example:**

```java
client.getDefaults()
    .doOnNext(defaults -> System.out.println("Current: " + defaults))
    .map(defaults -> defaults.getModelDeployments())  // Value passes through unchanged
    .doOnNext(deployments -> System.out.println("Deployments: " + deployments))
    .subscribe();
```

**Key point**: `doOnNext()` doesn't change the value - it just "peeks" at it.

#### flatMap() - Chain Sequential Async Operations

**Purpose**: Chain async operations where each returns a Mono/Flux.

**Example:**

```java
// Sequential operations
client.getDefaults()
    .flatMap(current -> {
        Map<String, String> updates = buildUpdates(current);
        return client.updateDefaults(updates);  // Returns Mono
    })
    .flatMap(updated -> {
        return client.getDefaults();  // Returns Mono
    })
    .subscribe();
```

**When to use**:

- Use `flatMap()` when the operation returns `Mono`/`Flux` (async)
- Use `map()` when the operation is synchronous (e.g., `toUpperCase()`)

#### then() - Chain Without Using Previous Value

**Purpose**: Chain operations when you don't need the previous value.

**Example:**

```java
client.updateDefaults(map)
    .then(client.getDefaults())  // Don't need updated value, just chain
    .subscribe(defaults -> System.out.println("Final: " + defaults));
```

## Conversion Patterns

### Pattern 1: Simple Get Operation

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

// Prevent premature exit
try {
    TimeUnit.SECONDS.sleep(5);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### Pattern 2: Sequential Operations

**Sync:**

```java
ContentUnderstandingDefaults current = client.getDefaults();
System.out.println("Current: " + current);

Map<String, String> updates = buildUpdates();
ContentUnderstandingDefaults updated = client.updateDefaults(updates);
System.out.println("Updated: " + updated);

ContentUnderstandingDefaults verified = client.getDefaults();
System.out.println("Verified: " + verified);
```

**Async:**

```java
client.getDefaults()
    .doOnNext(current -> {
        System.out.println("Current: " + current);
    })
    .flatMap(current -> {
        Map<String, String> updates = buildUpdates();
        return client.updateDefaults(updates);
    })
    .doOnNext(updated -> {
        System.out.println("Updated: " + updated);
    })
    .flatMap(updated -> {
        return client.getDefaults();
    })
    .doOnNext(verified -> {
        System.out.println("Verified: " + verified);
    })
    .doOnError(error -> {
        System.err.println("Error: " + error.getMessage());
        error.printStackTrace();
    })
    .subscribe(
        result -> System.out.println("Completed successfully"),
        error -> System.exit(1)
    );

try {
    TimeUnit.SECONDS.sleep(10);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    e.printStackTrace();
}
```

### Pattern 3: PollerFlux (Long-Running Operations)

**Sync:**

```java
SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult> operation
    = client.beginAnalyze("prebuilt-invoice", Arrays.asList(input));

AnalyzeResult result = operation.getFinalResult();
System.out.println("Analysis completed");
```

**Async:**

```java
PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalyzeResult> operation
    = client.beginAnalyze("prebuilt-invoice", Arrays.asList(input));

operation.last()
    .flatMap(pollResponse -> {
        if (pollResponse.getStatus().isComplete()) {
            System.out.println("Polling completed successfully");
            return pollResponse.getFinalResult();
        } else {
            return Mono.error(new RuntimeException(
                "Polling completed unsuccessfully with status: " + pollResponse.getStatus()));
        }
    })
    .doOnNext(result -> {
        System.out.println("Analysis completed");
    })
    .doOnError(error -> {
        System.err.println("Error: " + error.getMessage());
        error.printStackTrace();
    })
    .subscribe(
        result -> { /* Success */ },
        error -> System.exit(1)
    );

try {
    TimeUnit.MINUTES.sleep(1);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    e.printStackTrace();
}
```

### Pattern 4: Error Handling

**Sync:**

```java
try {
    ContentUnderstandingDefaults defaults = client.getDefaults();
    System.out.println("Success: " + defaults);
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
    e.printStackTrace();
}
```

**Async:**

```java
client.getDefaults()
    .doOnNext(defaults -> {
        System.out.println("Success: " + defaults);
    })
    .doOnError(error -> {
        System.err.println("Error: " + error.getMessage());
        error.printStackTrace();
    })
    .subscribe(
        result -> { /* Success */ },
        error -> System.exit(1)
    );
```

### Pattern 5: Conditional Operations

**Sync:**

```java
ContentAnalyzer analyzer = client.getAnalyzer(analyzerId);
if (analyzer != null) {
    System.out.println("Found: " + analyzer.getAnalyzerId());
} else {
    System.out.println("Not found");
}
```

**Async:**

```java
client.getAnalyzer(analyzerId)
    .doOnNext(analyzer -> {
        System.out.println("Found: " + analyzer.getAnalyzerId());
    })
    .switchIfEmpty(Mono.fromRunnable(() -> {
        System.out.println("Not found");
    }))
    .subscribe();
```

## Common Mistakes to Avoid

### ❌ Using .block() in Async Samples

**Wrong:**

```java
ContentUnderstandingDefaults defaults = client.getDefaults().block();
```

**Correct:**

```java
client.getDefaults()
    .subscribe(defaults -> { /* use defaults */ });
```

### ❌ Not Chaining Sequential Operations

**Wrong:**

```java
client.getDefaults().subscribe(current -> {});
client.updateDefaults(map).subscribe(updated -> {});  // May execute before first completes
```

**Correct:**

```java
client.getDefaults()
    .flatMap(current -> client.updateDefaults(map))
    .subscribe(updated -> {});
```

### ❌ Forgetting to Subscribe

**Wrong:**

```java
client.getDefaults()
    .doOnNext(defaults -> System.out.println(defaults));
// Nothing happens - chain is lazy!
```

**Correct:**

```java
client.getDefaults()
    .doOnNext(defaults -> System.out.println(defaults))
    .subscribe();  // Starts execution
```

### ❌ Not Preventing Premature Exit

**Wrong:**

```java
client.getDefaults()
    .subscribe(defaults -> System.out.println(defaults));
// Program exits before async operation completes
```

**Correct:**

```java
client.getDefaults()
    .subscribe(defaults -> System.out.println(defaults));

try {
    TimeUnit.SECONDS.sleep(5);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

## Real Examples from CU SDK

### Example 1: UpdateDefaults (Simple Sequential)

**Sync Pattern:**

```java
ContentUnderstandingDefaults current = client.getDefaults();
ContentUnderstandingDefaults updated = client.updateDefaults(map);
ContentUnderstandingDefaults verified = client.getDefaults();
```

**Async Pattern:**

```java
client.getDefaults()
    .flatMap(current -> client.updateDefaults(map))
    .flatMap(updated -> client.getDefaults())
    .subscribe(verified -> {});
```

### Example 2: Analyze Invoice (PollerFlux)

**Sync Pattern:**

```java
SyncPoller<Status, AnalyzeResult> operation = client.beginAnalyze(...);
AnalyzeResult result = operation.getFinalResult();
```

**Async Pattern:**

```java
PollerFlux<Status, AnalyzeResult> operation = client.beginAnalyze(...);
operation.last()
    .flatMap(pollResponse -> pollResponse.getFinalResult())
    .subscribe(result -> {});
```

## Required Imports

```java
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.concurrent.TimeUnit;
```

## Best Practices

1. **Always use reactive chaining** for sequential operations
2. **Use `doOnNext()` for side effects** (printing, logging)
3. **Use `flatMap()` for async operations** that return Mono/Flux
4. **Always call `subscribe()`** to start execution
5. **Add sleep** to prevent premature program exit
6. **Handle errors** with `doOnError()` and error callback in `subscribe()`
7. **Match sync sample output** exactly for parity
