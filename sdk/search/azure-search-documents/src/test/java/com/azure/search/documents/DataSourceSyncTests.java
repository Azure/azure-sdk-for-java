// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.DataSources;
import com.azure.search.documents.indexes.SearchDataSourceClient;
import com.azure.search.documents.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.models.DataSourceCredentials;
import com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchIndexerDataContainer;
import com.azure.search.documents.models.SearchIndexerDataSource;
import com.azure.search.documents.models.SearchIndexerDataSourceType;
import com.azure.search.documents.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.documents.models.SqlIntegratedChangeTrackingPolicy;
import com.azure.search.documents.test.AccessConditionTests;
import com.azure.search.documents.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataSourceSyncTests extends SearchServiceTestBase {
    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String FAKE_STORAGE_CONNECTION_STRING =
        "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;";
    private static final String FAKE_COSMOS_CONNECTION_STRING =
        "AccountEndpoint=https://NotaRealAccount.documents.azure.com;AccountKey=fake;Database=someFakeDatabase";

    private SearchDataSourceClient client;

    // commonly used lambda definitions
    private BiFunction<SearchIndexerDataSource, AccessOptions, SearchIndexerDataSource> createOrUpdateDataSourceFunc =
        (SearchIndexerDataSource ds, AccessOptions ac) ->
            createOrUpdateDataSource(ds, ac.getOnlyIfUnchanged(), ac.getRequestOptions());

    private Supplier<SearchIndexerDataSource> newDataSourceFunc = () -> createTestBlobDataSource(null);

    private Function<SearchIndexerDataSource, SearchIndexerDataSource> mutateDataSourceFunc = (SearchIndexerDataSource ds) ->
        ds.setDescription("somethingnew");

    private BiConsumer<SearchIndexerDataSource, AccessOptions> deleteDataSourceFunc = (SearchIndexerDataSource dataSource, AccessOptions ac) ->
        client.deleteDataSourceWithResponse(dataSource,
            ac.getOnlyIfUnchanged(), ac.getRequestOptions(), Context.NONE);

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient().getDataSourceClient();
    }

    private SearchIndexerDataSource createOrUpdateDataSource(SearchIndexerDataSource datasource, Boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        return client.createOrUpdateDataSourceWithResponse(datasource, onlyIfUnchanged, requestOptions, Context.NONE)
            .getValue();
    }

    @Test
    public void canCreateAndListDataSources() {
        SearchIndexerDataSource dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSource(dataSource1);
        client.createOrUpdateDataSource(dataSource2);

        Iterator<SearchIndexerDataSource> results = client.listDataSources().iterator();

        assertEquals(dataSource1.getName(), results.next().getName());
        assertEquals(dataSource2.getName(), results.next().getName());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndListDataSourcesWithResponse() {
        SearchIndexerDataSource dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSourceWithResponse(
            dataSource1, false, new RequestOptions(), Context.NONE);
        client.createOrUpdateDataSourceWithResponse(
            dataSource2, false, new RequestOptions(), Context.NONE);

        Iterator<SearchIndexerDataSource> results = client.listDataSources("name", new RequestOptions(), Context.NONE).iterator();

        assertEquals(dataSource1.getName(), results.next().getName());
        assertEquals(dataSource2.getName(), results.next().getName());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndDeleteDatasource() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);
        client.deleteDataSource(dataSource.getName());

        assertThrows(HttpResponseException.class, () -> client.getDataSource(dataSource.getName()));
    }

    @Test
    public void deleteDataSourceIsIdempotent() {
        SearchIndexerDataSource dataSource = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> result = client.deleteDataSourceWithResponse(dataSource, false, generateRequestOptions(), Context.NONE);
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

        SearchIndexerDataSource dataSource = createTestSqlDataSourceObject();
        dataSource.setType(SearchIndexerDataSourceType.fromString("thistypedoesnotexist"));

        assertHttpResponseException(
            () -> client.createOrUpdateDataSource(dataSource),
            HttpResponseStatus.BAD_REQUEST,
            "Data source type 'thistypedoesnotexist' is not supported"
        );
    }

    @Test
    public void canUpdateDataSource() {
        SearchIndexerDataSource initial = createTestSqlDataSourceObject();

        // Create the data source
        client.createOrUpdateDataSource(initial);

        SearchIndexerDataSource updatedExpected = createTestSqlDataSourceObject()
            .setName(initial.getName())
            .setContainer(new SearchIndexerDataContainer().setName("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy()
                .setHighWaterMarkColumnName("rowversion"))
            .setDataDeletionDetectionPolicy(new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("isDeleted"));

        SearchIndexerDataSource updatedActual = client.createOrUpdateDataSource(updatedExpected);

        updatedExpected.getCredentials().setConnectionString(null); // Create doesn't return connection strings.
        TestHelpers.assertObjectEquals(updatedExpected, updatedActual, false, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests.createOrUpdateIfNotExistsSucceedsOnNoResource(createOrUpdateDataSourceFunc,
            newDataSourceFunc);
    }

    @Test
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteDataSourceFunc,
            createOrUpdateDataSourceFunc, newDataSourceFunc);
    }

    @Test
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionTests.deleteIfNotChangedWorksOnlyOnCurrentResource(deleteDataSourceFunc, newDataSourceFunc,
            createOrUpdateDataSourceFunc, BLOB_DATASOURCE_TEST_NAME);
    }

    @Test
    public void updateDataSourceIfExistsSucceedsOnExistingResource() {
        AccessConditionTests.updateIfExistsSucceedsOnExistingResource(newDataSourceFunc, createOrUpdateDataSourceFunc,
            mutateDataSourceFunc);
    }

    @Test
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() {
        AccessConditionTests.updateIfNotChangedFailsWhenResourceChanged(newDataSourceFunc, createOrUpdateDataSourceFunc,
            mutateDataSourceFunc);
    }

    @Test
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionTests.updateIfNotChangedSucceedsWhenResourceUnchanged(newDataSourceFunc,
            createOrUpdateDataSourceFunc, mutateDataSourceFunc);
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
        createAndValidateSearchIndexerDataSource(createTestSqlDataSourceObject(null, new
            SqlIntegratedChangeTrackingPolicy()));
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
        SearchIndexerDataSource actualSearchIndexerDataSource = client.createOrUpdateDataSource(expectedSearchIndexerDataSource);

        expectedSearchIndexerDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));
        TestHelpers.assertObjectEquals(expectedSearchIndexerDataSource, actualSearchIndexerDataSource, false, "etag", "@odata.etag");
        // we delete the data source because otherwise we will hit the quota limits during the tests
        client.deleteDataSource(actualSearchIndexerDataSource.getName());

    }

    @Test
    public void getDataSourceReturnsCorrectDefinition() {
        createGetAndValidateSearchIndexerDataSource(createTestBlobDataSource(null));
        createGetAndValidateSearchIndexerDataSource(createTestTableStorageDataSource());
        createGetAndValidateSearchIndexerDataSource(createTestSqlDataSourceObject());
        createGetAndValidateSearchIndexerDataSource(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateSearchIndexerDataSource(SearchIndexerDataSource expectedSearchIndexerDataSource) {
        client.createOrUpdateDataSource(expectedSearchIndexerDataSource);
        String dataSourceName = expectedSearchIndexerDataSource.getName();
        expectedSearchIndexerDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null)); // Get doesn't return connection strings.

        SearchIndexerDataSource actualSearchIndexerDataSource = client.getDataSource(dataSourceName);
        TestHelpers.assertObjectEquals(expectedSearchIndexerDataSource, actualSearchIndexerDataSource, false, "etag", "@odata.etag");

        actualSearchIndexerDataSource = client.getDataSourceWithResponse(dataSourceName, generateRequestOptions(), Context.NONE)
            .getValue();
        TestHelpers.assertObjectEquals(expectedSearchIndexerDataSource, actualSearchIndexerDataSource, false, "etag", "@odata.etag");

        client.deleteDataSource(dataSourceName);
    }

    @Test
    public void getDataSourceThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getDataSource("thisdatasourcedoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "No data source with the name 'thisdatasourcedoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateDataSource() {
        SearchIndexerDataSource expectedSearchIndexerDataSource = createTestBlobDataSource(null);
        SearchIndexerDataSource actualSearchIndexerDataSource = client.createDataSource(expectedSearchIndexerDataSource);
        assertNotNull(actualSearchIndexerDataSource);
        assertEquals(expectedSearchIndexerDataSource.getName(), actualSearchIndexerDataSource.getName());

        Iterator<SearchIndexerDataSource> dataSources = client.listDataSources().iterator();
        assertEquals(expectedSearchIndexerDataSource.getName(), dataSources.next().getName());
        assertFalse(dataSources.hasNext());
    }

    @Test
    public void canCreateDataSourceWithResponse() {
        SearchIndexerDataSource expectedSearchIndexerDataSource = createTestBlobDataSource(null);
        Response<SearchIndexerDataSource> response = client
            .createDataSourceWithResponse(expectedSearchIndexerDataSource, new RequestOptions(), null);
        assertNotNull(response);
        assertNotNull(response.getValue());
        assertEquals(expectedSearchIndexerDataSource.getName(), response.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
    }

    @Test
    public void canUpdateConnectionData() {
        // Note: since connection string is not returned when queried from the service, actually saving the
        // datasource, retrieving it and verifying the change, won't work.
        // Hence, we only validate that the properties on the local items can change.

        // Create an initial dataSource
        SearchIndexerDataSource initial = createTestBlobDataSource(null);
        assertEquals(initial.getCredentials().getConnectionString(),
            FAKE_STORAGE_CONNECTION_STRING);

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setCredentials(new DataSourceCredentials().setConnectionString(newConnString));

        assertEquals(initial.getCredentials().getConnectionString(), newConnString);
    }

    SearchIndexerDataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.createFromAzureBlobStorage(BLOB_DATASOURCE_TEST_NAME, FAKE_STORAGE_CONNECTION_STRING, "fakecontainer",
            "/fakefolder/", FAKE_DESCRIPTION, deletionDetectionPolicy);
    }

    SearchIndexerDataSource createTestTableStorageDataSource() {
        return DataSources.createFromAzureTableStorage("azs-java-test-tablestorage", FAKE_STORAGE_CONNECTION_STRING, "faketable",
            "fake query", FAKE_DESCRIPTION, null);
    }

    SearchIndexerDataSource createTestCosmosDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return DataSources.createFromCosmos("azs-java-test-cosmos", FAKE_COSMOS_CONNECTION_STRING, "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark", useChangeDetection, FAKE_DESCRIPTION,
            deletionDetectionPolicy);
    }
}
