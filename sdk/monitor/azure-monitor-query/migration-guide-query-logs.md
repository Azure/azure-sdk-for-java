# Guide for migrating from `azure-monitor-query` to `azure-monitor-query-logs`

This guide assists in migrating querying logs operations in `azure-monitor-query` to the dedicated `azure-monitor-query-logs` library.

## Table of contents

- [Migration benefits](#migration-benefits)
- [Important changes](#important-changes)
    - [Group ID, artifact ID, and package names](#group-id-artifact-id-and-package-names)
    - [Client differences](#client-differences)
    - [API changes](#api-changes)
    - [Instantiate logs query clients](#instantiate-logs-query-clients)
    - [Query logs from workspace](#query-logs-from-workspace)
    - [Query resource logs](#query-resource-logs)
- [Additional samples](#additional-samples)

## Migration benefits

The Azure Monitor Query library for Java has been modularized to provide more focused functionality. The operations for querying logs have been moved from the combined `azure-monitor-query` package which also included querying metrics to a dedicated `azure-monitor-query-logs` package. This separation offers several advantages:

- Smaller dependency footprint for applications that only need to query logs 
- More focused API design specific to logs query operations
- Independent versioning allowing logs functionality to evolve separately
- Clearer separation of concerns between logs and metrics operations

## Important changes

### Group ID, artifact ID, and package names

Both libraries have the same group ID (`com.azure`), but the artifact ID and package names have changed:

- Previous artifact ID: `azure-monitor-query`
- New artifact ID: `azure-monitor-query-logs`

- Previous package for logs query clients: `com.azure.monitor.query`
- New package: `com.azure.monitor.query.logs`

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
    <artifactId>azure-monitor-query-logs</artifactId>
    <version>1.x.x</version>
</dependency>
```

### Client differences

`LogsQueryClient` and `LogsQueryAsyncClient` classes are in `com.azure.monitor.query` package in `azure-monitor-query` library. These clients have been
moved to `com.azure.monitor.query.logs` package in the new `azure-monitor-query-logs` library. The client names and the client builder name remains the same in both libraries.

### API changes

The following API changes were made between `azure-monitor-query` and `azure-monitor-query-logs`:

| `azure-monitor-query` | `azure-monitor-query-logs`                                  | Notes                                           |
|------------------------|-------------------------------------------------------------|-------------------------------------------------|
| `com.azure.monitor.query.models.QueryTimeInterval` | `com.azure.monitor.query.logs.models.LogsQueryTimeInterval` | Used to specify the time range for logs queries |

Code using the previous `QueryTimeInterval` class will need to be updated to use the new `LogsQueryTimeInterval` class:

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
import com.azure.monitor.query.logs.models.LogsQueryTimeInterval;
import java.time.OffsetDateTime;

public class LogsQueryTimeIntervalSample {
    public static void main(String[] args) {
        // Create a time interval with explicit start and end times
        OffsetDateTime endTime = OffsetDateTime.now();
        OffsetDateTime startTime = endTime.minusHours(24);
        LogsQueryTimeInterval timeInterval = new LogsQueryTimeInterval(startTime, endTime);
        
        System.out.println("Time interval created from " + timeInterval.getStartTime() + 
                " to " + timeInterval.getEndTime());
    }
}
```

### Instantiate logs query clients

In `azure-monitor-query`, logs functionality was accessed through the `LogsQueryClient` and `LogsQueryAsyncClient` classes:

```java
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class LogsQueryClientSample {
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create synchronous client
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .buildClient();

        // Create asynchronous client
        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .buildAsyncClient();
        
        System.out.println("Successfully created logs query client");
    }
}
```

In `azure-monitor-query-logs`, the class names remain the same but the package names used in import statements should be updated to `com.azure.monitor.query.logs.*`:

```java
import com.azure.monitor.query.logs.LogsQueryClient;
import com.azure.monitor.query.logs.LogsQueryClientBuilder;
import com.azure.monitor.query.logs.LogsQueryAsyncClient;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

public class LogsQueryClientSample {
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create synchronous client
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        // Create asynchronous client
        LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildAsyncClient();

        System.out.println("Successfully created logs query client");
    }
}
```

### Query logs from workspace

The method signatures for querying logs remain largely the same, but you'll need to update the import statements:

Previous code:
```java
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

import java.util.Optional;

public class LogsQuerySample {
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient.queryWorkspace("<workspace-id>", "AppRequests",
            null);
        System.out.println("Number of tables = " + queryResults.getAllTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }

        // Sample to iterate by row
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                row.getRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }

        // Sample to get value of a column
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                Optional<LogsTableCell> resourceGroup = row.getColumnValue("DurationMs");
                if (resourceGroup.isPresent()) {
                    System.out.println(resourceGroup.get().getValueAsString());
                }
            }
        }
    }
}
```

New code:
```java
import com.azure.monitor.query.logs.LogsQueryClient;
import com.azure.monitor.query.logs.LogsQueryClientBuilder;
import com.azure.monitor.query.logs.LogsQueryAsyncClient;
import com.azure.monitor.query.logs.models.LogsQueryResult;
import com.azure.monitor.query.logs.models.LogsTable;
import com.azure.monitor.query.logs.models.LogsTableCell;
import com.azure.monitor.query.logs.models.LogsTableRow;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;

import java.util.Optional;

public class LogsQuerySample {
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient.queryWorkspace("<workspace-id>", "AppRequests",
            null);
        System.out.println("Number of tables = " + queryResults.getAllTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }

        // Sample to iterate by row
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                row.getRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }

        // Sample to get value of a column
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                Optional<LogsTableCell> resourceGroup = row.getColumnValue("DurationMs");
                if (resourceGroup.isPresent()) {
                    System.out.println(resourceGroup.get().getValueAsString());
                }
            }
        }
    }
}
```
### Query resource logs

Previous code:

```java
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.util.Optional;

/**
 * A sample to demonstrate querying for logs of an Azure resource from Azure Monitor using a Kusto query
 */
public class LogsQueryResourceSample {

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient
            .queryResource("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}", "AppRequests",
            QueryTimeInterval.ALL);
        System.out.println("Number of tables = " + queryResults.getAllTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }

        // Sample to iterate by row
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                row.getRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }

        // Sample to get value of a column
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                Optional<LogsTableCell> resourceGroup = row.getColumnValue("DurationMs");
                if (resourceGroup.isPresent()) {
                    System.out.println(resourceGroup.get().getValueAsString());
                }
            }
        }
    }
}
```

New code:

```java
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.logs.LogsQueryClient;
import com.azure.monitor.query.logs.LogsQueryClientBuilder;
import com.azure.monitor.query.logs.LogsQueryAsyncClient;
import com.azure.monitor.query.logs.models.LogsQueryResult;
import com.azure.monitor.query.logs.models.LogsTable;
import com.azure.monitor.query.logs.models.LogsTableCell;
import com.azure.monitor.query.logs.models.LogsTableRow;
import com.azure.monitor.query.logs.models.LogsQueryTimeInterval;

import java.util.Optional;

/**
 * A sample to demonstrate querying for logs of an Azure resource from Azure Monitor using a Kusto query
 */
public class LogsQueryResourceSample {

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient
            .queryResource("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}", "AppRequests",
                LogsQueryTimeInterval.ALL);
        System.out.println("Number of tables = " + queryResults.getAllTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }

        // Sample to iterate by row
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                row.getRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }

        // Sample to get value of a column
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                Optional<LogsTableCell> resourceGroup = row.getColumnValue("DurationMs");
                if (resourceGroup.isPresent()) {
                    System.out.println(resourceGroup.get().getValueAsString());
                }
            }
        }
    }
}
```

## Additional samples

More examples can be found in the [Azure Monitor Query Logs samples][logs-samples].

<!-- Links -->
[logs-samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-logs/src/samples/README.md
