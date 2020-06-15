// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import org.junit.jupiter.api.Test;

/**
 * Unit Test DataSources utility class
 */
public class DataSourcesTest {

    @Test
    public void canCreateSqlDataSource() {
        // check utility method with minimal overloads
        SearchIndexerDataSourceConnection expected = new SearchIndexerDataSourceConnection()
            .setName("sql")
            .setType(SearchIndexerDataSourceType.AZURE_SQL)
            .setConnectionString("connectionString")
            .setContainer(new SearchIndexerDataContainer().setName("table"));
        SearchIndexerDataSourceConnection actual = SearchIndexerDataSources.createFromAzureSql(
            "sql", "connectionString", "table");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateStorageBlobDataSource() {
        // check utility method with minimal overloads
        SearchIndexerDataSourceConnection expected = new SearchIndexerDataSourceConnection()
            .setName("storageBlob")
            .setType(SearchIndexerDataSourceType.AZURE_BLOB)
            .setConnectionString("connectionString")
            .setContainer(new SearchIndexerDataContainer()
                .setName("container"));
        SearchIndexerDataSourceConnection actual = SearchIndexerDataSources.createFromAzureBlobStorage(
            "storageBlob", "connectionString", "container");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateStorageTableDataSource() {
        // check utility method with minimal overloads
        SearchIndexerDataSourceConnection expected = new SearchIndexerDataSourceConnection()
            .setName("storageTable")
            .setType(SearchIndexerDataSourceType.AZURE_TABLE)
            .setConnectionString("connectionString")
            .setContainer(new SearchIndexerDataContainer()
            .setName("table"));
        SearchIndexerDataSourceConnection actual = SearchIndexerDataSources.createFromAzureTableStorage(
            "storageTable", "connectionString", "table");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateCosmosDataSource() {
        // check utility method overloads
        SearchIndexerDataSourceConnection expected = new SearchIndexerDataSourceConnection()
            .setName("cosmos")
            .setType(SearchIndexerDataSourceType.COSMOS_DB)
            .setConnectionString("connectionString")
            .setContainer(new SearchIndexerDataContainer()
                .setName("collection"));

        SearchIndexerDataSourceConnection actual = SearchIndexerDataSources.createFromCosmos("cosmos", "connectionString", "collection", false);

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }

    @Test
    public void canCreateCosmosDataSourceWithMinimalOverload() {
        // check utility method with minimal overloads
        SearchIndexerDataSourceConnection expected = new SearchIndexerDataSourceConnection()
            .setName("cosmos")
            .setType(SearchIndexerDataSourceType.COSMOS_DB)
            .setConnectionString("connectionString")
            .setContainer(new SearchIndexerDataContainer()
                .setName("collection"))
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts"));

        SearchIndexerDataSourceConnection actual = SearchIndexerDataSources.createFromCosmos("cosmos",
            "connectionString", "collection");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }
}
