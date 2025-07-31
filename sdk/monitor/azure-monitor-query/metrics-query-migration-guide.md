# Guide for migrating to `azure-monitor-query-metrics` from `azure-monitor-query`

This guide assists in migrating from the metrics functionality in `azure-monitor-query` to the dedicated `azure-monitor-query-metrics` library.

## Table of contents

- [Migration benefits](#migration-benefits)
  - [Cross-service SDK improvements](#cross-service-sdk-improvements)
- [Important changes](#important-changes)
  - [Group ID, artifact ID, and package names](#group-id-artifact-id-and-package-names)
  - [Client differences](#client-differences)
  - [Instantiate metrics batch clients](#instantiate-metrics-batch-clients)
  - [Query metrics using batch operations](#query-metrics-using-batch-operations)
  - [Setting audience for authentication](#setting-audience-for-authentication)
- [Additional samples](#additional-samples)

## Migration benefits

The Azure Monitor Query SDK for Java has been modularized to provide more focused functionality. The metrics batch operations have been moved from the combined `azure-monitor-query` package to a dedicated `azure-monitor-query-metrics` package. This separation offers several advantages:

- Smaller dependency footprint for applications that only need metrics batch functionality
- More focused API design specific to metrics operations
- Independent versioning allowing metrics functionality to evolve separately
- Clearer separation of concerns between logs and metrics operations

### Cross-service SDK improvements

The new Azure Monitor Query Metrics client library maintains all the cross-service improvements that were part of the `azure-monitor-query` package:

- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.
- A unified asynchronous programming model using [Project Reactor][project-reactor].
- A unified method of creating clients via client builders to interact with Azure services.

## Important changes

### Group ID, artifact ID, and package names

Both libraries have the same group ID (`com.azure`), but the artifact ID and package names have changed:

- Previous artifact ID: `azure-monitor-query`
- New artifact ID: `azure-monitor-query-metrics`

- Previous package for metrics clients: `com.azure.monitor.query`
- New package: `com.azure.monitor.query.metrics`

**Maven dependency changes:**

Previous dependency in `pom.xml`:
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-query</artifactId>
    <version>1.x.x</version>
</dependency>
```

New dependency in `pom.xml`:
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-query-metrics</artifactId>
    <version>1.x.x</version>
</dependency>
```

### Client differences

It's important to note that the `azure-monitor-query` package contained two sets of metrics clients:

1. `MetricsQueryClient`/`MetricsQueryAsyncClient`: For querying individual resource metrics, namespaces, or definitions from Azure Monitor
2. `MetricsClient`/`MetricsAsyncClient`: For querying metrics from a batch of resources

The new `azure-monitor-query-metrics` library only includes the functionality previously provided by `MetricsClient`/`MetricsAsyncClient` for batch metrics operations. If you were using the `MetricsQueryClient`/`MetricsQueryAsyncClient` from the original package, you should migrate to usage the ARM library `azure-resourcemanager-monitor` package for that functionality.

_Note: If you only need to query metrics from resources, you should migrate to the `azure-monitor-query-metrics` package._

### API changes

The following API changes were made between `azure-monitor-query` and `azure-monitor-query-metrics`:

| `azure-monitor-query` | `azure-monitor-query-metrics` | Notes |
|------------------------|------------------------------|-------|
| `com.azure.monitor.query.models.QueryTimeInterval` | `com.azure.monitor.query.metrics.models.MetricsQueryTimeInterval` | Used to specify the time range for metrics queries |

Code using the previous `QueryTimeInterval` class will need to be updated to use the new `MetricsQueryTimeInterval` class:

Previous code:
```java
import com.azure.monitor.query.models.QueryTimeInterval;
import java.time.OffsetDateTime;

public class QueryTimeIntervalSample {
    public static void main(String[] args) {
        // Create a time interval with explicit start and end times
        OffsetDateTime endTime = OffsetDateTime.now();
        OffsetDateTime startTime = endTime.minusHours(24);
        QueryTimeInterval timeInterval = new QueryTimeInterval(startTime, endTime);
        
        System.out.println("Time interval created from " + timeInterval.getStartTime() + 
                " to " + timeInterval.getEndTime());
    }
}
```

New code:
```java
import com.azure.monitor.query.metrics.models.MetricsQueryTimeInterval;
import java.time.OffsetDateTime;

public class MetricsQueryTimeIntervalSample {
    public static void main(String[] args) {
        // Create a time interval with explicit start and end times
        OffsetDateTime endTime = OffsetDateTime.now();
        OffsetDateTime startTime = endTime.minusHours(24);
        MetricsQueryTimeInterval timeInterval = new MetricsQueryTimeInterval(startTime, endTime);
        
        System.out.println("Time interval created from " + timeInterval.getStartTime() + 
                " to " + timeInterval.getEndTime());
    }
}
```

Note that the Metrics service only supports time intervals with explicit start and end times. Do not use duration-based time intervals when querying metrics.
Duration-based `MetricsQueryTimeInterval` is not supported in the `azure-monitor-query-metrics`.

### Instantiate metrics client

In `azure-monitor-query`, metrics functionality was accessed through the `MetricsClient` and `MetricsAsyncClient` classes:

```java
import com.azure.monitor.query.MetricsClient;
import com.azure.monitor.query.MetricsClientBuilder;
import com.azure.monitor.query.MetricsAsyncClient;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class MetricsClientSample {
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create synchronous client
        MetricsClient metricsClient = new MetricsClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        // Create asynchronous client
        MetricsAsyncClient metricsAsyncClient = new MetricsClientBuilder()
                .credential(tokenCredential)
                .buildAsyncClient();
        
        System.out.println("Successfully created metrics clients");
    }
}
```

In `azure-monitor-query-metrics`, the class names remain the same but the package names used in import statements should be updated to `com.azure.monitor.query.metrics.*`:

```java
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.MetricsClientBuilder;
import com.azure.monitor.query.metrics.MetricsAsyncClient;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class MetricsClientSample {
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create synchronous client
        MetricsClient metricsClient = new MetricsClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        // Create asynchronous client
        MetricsAsyncClient metricsAsyncClient = new MetricsClientBuilder()
                .credential(tokenCredential)
                .buildAsyncClient();
        
        System.out.println("Successfully created metrics clients");
    }
}
```

### Query metrics using batch operations

The method signatures for querying metrics in batch operations remain largely the same, but you'll need to update the import statements:

Previous code:
```java
import com.azure.monitor.query.MetricsClient;
import com.azure.monitor.query.MetricsClientBuilder;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.models.QueryTimeInterval;
import com.azure.core.credential.TokenCredential;
import java.util.Arrays;
import java.util.Map;
import java.time.Duration;
import java.time.OffsetDateTime;

public class BatchMetricsQuerySample {
    public static void main(String[] args) {
        // Replace these with actual resource IDs from your Azure subscription
        String resourceId1 = "<resource-id-1>";
        String resourceId2 = "<resource-id-2>";
        
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        
        MetricsClient metricsClient = new MetricsClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        // Define the time range for the query
        OffsetDateTime endTime = OffsetDateTime.now();
        OffsetDateTime startTime = endTime.minusHours(24);

        // Example batch operation
        Map<String, MetricsQueryResult> results = metricsClient.queryResourceBatch(
                Arrays.asList(resourceId1, resourceId2),
                Arrays.asList("SuccessfulCalls", "FailedCalls"),
                new MetricsQueryResourcesOptions()
                    .setGranularity(Duration.ofHours(1))
                    .setTimeInterval(new QueryTimeInterval(startTime, endTime)));
                    
        // Process the results
        results.forEach((resourceId, result) -> {
            System.out.println("Results for resource: " + resourceId);
            System.out.println("Metrics count: " + result.getMetrics().size());
        });
    }
}
```

New code:
```java
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.MetricsClientBuilder;
import com.azure.monitor.query.metrics.models.MetricsQueryResult;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryTimeInterval;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;
import java.util.Arrays;
import java.util.Map;
import java.time.Duration;
import java.time.OffsetDateTime;

public class BatchMetricsQueryMetricsSample {
    public static void main(String[] args) {
        // Replace these with actual resource IDs from your Azure subscription
        String resourceId1 = "<resource-id-1>";
        String resourceId2 = "<resource-id-2>";
        
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        
        MetricsClient metricsClient = new MetricsClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        // Define the time range for the query
        OffsetDateTime endTime = OffsetDateTime.now();
        OffsetDateTime startTime = endTime.minusHours(24);

        // Example batch operation
        Map<String, MetricsQueryResult> results = metricsClient.queryResourceBatch(
                Arrays.asList(resourceId1, resourceId2),
                Arrays.asList("SuccessfulCalls", "FailedCalls"),
                new MetricsQueryResourcesOptions()
                    .setGranularity(Duration.ofHours(1))
                    .setTimeInterval(new MetricsQueryTimeInterval(startTime, endTime)));
                    
        // Process the results
        results.forEach((resourceId, result) -> {
            System.out.println("Results for resource: " + resourceId);
            System.out.println("Metrics count: " + result.getMetrics().size());
        });
    }
}
```

### Setting audience for authentication

The new `azure-monitor-query-metrics` library supports specifying the audience for authentication. This is useful when working with custom Azure environments:

```java
import com.azure.monitor.query.metrics.MetricsClient;
import com.azure.monitor.query.metrics.MetricsClientBuilder;
import com.azure.monitor.query.metrics.models.MetricsQueryAudience;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class MetricsAudienceSample {
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        
        // Create client with custom audience
        MetricsClient metricsClient = new MetricsClientBuilder()
                .credential(tokenCredential)
                .audience(MetricsQueryAudience.AZURE_CLOUD) // Set the audience for authentication
                .buildClient();
        
        System.out.println("Created metrics client with custom audience");
        
        // Now you can use the client to query metrics with the specified audience
    }
}
```

## Additional samples

More examples can be found in the [Azure Monitor Query Metrics samples][metrics-samples].

<!-- Links -->
[project-reactor]: https://projectreactor.io/
[azure_monitor_overview]: https://learn.microsoft.com/azure/azure-monitor/overview
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query-metrics
[package]: https://search.maven.org/artifact/com.azure/azure-monitor-query-metrics
[msdocs_apiref]: https://learn.microsoft.com/java/api/com.azure.monitor.query.metrics
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query-metrics/src/samples
[changelog]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-metrics/CHANGELOG.md
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/
[metrics-samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-metrics/src/samples/java/README.md
