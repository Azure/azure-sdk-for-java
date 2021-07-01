# Azure CosmosDB Client library for Java

[![Build Status](https://dev.azure.com/azure-sdk/public/_apis/build/status/17?branchName=master)](https://dev.azure.com/azure-sdk/public/_build/latest?definitionId=17) [![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html) [![Dependencies](https://img.shields.io/badge/dependencies-analyzed-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/dependencies.html) [![SpotBugs](https://img.shields.io/badge/SpotBugs-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/spotbugsXml.html) [![CheckStyle](https://img.shields.io/badge/CheckStyle-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/checkstyle-aggregate.html)

Azure Cosmos DB is Microsoft’s globally distributed, multi-model database service for operational and analytics workloads. It offers multi-mastering feature by automatically scaling throughput, compute, and storage.
This project provides SDK library in Java for interacting with [SQL API][sql_api_query] of [Azure Cosmos DB Database Service][cosmos_introduction].

## Getting started

To get started with a specific library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk/cosmos/` directory.
- [Azure Cosmos](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos/README.md) is Microsoft's Azure Cosmos DB Java SDK which provides client-side logical representation to access the Azure Cosmos DB SQL API. The SDK provides Reactor Core based async APIs.
- [Azure Spring Data Cosmos](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-spring-data-cosmos/README.md) provides Spring Data support for Azure Cosmos DB using the SQL API, based on Spring Data framework.
- [Azure Cosmos Encryption](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-encryption/README.md) supports encryption for Azure Cosmos DB using SQL API. This plugin library is still under development and not is not ready to be consumed yet. 
- [Azure Cosmos Benchmark](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-benchmark/README.md) is the benchmarking tool which provides different kinds of benchmarking workloads including but not limited to `readLatency`, `readThroughput`, `writeThroughput`, `readMyWrites`, etc.
- [Azure Cosmos DotNet Benchmark](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-dotnet-benchmark/README.md) is the port of CosmosDB .NET benchmarking tool. 
- [Azure Cosmos Examples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-examples/README.md) provides various examples / samples on how to use Azure Cosmos DB SDK for SQL API.

<!-- LINKS -->
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2Fazure-cosmos%2FREADME.png)
