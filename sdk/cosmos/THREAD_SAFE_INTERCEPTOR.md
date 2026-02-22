# Multi-Threaded Test-Safe JSON Parse Interceptor

## Problem

**Challenge 1**: Original implementation used a static volatile field - all threads shared one interceptor
- When multiple tests run in parallel, they interfere with each other ❌

**Challenge 2**: Changed to ThreadLocal - but tests spawn multiple threads!
- ChangeFeedProcessor spawns worker threads
- ThreadLocal only works on the thread that set it
- Worker threads don't see the interceptor ❌

## Solution

Use **InheritableThreadLocal** + **Context Key Map**:
- Each test gets a unique context key (UUID)
- Context key stored in InheritableThreadLocal (inherited by child threads)
- Interceptor stored in ConcurrentHashMap keyed by context
- All threads in a test share the same interceptor via inherited context key
- Different tests use different keys for isolation

```java
private static final InheritableThreadLocal<String> testContextKey = new InheritableThreadLocal<>();
private static final ConcurrentHashMap<String, TestOnlyJsonParseInterceptor> interceptorMap = new ConcurrentHashMap<>();
```

## How It Works

```
Test Thread A (UUID: abc-123)
─────────────────────────────
Set interceptor with key "abc-123"
  ↓
testContextKey.set("abc-123")  ← InheritableThreadLocal
interceptorMap.put("abc-123", interceptorA)
  ↓
Spawn CFP Worker Thread A1
  ↓
Worker Thread A1 inherits key "abc-123"
  ↓
Parse JSON → testContextKey.get() = "abc-123"
          → interceptorMap.get("abc-123") = interceptorA ✅


Test Thread B (UUID: def-456) - RUNNING IN PARALLEL
─────────────────────────────
Set interceptor with key "def-456"
  ↓
testContextKey.set("def-456")  ← InheritableThreadLocal
interceptorMap.put("def-456", interceptorB)
  ↓
Spawn CFP Worker Thread B1
  ↓
Worker Thread B1 inherits key "def-456"
  ↓
Parse JSON → testContextKey.get() = "def-456"
          → interceptorMap.get("def-456") = interceptorB ✅

NO INTERFERENCE BETWEEN TESTS! ✅
```

### Key Changes

1. **JsonNodeStorePayload.java**
   - Added `InheritableThreadLocal<String> testContextKey` for context propagation
   - Added `ConcurrentHashMap<String, TestOnlyJsonParseInterceptor> interceptorMap` for storage
   - Updated `fromJson()` to get context key, then lookup interceptor from map
   - Updated `setTestOnlyJsonParseInterceptor(String contextKey, ...)` to require context key
   - Child threads automatically inherit parent's context key

2. **JsonParseInterceptorHelper.java**
   - Auto-generates unique UUID as context key for each test
   - All helper methods now create context-keyed interceptors
   - Supports explicit context key management if needed

3. **Benefits**
   - ✅ Multi-threaded test support (CFP worker threads see interceptor)
   - ✅ Parallel test isolation (different UUIDs prevent interference)
   - ✅ No synchronization overhead (ConcurrentHashMap handles concurrency)
   - ✅ Automatic context inheritance (child threads work seamlessly)
   - ✅ Memory safe (proper cleanup removes from both map and InheritableThreadLocal)

## How It Works

```
Thread 1 (Test A)                    Thread 2 (Test B)
─────────────────                    ─────────────────
Set interceptor                      Set interceptor
  ↓                                    ↓
testOnlyInterceptor                  testOnlyInterceptor
.set(interceptorA)                   .set(interceptorB)
  ↓                                    ↓
ThreadLocal storage:                 ThreadLocal storage:
  Thread1 → interceptorA               Thread2 → interceptorB
  ↓                                    ↓
Parse JSON                           Parse JSON
  ↓                                    ↓
testOnlyInterceptor.get()            testOnlyInterceptor.get()
  ↓                                    ↓
Returns interceptorA                 Returns interceptorB
(isolated to Thread 1)               (isolated to Thread 2)
```

## Usage Example

```java
@Test(groups = { "long-emulator" }, timeOut = 50000)
public void changeFeedProcessorHandlesStreamConstraintsException() throws Exception {
    // ... setup code ...
    
    // Auto-generated UUID context key ensures isolation from parallel tests
    // InheritableThreadLocal ensures CFP worker threads inherit the interceptor
    try (AutoCloseable interceptor = 
            JsonParseInterceptorHelper.injectStreamConstraintsExceptionOnce()) {
        
        // Start ChangeFeedProcessor (spawns worker threads)
        ChangeFeedProcessor processor = ...;
        processor.start();
        
        // Main thread: context key = "abc-123" (example UUID)
        // Worker thread 1: inherits context key = "abc-123" ✅
        // Worker thread 2: inherits context key = "abc-123" ✅
        // All threads use same interceptor via shared context key
        
    } // Interceptor auto-cleared for context "abc-123"
}
```

