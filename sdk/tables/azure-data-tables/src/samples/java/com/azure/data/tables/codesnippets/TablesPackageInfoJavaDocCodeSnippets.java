// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.codesnippets;

import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.TableServiceAsyncClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.TableEntity;

public class TablesPackageInfoJavaDocCodeSnippets {
    /**
     * Generates code sample for creating a {@link TableServiceClient}.
     * @return TablesServiceClient object.
     */
    public TableServiceClient createTableServiceClient() {
        // BEGIN: com.azure.data.tables.TableServiceClient.instantiation.package
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString("connectionstring")
            .buildClient();
        // END: com.azure.data.tables.TableServiceClient.instantiation.package
        return tableServiceClient;
    }

    /**
     * Generates code sample for creating a {@link TableServiceAsyncClient}.
     * @return TablesServiceAsyncClient object.
     */
    public TableServiceAsyncClient createTableServiceAsyncClient() {
        // BEGIN: com.azure.data.tables.TableServiceAsyncClient.instantiation.package
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString("connectionstring")
            .buildAsyncClient();
        // END: com.azure.data.tables.TableServiceAsyncClient.instantiation.package
        return tableServiceAsyncClient; 
    }

    /**
     * Generates code sample for creating a {@link TableClient} with {@link TableServiceClient}.
     * @return TablesClient object.
     */
    public TableClient createTableClientFromServiceClient() {
        TableServiceClient tableServiceClient = createTableServiceClient();
        // BEGIN: com.azure.data.tables.TableClient.instantiationFromServiceClient.package
        TableClient tableClient = tableServiceClient.getTableClient("tableName");
        // END: com.azure.data.tables.TableClient.instantiationFromServiceClient.package
        return tableClient;
    } 

    /**
     * Generates code sample for creating a {@link TableClient} with {@link TableClientBuilder}.
     * @return TableClient object.
     */
    public TableClient createTableClientFromBuilder() {
        // BEGIN: com.azure.data.tables.TableClient.instantiationFromBuilder.package
        TableClient tableClient = new TableClientBuilder()
            .connectionString("connectionstring")
            .tableName("tableName")
            .buildClient();
        // END: com.azure.data.tables.TableClient.instantiationFromBuilder.package
        return tableClient;
    }

    /**
     * Generates code sample for creating a {@link TableAsyncClient} with {@link TableClientBuilder}.
     * @return TableAsyncClient object.
     */
    public TableAsyncClient createTableAsyncClientFromBuilder() {
        // BEGIN: com.azure.data.tables.TableClient.instantiationFromBuilder.package#async
        TableAsyncClient tableClient = new TableClientBuilder()
            .connectionString("connectionstring")
            .tableName("tableName")
            .buildAsyncClient();
        // END: com.azure.data.tables.TableClient.instantiationFromBuilder.package#async
        return tableClient;
    }

    /**
     * Generates code sample for creating a {@link TableAsyncClient} with {@link TableServiceAsyncClient}.
     * @return TableAsyncClient object.
     */
    public TableAsyncClient createTableAsyncClientFromServiceAsyncClient() {
        TableServiceAsyncClient tableServiceAsyncClient = createTableServiceAsyncClient();
        // BEGIN: com.azure.data.tables.TableAsyncClient.instantiationFromServiceAsyncClient.package
        TableAsyncClient tableAsyncClient = tableServiceAsyncClient.getTableClient("tableName");
        // END: com.azure.data.tables.TableAsyncClient.instantiationFromServiceAsyncClient.package
        return tableAsyncClient;
    }

    //create a table using TableServiceClient
    public void createTable() {
        TableServiceClient tableServiceClient = createTableServiceClient();
        // BEGIN: com.azure.data.tables.TableServiceClient.createTable.package#String
        tableServiceClient.createTable("tableName");
        // END: com.azure.data.tables.TableServiceClient.createTable.package#String
    }

    //list tables using TableServiceClient
    public void listTables() {
        TableServiceClient tableServiceClient = createTableServiceClient();
        // BEGIN: com.azure.data.tables.TableServiceClient.listTables.package
        tableServiceClient.listTables().forEach(table -> {
            String tableName = table.getName();
            System.out.println("Table name: " + tableName);
        });
        // END: com.azure.data.tables.TableServiceClient.listTables.package
    }
    
    //delete a table using TableServiceClient
    public void deleteTable() {
        TableServiceClient tableServiceClient = createTableServiceClient();
        // BEGIN: com.azure.data.tables.TableServiceClient.deleteTable.package#String
        tableServiceClient.deleteTable("tableName");
        // END: com.azure.data.tables.TableServiceClient.deleteTable.package#String
    }

    //Return a TableClient from TableServiceClient
    public TableClient getTableClient() {
        TableServiceClient tableServiceClient = createTableServiceClient();
        // BEGIN: com.azure.data.tables.TableServiceClient.getTableClient.package#String
        TableClient tableClient = tableServiceClient.getTableClient("tableName");
        // END: com.azure.data.tables.TableServiceClient.getTableClient.package#String
        return tableClient;
    }

    //Create an Entity using TableClient
    public void createEntity() {
        TableClient tableClient = createTableClientFromBuilder();
        // BEGIN: com.azure.data.tables.TableClient.createEntity.package#Map
        tableClient.createEntity(new TableEntity("partitionKey", "rowKey")
            .addProperty("property", "value"));
        // END: com.azure.data.tables.TableClient.createEntity.package#Map
    }
    
    //Retrieve and update an entity using TableClient
    public void updateEntity() {
        TableClient tableClient = createTableClientFromBuilder();
        // BEGIN: com.azure.data.tables.TableClient.updateEntity.package#TableEntity
        TableEntity entity = tableClient.getEntity("partitionKey", "rowKey");
        entity.addProperty("newProperty", "newValue");
        tableClient.updateEntity(entity);
        // END: com.azure.data.tables.TableClient.updateEntity.package#TableEntity
    }

    //List entities using TableClient
    public void listEntities() {
        TableClient tableClient = createTableClientFromBuilder();
        // BEGIN: com.azure.data.tables.TableClient.listEntities.package
        tableClient.listEntities().forEach(entity -> {
            String partitionKey = entity.getPartitionKey();
            String rowKey = entity.getRowKey();
            System.out.println("Partition key: " + partitionKey + ", Row key: " + rowKey);
        });
        // END: com.azure.data.tables.TableClient.listEntities.package
    }

    //Delete an entity using TableClient
    public void deleteEntity() {
        TableClient tableClient = createTableClientFromBuilder();
        // BEGIN: com.azure.data.tables.TableClient.deleteEntity.package#TableEntity
        tableClient.deleteEntity("partitionKey", "rowKey");
        // END: com.azure.data.tables.TableClient.deleteEntity.package#TableEntity
    }
}
