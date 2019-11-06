// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

/**
 * This sample demonstrates how to create an Azure Search data source for SQL Server/Azure SQL,
 * Azure Cosmos DB, Blob Storage and Table Storage.
 * To use it, create the respective databases/storage services and replace their connection strings below.
 */
public class DataSourceExample {
    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ADMIN_KEY");
    public static final String TABLE_STORAGE_CONNECTION_STRING = "<Your Table Storage connection string>";
    public static final String COSMOS_DB_CONNECTION_STRING = "<Your Cosmos DB connection string>";
    public static final String BLOB_STORAGE_CONNECTION_STRING = "<Your Blob Storage connection string>";
    public static final String SQL_CONNECTION_STRING = "<Your SQL connection string>";

    public static void main(String[] args) {
        SearchServiceClient client = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(ADMIN_KEY))
            .buildClient();

        /*
        * Store the names of the created data sources so that we can delete them later
        * without affecting other resources.
        * */
        Collection<String> names = new HashSet<String>();

        names.add(createSqlDataSource(client));
        names.add(createCosmosDBDataSource(client));
        names.add(createBlobDataSource(client));
        names.add(createTableStorageDataSource(client));

        /*
        * Get all existing data sources; list should include the ones we just created.
        * */
        PagedIterable<DataSource> dataSources = client.listDataSources();
        for (DataSource dataSource: dataSources) {
            if (names.contains(dataSource.getName())) {
                System.out.println(String.format("Found data source %s of type %s", dataSource.getName(), dataSource.getType().toString() ));
            }
        }

        /*
        * Delete the data sources we just created.
        * */
        for (String name: names) {
            deleteDataSource(client, name);
        }
    }

    private static void deleteDataSource(SearchServiceClient client, String dataSourceName) {
        try {
            client.deleteDataSource(dataSourceName);
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }

    private static String createDataSource(
        SearchServiceClient client,
        DataSourceType type,
        String connectionString,
        DataContainer container,
        DataChangeDetectionPolicy dataChangeDetectionPolicy) {

        String name = generateDataSourceName();
        try {
            DataSource dataSource = new DataSource()
                .setName(name)
                .setType(type)
                .setCredentials(new DataSourceCredentials()
                    .setConnectionString(connectionString))
                .setContainer(container)
                .setDataChangeDetectionPolicy(dataChangeDetectionPolicy);

            DataSource createdDataSource = client.createOrUpdateDataSource(dataSource);
            return createdDataSource.getName();
        } catch (Exception ex) {
            System.err.println(ex.toString());
        } finally {
            return name;
        }
    }

    private static String createTableStorageDataSource(SearchServiceClient client) {
        return createDataSource(
            client,
            DataSourceType.AZURETABLE,
            TABLE_STORAGE_CONNECTION_STRING,
            new DataContainer()
                .setName("testtable") // Replace your table name here
                .setQuery("PartitionKey eq 'test'"), // Add your query here or remove this if you don't need one
            null
        );
    }

    private static String createCosmosDBDataSource(SearchServiceClient client) {
        return createDataSource(
            client,
            DataSourceType.COSMOSDB,
            COSMOS_DB_CONNECTION_STRING,
            new DataContainer()
                .setName("testcollection") // Replace your collection name here
                .setQuery(null), // Add your query here or remove this if you don't need one
            new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts")
        );
    }

    private static String createBlobDataSource(SearchServiceClient client) {
        return createDataSource(
            client,
            DataSourceType.AZUREBLOB,
            BLOB_STORAGE_CONNECTION_STRING,
            new DataContainer()
                .setName("testcontainer") // Replace your container name here
                .setQuery("testfolder"), // Add your folder here or remove this if you want to index all folders within the container
            null
        );
    }

    private static String createSqlDataSource(SearchServiceClient client) {
        return createDataSource(
            client,
            DataSourceType.AZURESQL,
            SQL_CONNECTION_STRING,
            new DataContainer()
                .setName("testtable"),  // Replace your table or view name here
             null); // Or new SqlIntegratedChangeTrackingPolicy() if your database has change tracking enabled
    }

    private static String generateDataSourceName() {
        return "datasource" + UUID.randomUUID().toString();
    }
}
