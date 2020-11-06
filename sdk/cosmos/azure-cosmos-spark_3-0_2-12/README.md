# Azure Cosmos DB OLTP Spark connector library for Java

**Azure Cosmos DB OLTP Spark connector ** provides Apache Spark support for Azure Cosmos DB using 
the [SQL API][sql_api_query].
[Azure Cosmos DB][cosmos_introduction] is a globally-distributed database service which allows 
developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Cassandra, Graph, and Table.

## Getting started
TBD

### Prerequisites

- Java Development Kit 11
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. 
Alternatively, you can use the [Azure Cosmos DB Emulator][local_emulator] for development and testing. 
As emulator https certificate is self signed, you need to import its certificate to java trusted cert store, 
[explained here][local_emulator_export_ssl_certificates]
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](https://www.slf4j.org/manual.html) is used to associate a 
specific logging framework with SLF4J.
- (Optional) Maven

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will 
link the SLF4J API with the logging implementation of your choice. See 
the [SLF4J user manual](https://www.slf4j.org/manual.html) for more information.

## Key concepts

TBD

### Azure Cosmos DB Partition
- Azure-spring-data-cosmos supports [Azure Cosmos DB partition][azure_cosmos_db_partition].
- To specify a field of domain class to be partition key field, just annotate it with `@PartitionKey`.
- When you perform CRUD operation, specify your partition value.
- For more sample on partition CRUD, please refer [test here][address_repository_it_test]

## Beta version package

Beta version built from `master` branch are available, you can refer to
 the [instruction](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md#nightly-package-builds)
to use beta version packages.

## Troubleshooting

### General

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues/new).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

### Enable Client Logging
TBD

## Examples
TBD

## Next steps
TBD

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
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-cosmos/src/samples/java/com/azure/spring/data/cosmos
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[local_emulator]: https://docs.microsoft.com/azure/cosmos-db/local-emulator
[local_emulator_export_ssl_certificates]: https://docs.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates
[azure_cosmos_db_partition]: https://docs.microsoft.com/azure/cosmos-db/partition-data
[sql_queries_in_cosmos]: https://docs.microsoft.com/azure/cosmos-db/tutorial-query-sql-api
[sql_queries_getting_started]: https://docs.microsoft.com/azure/cosmos-db/sql-query-getting-started