# How to migrate away from `azure-monitor-query`

To migrate away from the deprecated [azure-monitor-query](https://central.sonatype.com/artifact/com.azure/azure-monitor-query) package, see the table below.

| Use case      | Replacement package           | Migration guidance |
|---------------|-------------------------------|--------------------|
| Logs query    | [azure-monitor-query-logs]    | [Guide][mg-lq]     |
| Metrics query | [azure-monitor-query-metrics] | [Guide][mg-mq]     |

<!-- LINKS -->
[azure-monitor-query-logs]: https://central.sonatype.com/artifact/com.azure/azure-monitor-query-logs
[azure-monitor-query-metrics]: https://central.sonatype.com/artifact/com.azure/azure-monitor-query-metrics
[mg-lq]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-logs/migration-guide.md
[mg-mq]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query-metrics/migration-guide.md
