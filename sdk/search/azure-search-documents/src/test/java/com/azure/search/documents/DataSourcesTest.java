// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.DataContainer;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.DataSourceCredentials;
import com.azure.search.documents.models.DataSourceType;
import com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy;
import org.junit.jupiter.api.Test;

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
        DataSource actual = DataSources.createFromAzureSql(
            "sql", "connectionString", "table");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
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
        DataSource actual = DataSources.createFromAzureBlobStorage(
            "storageBlob", "connectionString", "container");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
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
        DataSource actual = DataSources.createFromAzureTableStorage(
            "storageTable", "connectionString", "table");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
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

        DataSource actual = DataSources.createFromCosmos("cosmos", "connectionString", "collection", false);

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
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

        DataSource actual = DataSources.createFromCosmos("cosmos", "connectionString", "collection");

        TestHelpers.assertObjectEquals(expected, actual, false, "etag");
    }
}
