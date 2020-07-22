# How to use azure-spring-data-gremlin

## Overview

This article demonstrates how to use `azure-spring-data-gremlin` to store / retrieve data in `Azure Cosmos DB`.


## Prerequisites

The following prerequisites are required in order to follow the steps in this article:

* An Azure subscription. If you don't know what is `Azure subscription`, please read [this document](https://docs.microsoft.com/en-us/office365/enterprise/subscriptions-licenses-accounts-and-tenants-for-microsoft-cloud-offerings).
* A supported Java Development Kit (JDK). For more information about the JDKs available for use when developing on Azure, see <https://aka.ms/azure-jdks>.
* [Apache Maven](http://maven.apache.org/), version 3.0 or later.


## Create Resource

### Create Azure Cosmos DB

1. Browse to the Azure portal at <https://portal.azure.com/> and click `+Create a resource`.

   ![create-a-resource](readme-images/create-a-resource.png)

1. Click `Databases`, and then click `Azure Cosmos DB`.

   ![create-azure-cosmos-db](readme-images/create-azure-cosmos-db.png)

1. On the `Azure Cosmos DB` page, enter the following information:

   * Choose the `Subscription` that you want to use for your database.
   * Specify whether to create a new `Resource Group` for your database, or choose an existing resource group.
   * Enter a unique `Account Name` to use as part of the Gremlin URI for your database. For example: if you entered `account-sample` for the `Account Name`, the Gremlin URI would be `account-samplewingtiptoysdata.gremlin.cosmosdb.azure.com`.
   * Choose `Gremlin (Graph)` for the API.
   * Specify the `Location` for your database.
   
When you have specified these options, click `Review + create`.

   ![create-azure-cosmos-db-account](readme-images/create-azure-cosmos-db-account.png)

Review the specification and click `Create` to create your database.

### Add a graph to your Azure Cosmos Database

1. In CosmosDb page, Click `Data Explorer`, and then click `New Graph`.

   ![new-graph](readme-images/new-graph.png)

1. When the `Add Graph` is displayed, enter the following information:

   * Specify a unique `Database id` for your database.
   * You can choose to specify your `Storage capacity`, or you can accept the default.
   * Specify a unique `Graph id` for your graph.
   * Specify a `Partition key`. For more information see [Using a partitioned graph in Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/graph-partitioning).
Click `OK`.
   
   When you have specified these options, click `OK` to create your graph.

   ![add-graph](readme-images/add-graph.png)

1. After your graph has been created, you can use the `Data Explorer` to view it.

   ![graph-detail](readme-images/graph-detail.png)
   
   

## Create simple Spring Boot application with the Spring Initializr

1. Browse to <https://start.spring.io/>.

1. Fill project metadata then click `GENERATE`:

   ![spring-initializr](readme-images/spring-initializr.png)

1. Unzip the file then import to your IDE.

## Update code according to this sample project

1. Add dependency of `azure-spring-data-gremlin`, just like the repo does. Choose the correct version.

1. Delete all contents in `src/test/`

1. Add all java files in `src/main/java`, just like this sample does.

1. Update config in `src/main/resorces/application.properties`, where:

   | Field              | Description                                                                                                                                                                                                             |
   |--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
   | `endpoint`         | Specifies the Gremlin URI for your database, which is derived from the unique **ID** that you specified when you created your Azure Cosmos DB earlier in this tutorial.                                                 |
   | `port`             | Specifies the TCP/IP port, which should be **443** for HTTPS.                                                                                                                                                           |
   | `username`         | Specifies the unique **Database id** and **Graph id** that you used when you added your graph earlier in this tutorial; this must be entered using the following syntax: "/dbs/**{Database id}**/colls/**{Graph id}**". |
   | `password`         | Specifies either the primary or secondary **Access key** that you copied earlier in this tutorial.                                                                                                                      |
   | `sslEnabled`       | Specifies whether enable ssl.                                                                                                                                                                                           |
   | `telemetryAllowed` | Specify **true** if you want to enable telemetry; otherwise, **false**.
   | `maxContentLength` | Specifies max content length.                                                                                                                                                                                           |

1. About how to get password:

   ![get-password](readme-images/get-password.png)

## Build and run the project

1. Build your Spring Boot application with Maven and run it; for example:

   ```shell
   mvn clean package
   mvn spring-boot:run
   ```

1. If your app start successfully, you can check the graph in Azure portal:

   ![execute-result](readme-images/execute-result.png)
