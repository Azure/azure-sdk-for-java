---
page_type: sample
languages:
- java
products:
- azure-cosmos-db
description: "Azure Spring Boot Sample project for Cosmos Multi Database Multi Account"
urlFragment: "azure-spring-boot-sample-cosmos-multi-database-multi-account"
---

# Azure Spring Boot Sample Cosmos Multi Database Multi Account for Java

## Key concepts
## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

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

1. Replace the content in `application.properties` with the obtained information.

1. We need to create another cosmos DB as the secondary like the steps above.

1. Add MYSQL connection attributes in `application.properties`.

## Key concepts
## Examples
### Configure application.yml
```yaml
# primary account cosmos config
azure.cosmos.primary.uri=your-primary-cosmosDb-uri
azure.cosmos.primary.key=your-primary-cosmosDb-key
azure.cosmos.primary.secondary-key=your-primary-cosmosDb-secondary-key
azure.cosmos.primary.database=your-primary-cosmosDb-dbName
azure.cosmos.primary.populate-query-metrics=if-populate-query-metrics

# secondary account cosmos config
azure.cosmos.secondary.uri=your-secondary-cosmosDb-uri
azure.cosmos.secondary.key=your-secondary-cosmosDb-key
azure.cosmos.secondary.secondary-key=your-secondary-cosmosDb-secondary-key
azure.cosmos.secondary.database=your-secondary-cosmosDb-dbName
azure.cosmos.secondary.populate-query-metrics=if-populate-query-metrics

#mysql connection attributes
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/db_example
spring.datasource.username=your-mysql-username
spring.datasource.password=your-mysql-password
```

### Run with Maven
```shell
cd azure-spring-boot-samples/azure-spring-boot-sample-multi-database
mvn spring-boot:run
```

Verify Result:
The corresponding data is added to the mysql database and cosmos database
    ![Result in MYSQL](resource/result-in-mysql.png)
    ![Result in Primary Cosmos Database](resource/result-in-primary-cosmos-database.png)
    ![Result in Secondary Cosmos Database](resource/result-in-secondary-cosmos-database.png)
    
## Troubleshooting
## Next steps
## Contributing

<!-- LINKS -->
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
