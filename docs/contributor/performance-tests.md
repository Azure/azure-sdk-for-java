# Writing Performance Tests


Performance tests for Azure SDK libraries use the `perf-test-core` framework (`com.azure:perf-test-core`). Each library that needs benchmarking gets its own Maven module at `sdk/<service>/azure-<service>-perf/`.

---

## 1. Create the Performance Test Module

Module path: `sdk/<service>/azure-<service>-perf/`

### `pom.xml` structure

```xml
<parent>
  <groupId>com.azure</groupId>
  <artifactId>azure-client-sdk-parent</artifactId>
  <version>1.7.0</version>
  <relativePath>../../parents/azure-client-sdk-parent</relativePath>
</parent>

<groupId>com.azure</groupId>
<artifactId>azure-<service-name>-perf</artifactId>
<version>1.0.0-beta.1</version>
<packaging>jar</packaging>

<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId><sdk-artifact-id></artifactId>
    <version><sdk-version></version>
  </dependency>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>perf-test-core</artifactId>
    <version>1.0.0-beta.1</version>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-assembly-plugin</artifactId>
      <executions>
        <execution>
          <phase>package</phase>
          <goals><goal>single</goal></goals>
          <configuration>
            <archive>
              <manifest>
                <mainClass>com.azure.<service>.<subservice>.App</mainClass>
              </manifest>
            </archive>
            <descriptorRefs>
              <descriptorRef>jar-with-dependencies</descriptorRef>
            </descriptorRefs>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

---

## 2. Main Class

```java
/**
 * Runs the <Service-Name> performance test.
 * To run: mvn clean package, then java -jar compiled-jar-with-dependencies
 */
public class App {
    public static void main(String[] args) {
        Class<?>[] testClasses;
        try {
            testClasses = new Class<?>[] {
                Class.forName("com.azure.<service>.perf.<APIName>Test"),
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        PerfStressProgram.run(testClasses, args);
    }
}
```

---

## 3. Performance Test Class Structure

### Abstract base class (shared client setup)

```java
public abstract class ServiceTest<TOptions extends PerfStressOptions>
        extends PerfStressTest<TOptions> {

    protected final ServiceClient serviceClient;
    protected final ServiceAsyncClient serviceAsyncClient;

    public ServiceTest(TOptions options) {
        super(options);
        // Build your service client(s) here
    }
}
```

### Concrete test class

```java
// Options class: extend PerfStressOptions to add custom CLI flags
public class UploadTest extends ServiceTest<PerfStressOptions> {

    public UploadTest(PerfStressOptions options) {
        super(options);
    }

    // One-time global resource setup
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.fromRunnable(() -> { /* create container/bucket */ }));
    }

    // Synchronous performance operation
    @Override
    public void run() {
        serviceClient.uploadData(data);
    }

    // Asynchronous performance operation
    @Override
    public Mono<Void> runAsync() {
        return serviceAsyncClient.uploadData(data).then();
    }
}
```

For a reference implementation see `sdk/storage/azure-storage-perf`.

---

## 4. Custom Options

Extend `PerfStressOptions` to add service-specific command-line flags:

```java
public class UploadOptions extends PerfStressOptions {
    @CliOption(name = "--size", description = "Size in bytes")
    private long size = 10_240;

    public long getSize() { return size; }
}
```

Pass your options class as the type parameter: `extends ServiceTest<UploadOptions>`.

---

## 5. Running the Performance Test

```bash
# 1. Build the JAR with all dependencies
mvn clean package -f sdk/<service>/azure-<service>-perf/pom.xml

# 2. Execute
java -jar sdk/<service>/azure-<service>-perf/target/azure-<service>-perf-<version>-jar-with-dependencies.jar \
  UploadTest \
  --duration 10 \
  --parallel 1 \
  --warmup 5
```

Common options from `PerfStressOptions`:

| Flag | Description |
|------|-------------|
| `--duration` | Test duration in seconds (default 10) |
| `--warmup` | Warm-up duration in seconds (default 5) |
| `--parallel` | Number of concurrent operations (default 1) |
| `--no-cleanup` | Skip cleanup after test |

---

## See Also

- [Performance Tuning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/performance-tuning.md) — user-facing guidance
- [Building](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/building.md)
