# Unit Testing

> **See also**: [Live Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md) · [Building](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/building.md)

---

## Philosophy: Fakes Over Mocks

Unit tests should be testable **without** mocking libraries where possible.
A *fake* is a simple replacement that returns canned responses; a *mock* uses a library
like Mockito to intercept real calls.

```java
// Prefer a fake over a Mockito mock when the fake is simple to write
public class FakeConfigurationService implements ConfigurationService {
    public Configuration readConfiguration(String configurationName) {
        Configuration config = new Configuration();
        config.setOwner("foo");
        return config;
    }
}
```

Good candidates for mocks (rather than fakes):
1. Network-connected dependencies (HTTP, database)
2. File-system operations
3. Third-party libraries with file/network I/O
4. Time-dependent operations (mock a `Clock`)

---

## Mocking Static Methods

**Avoid** libraries like PowerMock — they interfere with classloaders and JaCoCo coverage.

Instead, wrap the static call in an instance method you can mock:

```java
// Before (not testable)
public String methodToTest() {
    return process(Util.notTestFriendlyStaticMethod());
}

// After (testable via injection)
public class ClassToTest {
    private final UtilWrapper utilWrapper;

    public ClassToTest(UtilWrapper utilWrapper) {
        this.utilWrapper = utilWrapper;
    }

    public String methodToTest() {
        return process(utilWrapper.callStaticUtilMethod());
    }
}
```

---

## Mocking Final Classes (Azure SDK Clients)

Most Azure SDK client classes are `final`. To mock them with Mockito 2+:

### Steps

1. Create `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
2. Add a single line to that file:
   ```
   mock-maker-inline
   ```

### Example

```java
// Product code
public class TelemetryEvents {
    private final EventHubConsumerAsyncClient consumerClient;

    public TelemetryEvents(EventHubConsumerAsyncClient consumerClient) {
        this.consumerClient = consumerClient;
    }

    public Flux<TelemetryEvent> getEvents() {
        return consumerClient.receiveFromPartition("1", EventPosition.latest())
            .map(event -> new TelemetryEvent(event));
    }
}
```

```java
// Test code
import reactor.test.publisher.TestPublisher;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;

public class TelemetryEventsTest {
    @Test
    public void canGetEvents() {
        EventHubConsumerAsyncClient consumerClient = mock(EventHubConsumerAsyncClient.class);

        TestPublisher<EventData> eventsPublisher = TestPublisher.createCold();
        eventsPublisher.emit(new EventData("Foo"), new EventData("Bar"));
        when(consumerClient.receiveFromPartition(eq("1"), eq(EventPosition.latest())))
            .thenReturn(eventsPublisher.flux());

        TelemetryEvents telemetryEvents = new TelemetryEvents(consumerClient);

        StepVerifier.create(telemetryEvents.getEvents())
            .assertNext(event -> isMatchingTelemetry(event))
            .assertNext(event -> isMatchingTelemetry(event))
            .verifyComplete();
    }
}
```

---

## Test Parallelization

JUnit 5 runs tests in parallel by default to reduce execution time.
Several standard dependencies have non-thread-safe global state — understand the gotchas below.

### `StepVerifier.setDefaultTimeout`

**Problem:** Mutates a global timeout; can cause other parallel tests to fail with `AssertionError`.

**Fix:** Use `.verify(Duration)` instead of the global timeout:

```java
// Instead of:
StepVerifier.create(flux).verifyComplete();  // may wait forever

// Use:
StepVerifier.create(flux).expectComplete().verify(Duration.ofSeconds(30));
```

### `StepVerifier.withVirtualTime`

The default overload uses a shared global `VirtualTimeScheduler`. When tests run in parallel this can lead to races.

**Fix** (choose one):
1. Use the [overload that accepts a `Supplier<VirtualTimeScheduler>`](https://projectreactor.io/docs/test/release/api/reactor/test/StepVerifier.html#withVirtualTime-java.util.function.Supplier-java.util.function.Supplier-long-) and `dispose()` the scheduler after the test.
2. Annotate the test class with `@Isolated` and `@Execution(ExecutionMode.SAME_THREAD)`.
3. Annotate the specific test method with `@Execution(ExecutionMode.SAME_THREAD)`.

### `hasNotDroppedElements` / `hasNotDroppedErrors`

These mutate global state in `Hooks`. If other parallel tests drop elements, your test may fail.
Use `@Isolated` + `@Execution(ExecutionMode.SAME_THREAD)` when you need these validators.

### Forcing Sequential Execution

```java
// Make entire test class sequential and isolated from other parallel tests
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class MyTest { ... }
```

---

## Remote Debugging with Maven

Run a specific test class:

```bash
mvn -f sdk/<service>/pom.xml test -Dtest=MyTestClass
```

Run a specific test method:

```bash
mvn -f sdk/<service>/pom.xml test -Dtest=MyTestClass#myTestMethod
```

Attach a debugger (halts on port 5005):

```bash
mvn -f sdk/<service>/pom.xml test -Dtest=MyTestClass#myTestMethod -Dmaven.surefire.debug
```

> **PowerShell:** Quote the flag: `"-Dmaven.surefire.debug"`

Re-run without recompiling (use `surefire:test` instead of `test`):

```bash
mvn -f sdk/<service>/pom.xml surefire:test -Dtest=MyTestClass -Dmaven.surefire.debug
```

Then connect a remote debugger in [IntelliJ](https://www.jetbrains.com/help/idea/tutorial-remote-debug.html) to `localhost:5005`.

---

## Additional Resources

- [Live Testing](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/live-testing.md)
- [Test Proxy Migration](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/test-proxy-migration.md)
- [Test Proxy onboarding guide](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/testproxy/onboarding/README.md)
