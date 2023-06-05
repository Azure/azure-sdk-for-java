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

**Code Sample Here**

### Setting the Log Analytics Workspace ID

Set the `LOG_WORKSPACE_ID` variable to the ID of the Log Analytics workspace you want to query.

```java
String LOG_WORKSPACE_ID = "<your workspace id>";
```

### Defining a Helper Function

In order to overcome the service limits, the strategy is to query data in smaller chunks based on some time column (i.e. TImeGenerated). The
following helper function takes a large query and splits it into a `LogsBatchQuery` based on the time range.
