// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.models.DataContainer;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

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
        DataSource actual = DataSources.azureSql(
            "sql", "connectionString", "table");

        Assert.assertTrue(assertDataSourceEqual(actual, expected));
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
        DataSource actual = DataSources.azureBlobStorage(
            "storageBlob", "connectionString", "container");

        Assert.assertTrue(assertDataSourceEqual(actual, expected));
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
        DataSource actual = DataSources.azureTableStorage(
            "storageTable", "connectionString", "table");

        Assert.assertTrue(assertDataSourceEqual(actual, expected));
    }

    @Test
    public void canCreateCosmosDataSource() {
        // check utility method overloads
        DataSource expected = new DataSource()
            .setName("cosmos")
            .setType(DataSourceType.COSMOS_DB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new DataContainer()
                .setName("collection"));
        DataSource actual = DataSources.cosmosDb(
            "cosmos", "connectionString", "collection", false);

        Assert.assertTrue(assertDataSourceEqual(actual, expected));
    }

    @Test
    public void canCreateCosmosDataSourceWithMinimalOverload() {
        // check utility method with minimal overloads
        DataSource expected = new DataSource()
            .setName("cosmos")
            .setType(DataSourceType.COSMOS_DB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("connectionString"))
            .setContainer(new DataContainer()
                .setName("collection"))
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("_ts"));
        DataSource actual = DataSources.cosmosDb(
            "cosmos", "connectionString", "collection");

        Assert.assertTrue(assertDataSourceEqual(actual, expected));
    }

    private boolean assertDataSourceEqual(DataSource actual, DataSource expected) {
        return StringUtils.equals(actual.getName(), expected.getName())
            && StringUtils.equals(actual.getType().toString(), expected.getType().toString())
            && StringUtils.equals(actual.getCredentials().getConnectionString(), expected.getCredentials().getConnectionString())
            && StringUtils.equals(actual.getContainer().getName(), expected.getContainer().getName())
            && StringUtils.equals(actual.getContainer().getQuery(), expected.getContainer().getQuery())
            && StringUtils.equals(actual.getDescription(), expected.getDescription())

            && ((actual.getDataChangeDetectionPolicy() == null && expected.getDataChangeDetectionPolicy() == null)
            || actual.getDataChangeDetectionPolicy().getClass().equals(expected.getDataChangeDetectionPolicy().getClass()))

            && ((actual.getDataDeletionDetectionPolicy() == null && expected.getDataDeletionDetectionPolicy() == null)
            || actual.getDataDeletionDetectionPolicy().getClass().equals(expected.getDataDeletionDetectionPolicy().getClass()));
    }
}
