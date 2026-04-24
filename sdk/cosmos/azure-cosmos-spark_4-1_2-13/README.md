# Azure Cosmos DB OLTP Spark 4 connector

## Azure Cosmos DB OLTP Spark 4 connector for Spark 4.1
**Azure Cosmos DB OLTP Spark connector** provides Apache Spark support for Azure Cosmos DB using
the [SQL API][sql_api_query].
[Azure Cosmos DB][cosmos_introduction] is a globally-distributed database service which allows
developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Cassandra, Graph, and Table.

If you have any feedback or ideas on how to improve your experience please let us know here:
https://github.com/Azure/azure-sdk-for-java/issues/new

### Documentation

- [Getting started](https://aka.ms/azure-cosmos-spark-3-quickstart)
- [Catalog API](https://aka.ms/azure-cosmos-spark-3-catalog-api)
- [Configuration Parameter Reference](https://aka.ms/azure-cosmos-spark-3-config)

### Version Compatibility

#### azure-cosmos-spark_4-1_2-13

| Connector | Supported Spark Versions | Minimum Java Version | Supported Scala Versions  | Supported Databricks Runtimes | Supported Fabric Runtimes |
|-----------|--------------------------|----------------------|---------------------------|-------------------------------|---------------------------|
| 4.48.0-beta.1 | 4.1.0                    | [17, 21]             | 2.13                      | TBD                           | TBD                       |

Note: Spark 4.1 requires Scala 2.13 and Java 17 or higher. When using the Scala API, it is necessary for applications
to use Scala 2.13 that Spark 4.1 was compiled for.

[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/sql-api-introduction
