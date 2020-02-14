// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.models.DataContainer;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit Test DataSources utility class
 */
public class DataSourcesTest {

    @Test
    public void canCreateSqlDataSource() {
        // check utility method with minimal overloads
        DataSource expected = new DataSource()
            .setName("sql")
            .setType(DataSourceType.AZURE_SQL)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new DataContainer().setName("table"));

        DataSource actual = DataSources.azureSql("sql", "connectionString", "table");

        assertTrue(TestHelpers.areDataSourcesEqual(actual, expected));
    }

    @Test
    public void canCreateStorageBlobDataSource() {
        // check utility method with minimal overloads
        DataSource expected = new DataSource()
            .setName("storageBlob")
            .setType(DataSourceType.AZURE_BLOB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new DataContainer()
                .setName("container"));

        DataSource actual = DataSources.azureBlobStorage("storageBlob", "connectionString", "container");

        assertTrue(TestHelpers.areDataSourcesEqual(actual, expected));
    }

    @Test
    public void canCreateStorageTableDataSource() {
        // check utility method with minimal overloads
        DataSource expected = new DataSource()
            .setName("storageTable")
            .setType(DataSourceType.AZURE_TABLE)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new DataContainer()
                .setName("table"));

        DataSource actual = DataSources.azureTableStorage("storageTable", "connectionString", "table");

        assertTrue(TestHelpers.areDataSourcesEqual(actual, expected));
    }

    @Test
    public void canCreateCosmosDataSource() {
        // check utility method overloads
        DataSource expected = new DataSource()
            .setName("cosmos")
            .setType(DataSourceType.COSMOS)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new DataContainer()
                .setName("collection"));

        DataSource actual = DataSources.cosmos("cosmos", "connectionString", "collection", false);

        assertTrue(TestHelpers.areDataSourcesEqual(actual, expected));
    }

    @Test
    public void canCreateCosmosDataSourceWithMinimalOverload() {
        // check utility method with minimal overloads
        DataSource expected = new DataSource()
            .setName("cosmos")
            .setType(DataSourceType.COSMOS)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new DataContainer()
                .setName("collection"))
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts"));

        DataSource actual = DataSources.cosmos("cosmos", "connectionString", "collection");

        assertTrue(TestHelpers.areDataSourcesEqual(actual, expected));
    }
}
