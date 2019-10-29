// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.common.DataSources;
import com.azure.search.models.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public abstract class DataSourceTestBase extends SearchServiceTestBase {
    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String RESOURCE_NAME_PREFIX = "azs-";
    // The connection string we use here, as well as table name and target index schema, use the USGS database
    // that we set up to support our code samples.
    //
    // ASSUMPTION: Change tracking has already been enabled on the database with ALTER DATABASE ... SET CHANGE_TRACKING = ON
    // and it has been enabled on the table with ALTER TABLE ... ENABLE CHANGE_TRACKING
    private static final String SQL_CONN_STRING_FIXTURE = "Server=tcp:xxx.database.windows.net,1433;Database=xxx;User ID=reader;Password=xxx;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;";

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    public abstract void createAndListDataSources();

    @Test
    public abstract void createDataSourceFailsWithUsefulMessageOnUserError();

    @Test
    public abstract void createDataSourceReturnsCorrectDefinition();

    @Test
    public abstract void deleteDataSourceIsIdempotent();

    protected DataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureBlobStorage(
            "azs-java-test-blob",
            "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;",
            "fakecontainer",
            "/fakefolder/",
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    protected DataSource createTestSqlDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy, DataChangeDetectionPolicy changeDetectionPolicy) {
        return azureSql(
            "azs-java-test-sql",
            SQL_CONN_STRING_FIXTURE,
            "GeoNamesRI",
            changeDetectionPolicy,
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    protected DataSource createTestSqlDataSource() {
        DataDeletionDetectionPolicy deletionDetectionPolicy = null;
        return azureSql(
            "azs-java-test-sql",
            SQL_CONN_STRING_FIXTURE,
            "GeoNamesRI",
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    /**
     * Creates a new DataSource to connect to an Azure SQL database.
     *
     * @param name The name of the datasource.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the datasource.
     * @param description Optional. Description of the datasource.
     * @return A new DataSource instance.
     */
    public static DataSource azureSql(
        String name,
        String sqlConnectionString,
        String tableOrViewName,
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        String description) {
        return DataSources.sqlDataSource(
            name,
            sqlConnectionString,
            tableOrViewName,
            description,
            null,
            deletionDetectionPolicy);
    }

    /**
     * CCreates a new DataSource to connect to an Azure SQL database with change detection enabled.
     *
     * @param name The name of the datasource.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @param changeDetectionPolicy The change detection policy for the datasource.
     * @param description Optional. Description of the datasource.
     * @return A new DataSource instance.
     */
    public static DataSource azureSql(
        String name,
        String sqlConnectionString,
        String tableOrViewName,
        DataChangeDetectionPolicy changeDetectionPolicy,
        String description) {
        Objects.requireNonNull(changeDetectionPolicy);
        return DataSources.sqlDataSource(
            name,
            sqlConnectionString,
            tableOrViewName,
            description,
            changeDetectionPolicy,
            null);
    }

    /**
     * Creates a new DataSource to connect to an Azure SQL database.
     *
     * @param name The name of the datasource.
     * @param sqlConnectionString The connection string for the Azure SQL database.
     * @param tableOrViewName The name of the table or view from which to read rows.
     * @param changeDetectionPolicy The change detection policy for the datasource.
     * Note that only high watermark change detection
     * is allowed for Azure SQL when deletion detection is enabled.
     * @param deletionDetectionPolicy Optional. The data deletion detection policy for the datasource.
     * @param description Optional. Description of the datasource.
     * @return A new DataSource instance.
     */
    public static DataSource azureSql(
        String name,
        String sqlConnectionString,
        String tableOrViewName,
        DataChangeDetectionPolicy changeDetectionPolicy,
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        String description) {
        Objects.requireNonNull(deletionDetectionPolicy);
        Objects.requireNonNull(changeDetectionPolicy);
        return DataSources.sqlDataSource(
            name,
            sqlConnectionString,
            tableOrViewName,
            description,
            changeDetectionPolicy,
            deletionDetectionPolicy);
    }

    protected void assertDataSourcesEqual(DataSource expected, DataSource actual) {
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getType().toString(), actual.getType().toString());
        Assert.assertEquals(expected.getContainer().getName(), actual.getContainer().getName());
        Assert.assertEquals(expected.getContainer().getQuery(), actual.getContainer().getQuery());
        Assert.assertEquals(expected.getCredentials().getConnectionString(),
            actual.getCredentials().getConnectionString());
        Assert.assertEquals(expected.getDataChangeDetectionPolicy(),
            actual.getDataChangeDetectionPolicy());
        Assert.assertEquals(expected.getDataDeletionDetectionPolicy(),
            actual.getDataDeletionDetectionPolicy());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
    }
}
