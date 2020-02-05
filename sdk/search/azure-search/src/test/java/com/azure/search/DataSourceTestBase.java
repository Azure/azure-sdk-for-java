// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public abstract class DataSourceTestBase extends SearchServiceTestBase {

    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String FAKE_STORAGE_CONNECTION_STRING =
        "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;";
    private static final String FAKE_COSMOS_CONNECTION_STRING =
        "AccountEndpoint=https://NotaRealAccount.documents.azure.com;AccountKey=fake;Database=someFakeDatabase";

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    public abstract void canCreateAndListDataSources();

    @Test
    public abstract void canCreateAndListDataSourcesWithResponse();

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
    public abstract void getDataSourceReturnsCorrectDefinition();

    @Test
    public abstract void getDataSourceThrowsOnNotFound();

    @Test
    public abstract void deleteDataSourceIfExistsWorksOnlyWhenResourceExists();

    @Test
    public abstract void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource();

    @Test
    public abstract void updateDataSourceIfExistsFailsOnNoResource();

    @Test
    public abstract void updateDataSourceIfExistsSucceedsOnExistingResource();

    @Test
    public abstract void updateDataSourceIfNotChangedFailsWhenResourceChanged();

    @Test
    public abstract void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged();

    @Test
    public abstract void existsReturnsFalseForNonExistingDatasource();

    @Test
    public abstract void existsReturnsTrueForExistingDatasource();

    @Test
    public abstract void existsReturnsTrueForExistingDatasourceWithResponse();

    @Test
    public abstract void canCreateAndDeleteDatasource();

    @Test
    public abstract void canCreateDataSource();

    @Test
    public abstract void canCreateDataSourceWithResponse();

    @Test
    public void canUpdateConnectionData() {
        // Note: since connection string is not returned when queried from the service, actually saving the
        // datasource, retrieving it and verifying the change, won't work.
        // Hence, we only validate that the properties on the local items can change.

        // Create an initial dataSource
        DataSource initial = createTestBlobDataSource(null);
        Assert.assertEquals(initial.getCredentials().getConnectionString(),
            FAKE_STORAGE_CONNECTION_STRING);

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setCredentials(new DataSourceCredentials().setConnectionString(newConnString));

        Assert.assertEquals(initial.getCredentials().getConnectionString(), newConnString);
    }

    DataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureBlobStorage(
            BLOB_DATASOURCE_TEST_NAME,
            FAKE_STORAGE_CONNECTION_STRING,
            "fakecontainer",
            "/fakefolder/",
            FAKE_DESCRIPTION,
            deletionDetectionPolicy
        );
    }

    DataSource createTestTableStorageDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureTableStorage(
            "azs-java-test-tablestorage",
            FAKE_STORAGE_CONNECTION_STRING,
            "faketable",
            "fake query",
            FAKE_DESCRIPTION,
            deletionDetectionPolicy
        );
    }

    DataSource createTestCosmosDataSource(
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return DataSources.cosmos(
            "azs-java-test-cosmos",
            FAKE_COSMOS_CONNECTION_STRING,
            "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark",
            useChangeDetection,
            FAKE_DESCRIPTION,
            deletionDetectionPolicy
        );
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