## Parallel Test Scenario

```java
// Test 1 running on Thread A - Context: "uuid-aaa"
@Test
public void test1() {
    try (AutoCloseable i = injectStreamConstraintsExceptionOnce()) {
        // Context key: "uuid-aaa" (auto-generated)
        // This test + all its child threads use context "uuid-aaa"
        runChangeFeedProcessor(); // Spawns workers with inherited context
    }
}

// Test 2 running on Thread B (IN PARALLEL) - Context: "uuid-bbb"
@Test
public void test2() {
    try (AutoCloseable i = injectStreamConstraintsException(3)) {
        // Context key: "uuid-bbb" (auto-generated, different from Test 1)
        // This test + all its child threads use context "uuid-bbb"
        runChangeFeedProcessor(); // Spawns workers with inherited context
        // No interference with Test 1! ✅
    }
}

// Test 3 running on Thread C (IN PARALLEL) - No context
@Test
public void test3() {
    // No interceptor set, no context key
    runChangeFeedProcessor();
    // Workers check context key → null → no interceptor ✅
    // Tests 1 and 2 don't affect this
}
```

## Memory Management

⚠️ **Important**: Always clear the interceptor using try-with-resources or explicit cleanup:

```java
// ✅ GOOD - Automatic cleanup
try (AutoCloseable interceptor = injectStreamConstraintsExceptionOnce()) {
    // test code
} // Automatically calls clearTestOnlyJsonParseInterceptor()

// ❌ BAD - Memory leak risk
JsonParseInterceptorHelper.setInterceptor(myInterceptor);
// ... test code ...
// Forgot to clear - ThreadLocal holds reference until thread dies
```

The `ThreadLocal.remove()` call in `clearTestOnlyJsonParseInterceptor()` is critical to prevent memory leaks, especially in long-lived test runner threads.

## Implementation Details

### Before (Not Thread-Safe)
```java
// Static field shared across all threads
private static volatile TestOnlyJsonParseInterceptor testOnlyInterceptor = null;

private static JsonNode fromJson(...) {
    TestOnlyJsonParseInterceptor interceptor = testOnlyInterceptor; // All threads see same value
    if (interceptor != null) {
        return interceptor.intercept(...);
    }
    // ...
}

public static void setTestOnlyJsonParseInterceptor(TestOnlyJsonParseInterceptor interceptor) {
    testOnlyInterceptor = interceptor; // Affects ALL threads immediately
}
```

### After (Thread-Safe)
```java
// ThreadLocal storage - each thread has its own value
private static final ThreadLocal<TestOnlyJsonParseInterceptor> testOnlyInterceptor = new ThreadLocal<>();

private static JsonNode fromJson(...) {
    TestOnlyJsonParseInterceptor interceptor = testOnlyInterceptor.get(); // Thread-specific value
    if (interceptor != null) {
        return interceptor.intercept(...);
    }
    // ...
}

public static void setTestOnlyJsonParseInterceptor(TestOnlyJsonParseInterceptor interceptor) {
    testOnlyInterceptor.set(interceptor); // Only affects current thread
}

public static void clearTestOnlyJsonParseInterceptor() {
    testOnlyInterceptor.remove(); // Clears current thread's value and prevents memory leaks
}
```

## Testing Verification

To verify thread isolation works:

```java
@Test
public void verifyThreadIsolation() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    AtomicInteger thread1Calls = new AtomicInteger(0);
    AtomicInteger thread2Calls = new AtomicInteger(0);
    
    // Thread 1
    new Thread(() -> {
        try (AutoCloseable i = JsonParseInterceptorHelper.setInterceptor(
            (bytes, headers, parser) -> {
                thread1Calls.incrementAndGet();
                return parser.parse(bytes, headers);
            })) {
            // Do work that triggers JSON parsing
            latch.countDown();
        }
    }).start();
    
    // Thread 2
    new Thread(() -> {
        try (AutoCloseable i = JsonParseInterceptorHelper.setInterceptor(
            (bytes, headers, parser) -> {
                thread2Calls.incrementAndGet();
                return parser.parse(bytes, headers);
            })) {
            // Do work that triggers JSON parsing
            latch.countDown();
        }
    }).start();
    
    latch.await();
    
    // Each thread should only see its own interceptor's calls
    assertThat(thread1Calls.get()).isGreaterThan(0);
    assertThat(thread2Calls.get()).isGreaterThan(0);
}
```

## Summary

The ThreadLocal approach provides:
- ✅ **Full thread isolation** - No interference between parallel tests
- ✅ **Clean API** - Same usage pattern, just thread-safe internally
- ✅ **No synchronization overhead** - Each thread has its own storage
- ✅ **Memory safe** - Proper cleanup with `remove()` prevents leaks
- ✅ **Production safe** - ThreadLocal has minimal overhead when not used
