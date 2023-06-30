# Running Large Log Queries using Log Analytics

This guide will show you how to run large log queries **without** extra costs using Log Analytics and the Monitor Query SDK.

Due to Log Analytics [service limits](https://learn.microsoft.com/en-us/azure/azure-monitor/service-limits#la-query-api), sometimes it may
not be possible to retrieve all the expected data in a single query. For example, the nnumber of rows returned or the maximum size of the
data returned may exceed the stated limits. One approach for overcoming these limits is to split the queries into multiple smaller queries
using different time ranges.

**Disclaimer:** This approach of spliting data retrieval into smaller queries is good when dealing with a few GBs of data or a few millions
of records per hour. For larger data sets, 
[exporting](https://learn.microsoft.com/en-us/azure/azure-monitor/logs/logs-data-export?tabs=portal) is recommended.

## Getting Started

An authenticated client is required to query data from Log Analytics. The following code shows how to create a `LogsQueryClient` using the
`DefaultAzureCredential` authentication method.

```java com.azure.monitor.query.LargeQuerySample-createLogsQueryClient
LogsQueryClient client = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Setting the Log Analytics Workspace ID

Set the `LOG_WORKSPACE_ID` variable to the ID of the Log Analytics workspace you want to query.

```java com.azure.monitor.query.LargeQuerySample-setWorkspaceId
static final String WORKSPACE_ID = "{workspace-id}";
```

### Defining the Helper Function

In order to overcome the service limits, the strategy is to query data in smaller chunks based on some time column (i.e. TImeGenerated). The
following helper function take a large query and splits it into a `LogsBatchQuery` based on a list of time ranges (called endpoints).

```java com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQuery
static LogsBatchQuery createBatchQueryFromLargeQuery(String originalQuery,
                                                     List<OffsetDateTime> endpoints,
                                                     String timeColumn) {
    LogsBatchQuery batchQuery = new LogsBatchQuery();

    for (int i = 0; i < endpoints.size(); i++) {
        String queryString;
        if (i == endpoints.size() - 1) {
            queryString = String.format("%1$s | where %3$s >= datetime('%2$s')", originalQuery, endpoints.get(i), timeColumn);
        }
        else {
            queryString = String.format("%1$s | where %4$s >= datetime('%2$s') and %4$s < datetime('%3$s')", originalQuery, endpoints.get(i), endpoints.get(i + 1), timeColumn);
        }
        batchQuery.addWorkspaceQuery(WORKSPACE_ID,queryString, null);
    }

    return batchQuery;
}
```

### Defining the Query-BatchQuery conversion Functions

This sample provids two functions to convert a `LogsQuery` into a `LogsBatchQuery`. One function splits the query into multiple smaller queries
based upon the approximate* maximum amount of rows to be present in each query. The other function splits the query into multiple smaller queries based upon
the approximate* maximum size of the data in bytes to be present in each query.

_*If multiple rows have the exact same TimeGenerated, the queries in the batch query may exceed the maximum amount of rows/bytes_

#### Splitting by Rows

```java com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQueryByRows
static LogsBatchQuery createBatchQueryFromLargeQueryByRowCount(String originalQuery,
                                                               int maxRowPerBatch,
                                                               String timeColumn) {

    String findBatchEndpointsQuery = String.format(
        "%1$s | sort by %2$s desc | extend batch_num = row_cumsum(1) / %3$s | summarize endpoint=min(%2$s) by batch_num | sort by batch_num desc | project endpoint",
        originalQuery,
        timeColumn,
        maxRowPerBatch);

    LogsQueryResult result = client.queryWorkspace(WORKSPACE_ID, findBatchEndpointsQuery, QueryTimeInterval.ALL);
    LogsTable table = result.getTable();
    List<LogsTableColumn> columns = table.getColumns();
    List<LogsTableRow> rows = table.getRows();
    List<OffsetDateTime> endpoints = new ArrayList<>();

    for (LogsTableRow row : rows) {
        row.getColumnValue("endpoint").ifPresent(rowValue -> {
            endpoints.add(rowValue.getValueAsDateTime());
        });
    }

    return createBatchQueryFromLargeQuery(originalQuery, endpoints, timeColumn);
}
```

#### Splitting by Size

```java com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQueryBySize
static LogsBatchQuery createBatchQueryFromLargeQueryByByteSize(String originalQuery,
                                                               int maxByteSizePerBatch,
                                                               String timeColumn) {
    String findBatchEndpointsQuery = String.format(
        "%1$s | sort by %2$s desc | extend batch_num = row_cumsum(estimate_data_size(*)) / %3$s | summarize endpoint=min(%2$s) by batch_num | sort by batch_num desc | project endpoint",
        originalQuery,
        timeColumn,
        maxByteSizePerBatch);

    LogsQueryResult result = client.queryWorkspace(WORKSPACE_ID, findBatchEndpointsQuery, QueryTimeInterval.ALL);
    LogsTable table = result.getTable();
    List<LogsTableColumn> columns = table.getColumns();
    List<LogsTableRow> rows = table.getRows();
    List<OffsetDateTime> endpoints = new ArrayList<>();

    for (LogsTableRow row : rows) {
        row.getColumnValue("endpoint").ifPresent(rowValue -> {
            endpoints.add(rowValue.getValueAsDateTime());
        });
    }

    return createBatchQueryFromLargeQuery(originalQuery, endpoints, timeColumn);
}
```

