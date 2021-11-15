// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.SqlIntegratedChangeTrackingPolicy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.azure.search.documents.TestHelpers.BLOB_DATASOURCE_TEST_NAME;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
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
    public static final String FAKE_AZURE_SQL_CONNECTION_STRING =
        "Server=tcp:fakeUri,1433;Database=fakeDatabase;User ID=reader;Password=fakePassword;Trusted_Connection=False;Encrypt=True;Connection Timeout=30;";

    private final List<String> dataSourcesToDelete = new ArrayList<>();
    private SearchIndexerClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchIndexerClientBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        for (String dataSource : dataSourcesToDelete) {
            client.deleteDataSourceConnection(dataSource);
        }

    }

    @Test
    public void canCreateAndListDataSources() {
        SearchIndexerDataSourceConnection dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSourceConnection dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSourceConnection(dataSource1);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdateDataSourceConnection(dataSource2);
        dataSourcesToDelete.add(dataSource2.getName());

        Iterator<SearchIndexerDataSourceConnection> results = client.listDataSourceConnections().iterator();

        assertDataSourceEquals(dataSource1, results.next());
        assertDataSourceEquals(dataSource2, results.next());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndListDataSourcesWithResponse() {
        SearchIndexerDataSourceConnection dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSourceConnection dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSourceConnectionWithResponse(dataSource1, false, Context.NONE);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdateDataSourceConnectionWithResponse(dataSource2, false, Context.NONE);
        dataSourcesToDelete.add(dataSource2.getName());

        Iterator<String> results = client.listDataSourceConnectionNames(Context.NONE).iterator();
        assertEquals(dataSource1.getName(), results.next());
        assertEquals(dataSource2.getName(), results.next());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndDeleteDatasource() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        client.deleteDataSourceConnection(dataSource.getName());

        assertThrows(HttpResponseException.class, () -> client.getDataSourceConnection(dataSource.getName()));
    }

    @Test
    public void deleteDataSourceIsIdempotent() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> result = client.deleteDataSourceConnectionWithResponse(dataSource, false,
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Create the data source
        client.createOrUpdateDataSourceConnection(dataSource);

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        result = client.deleteDataSourceConnectionWithResponse(dataSource, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());
        // Again, expect to fail
        result = client.deleteDataSourceConnectionWithResponse(dataSource, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        SearchIndexerDataSourceConnection dataSource = createTestSqlDataSourceObject();
        dataSource.setType(SearchIndexerDataSourceType.fromString("thistypedoesnotexist"));

        assertHttpResponseException(
            () -> client.createOrUpdateDataSourceConnection(dataSource),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Data source type 'thistypedoesnotexist' is not supported"
        );
    }

    @Test
    public void canUpdateDataSource() throws Exception {
        SearchIndexerDataSourceConnection initial = createTestSqlDataSourceObject();

        // Create the data source
        client.createOrUpdateDataSourceConnection(initial);
        dataSourcesToDelete.add(initial.getName());
        SearchIndexerDataSourceConnection updatedExpected = createTestSqlDataSourceObject();
        Field updatedDataSource = updatedExpected.getClass().getDeclaredField("name");
        updatedDataSource.setAccessible(true);
        updatedDataSource.set(updatedExpected, initial.getName());
        updatedExpected = createTestSqlDataSourceObject()
            .setContainer(new SearchIndexerDataContainer("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy("rowversion"))
            .setDataDeletionDetectionPolicy(new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("isDeleted"));

        SearchIndexerDataSourceConnection updatedActual = client.createOrUpdateDataSourceConnection(updatedExpected);

        updatedExpected.setConnectionString(null); // Create doesn't return connection strings.
        TestHelpers.assertObjectEquals(updatedExpected, updatedActual, false, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection response = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, true, Context.NONE)
            .getValue();

        assertFalse(CoreUtils.isNullOrEmpty(response.getETag()));
    }

    @Test
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection response = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE)
            .getValue();

        client.deleteDataSourceConnectionWithResponse(response, true, Context.NONE);

        try {
            client.deleteDataSourceConnectionWithResponse(response, true, Context.NONE);
            fail("Second call to delete with specified ETag should have failed due to non existent data source.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    @Test
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);

        SearchIndexerDataSourceConnection stale = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE)
            .getValue();

        SearchIndexerDataSourceConnection current = client.createOrUpdateDataSourceConnectionWithResponse(stale, false, Context.NONE)
            .getValue();

        try {
            client.deleteDataSourceConnectionWithResponse(stale, true, Context.NONE);
            fail("Delete specifying a stale ETag should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteDataSourceConnectionWithResponse(current, true, Context.NONE);
    }

    @Test
    public void updateDataSourceIfExistsSucceedsOnExistingResource() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection original = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSourceConnection updated = client.createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), false,
            Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection original = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSourceConnection updated = client.createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), false,
            Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        try {
            client.createOrUpdateDataSourceConnectionWithResponse(original, true, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection original = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSourceConnection updated = client.createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), false,
            Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Check eTags as expected
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    // TODO (alzimmer): Re-enable this test once live resource deployment is configured.
    //@Test
    public void createDataSourceReturnsCorrectDefinition() {
        SoftDeleteColumnDeletionDetectionPolicy deletionDetectionPolicy =
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("isDeleted")
                .setSoftDeleteMarkerValue("1");

        HighWaterMarkChangeDetectionPolicy changeDetectionPolicy =
            new HighWaterMarkChangeDetectionPolicy("fakecolumn");

        // AzureSql
        createAndValidateDataSource(createTestSqlDataSourceObject(null, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(deletionDetectionPolicy, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(null, new SqlIntegratedChangeTrackingPolicy()));
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

    private void createAndValidateDataSource(SearchIndexerDataSourceConnection expectedDataSource) {
        SearchIndexerDataSourceConnection actualDataSource = client.createOrUpdateDataSourceConnection(expectedDataSource);

        expectedDataSource.setConnectionString(null);
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");
        // we delete the data source because otherwise we will hit the quota limits during the tests
        client.deleteDataSourceConnection(actualDataSource.getName());

    }

    @Test
    public void getDataSourceReturnsCorrectDefinition() {
        createGetAndValidateDataSource(createTestBlobDataSource(null));
        createGetAndValidateDataSource(createTestTableStorageDataSource());
        createGetAndValidateDataSource(createTestSqlDataSourceObject());
        createGetAndValidateDataSource(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateDataSource(SearchIndexerDataSourceConnection expectedDataSource) {
        client.createOrUpdateDataSourceConnection(expectedDataSource);
        String dataSourceName = expectedDataSource.getName();

        // Get doesn't return connection strings.
        expectedDataSource.setConnectionString(null);

        SearchIndexerDataSourceConnection actualDataSource = client.getDataSourceConnection(dataSourceName);
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        actualDataSource = client.getDataSourceConnectionWithResponse(dataSourceName, Context.NONE)
            .getValue();
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        client.deleteDataSourceConnection(dataSourceName);
    }

    @Test
    public void getDataSourceThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getDataSourceConnection("thisdatasourcedoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "No data source with the name 'thisdatasourcedoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateDataSource() {
        SearchIndexerDataSourceConnection expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        SearchIndexerDataSourceConnection actualDataSource = client.createDataSourceConnection(expectedDataSource);
        assertNotNull(actualDataSource);
        assertEquals(expectedDataSource.getName(), actualDataSource.getName());

        Iterator<SearchIndexerDataSourceConnection> dataSources = client.listDataSourceConnections().iterator();
        assertEquals(expectedDataSource.getName(), dataSources.next().getName());
        assertFalse(dataSources.hasNext());
    }

    @Test
    public void canCreateDataSourceWithResponse() {
        SearchIndexerDataSourceConnection expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        Response<SearchIndexerDataSourceConnection> response = client
            .createDataSourceConnectionWithResponse(expectedDataSource, null);
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
        SearchIndexerDataSourceConnection initial = createTestBlobDataSource(null);
        assertEquals(initial.getConnectionString(), FAKE_STORAGE_CONNECTION_STRING);

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setConnectionString(newConnString);

        assertEquals(initial.getConnectionString(), newConnString);
    }

    SearchIndexerDataSourceConnection createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return SearchIndexerDataSources.createFromAzureBlobStorage(testResourceNamer.randomName(BLOB_DATASOURCE_TEST_NAME, 32),
            FAKE_STORAGE_CONNECTION_STRING, "fakecontainer", "/fakefolder/", FAKE_DESCRIPTION, deletionDetectionPolicy);
    }

    SearchIndexerDataSourceConnection createTestTableStorageDataSource() {
        return SearchIndexerDataSources.createFromAzureTableStorage("azs-java-test-tablestorage", FAKE_STORAGE_CONNECTION_STRING,
            "faketable", "fake query", FAKE_DESCRIPTION, null);
    }

    SearchIndexerDataSourceConnection createTestCosmosDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return SearchIndexerDataSources.createFromCosmos("azs-java-test-cosmos", FAKE_COSMOS_CONNECTION_STRING, "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark", useChangeDetection, FAKE_DESCRIPTION,
            deletionDetectionPolicy);
    }

    private void assertDataSourceEquals(SearchIndexerDataSourceConnection expect,
        SearchIndexerDataSourceConnection actual) {
        assertEquals(expect.getName(), actual.getName());
        assertEquals(expect.getDescription(), actual.getDescription());
        assertEquals(expect.getType(), actual.getType());
        assertEquals(expect.getContainer().getName(), actual.getContainer().getName());
    }
}
