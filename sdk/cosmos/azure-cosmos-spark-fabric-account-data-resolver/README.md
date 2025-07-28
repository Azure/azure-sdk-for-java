## Azure Cosmos DB OLTP Spark 3 Connector Fabric Account Data Resolver

This project provides an implementation of the `AccountDataResolver` interface for the Azure Cosmos DB Spark Connector, specifically designed to work with Azure Fabric accounts. 
It allows signed-in user to authenticate with Microsoft Entra ID within the Azure Fabric environment. To use this implementation, you have to upload the jar file for this project to your fabric environment. It is also necessary to grant the signed-in user's 
identity with the correct permissions to perform data plane operations. Follow the instructions [here](https://learn.microsoft.com/azure/cosmos-db/nosql/how-to-grant-data-plane-access?tabs=built-in-definition%2Ccsharp&pivots=azure-interface-cli) to grant the relevant permissions. 
To learn more about Microsoft Entra ID (AAD) authentication using the Azure Cosmos DB Spark Connector, see the 
[AAD Auth Documentation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos-spark_3_2-12/docs/AAD-Auth.md). Fabric Native accounts only support gateway mode, so the `spark.cosmos.useGatewayMode` must be set to `true` in the Spark configuration for these accounts.

### Configuration options

| Config Property Name                          | Default                               | Description                                                                                                                                                |
|:----------------------------------------------|:--------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spark.cosmos.accountDataResolverServiceName` | None                                  | Set this value to `com.azure.cosmos.spark.fabric.FabricAccountDataResolver` to use this implementation of the `AccountDataResolver`                        |
| `spark.cosmos.auth.aad.audience`              | `https://cosmos.azure.com/.default`   | Set this value to change the audience used to obtain the Entra Id token.                                                                                   |

### Current Limitations

* There is no support for the [catalog api](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos-spark_3_2-12/docs/catalog-api.md) operations using this implementation of the `AccountDataResolver` with a CosmosDB Fabric Native Account.
* Only Spark 3.5 is supported with this implementation of the `AccountDataResolver`.