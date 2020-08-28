# Azure Cosmos DB Spring Boot 2.2 Starter client library for Java

[Azure Cosmos DB](https://azure.microsoft.com/services/cosmos-db/) is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Graph, and Azure Table storage. 

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](http://maven.apache.org/) 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.microsoft.azure:azure-cosmosdb-spring-boot-2-2-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-cosmosdb-spring-boot-2-2-starter</artifactId>
    <version>2.4.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
Please refer to [Azure Cosmos DB Spring Boot 2.3 Starter][azure_spring_boot_2_3_starter_cosmosdb_readme_key_concepts] for key concepts.

## Examples
Please refer to [Azure Cosmos DB Spring Boot 2.3 Starter][azure_spring_boot_2_3_starter_cosmosdb_readme_examples] for examples.

## Troubleshooting
Please refer to [Azure Cosmos DB Spring Boot 2.3 Starter][azure_spring_boot_2_3_starter_cosmosdb_readme_troubleshooting] for troubleshooting.

## Next steps

Besides using this Azure CosmosDb Spring Boot Starter, you can directly use Spring Data for Azure CosmosDb package for more complex scenarios. Please refer to [Spring Data for Azure CosmosDB](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-cosmos-core) for more details.

The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Cosmos DB SQL API][cosmos_db_sql_api]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_readme] to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-cosmos-db
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#azure-cosmosdb-spring-boot-starter
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-cosmosdb-spring-boot-starter
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[azure_spring_boot_2_3_starter_cosmosdb_readme_key_concepts]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-2-3-starter-cosmosdb#key-concepts
[azure_spring_boot_2_3_starter_cosmosdb_readme_examples]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-2-3-starter-cosmosdb#examples
[azure_spring_boot_2_3_starter_cosmosdb_readme_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-2-3-starter-cosmosdb#troubleshooting
[cosmos_db_sql_api]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb
[contributing_readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md
