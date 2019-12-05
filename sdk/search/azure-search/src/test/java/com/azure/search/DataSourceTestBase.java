// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import org.junit.Test;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public abstract class DataSourceTestBase extends SearchServiceTestBase {

    private static final String FAKE_DESCRIPTION = "Some data source";
    static final String FAKE_STORAGE_CONNECTION_STRING =
        "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;";
    private static final String FAKE_COSMOS_CONNECTION_STRING =
        "AccountEndpoint=https://NotaRealAccount.documents.azure.com;AccountKey=fake;Database=someFakeDatabase";

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
    public abstract void canCreateAndDeleteDatasource();

    @Test
    public abstract void canUpdateConnectionData();

    DataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureBlobStorage(
            BLOB_DATASOURCE_TEST_NAME,
            FAKE_STORAGE_CONNECTION_STRING,
            "fakecontainer",
            "/fakefolder/",
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    DataSource createTestTableStorageDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.azureTableStorage(
            "azs-java-test-tablestorage",
            FAKE_STORAGE_CONNECTION_STRING,
            "faketable",
            "fake query",
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
        );
    }

    DataSource createTestCosmosDbDataSource(
        DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return DataSources.cosmosDb(
            "azs-java-test-cosmos",
            FAKE_COSMOS_CONNECTION_STRING,
            "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark",
            useChangeDetection,
            deletionDetectionPolicy,
            FAKE_DESCRIPTION
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
