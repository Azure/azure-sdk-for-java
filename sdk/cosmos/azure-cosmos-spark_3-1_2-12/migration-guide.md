# Azure Cosmos DB OLTP Spark 3 connector

## Azure Cosmos DB OLTP Spark 3.1 connector - Migration to Spark 3.5
There are no application-level changes required to migrate from Spark 3.1 or 3.2 to Spark 3.5 from the perspective of the Azure Cosmos DB Spark connector. Information on how to install the latest Connector version for the target Spark runtime can be found in the [Quick Start Guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark_3_2-12/docs/quick-start.md)

## Spark runtime
**Azure Databricks**, **Azure Synapse** and **Azure HDInsights** don't provide any supported runtime for Spark 3.1 anymore. To migrate to a supported Spark runtime, please follow the guidance below.

### Azure Databricks
- [Migration guide](https://learn.microsoft.com/azure/databricks/release-notes/runtime/#apache-spark-migration-guidance)

### Azure Synapse
- [Migration Guide](https://learn.microsoft.com/azure/synapse-analytics/spark/apache-spark-version-support#migration-between-apache-spark-versions---support)

### Azure HDInsight
- [Migration Guide](https://learn.microsoft.com/azure/hdinsight/hdinsight-upgrade-cluster)


