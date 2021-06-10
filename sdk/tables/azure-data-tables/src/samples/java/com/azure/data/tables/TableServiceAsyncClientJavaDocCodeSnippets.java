// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.ListTablesOptions;
import com.azure.data.tables.models.TableAccessPolicies;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableServiceCorsRule;
import com.azure.data.tables.models.TableServiceLogging;
import com.azure.data.tables.models.TableServiceMetrics;
import com.azure.data.tables.models.TableServiceProperties;
import com.azure.data.tables.models.TableServiceRetentionPolicy;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link TableServiceAsyncClient}.
 */
public class TableServiceAsyncClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link TableServiceAsyncClient}.
     *
     * @return An instance of {@link TableServiceAsyncClient}.
     */
    public TableServiceAsyncClient createAsyncClient() {
        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.instantiation
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .endpoint("https://myvault.azure.net/")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .buildAsyncClient();
        // END: com.azure.data.tables.tableServiceAsyncClient.instantiation

        return tableServiceAsyncClient;
    }

    /**
     * Generates a code sample for creating a {@link TableServiceAsyncClient} while providing an {@link HttpClient}.
     *
     * @return An instance of {@link TableServiceAsyncClient}.
     */
    public TableServiceAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.withHttpClient.instantiation
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .endpoint("https://myaccount.core.windows.net/")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.data.tables.tableServiceAsyncClient.withHttpClient.instantiation

        return tableServiceAsyncClient;
    }

    /**
     * Generates a code sample for creating a {@link TableServiceAsyncClient} while providing an {@link HttpPipeline}.
     *
     * @return An instance of {@link TableServiceAsyncClient}.
     */
    public TableServiceAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.withPipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new TableAzureNamedKeyCredentialPolicy(new AzureNamedKeyCredential("name", "key")),
                new RetryPolicy())
            .build();

        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .endpoint("https://myaccount.core.windows.net/")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .pipeline(pipeline)
            .buildAsyncClient();
        // END: com.azure.data.tables.tableServiceAsyncClient.withPipeline.instantiation

        return tableServiceAsyncClient;
    }

    /**
     * Generates code samples for using {@link TableServiceAsyncClient#createTable(String)} and
     * {@link TableServiceAsyncClient#createTableWithResponse(String)}.
     */
    public void createTable() {
        TableServiceAsyncClient tableServiceAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.createTable#String
        tableServiceAsyncClient.createTable("myTable")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(tableAsyncClient ->
                System.out.printf("Table with name '%s' was created.", tableAsyncClient.getTableName()));
        // END: com.azure.data.tables.tableServiceAsyncClient.createTable#String

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.createTableWithResponse#String
        tableServiceAsyncClient.createTableWithResponse("myTable")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table with name '%s' was created.",
                    response.getStatusCode(), response.getValue().getTableName()));
        // END: com.azure.data.tables.tableServiceAsyncClient.createTableWithResponse#String
    }

    /**
     * Generates code samples for using {@link TableServiceAsyncClient#createTableIfNotExists(String)} and
     * {@link TableServiceAsyncClient#createTableIfNotExistsWithResponse(String)}.
     */
    public void createTableIfNotExists() {
        TableServiceAsyncClient tableServiceAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExists#String
        tableServiceAsyncClient.createTableIfNotExists("myTable")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(tableAsyncClient ->
                System.out.printf("Table with name '%s' was created.", tableAsyncClient.getTableName()));
        // END: com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExists#String

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExistsWithResponse#String
        tableServiceAsyncClient.createTableIfNotExistsWithResponse("myTable")
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table with name '%s' was created.",
                    response.getStatusCode(), response.getValue().getTableName()));
        // END: com.azure.data.tables.tableServiceAsyncClient.createTableIfNotExistsWithResponse#String
    }

    /**
     * Generates code samples for using {@link TableServiceAsyncClient#deleteTable(String)} and
     * {@link TableServiceAsyncClient#deleteTableWithResponse(String)}.
     */
    public void deleteTable() {
        TableServiceAsyncClient tableServiceAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.deleteTable#String
        String tableName = "myTable";

        tableServiceAsyncClient.deleteTable(tableName)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused ->
                System.out.printf("Table with name '%s' was deleted.", tableName));
        // END: com.azure.data.tables.tableServiceAsyncClient.deleteTable#String

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.deleteTableWithResponse#String
        String myTableName = "myTable";

        tableServiceAsyncClient.deleteTableWithResponse(myTableName)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Table with name '%s' was deleted.",
                    response.getStatusCode(), myTableName));
        // END: com.azure.data.tables.tableServiceAsyncClient.deleteTableWithResponse#String
    }

    /**
     * Generates code samples for using {@link TableServiceAsyncClient#listTables()} and
     * {@link TableServiceAsyncClient#listTables(ListTablesOptions)}.
     */
    public void listTables() {
        TableServiceAsyncClient tableServiceAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.listTables
        tableServiceAsyncClient.listTables().subscribe(tableItem ->
            System.out.printf("Retrieved table with name '%s'.\n", tableItem.getName()));
        // END: com.azure.data.tables.tableServiceAsyncClient.listTables

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.listTables#ListTablesOptions
        ListTablesOptions options = new ListTablesOptions().setFilter("TableName eq 'myTable'");

        tableServiceAsyncClient.listTables(options).subscribe(tableItem ->
            System.out.printf("Retrieved table with name '%s'.\n", tableItem.getName()));
        // END: com.azure.data.tables.tableServiceAsyncClient.listTables#ListTablesOptions
    }

    /**
     * Generates code samples for using {@link TableServiceAsyncClient#getProperties()}  and
     * {@link TableServiceAsyncClient#getPropertiesWithResponse()}.
     */
    public void getProperties() {
        TableServiceAsyncClient tableServiceAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.getProperties
        tableServiceAsyncClient.getProperties()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(properties -> System.out.print("Retrieved service properties successfully."));
        // END: com.azure.data.tables.tableServiceAsyncClient.getProperties

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.getPropertiesWithResponse
        tableServiceAsyncClient.getPropertiesWithResponse()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Retrieved service properties successfully with status code: %d.",
                    response.getStatusCode()));
        // END: com.azure.data.tables.tableServiceAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates code samples for using {@link TableServiceAsyncClient#setProperties(TableServiceProperties)}  and
     * {@link TableServiceAsyncClient#setPropertiesWithResponse(TableServiceProperties)}.
     */
    public void setProperties() {
        TableServiceAsyncClient tableServiceAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.setProperties#TableServiceProperties
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

        tableServiceAsyncClient.setProperties(properties)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(unused -> System.out.print("Set service properties successfully."));
        // END: com.azure.data.tables.tableServiceAsyncClient.setProperties#TableServiceProperties

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.setPropertiesWithResponse#TableServiceProperties
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

        tableServiceAsyncClient.setPropertiesWithResponse(myProperties)
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Retrieved service properties successfully with status code: %d.",
                    response.getStatusCode()));
        // END: com.azure.data.tables.tableServiceAsyncClient.setPropertiesWithResponse#TableServiceProperties
    }

    /**
     * Generates code samples for using {@link TableServiceAsyncClient#getStatistics()}  and
     * {@link TableServiceAsyncClient#getStatisticsWithResponse()}.
     */
    public void getStatistics() {
        TableServiceAsyncClient tableServiceAsyncClient = createAsyncClient();

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.getStatistics
        tableServiceAsyncClient.getStatistics()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(statistics -> System.out.print("Retrieved service statistics successfully."));
        // END: com.azure.data.tables.tableServiceAsyncClient.getStatistics

        // BEGIN: com.azure.data.tables.tableServiceAsyncClient.getStatisticsWithResponse
        tableServiceAsyncClient.getStatisticsWithResponse()
            .contextWrite(Context.of("key1", "value1", "key2", "value2"))
            .subscribe(response ->
                System.out.printf("Retrieved service statistics successfully with status code: %d.",
                    response.getStatusCode()));
        // END: com.azure.data.tables.tableServiceAsyncClient.getStatisticsWithResponse
    }
}
