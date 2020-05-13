// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.SearchIndexerDataSourceClient;
import com.azure.search.documents.indexes.SearchIndexerDataSources;
import com.azure.search.documents.indexes.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.DataSourceCredentials;
import com.azure.search.documents.indexes.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.indexes.models.RequestOptions;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSource;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.indexes.models.SqlIntegratedChangeTrackingPolicy;
import com.azure.search.documents.models.SearchErrorException;
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
    //private SearchServiceClient client;
    private SearchIndexerDataSourceClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient().getSearchIndexerDataSourceClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        for (String dataSource : dataSourcesToDelete) {
            client.delete(dataSource);
        }

    }

    @Test
    public void canCreateAndListDataSources() {
        SearchIndexerDataSource dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdate(dataSource1);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdate(dataSource2);
        dataSourcesToDelete.add(dataSource2.getName());

        Iterator<SearchIndexerDataSource> results = client.listDataSources().iterator();

        assertEquals(dataSource1.getName(), results.next().getName());
        assertEquals(dataSource2.getName(), results.next().getName());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndListDataSourcesWithResponse() {
        SearchIndexerDataSource dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateWithResponse(dataSource1, false, new RequestOptions(), Context.NONE);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdateWithResponse(dataSource2, false, new RequestOptions(), Context.NONE);
        dataSourcesToDelete.add(dataSource2.getName());

        Iterator<SearchIndexerDataSource> results =
            client.listDataSourceNames(new RequestOptions(), Context.NONE).iterator();

        assertEquals(dataSource1.getName(), results.next().getName());
        assertEquals(dataSource2.getName(), results.next().getName());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndDeleteDatasource() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);
        client.delete(dataSource.getName());

        assertThrows(HttpResponseException.class, () -> client.getDataSource(dataSource.getName()));
    }

    @Test
    public void deleteDataSourceIsIdempotent() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> result = client.deleteWithResponse(dataSource, false, generateRequestOptions(),
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Create the data source
        client.createOrUpdate(dataSource);

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        result = client.deleteWithResponse(dataSource, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());
        // Again, expect to fail
        result = client.deleteWithResponse(dataSource, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSource.setType(SearchIndexerDataSourceType.fromString("thistypedoesnotexist"));

        assertHttpResponseException(
            () -> client.createOrUpdate(dataSource),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Data source type 'thistypedoesnotexist' is not supported"
        );
    }

    @Test
    public void canUpdateDataSource() {
        SearchIndexerDataSource initial = createTestSqlDataSourceObject();

        // Create the data source
        client.createOrUpdate(initial);
        dataSourcesToDelete.add(initial.getName());

        SearchIndexerDataSource updatedExpected = createTestSqlDataSourceObject()
            .setName(initial.getName())
            .setContainer(new SearchIndexerDataContainer().setName("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy()
                .setHighWaterMarkColumnName("rowversion"))
            .setDataDeletionDetectionPolicy(new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("isDeleted"));

        SearchIndexerDataSource updatedActual = client.createOrUpdate(updatedExpected);

        updatedExpected.getCredentials().setConnectionString(null); // Create doesn't return connection strings.
        TestHelpers.assertObjectEquals(updatedExpected, updatedActual, false, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSource response = client.createOrUpdateWithResponse(dataSource, true, null, Context.NONE)
            .getValue();

        assertFalse(CoreUtils.isNullOrEmpty(response.getETag()));
    }

    @Test
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSource response = client.createOrUpdateWithResponse(dataSource, false, null, Context.NONE)
            .getValue();

        client.deleteWithResponse(response, true, null, Context.NONE);

        try {
            client.deleteWithResponse(response, true, null, Context.NONE);
            fail("Second call to delete with specified ETag should have failed due to non existent data source.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    @Test
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);

        SearchIndexerDataSource stale = client.createOrUpdateWithResponse(dataSource, false, null, Context.NONE)
            .getValue();

        SearchIndexerDataSource current = client.createOrUpdateWithResponse(stale, false, null, Context.NONE)
            .getValue();

        try {
            client.deleteWithResponse(stale, true, null, Context.NONE);
            fail("Delete specifying a stale ETag should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteWithResponse(current, true, null, Context.NONE);
    }

    @Test
    public void updateDataSourceIfExistsSucceedsOnExistingResource() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSource original = client.createOrUpdateWithResponse(dataSource, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSource updated = client.createOrUpdateWithResponse(original.setDescription("an update"), false,
            null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSource original = client.createOrUpdateWithResponse(dataSource, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSource updated = client.createOrUpdateWithResponse(original.setDescription("an update"), false,
            null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        try {
            client.createOrUpdateWithResponse(original, true, null, Context.NONE);
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
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSource original = client.createOrUpdateWithResponse(dataSource, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSource updated = client.createOrUpdateWithResponse(original.setDescription("an update"), false,
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
        createAndValidateSearchIndexerDataSource(createTestSqlDataSourceObject(null, null));
        createAndValidateSearchIndexerDataSource(createTestSqlDataSourceObject(deletionDetectionPolicy, null));
        createAndValidateSearchIndexerDataSource(createTestSqlDataSourceObject(null, new SqlIntegratedChangeTrackingPolicy()));
        createAndValidateSearchIndexerDataSource(createTestSqlDataSourceObject(deletionDetectionPolicy,
            changeDetectionPolicy));

        // Cosmos
        createAndValidateSearchIndexerDataSource(createTestCosmosDataSource(null, false));
        createAndValidateSearchIndexerDataSource(createTestCosmosDataSource(null, true));
        createAndValidateSearchIndexerDataSource(createTestCosmosDataSource(deletionDetectionPolicy, false));
        createAndValidateSearchIndexerDataSource(createTestCosmosDataSource(deletionDetectionPolicy, false));

        // Azure Blob Storage
        createAndValidateSearchIndexerDataSource(createTestBlobDataSource(null));
        createAndValidateSearchIndexerDataSource(createTestBlobDataSource(deletionDetectionPolicy));

        // Azure Table Storage
        createAndValidateSearchIndexerDataSource(createTestTableStorageDataSource());
        createAndValidateSearchIndexerDataSource(createTestBlobDataSource(deletionDetectionPolicy));
    }

    private void createAndValidateSearchIndexerDataSource(SearchIndexerDataSource expectedSearchIndexerDataSource) {
        SearchIndexerDataSource actualSearchIndexerDataSource = client.createOrUpdate(expectedSearchIndexerDataSource);

        expectedSearchIndexerDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));
        TestHelpers.assertObjectEquals(expectedSearchIndexerDataSource, actualSearchIndexerDataSource, false, "etag", "@odata.etag");
        // we delete the data source because otherwise we will hit the quota limits during the tests
        client.delete(actualSearchIndexerDataSource.getName());

    }

    @Test
    public void getDataSourceReturnsCorrectDefinition() {
        createGetAndValidateDataSource(createTestBlobDataSource(null));
        createGetAndValidateDataSource(createTestTableStorageDataSource());
        createGetAndValidateDataSource(createTestSqlDataSourceObject());
        createGetAndValidateDataSource(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateDataSource(SearchIndexerDataSource expectedDataSource) {
        client.createOrUpdate(expectedDataSource);
        String dataSourceName = expectedDataSource.getName();

        // Get doesn't return connection strings.
        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));

        SearchIndexerDataSource actualSearchIndexerDataSource = client.getDataSource(dataSourceName);
        TestHelpers.assertObjectEquals(expectedDataSource, actualSearchIndexerDataSource, false, "etag", "@odata.etag");

        actualSearchIndexerDataSource = client.getDataSourceWithResponse(dataSourceName, generateRequestOptions(), Context.NONE)
            .getValue();
        TestHelpers.assertObjectEquals(expectedDataSource, actualSearchIndexerDataSource, false, "etag", "@odata.etag");

        client.delete(dataSourceName);
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
        SearchIndexerDataSource expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        SearchIndexerDataSource actualDataSource = client.create(expectedDataSource);
        assertNotNull(actualDataSource);
        assertEquals(expectedDataSource.getName(), actualDataSource.getName());

        Iterator<SearchIndexerDataSource> dataSources = client.listDataSources().iterator();
        assertEquals(expectedDataSource.getName(), dataSources.next().getName());
        assertFalse(dataSources.hasNext());
    }

    @Test
    public void canCreateDataSourceWithResponse() {
        SearchIndexerDataSource expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        Response<SearchIndexerDataSource> response = client
            .createWithResponse(expectedDataSource, new RequestOptions(), null);
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
        SearchIndexerDataSource initial = createTestBlobDataSource(null);
        assertEquals(initial.getCredentials().getConnectionString(), FAKE_STORAGE_CONNECTION_STRING);

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setCredentials(new DataSourceCredentials().setConnectionString(newConnString));

        assertEquals(initial.getCredentials().getConnectionString(), newConnString);
    }

    SearchIndexerDataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return SearchIndexerDataSources.createFromAzureBlobStorage(testResourceNamer.randomName(BLOB_DATASOURCE_TEST_NAME, 32),
            FAKE_STORAGE_CONNECTION_STRING, "fakecontainer", "/fakefolder/", FAKE_DESCRIPTION, deletionDetectionPolicy);
    }

    SearchIndexerDataSource createTestTableStorageDataSource() {
        return SearchIndexerDataSources.createFromAzureTableStorage("azs-java-test-tablestorage", FAKE_STORAGE_CONNECTION_STRING,
            "faketable", "fake query", FAKE_DESCRIPTION, null);
    }

    SearchIndexerDataSource createTestCosmosDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return SearchIndexerDataSources.createFromCosmos("azs-java-test-cosmos", FAKE_COSMOS_CONNECTION_STRING, "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark", useChangeDetection, FAKE_DESCRIPTION,
            deletionDetectionPolicy);
    }
}
