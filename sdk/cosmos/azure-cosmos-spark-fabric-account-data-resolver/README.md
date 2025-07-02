## Azure Cosmos DB OLTP Spark 3 Connector Fabric Account Data Resolver

This project provides an implementation of the `AccountDataResolver` interface for the Azure Cosmos DB Spark Connector, specifically designed to work with Azure Fabric accounts. 
It allows signed in user to authenticate with EntraId within the Azure Fabric environment. To use this implementation, you have to upload the jar file for this project to your fabric environment.  

### Configuration options

| Config Property Name                          | Default                               | Description                                                                                                                                                |
|:----------------------------------------------|:--------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spark.cosmos.auth.type`                      | `MasterKey`                           | Set this value to `AccessToken` to enable AAD / Microsoft Entra ID authentication via access tokens from your custom `AccountDataResolver` implementation. |
| `spark.cosmos.accountDataResolverServiceName` | None                                  | Set this value to `com.azure.cosmos.spark.fabric.FabricAccountDataResolver` to use this implementation of the `AccountDataResolver`                        |
| `spark.cosmos.auth.fabric.enabled`            | `false`                               | Set this value to `true`.                                                                                                                                  |
| `cosmos.auth.fabric.audience`                 | `https://cosmos.azure.com/.default`   | The audience used to obtain the Entra Id token.                                                                                                            |
