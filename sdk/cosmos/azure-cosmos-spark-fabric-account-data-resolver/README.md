## Azure Cosmos DB OLTP Spark 3 Connector Fabric Account Data Resolver

This project provides an implementation of the `AccountDataResolver` interface for the Azure Cosmos DB Spark Connector, specifically designed to work with Azure Fabric accounts. 
It allows signed-in user to authenticate with Microsoft Entra ID within the Azure Fabric environment. To use this implementation, you have to upload the jar file for this project to your fabric environment. It is also necessary to grant the signed-in user's 
identity with the correct permissions to perform data plane operations. Follow the instructions [here](https://learn.microsoft.com/azure/cosmos-db/nosql/how-to-grant-data-plane-access?tabs=built-in-definition%2Ccsharp&pivots=azure-interface-cli) to grant the relevant permissions. 
To learn more about Microsoft Entra ID (AAD) authentication using the Azure Cosmos DB Spark Connector, see the 
[AAD Auth Documentation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos-spark_3_2-12/docs/AAD-Auth.md).

### Configuration options

| Config Property Name                          | Default                               | Description                                                                                                                                                |
|:----------------------------------------------|:--------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spark.cosmos.auth.type`                      | `MasterKey`                           | Set this value to `AccessToken` to enable AAD / Microsoft Entra ID authentication via access tokens from your custom `AccountDataResolver` implementation. |
| `spark.cosmos.accountDataResolverServiceName` | None                                  | Set this value to `com.azure.cosmos.spark.fabric.FabricAccountDataResolver` to use this implementation of the `AccountDataResolver`                        |
| `spark.cosmos.auth.fabric.enabled`            | `false`                               | Set this value to `true`.                                                                                                                                  |
| `spark.cosmos.auth.fabric.audience`           | `https://cosmos.azure.com/.default`   | Set this value to change the audience used to obtain the Entra Id token.                                                                                   |
