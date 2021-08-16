// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.codesnippets;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableItem;
import com.azure.data.tables.models.TableServiceLogging;
import com.azure.data.tables.models.TableServiceMetrics;
import com.azure.data.tables.models.TableServiceProperties;
import com.azure.data.tables.models.TableServiceRetentionPolicy;
import com.azure.data.tables.models.TableServiceStatistics;

import java.time.Duration;

/**
 * This class contains code samples for generating javadocs through doclets for {@link TableServiceClient}.
 */
public class TableServiceClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link TableServiceClient}.
     *
     * @return An instance of {@link TableServiceClient}.
     */
    public TableServiceClient createAsyncClient() {
        // BEGIN: com.azure.data.tables.tableServiceClient.instantiation
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .endpoint("https://myvault.azure.net/")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .buildClient();
        // END: com.azure.data.tables.tableServiceClient.instantiation

        return tableServiceClient;
    }

    /**
     * Generates code samples for using {@link TableServiceClient#createTable(String)} and
     * {@link TableServiceClient#createTableWithResponse(String, Duration, Context)}.
     */
    public void createTable() {
        TableServiceClient tableServiceClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceClient.createTable#String
        TableClient tableClient = tableServiceClient.createTable("myTable");

        System.out.printf("Table with name '%s' was created.", tableClient.getTableName());
        // END: com.azure.data.tables.tableServiceClient.createTable#String

        // BEGIN: com.azure.data.tables.tableServiceClient.createTableWithResponse#String-Duration-Context
        Response<TableClient> response = tableServiceClient.createTableWithResponse("myTable", Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table with name '%s' was created.",
            response.getStatusCode(), response.getValue().getTableName());
        // END: com.azure.data.tables.tableServiceClient.createTableWithResponse#String-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableServiceClient#createTableIfNotExists(String)} and
     * {@link TableServiceClient#createTableIfNotExistsWithResponse(String, Duration, Context)}.
     */
    public void createTableIfNotExists() {
        TableServiceClient tableServiceClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceClient.createTableIfNotExists#String
        TableClient tableClient = tableServiceClient.createTableIfNotExists("myTable");

        System.out.printf("Table with name '%s' was created.", tableClient.getTableName());
        // END: com.azure.data.tables.tableServiceClient.createTableIfNotExists#String

        // BEGIN: com.azure.data.tables.tableServiceClient.createTableIfNotExistsWithResponse#String-Duration-Context
        Response<TableClient> response =
            tableServiceClient.createTableIfNotExistsWithResponse("myTable", Duration.ofSeconds(5),
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table with name '%s' was created.",
            response.getStatusCode(), response.getValue().getTableName());
        // END: com.azure.data.tables.tableServiceClient.createTableIfNotExistsWithResponse#String-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableServiceClient#deleteTable(String)} and
     * {@link TableServiceClient#deleteTableWithResponse(String, Duration, Context)}.
     */
    public void deleteTable() {
        TableServiceClient tableServiceClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceClient.deleteTable#String
        String tableName = "myTable";

        tableServiceClient.deleteTable(tableName);

        System.out.printf("Table with name '%s' was deleted.", tableName);
        // END: com.azure.data.tables.tableServiceClient.deleteTable#String

        // BEGIN: com.azure.data.tables.tableServiceClient.deleteTableWithResponse#String-Duration-Context
        String myTableName = "myTable";

        Response<Void> response = tableServiceClient.deleteTableWithResponse(myTableName, Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Table with name '%s' was deleted.",
            response.getStatusCode(), myTableName);
        // END: com.azure.data.tables.tableServiceClient.deleteTableWithResponse#String-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableServiceClient#listTables()} and
     * {@link TableServiceClient#listTables(ListTablesOptions, Duration, Context)}.
     */
    public void listTables() {
        TableServiceClient tableServiceClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceClient.listTables
        PagedIterable<TableItem> tableItems = tableServiceClient.listTables();

        tableItems.forEach(tableItem ->
            System.out.printf("Retrieved table with name '%s'.%n", tableItem.getName()));
        // END: com.azure.data.tables.tableServiceClient.listTables

        // BEGIN: com.azure.data.tables.tableServiceClient.listTables#ListTablesOptions-Duration-Context
        ListTablesOptions options = new ListTablesOptions().setFilter("TableName eq 'myTable'");

        PagedIterable<TableItem> retrievedTableItems = tableServiceClient.listTables(options, Duration.ofSeconds(5),
            new Context("key1", "value1"));

        retrievedTableItems.forEach(tableItem ->
            System.out.printf("Retrieved table with name '%s'.%n", tableItem.getName()));
        // END: com.azure.data.tables.tableServiceClient.listTables#ListTablesOptions-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableServiceClient#getProperties()}  and
     * {@link TableServiceClient#getPropertiesWithResponse(Duration, Context)}.
     */
    public void getProperties() {
        TableServiceClient tableServiceClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceClient.getProperties
        TableServiceProperties properties = tableServiceClient.getProperties();

        System.out.print("Retrieved service properties successfully.");
        // END: com.azure.data.tables.tableServiceClient.getProperties

        // BEGIN: com.azure.data.tables.tableServiceClient.getPropertiesWithResponse#Duration-Context
        Response<TableServiceProperties> response =
            tableServiceClient.getPropertiesWithResponse(Duration.ofSeconds(5), new Context("key1", "value1"));

        System.out.printf("Retrieved service properties successfully with status code: %d.", response.getStatusCode());
        // END: com.azure.data.tables.tableServiceClient.getPropertiesWithResponse#Duration-Context
    }

    /**
     * Generates code samples for using {@link TableServiceClient#setProperties(TableServiceProperties)}  and
     * {@link TableServiceClient#setPropertiesWithResponse(TableServiceProperties, Duration, Context)}.
     */
    public void setProperties() {
        TableServiceClient tableServiceClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceClient.setProperties#TableServiceProperties
        TableServiceProperties properties = new TableServiceProperties()
            .setHourMetrics(new TableServiceMetrics()
                .setVersion("1.0")
                .setEnabled(true))
            .setLogging(new TableServiceLogging()
                .setAnalyticsVersion("1.0")
                .setReadLogged(true)
                .setRetentionPolicy(new TableServiceRetentionPolicy()
                    .setEnabled(true)
                    .setDaysToRetain(5)));

        tableServiceClient.setProperties(properties);

        System.out.print("Set service properties successfully.");
        // END: com.azure.data.tables.tableServiceClient.setProperties#TableServiceProperties

        // BEGIN: com.azure.data.tables.tableServiceClient.setPropertiesWithResponse#TableServiceProperties-Duration-Context
        TableServiceProperties myProperties = new TableServiceProperties()
            .setHourMetrics(new TableServiceMetrics()
                .setVersion("1.0")
                .setEnabled(true))
            .setLogging(new TableServiceLogging()
                .setAnalyticsVersion("1.0")
                .setReadLogged(true)
                .setRetentionPolicy(new TableServiceRetentionPolicy()
                    .setEnabled(true)
                    .setDaysToRetain(5)));

        Response<Void> response = tableServiceClient.setPropertiesWithResponse(myProperties, Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Retrieved service properties successfully with status code: %d.", response.getStatusCode());
        // END: com.azure.data.tables.tableServiceClient.setPropertiesWithResponse#TableServiceProperties-Duration-Context
    }

    /**
     * Generates code samples for using {@link TableServiceClient#getStatistics()}  and
     * {@link TableServiceClient#getStatisticsWithResponse(Duration, Context)}.
     */
    public void getStatistics() {
        TableServiceClient tableServiceClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceClient.getStatistics
        TableServiceStatistics statistics = tableServiceClient.getStatistics();

        System.out.print("Retrieved service statistics successfully.");
        // END: com.azure.data.tables.tableServiceClient.getStatistics

        // BEGIN: com.azure.data.tables.tableServiceClient.getStatisticsWithResponse#Duration-Context
        Response<TableServiceStatistics> response = tableServiceClient.getStatisticsWithResponse(Duration.ofSeconds(5),
            new Context("key1", "value1"));

        System.out.printf("Retrieved service statistics successfully with status code: %d.",
            response.getStatusCode());
        // END: com.azure.data.tables.tableServiceClient.getStatisticsWithResponse#Duration-Context
    }
}
