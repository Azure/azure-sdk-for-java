# Azure Cosmos DB OLTP Spark 3 connector

**Azure Cosmos DB OLTP Spark connector** provides Apache Spark support for Azure Cosmos DB using 
the [SQL API][sql_api_query].
[Azure Cosmos DB][cosmos_introduction] is a globally-distributed database service which allows 
developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Cassandra, Graph, and Table.

If you have any feedback or ideas on how to improve your experience please let us know here:
https://github.com/Azure/azure-sdk-for-java/issues/new

## Documentation

- [Getting started](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/quick-start.md)
- [Catalog API](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/catalog-api.md)
- [Configuration Parameter Reference](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark_3-1_2-12/docs/configuration-reference.md)

[//]: # (//TODO: moderakh add more sections)
[//]: # (//TODO: moderakh Enable Client Logging)
[//]: # (//TODO: moderakh Examples)
[//]: # (//TODO: moderakh Next steps)
[//]: # (//TODO: moderakh Key concepts)
[//]: # (//TODO: moderakh Azure Cosmos DB Partition)
[//]: # (//TODO: moderakh Troubleshooting)

## Version Compatibility

| Connector     | Minimum Spark Version | Minimum Java Version | Supported Scala Versions | Supported Databricks Runtimes |
| ------------- | --------------------- | -------------------- | -----------------------  | ----------------------------- |
| 4.3.1         | 3.1.1                 | 8                    | 2.12                     | 8.\*, 9.\*                    |
| 4.3.0         | 3.1.1                 | 8                    | 2.12                     | 8.\*, 9.\*                    |
| 4.2.0         | 3.1.1                 | 8                    | 2.12                     | 8.\*                          |
| 4.1.0         | 3.1.1                 | 8                    | 2.12                     | 8.\*                          |
| 4.0.0         | 3.1.1                 | 8                    | 2.12                     | 8.\*                          |
| 4.0.0-beta.3  | 3.1.1                 | 8                    | 2.12                     | 8.\*                          |
| 4.0.0-beta.2  | 3.1.1                 | 8                    | 2.12                     | 8.\*                          |
| 4.0.0-beta.1  | 3.1.1                 | 8                    | 2.12                     | 8.\*                          |

## Download

You can use the maven coordinate of the jar to auto install the Spark Connector to your Databricks Runtime 8 from Maven:
`com.azure.cosmos.spark:azure-cosmos-spark_3-1_2-12:4.3.1`

You can also integrate against Cosmos DB Spark Connector in your SBT project:
```scala
libraryDependencies += "com.azure.cosmos.spark" % "azure-cosmos-spark_3-1_2-12" % "4.3.1"
```

Cosmos DB Spark Connector is available on [Maven Central Repo](https://search.maven.org/search?q=g:com.azure.cosmos.spark).

### General

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues/new).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

## License
This project is under MIT license and uses and repackages other third party libraries as an uber jar.
See [NOTICE.txt](https://github.com/Azure/azure-sdk-for-java/blob/main/NOTICE.txt).

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source_code]: src
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-spring-data-cosmos/src/samples/java/com/azure/spring/data/cosmos
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[local_emulator]: https://docs.microsoft.com/azure/cosmos-db/local-emulator
[local_emulator_export_ssl_certificates]: https://docs.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates
[azure_cosmos_db_partition]: https://docs.microsoft.com/azure/cosmos-db/partition-data
[sql_queries_in_cosmos]: https://docs.microsoft.com/azure/cosmos-db/tutorial-query-sql-api
[sql_queries_getting_started]: https://docs.microsoft.com/azure/cosmos-db/sql-query-getting-started


