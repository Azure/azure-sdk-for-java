# Azure Spring Data 2.2 Cosmos client library for Java

**Azure Spring Data Cosmos** provides Spring Data 2.2.x support for Azure Cosmos DB using the [SQL API][sql_api_query], based on Spring Data framework.
[Azure Cosmos DB][cosmos_introduction] is a globally-distributed database service which allows developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Cassandra, Graph, and Table.

[Source code][source_code] | [Package (Maven)][azure_spring_data_2_2_cosmos_maven] | [API reference documentation][api_documentation] | [Product documentation][azure_spring_data_cosmos_docs] |
[Samples][samples]

## Spring data version support
This project supports [spring-data-commons 2.2.x][spring_data_2_2_commons] version.

## Getting started
### Include the package
If you are using Maven, add the following dependency.

[//]: # ({x-version-update-start;com.azure:azure-spring-data-2-2-cosmos;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-spring-data-2-2-cosmos</artifactId>
    <version>3.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
Please refer to [Azure Spring Data Cosmos Core module][azure_spring_data_cosmos_core_readme_key_concepts] for key concepts.

## Troubleshooting
Please refer to [Azure Spring Data Cosmos Core module][azure_spring_data_cosmos_core_readme_troubleshooting] for troubleshooting guide.

## Examples
- Please refer to [sample project here][samples].

## Next steps
- Read more about [azure spring data cosmos][azure_spring_data_cosmos_docs].
- Read more about [azure cosmosdb service][cosmos_docs].

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
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-cosmos-core
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/java-supported-jdk-runtime?view=azure-java-stable
[maven]: https://maven.apache.org/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-cosmos-core/src/samples/java/com/azure/cosmos
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[spring_data_2_2_commons]: https://mvnrepository.com/artifact/org.springframework.data/spring-data-commons/2.2.0.RELEASE
[api_documentation]: https://azure.github.io/azure-sdk-for-java/cosmos.html#azure-spring-data-2-2-cosmos
[maven]: https://maven.apache.org/
[azure_spring_data_cosmos_core_readme_key_concepts]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-spring-data-cosmos-core/README.md#key-concepts
[azure_spring_data_cosmos_core_readme_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-spring-data-cosmos-core/README.md#troubleshooting
[azure_spring_data_cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sdk-java-spring-v3
[azure_spring_data_2_2_cosmos_maven]: https://search.maven.org/artifact/com.azure/azure-spring-data-2-2-cosmos
[azure_spring_data_2_2_cosmos_maven_svg]: https://img.shields.io/maven-central/v/com.azure/azure-spring-data-2-2-cosmos.svg

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2F%2Fazure-spring-data-2-2-cosmos%2FREADME.png)
