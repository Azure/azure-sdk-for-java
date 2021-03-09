# Azure Spring Boot App Multi Cosmos Database Sample for Java

## Key concepts
## Getting started

### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Configure Cosmos Database
1. Log into <https://portal.azure.com>.

1. Click `Create a resource`.

1. Input `Azure Cosmos DB`.

1. Click `Azure Cosmos DB`
    ![Find Cosmos Resource 01](resource/creating-cosmos-01.png)

    ![Find Cosmos Resource 02](resource/creating-cosmos-02.png)

1. Click **Create**.

    ![Create new Cosmos](resource/creating-cosmos-03.png)

1. On the **Create key vault** page, input `Subscription`, `Resource group`, `Account Name`, then click `Review + Create`.

    ![Specify the options](resource/specify-the-options.png)

    ![Create Cosmos resource](resource/create-cosmos-resource.png)

1. When complete, click `Go to resource`.

    ![Go to resource](resource/go-to-resource.png)

1. Click **Keys** in the left navigation pane, copy your **URI**, the **PRIMARY KEY** and **SECONDARY KEY**;

    ![Get Connect Info](resource/get-connect-info.png)

## Key concepts
## Examples
### Configure application.yml
```yaml
azure.cosmos.uri=your-cosmosDb-uri
azure.cosmos.key=your-cosmosDb-key
azure.cosmos.secondaryKey=your-cosmosDb-secondary-key
azure.cosmos.database=your-cosmosDb-dbName
azure.cosmos.populateQueryMetrics=if-populate-query-metrics
```

### Run with Maven
```shell
cd azure-spring-boot-samples/azure-spring-boot-sample-multi-database
mvn spring-boot:run
```

Verify Result:
The corresponding data is added to cosmos database
    ![Result in Primary Cosmos Database](resource/result-in-primary-cosmos-database.png)
    ![Result in Secondary Cosmos Database](resource/result-in-secondary-cosmos-database.png)
    
## Troubleshooting
## Next steps
## Contributing

<!-- LINKS -->
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
