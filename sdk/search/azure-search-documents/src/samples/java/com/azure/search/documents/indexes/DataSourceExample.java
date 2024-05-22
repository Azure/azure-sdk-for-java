// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.DataChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

/**
 * This sample demonstrates how to create an Azure Cognitive Search data source for SQL Server/Azure SQL,
 * Azure Cosmos DB, Blob Storage and Table Storage.
 * To use it, create the respective databases/storage services and replace their connection strings below.
 */
public class DataSourceExample {
    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");
    private static final String TABLE_STORAGE_CONNECTION_STRING = "<Your Table Storage connection string>";
    private static final String COSMOS_CONNECTION_STRING = "<Your Cosmos connection string>";
    private static final String BLOB_STORAGE_CONNECTION_STRING = "<Your Blob Storage connection string>";
    private static final String SQL_CONNECTION_STRING = "<Your SQL connection string>";

    public static void main(String[] args) {
        SearchIndexerClient client = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();

        /*
         * Store the names of the created data sources so that we can delete them later
         * without affecting other resources.
         * */
        Collection<String> names = new HashSet<>();

        names.add(createSqlDataSource(client));
        names.add(createCosmosDataSource(client));
        names.add(createBlobDataSource(client));
        names.add(createTableStorageDataSource(client));

        /*
         * Get all existing data sources; list should include the ones we just created.
         * */
        PagedIterable<SearchIndexerDataSourceConnection> dataSources = client.listDataSourceConnections();
        for (SearchIndexerDataSourceConnection dataSource : dataSources) {
            if (names.contains(dataSource.getName())) {
                System.out.println(String.format("Found data source %s of type %s", dataSource.getName(),
                    dataSource.getType().toString()));
            }
        }

        /*
         * Delete the data sources we just created.
         * */
        for (String name : names) {
            deleteDataSource(client, name);
        }
    }

    private static void deleteDataSource(SearchIndexerClient client, String dataSourceName) {
        try {
            client.deleteDataSourceConnection(dataSourceName);
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }

    private static SearchIndexerDataSourceConnection createSampleDatasource(SearchIndexerDataSourceType type,
        String connectionString, SearchIndexerDataContainer container,
        DataChangeDetectionPolicy dataChangeDetectionPolicy) {
        return new SearchIndexerDataSourceConnection(generateDataSourceName(), type, connectionString, container)
            .setDataChangeDetectionPolicy(dataChangeDetectionPolicy);
    }

    private static String createDataSource(
        SearchIndexerClient client,
        SearchIndexerDataSourceType type,
        String connectionString,
        SearchIndexerDataContainer container,
        DataChangeDetectionPolicy dataChangeDetectionPolicy) {

        SearchIndexerDataSourceConnection dataSource = createSampleDatasource(type, connectionString, container,
            dataChangeDetectionPolicy);
        try {
            client.createOrUpdateDataSourceConnection(dataSource);
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
        return dataSource.getName();
    }

    private static String createTableStorageDataSource(SearchIndexerClient client) {
        return createDataSource(
            client,
            SearchIndexerDataSourceType.AZURE_TABLE,
            TABLE_STORAGE_CONNECTION_STRING,
            new SearchIndexerDataContainer("testtable") // Replace your table name here
                .setQuery("PartitionKey eq 'test'"), // Add your query here or remove this if you don't need one
            null
        );
    }

    private static String createCosmosDataSource(SearchIndexerClient client) {
        return createDataSource(
            client,
            SearchIndexerDataSourceType.COSMOS_DB,
            COSMOS_CONNECTION_STRING,
            new SearchIndexerDataContainer("testcollection") // Replace your collection name here
                .setQuery(null), // Add your query here or remove this if you don't need one
            new HighWaterMarkChangeDetectionPolicy("_ts")
        );
    }

    private static String createBlobDataSource(SearchIndexerClient client) {
        return createDataSource(
            client,
            SearchIndexerDataSourceType.AZURE_BLOB,
            BLOB_STORAGE_CONNECTION_STRING,
            new SearchIndexerDataContainer("testcontainer") // Replace your container name here
                .setQuery("testfolder"), // Add your folder here or remove this if you want to index all folders within the container
            null
        );
    }

    private static String createSqlDataSource(SearchIndexerClient client) {
        return createDataSource(
            client,
            SearchIndexerDataSourceType.AZURE_SQL,
            SQL_CONNECTION_STRING,
            new SearchIndexerDataContainer("testtable"),  // Replace your table or view name here
            null); // Or new SqlIntegratedChangeTrackingPolicy() if your database has change tracking enabled
    }

    private static String generateDataSourceName() {
        return "datasource" + UUID.randomUUID().toString();
    }
}
