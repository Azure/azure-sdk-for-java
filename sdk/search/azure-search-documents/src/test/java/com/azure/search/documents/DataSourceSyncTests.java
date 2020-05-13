// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.models.DataContainer;
import com.azure.search.documents.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.DataSourceCredentials;
import com.azure.search.documents.models.DataSourceType;
import com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchErrorException;
import com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.models.SqlIntegratedChangeTrackingPolicy;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.azure.search.documents.TestHelpers.BLOB_DATASOURCE_TEST_NAME;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.generateRequestOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class DataSourceSyncTests extends SearchTestBase {
    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String FAKE_STORAGE_CONNECTION_STRING =
        "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;";
    private static final String FAKE_COSMOS_CONNECTION_STRING =
        "AccountEndpoint=https://NotaRealAccount.documents.azure.com;AccountKey=fake;Database=someFakeDatabase";

    private final List<String> dataSourcesToDelete = new ArrayList<>();
    private SearchServiceClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        for (String dataSource : dataSourcesToDelete) {
            client.deleteDataSource(dataSource);
        }

    }

    @Test
    public void canCreateAndListDataSources() {
        DataSource dataSource1 = createTestBlobDataSource(null);
        DataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSource(dataSource1);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdateDataSource(dataSource2);
        dataSourcesToDelete.add(dataSource2.getName());

        Iterator<DataSource> results = client.listDataSources().iterator();

        assertEquals(dataSource1.getName(), results.next().getName());
        assertEquals(dataSource2.getName(), results.next().getName());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndListDataSourcesWithResponse() {
        DataSource dataSource1 = createTestBlobDataSource(null);
        DataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSourceWithResponse(dataSource1, false, new RequestOptions(), Context.NONE);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdateDataSourceWithResponse(dataSource2, false, new RequestOptions(), Context.NONE);
        dataSourcesToDelete.add(dataSource2.getName());

        Iterator<DataSource> results = client.listDataSources("name", new RequestOptions(), Context.NONE).iterator();

        assertEquals(dataSource1.getName(), results.next().getName());
        assertEquals(dataSource2.getName(), results.next().getName());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndDeleteDatasource() {
        DataSource dataSource = createTestBlobDataSource(null);
        client.deleteDataSource(dataSource.getName());

        assertThrows(HttpResponseException.class, () -> client.getDataSource(dataSource.getName()));
    }

    @Test
    public void deleteDataSourceIsIdempotent() {
        DataSource dataSource = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> result = client.deleteDataSourceWithResponse(dataSource, false, generateRequestOptions(),
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Create the data source
        client.createOrUpdateDataSource(dataSource);

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        result = client.deleteDataSourceWithResponse(dataSource, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());
        // Again, expect to fail
        result = client.deleteDataSourceWithResponse(dataSource, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        DataSource dataSource = createTestSqlDataSourceObject();
        dataSource.setType(DataSourceType.fromString("thistypedoesnotexist"));

        assertHttpResponseException(
            () -> client.createOrUpdateDataSource(dataSource),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Data source type 'thistypedoesnotexist' is not supported"
        );
    }

    @Test
    public void canUpdateDataSource() {
        DataSource initial = createTestSqlDataSourceObject();

        // Create the data source
        client.createOrUpdateDataSource(initial);
        dataSourcesToDelete.add(initial.getName());

        DataSource updatedExpected = createTestSqlDataSourceObject()
            .setName(initial.getName())
            .setContainer(new DataContainer().setName("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy()
                .setHighWaterMarkColumnName("rowversion"))
            .setDataDeletionDetectionPolicy(new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("isDeleted"));

        DataSource updatedActual = client.createOrUpdateDataSource(updatedExpected);

        updatedExpected.getCredentials().setConnectionString(null); // Create doesn't return connection strings.
        TestHelpers.assertObjectEquals(updatedExpected, updatedActual, false, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        DataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        DataSource response = client.createOrUpdateDataSourceWithResponse(dataSource, true, null, Context.NONE)
            .getValue();

        assertFalse(CoreUtils.isNullOrEmpty(response.getETag()));
    }

    @Test
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        DataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        DataSource response = client.createOrUpdateDataSourceWithResponse(dataSource, false, null, Context.NONE)
            .getValue();

        client.deleteDataSourceWithResponse(response, true, null, Context.NONE);

        try {
            client.deleteDataSourceWithResponse(response, true, null, Context.NONE);
            fail("Second call to delete with specified ETag should have failed due to non existent data source.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    @Test
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() {
        DataSource dataSource = createTestBlobDataSource(null);

        DataSource stale = client.createOrUpdateDataSourceWithResponse(dataSource, false, null, Context.NONE)
            .getValue();

        DataSource current = client.createOrUpdateDataSourceWithResponse(stale, false, null, Context.NONE)
            .getValue();

        try {
            client.deleteDataSourceWithResponse(stale, true, null, Context.NONE);
            fail("Delete specifying a stale ETag should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteDataSourceWithResponse(current, true, null, Context.NONE);
    }

    @Test
    public void updateDataSourceIfExistsSucceedsOnExistingResource() {
        DataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        DataSource original = client.createOrUpdateDataSourceWithResponse(dataSource, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        DataSource updated = client.createOrUpdateDataSourceWithResponse(original.setDescription("an update"), false,
            null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() {
        DataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        DataSource original = client.createOrUpdateDataSourceWithResponse(dataSource, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        DataSource updated = client.createOrUpdateDataSourceWithResponse(original.setDescription("an update"), false,
            null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        try {
            client.createOrUpdateDataSourceWithResponse(original, true, null, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() {
        DataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        DataSource original = client.createOrUpdateDataSourceWithResponse(dataSource, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        DataSource updated = client.createOrUpdateDataSourceWithResponse(original.setDescription("an update"), false,
            null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Check eTags as expected
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createDataSourceReturnsCorrectDefinition() {
        SoftDeleteColumnDeletionDetectionPolicy deletionDetectionPolicy =
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("isDeleted")
                .setSoftDeleteMarkerValue("1");

        HighWaterMarkChangeDetectionPolicy changeDetectionPolicy =
            new HighWaterMarkChangeDetectionPolicy()
                .setHighWaterMarkColumnName("fakecolumn");

        // AzureSql
        createAndValidateDataSource(createTestSqlDataSourceObject(null, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(deletionDetectionPolicy, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(null, new
            SqlIntegratedChangeTrackingPolicy()));
        createAndValidateDataSource(createTestSqlDataSourceObject(deletionDetectionPolicy,
            changeDetectionPolicy));

        // Cosmos
        createAndValidateDataSource(createTestCosmosDataSource(null, false));
        createAndValidateDataSource(createTestCosmosDataSource(null, true));
        createAndValidateDataSource(createTestCosmosDataSource(deletionDetectionPolicy, false));
        createAndValidateDataSource(createTestCosmosDataSource(deletionDetectionPolicy, false));

        // Azure Blob Storage
        createAndValidateDataSource(createTestBlobDataSource(null));
        createAndValidateDataSource(createTestBlobDataSource(deletionDetectionPolicy));

        // Azure Table Storage
        createAndValidateDataSource(createTestTableStorageDataSource());
        createAndValidateDataSource(createTestBlobDataSource(deletionDetectionPolicy));
    }

    private void createAndValidateDataSource(DataSource expectedDataSource) {
        DataSource actualDataSource = client.createOrUpdateDataSource(expectedDataSource);

        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");
        // we delete the data source because otherwise we will hit the quota limits during the tests
        client.deleteDataSource(actualDataSource.getName());

    }

    @Test
    public void getDataSourceReturnsCorrectDefinition() {
        createGetAndValidateDataSource(createTestBlobDataSource(null));
        createGetAndValidateDataSource(createTestTableStorageDataSource());
        createGetAndValidateDataSource(createTestSqlDataSourceObject());
        createGetAndValidateDataSource(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateDataSource(DataSource expectedDataSource) {
        client.createOrUpdateDataSource(expectedDataSource);
        String dataSourceName = expectedDataSource.getName();

        // Get doesn't return connection strings.
        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));

        DataSource actualDataSource = client.getDataSource(dataSourceName);
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        actualDataSource = client.getDataSourceWithResponse(dataSourceName, generateRequestOptions(), Context.NONE)
            .getValue();
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        client.deleteDataSource(dataSourceName);
    }

    @Test
    public void getDataSourceThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getDataSource("thisdatasourcedoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "No data source with the name 'thisdatasourcedoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateDataSource() {
        DataSource expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        DataSource actualDataSource = client.createDataSource(expectedDataSource);
        assertNotNull(actualDataSource);
        assertEquals(expectedDataSource.getName(), actualDataSource.getName());

        Iterator<DataSource> dataSources = client.listDataSources().iterator();
        assertEquals(expectedDataSource.getName(), dataSources.next().getName());
        assertFalse(dataSources.hasNext());
    }

    @Test
    public void canCreateDataSourceWithResponse() {
        DataSource expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        Response<DataSource> response = client
            .createDataSourceWithResponse(expectedDataSource, new RequestOptions(), null);
        assertNotNull(response);
        assertNotNull(response.getValue());
        assertEquals(expectedDataSource.getName(), response.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
    }

    @Test
    public void canUpdateConnectionData() {
        // Note: since connection string is not returned when queried from the service, actually saving the
        // datasource, retrieving it and verifying the change, won't work.
        // Hence, we only validate that the properties on the local items can change.

        // Create an initial dataSource
        DataSource initial = createTestBlobDataSource(null);
        assertEquals(initial.getCredentials().getConnectionString(), FAKE_STORAGE_CONNECTION_STRING);

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setCredentials(new DataSourceCredentials().setConnectionString(newConnString));

        assertEquals(initial.getCredentials().getConnectionString(), newConnString);
    }

    DataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.createFromAzureBlobStorage(testResourceNamer.randomName(BLOB_DATASOURCE_TEST_NAME, 32),
            FAKE_STORAGE_CONNECTION_STRING, "fakecontainer", "/fakefolder/", FAKE_DESCRIPTION, deletionDetectionPolicy);
    }

    DataSource createTestTableStorageDataSource() {
        return DataSources.createFromAzureTableStorage("azs-java-test-tablestorage", FAKE_STORAGE_CONNECTION_STRING,
            "faketable", "fake query", FAKE_DESCRIPTION, null);
    }

    DataSource createTestCosmosDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return DataSources.createFromCosmos("azs-java-test-cosmos", FAKE_COSMOS_CONNECTION_STRING, "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark", useChangeDetection, FAKE_DESCRIPTION,
            deletionDetectionPolicy);
    }
}
