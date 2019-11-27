// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.common.DataSources;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import org.junit.Assert;
import org.junit.Test;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public abstract class DataSourceTestBase extends SearchServiceTestBase {

    private static final String FAKE_DESCRIPTION = "Some data source";

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
    public void canUpdateConnectionData() {
        // Note: since connection string is not returned when queried from the service, actually saving the
        // datasource, retrieving it and verifying the change, won't work.
        // Hence, we only validate that the properties on the local items can change.

        // Create an initial datasource
        DataSource initial = createTestBlobDataSource(null);
        Assert.assertEquals(initial.getCredentials().getConnectionString(),
            "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;");

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setCredentials(new DataSourceCredentials().setConnectionString(newConnString));

        Assert.assertEquals(initial.getCredentials().getConnectionString(), newConnString);
    }

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

    protected void assertDataSourcesEqual(DataSource updatedExpected, DataSource actualDataSource) {
        // Using assertReflectionEquals also checks the etag, however we do not care
        // for that value, hence, we change both to the same value to make sure it
        // won't fail the assertion
        updatedExpected.setETag("none");
        actualDataSource.setETag("none");
        assertReflectionEquals(updatedExpected, actualDataSource);
    }
}
