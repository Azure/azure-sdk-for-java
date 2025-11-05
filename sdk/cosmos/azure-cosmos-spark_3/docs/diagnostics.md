# Cosmos DB diagnostics for Azure Monitor


## How to enable?
When using the Azure Cosmos DB Spark connector for Spark 3.* you can enable emitting diagnostics (OpenTelemetry traces, log and metrics) to Azure Monitor / ApplicationInsights simply via configuration. The only necessary changes are to set the `spark.cosmos.diagnostics.azureMonitor.enabled` config property to `true` and to specify the Azure Monitor connection string in the `spark.cosmos.diagnostics.azureMonitor.connectionString` setting.

## Configuration 
When enabled, this feature will result in emitting OpenTelemetry traces, logs and metrics to Azure Monitor. Each of these areas can be configured/disabled to tune the noise level of the diagnostics - see [Configuration reference](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos-spark_3/docs/configuration-reference.md).

### Best practices
As a best practice we recommend enabling the following configuration settings for any production workload to enable diagnostics in Azure Monitor.

| Config Property Name                                     | Recommended value                                               |
|:---------------------------------------------------------|:----------------------------------------------------------------|
| `spark.cosmos.diagnostics`                               | `sampled`                                                       |
| `spark.cosmos.diagnostics.azureMonitor.enabled`          | `true`                                                          |
| `spark.cosmos.diagnostics.azureMonitor.connectionString` | The ConnectionString for the Azure Monitor resource to be used. |
