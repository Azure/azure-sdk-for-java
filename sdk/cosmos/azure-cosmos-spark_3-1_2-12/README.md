# Azure Cosmos DB OLTP Spark 3 connector

## Azure Cosmos DB OLTP Spark 3 connector for Spark 3.1
**Azure Databricks**, **Azure Synapse** and **Azure HDInsights** don't provide any supported runtime for Spark 3.1 anymore. So, we have stopped releasing new versions for the **Azure Cosmos DB OLTP Spark connector** targeting Spark 3.1 - please move to Spark 3.5 instead.

The last published **Azure Cosmos DB OLTP Spark connector** for Spark 3.1 is version **4.37.1**

### Download

You can use the maven coordinate of the jar to auto install the Spark Connector to your Databricks Runtime 14 from Maven:
`com.azure.cosmos.spark:azure-cosmos-spark_3-5_2-12:4.37.1`

You can also integrate against Cosmos DB Spark Connector in your SBT project:
```scala
libraryDependencies += "com.azure.cosmos.spark" % "azure-cosmos-spark_3-5_2-12" % "4.37.1"
```

Cosmos DB Spark Connector is available on [Maven Central Repo](https://central.sonatype.com/search?namespace=com.azure.cosmos.spark).
