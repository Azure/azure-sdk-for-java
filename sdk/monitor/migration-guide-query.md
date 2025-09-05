# How to migrate away from `azure-monitor-query`

To migrate away from the deprecated [azure-monitor-query](https://central.sonatype.com/artifact/com.azure/azure-monitor-query) package, see the table below.

| Client name                                      | Replacement package             | Migration guidance |
|--------------------------------------------------|---------------------------------|--------------------|
| `LogsQueryClient` / `LogsQueryAsyncClient`       | [azure-monitor-query-logs]      | [Guide][mg-lq]     |
| `MetricsClient` / `MetricsAsyncClient`           | [azure-monitor-query-metrics]   | [Guide][mg-mq]     |
| `MetricsQueryClient` / `MetricsQueryAsyncClient` | [azure-resourcemanager-monitor] | [Guide][mg-mq]     |

<!-- LINKS -->
[azure-monitor-query-logs]: https://central.sonatype.com/artifact/com.azure/azure-monitor-query-logs
[azure-monitor-query-metrics]: https://central.sonatype.com/artifact/com.azure/azure-monitor-query-metrics
[azure-resourcemanager-monitor]: https://central.sonatype.com/artifact/com.azure.resourcemanager/azure-resourcemanager-monitor
[mg-lq]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-logs/migration-guide.md
[mg-mq]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-metrics/migration-guide.md
