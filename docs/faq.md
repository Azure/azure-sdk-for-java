# Frequently Asked Questions

---

## My app uses the async client libraries but never gets any results

**Cause:** Reactive types (`Mono<T>`, `Flux<T>`) are *cold* — they do nothing until subscribed to.

```java
// WRONG — subscribes but may exit before results arrive
Mono<Response<Key>> result = asyncClient.getKey();
result.subscribe(response -> System.out.println(response.getValue()));
```

```java
// CORRECT — block() ensures the result is received before moving on
asyncClient.getKey()
    .subscribe(response -> System.out.println(response.getValue()))
    .block();

// OR — use the synchronous client directly
Key key = client.getKey();
```

> **Note:** `.block()` is only appropriate in non-reactive applications (e.g. command-line tools). In a fully reactive pipeline, propagate `Mono`/`Flux` to the caller.

---

## I'm getting `NoSuchMethodError` or `NoClassDefFoundError`

This almost always means a **dependency version conflict**.

### Diagnosis

```bash
mvn dependency:tree
```

Find the package that contains the missing type. Look for multiple versions in the tree.

### Resolution

Force a specific version in your project's `pom.xml`:

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>conflicting-library</artifactId>
  <version>2.14.3</version> <!-- force the version both libraries are compatible with -->
</dependency>
```

See [Troubleshoot dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for a detailed walkthrough.

---

## Creating Shaded JARs to Avoid Dependency Conflicts

Shaded JARs bundle and relocate all dependencies, eliminating transitive conflicts. The Maven Shade Plugin (`maven-shade-plugin`) is the standard tool for this.

---

## How to Manage Dependencies on Azure Databricks (ADB)

ADB injects Spark and its dependencies (including older Jackson versions) into the app classpath, causing conflicts with Azure SDK dependencies.

**Recommended solution:** Create a shaded JAR (see above), then upload it as a cluster library or job JAR.

### Steps

1. Create a shaded JAR with relocation rules for conflicting packages (e.g. Jackson) using `maven-shade-plugin`.
2. In the Azure Portal → Databricks workspace → create a cluster.
3. From the cluster, create a Job → set task as "Set JAR" → upload your shaded JAR.
4. Set the Main class to the `mainClass` entry from the maven-shade-plugin configuration.

**If conflicts persist:** Add relocation rules for each conflicting dependency in the shade plugin configuration.

---

## My App Uses Java's Security Manager and I Have All Permissions But Still Get Denied

**Cause:** The Azure SDK uses MSAL4J for authentication, which calls `CompletableFuture` internally. This uses threads from the common `ForkJoinPool`, and those threads have **no permissions** by default under a Security Manager.

### Fix 1: Use a custom `ExecutorService` (preferred for azure-identity ≥ 1.1.0)

```java
ExecutorService executor = Executors.newCachedThreadPool();
try {
    ClientSecretCredential credential = new ClientSecretCredentialBuilder()
        .clientId("<client-ID>")
        .clientSecret("<client-secret>")
        .tenantId("<tenant-ID>")
        .executorService(executor)  // <-- provide your thread pool
        .build();

    SecretClient secretClient = new SecretClientBuilder()
        .vaultUrl("https://<vault>.vault.azure.net")
        .credential(credential)
        .buildClient();

    KeyVaultSecret secret = secretClient.getSecret("<secret-name>");
} finally {
    executor.shutdown();
}
```

### Fix 2: Custom `ForkJoinWorkerThreadFactory`

```java
public static class MyForkJoinWorkerThreadFactory
        implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new ForkJoinWorkerThread(pool) {};
    }
}
```

Then add this JVM argument:

```
-Djava.util.concurrent.ForkJoinPool.common.threadFactory=MyForkJoinWorkerThreadFactory
```

---

## See Also

- [Configuration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/configuration.md)
- [Azure Identity Examples](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/identity-examples.md)
- [Performance Tuning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/performance-tuning.md)
