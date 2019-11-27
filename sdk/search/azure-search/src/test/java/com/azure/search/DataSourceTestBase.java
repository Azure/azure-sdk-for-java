// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.common.DataSources;
import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import org.junit.Test;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public abstract class DataSourceTestBase extends SearchServiceTestBase {

    private static final String FAKE_DESCRIPTION = "Some data source";
    // The connection string we use here, as well as table name and target index schema, use the USGS database
    // that we set up to support our code samples.
    //
    // ASSUMPTION: Change tracking has already been enabled on the database with ALTER DATABASE ... SET CHANGE_TRACKING = ON
    // and it has been enabled on the table with ALTER TABLE ... ENABLE CHANGE_TRACKING
    private static final String SQL_CONN_STRING_FIXTURE =
        "Server=tcp:azs-playground.database.windows.net,1433;Database=usgs;User ID=reader;Password=EdrERBt3j6mZDP;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;";

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

    @Test
    public abstract void canUpdateDataSource();

    @Test
    public abstract void createOrUpdateDataSourceIfNotExistsFailsOnExistingResource();

    @Test
    public abstract void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource();

    @Test
    public abstract void createOrUpdateDatasourceWithResponseIfNotExistsSucceedsOnNoResource();

    @Test
    public abstract void getDataSourceReturnsCorrectDefinition();

    @Test
    public abstract void getDataSourceThrowsOnNotFound();

    @Test
    public abstract void deleteDataSourceIfExistsWorksOnlyWhenResourceExists();

    @Test
    public abstract void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void updateDataSourceIfExistsFailsOnNoResource() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void updateDataSourceIfExistsSucceedsOnExistingResource() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void updateDataSourceIfNotChangedFailsWhenResourceChanged() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void existsReturnsFalseForNonExistingDatasource();

    @Test
    public abstract void existsReturnsTrueForExistingDatasource();

    @Test
    public abstract void canCreateAndDeleteDatasource();

    @Test
    public abstract void canUpdateConnectionData();

    protected DataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureBlobStorage(
            BLOB_DATASOURCE_TEST_NAME,
            "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;",
            "fakecontainer",
            "/fakefolder/",
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    protected DataSource createTestTableStorageDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureTableStorage(
            "azs-java-test-tablestorage",
            "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;",
            "faketable",
            "fake query",
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    protected DataSource createTestCosmosDbDataSource(
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return DataSources.cosmosDb(
            "azs-java-test-cosmos",
            "AccountEndpoint=https://NotaRealAccount.documents.azure.com;AccountKey=fake;Database=someFakeDatabase",
            "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark",
            useChangeDetection,
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
     * @param changeDetectionPolicy The change detection policy for the datasource.
     * Note that only high watermark change detection
     * is allowed for Azure SQL when deletion detection is enabled.
     * @param deletionDetectionPolicy The data deletion detection policy for the datasource.
     * @param description Optional. Description of the datasource.
     * @return A new DataSource instance.
     */
    static DataSource azureSql(
        String name,
        String sqlConnectionString,
        String tableOrViewName,
        DataChangeDetectionPolicy changeDetectionPolicy,
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        String description) {
        return DataSources.azureSql(
            name,
            sqlConnectionString,
            tableOrViewName,
            description,
            changeDetectionPolicy,
            deletionDetectionPolicy);
    }

    void assertDataSourcesEqual(DataSource updatedExpected, DataSource actualDataSource) {
        // Using assertReflectionEquals also checks the etag, however we do not care
        // for that value, hence, we change both to the same value to make sure it
        // won't fail the assertion
        updatedExpected.setETag("none");
        actualDataSource.setETag("none");
        assertReflectionEquals(updatedExpected, actualDataSource);
    }
}
