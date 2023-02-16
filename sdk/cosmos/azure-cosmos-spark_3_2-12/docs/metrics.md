# Cosmos DB client-side metrics

The Cosmos DB SDK for Java allows enabling micrometer.io metrics to track latency, request charge (RU/s), request rates etc. for logical operations (API calls into the SDK from your application/service) as well as actual requests to the Cosmos DB service (for example when due to Consistency level Strong or Bounded Staleness requests need to be sent to multiple replica). These metrics are tagged with different dimensions - so it is possible to look at metrics like latency, request charge etc. for a certain dimension like operation type, response status code etc.

**<u>Please note:</u> There is some performance overhead when enabling metrics due to increased CPU usage. The overhead is small (<10%) but should be tested before enabling it in production especially for large containers/workloads (> 5 TB of data or > 1,000,000 RU/s)** 

More information about the metrics being emitted by the Azure Cosmos DB SDK are available here: https://aka.ms/azure-cosmos-metrics



## How to enable metrics?

When using the Azure Cosmos DB Spark connector for Spark 3.* you can enable these client-side metrics and emit them to the Spark metric system (ganglia plus optionally in Log4j log files) as well as to Azure Monitor / ApplicationInsights simply via configuration.

### How to enable in Azure Databricks?

The Spark API that would allow to provide custom metrics is the Spark plugin API - see ` https://issues.apache.org/jira/browse/SPARK-24918`,  `https://github.com/apache/spark/pull/26170` and `http://blog.madhukaraphatak.com/spark-plugin-part-4/`

The Azure Cosmos DB Spark connector contains two such plugins to emit metrics:

- `com.azure.cosmos.spark.plugins.CosmosMetricsSparkPlugin`: This plugin emits the metrics to the Spark metric system (Spark config can be used to control where Spark emits metrics - Azure Databricks would usually push them to the Ganglia Metrics UI included in the Spark UI). Optionally the metrics can also be pushed to a Log4J log file (by setting the `spark.cosmos.metrics.slf4j.enabled` property in the spark config to `true`). The `spark.cosmos.metrics.intervalInSeconds` property can be used to determine how frequently metrics should be collected/aggregated (default is once per minute)
- `com.azure.cosmos.spark.plugins.CosmosMetricsApplicationInsightsPlugin`: This plugin can be used to emit the metrics to Azure monitor. You can provide the Azure Monitor connection string in the `spark.cosmos.metrics.azureMonitor.connectionString` property. The `spark.cosmos.metrics.intervalInSeconds` property can be used to determine how frequently metrics should be collected/aggregated (default is once per minute)

Last-but-not-least - to be able to use these Spark plugins the jar containing the plugins needs to be available at Cluster creation - so it is not sufficient to just install the Azure Cosmos DB Spark connector in the `Libraries` section of the cluster, but the jar needs to be copied into the `/databricks/jars` folder at cluster initialization via a start-up script.

Follow these steps for the installation:

- Download the latest Azure Cosmos DB Spark connector jar from Maven
  - Spark 3.1: `https://repo1.maven.org/maven2/com/azure/cosmos/spark/azure-cosmos-spark_3-1_2-12/ReplaceWithTheLatestVersion/azure-cosmos-spark_3-1_2-12-ReplaceWithTheLatestVersion.jar`
  - Spark 3.2: `https://repo1.maven.org/maven2/com/azure/cosmos/spark/azure-cosmos-spark_3-1_2-12/ReplaceWithTheLatestVersion/azure-cosmos-spark_3-2_2-12-ReplaceWithTheLatestVersion.jar` 

- Upload this jar to your Databricks file system (in `/dbfs/FileStore/plugins` folder)
- Create a text file (NOTE: Use Unix Line feeds (LF - not Windows line feeds) with the content below and upload this start-up script as well.
- Configure the new start-up script in the cluster configuration

- Content of the start-up script
  ```sh
  #!/bin/bash
  
  STAGE_DIR="/dbfs/FileStore/plugins"
  
  echo "BEGIN: Upload Spark Plugins"
  cp -f $STAGE_DIR/*.jar /databricks/jars || { echo "Error copying Spark Plugin library file"; exit 1;}
  echo "END: Upload Spark Plugin JARs"
  
  ```

- Now change the clusters's spark configuration to add these two options

  - `spark.plugins ` com.azure.cosmos.spark.plugins.CosmosMetricsSparkPlugin,com.azure.cosmos.spark.plugins.CosmosMetricsApplicationInsightsPlugin
  - `spark.cosmos.metrics.azureMonitor.connectionString  ` **REPLACE_WITH_YOUR_AZURE_MONITOR_CONNECTION_STRING**

- Restart the cluster - and test...




### How to enable in Azure Synapse?

TBD