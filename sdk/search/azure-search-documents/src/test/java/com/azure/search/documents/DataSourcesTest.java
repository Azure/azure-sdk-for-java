// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.DataSourceCredentials;
import com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.models.SearchIndexerDataContainer;
import com.azure.search.documents.models.SearchIndexerDataSource;
import com.azure.search.documents.models.SearchIndexerDataSourceType;
import org.junit.jupiter.api.Test;

/**
 * Unit Test DataSources utility class
 */
public class DataSourcesTest {

    @Test
    public void canCreateSqlDataSource() {
        // check utility method with minimal overloads
        SearchIndexerDataSource expected = new SearchIndexerDataSource()
            .setName("sql")
            .setType(SearchIndexerDataSourceType.AZURE_SQL)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new SearchIndexerDataContainer().setName("table"));
        SearchIndexerDataSource actual = SearchIndexerDataSources.createFromAzureSql(
            "sql", "connectionString", "table");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateStorageBlobDataSource() {
        // check utility method with minimal overloads
        SearchIndexerDataSource expected = new SearchIndexerDataSource()
            .setName("storageBlob")
            .setType(SearchIndexerDataSourceType.AZURE_BLOB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new SearchIndexerDataContainer()
                .setName("container"));
        SearchIndexerDataSource actual = SearchIndexerDataSources.createFromAzureBlobStorage(
            "storageBlob", "connectionString", "container");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateStorageTableDataSource() {
        // check utility method with minimal overloads
        SearchIndexerDataSource expected = new SearchIndexerDataSource()
            .setName("storageTable")
            .setType(SearchIndexerDataSourceType.AZURE_TABLE)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new SearchIndexerDataContainer()
            .setName("table"));
        SearchIndexerDataSource actual = SearchIndexerDataSources.createFromAzureTableStorage(
            "storageTable", "connectionString", "table");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateCosmosDataSource() {
        // check utility method overloads
        SearchIndexerDataSource expected = new SearchIndexerDataSource()
            .setName("cosmos")
            .setType(SearchIndexerDataSourceType.COSMOS_DB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new SearchIndexerDataContainer()
                .setName("collection"));

        SearchIndexerDataSource actual = SearchIndexerDataSources.createFromCosmos("cosmos", "connectionString", "collection", false);

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateCosmosDataSourceWithMinimalOverload() {
        // check utility method with minimal overloads
        SearchIndexerDataSource expected = new SearchIndexerDataSource()
            .setName("cosmos")
            .setType(SearchIndexerDataSourceType.COSMOS_DB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new SearchIndexerDataContainer()
                .setName("collection"))
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts"));

        SearchIndexerDataSource actual = SearchIndexerDataSources.createFromCosmos("cosmos",
            "connectionString", "collection");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }
}
