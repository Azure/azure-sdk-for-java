---
page_type: sample
languages:
    - java
products:
    - azure
    - azure-monitor
urlFragment: query-azuremonitor-java-samples
---

# Azure Monitor Logs and Metrics query client library samples for Java

Azure Monitor query samples are a set of self-contained Java programs that demonstrate interacting with Azure self
-contained service using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts

Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started

Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples

The following sections provide code samples covering common operations with the Azure Monitor Query client library.

* [Get logs for a query][get_logs]
* [Get logs for a batch for queries][get_batch_logs]
* [Get logs for a query with server timeout][get_servertimeout_logs]
* [Get metrics][get_metrics]

## Run large log queries using Log Analytics

Due to Log Analytics [service limits][monitor_service_limits], sometimes it may
not be possible to retrieve all the expected data in a single query. For example, the number of rows returned or the maximum size of the
data returned may exceed the stated limits. One approach for overcoming these limits is to split the queries into multiple smaller queries
using different time ranges.

This workaround allows you to avoid the cost of exporting data to a storage account (and potentially the cost of the storage account as well).

**Disclaimer:** This approach of splitting data retrieval into smaller queries is useful when dealing with a few GBs of data or a few million records per hour. For larger data sets, [exporting][logs_data_export] is recommended.

We've provided a sample that demonstrates how to split a large query into a batch query based on the number of rows. The sample can be found [here][split_query_by_rows].
We've also provided a sample that demonstrates how to split a large query into a batch query based on the size of the data returned. The sample can be found [here][split_query_by_bytes].

These samples show how to partition a large query into smaller queries using the `LogsBatchQuery` class. The partitioning is based on the timestamp "TimeGenerated".

These samples are suitable for simple data retrieval queries that utilize a subset of KQL operators. The subset of supported KQL operators can be found [here][kql_language_subset].

## Troubleshooting

Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps

See [Next steps][SDK_README_NEXT_STEPS].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines][SDK_README_CONTRIBUTING] for more information.

<!-- LINKS -->
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query
[SDK_README_DEPENDENCY]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query
[SDK_README_NEXT_STEPS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query
[get_logs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/com/azure/monitor/query/LogsQuerySample.java
[get_batch_logs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/com/azure/monitor/query/LogsQueryBatchSample.java
[get_servertimeout_logs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/com/azure/monitor/query/ServerTimeoutSample.java
[get_metrics]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/com/azure/monitor/query/MetricsQuerySample.java
[monitor_service_limits]: https://learn.microsoft.com/azure/azure-monitor/service-limits#la-query-api
[logs_data_export]: https://learn.microsoft.com/azure/azure-monitor/logs/logs-data-export?tabs=portal
[kql_language_subset]: https://learn.microsoft.com/azure/azure-monitor/logs/basic-logs-query?tabs=portal-1#kql-language-limits
[split_query_by_rows]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/com/azure/monitor/query/SplitQueryByRowCountSample.java
[split_query_by_bytes]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/com/azure/monitor/query/SplitQueryByByteSizeSample.java
