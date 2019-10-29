// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.models.*;
import org.junit.Assert;
import org.junit.Test;

public abstract class DataSourceTestBase extends SearchServiceTestBase {
    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String RESOURCE_NAME_PREFIX = "azs-";

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
        return new DataSource()
            .setName("azs-java-test-blob")
            .setType(DataSourceType.AZUREBLOB)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;"))
            .setContainer(new DataContainer()
                .setName("fakecontainer")
                .setQuery("/fakefolder/"))
            .setDescription(FAKE_DESCRIPTION)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy);
    }

    protected DataSource createTestSqlDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy, DataChangeDetectionPolicy changeDetectionPolicy) {
        // The connection string we use here, as well as table name and target index schema, use the USGS database
        // that we set up to support our code samples.
        //
        // ASSUMPTION: Change tracking has already been enabled on the database with ALTER DATABASE ... SET CHANGE_TRACKING = ON
        // and it has been enabled on the table with ALTER TABLE ... ENABLE CHANGE_TRACKING
        return new DataSource()
            .setName("azs-java-test-sql")
            .setType(DataSourceType.AZURESQL)
            .setCredentials(new DataSourceCredentials()
                .setConnectionString("Server=tcp:azs-playground.database.windows.net,1433;Database=usgs;User ID=reader;Password=EdrERBt3j6mZDP;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;"))
            .setContainer(new DataContainer()
                .setName("GeoNamesRI"))
            .setDescription(FAKE_DESCRIPTION)
            .setDataDeletionDetectionPolicy(deletionDetectionPolicy)
            .setDataChangeDetectionPolicy(changeDetectionPolicy);
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
