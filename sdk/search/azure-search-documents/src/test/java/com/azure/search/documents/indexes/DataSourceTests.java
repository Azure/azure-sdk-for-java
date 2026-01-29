// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.BLOB_DATASOURCE_TEST_NAME;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DataSourceTests extends SearchTestBase {
    private static final String FAKE_DESCRIPTION = "Some data source";
    private static final String FAKE_STORAGE_CONNECTION_STRING
        = "DefaultEndpointsProtocol=https;AccountName=NotaRealAccount;AccountKey=fake;";
    private static final String FAKE_COSMOS_CONNECTION_STRING
        = "AccountEndpoint=https://NotaRealAccount.documents.azure.com;AccountKey=fake;Database=someFakeDatabase";
    public static final String FAKE_AZURE_SQL_CONNECTION_STRING
        = "Server=tcp:fakeUri,1433;Database=fakeDatabase;User ID=reader;Password=fakePassword;"
            + "Trusted_Connection=False;Encrypt=True;Connection Timeout=30;";

    private final List<String> dataSourcesToDelete = new ArrayList<>();

    private SearchIndexerAsyncClient asyncClient;
    private SearchIndexerClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();

        asyncClient = getSearchIndexerClientBuilder(false).buildAsyncClient();
        client = getSearchIndexerClientBuilder(true).buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        for (String dataSource : dataSourcesToDelete) {
            client.deleteDataSourceConnection(dataSource);
        }

    }

    @Test
    public void canCreateAndListDataSourcesSync() {
        SearchIndexerDataSourceConnection dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSourceConnection dataSource2 = createTestSqlDataSourceObject();

        Map<String, SearchIndexerDataSourceConnection> expectedDataSources = new HashMap<>();
        expectedDataSources.put(dataSource1.getName(), dataSource1);
        expectedDataSources.put(dataSource2.getName(), dataSource2);

        client.createOrUpdateDataSourceConnection(dataSource1);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdateDataSourceConnection(dataSource2);
        dataSourcesToDelete.add(dataSource2.getName());

        Map<String, SearchIndexerDataSourceConnection> actualDataSources = client.listDataSourceConnections()
            .stream()
            .collect(Collectors.toMap(SearchIndexerDataSourceConnection::getName, ds -> ds));

        compareMaps(expectedDataSources, actualDataSources, DataSourceTests::assertDataSourceEquals);
    }

    @Test
    public void canCreateAndListDataSourcesAsync() {
        SearchIndexerDataSourceConnection dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSourceConnection dataSource2 = createTestSqlDataSourceObject();

        Map<String, SearchIndexerDataSourceConnection> expectedDataSources = new HashMap<>();
        expectedDataSources.put(dataSource1.getName(), dataSource1);
        expectedDataSources.put(dataSource2.getName(), dataSource2);

        Mono<Map<String, SearchIndexerDataSourceConnection>> listMono
            = Flux.fromIterable(Arrays.asList(dataSource1, dataSource2))
                .flatMap(asyncClient::createOrUpdateDataSourceConnection)
                .doOnNext(ds -> dataSourcesToDelete.add(ds.getName()))
                .thenMany(asyncClient.listDataSourceConnections())
                .collectMap(SearchIndexerDataSourceConnection::getName);

        StepVerifier.create(listMono)
            .assertNext(actualDataSources -> compareMaps(expectedDataSources, actualDataSources,
                DataSourceTests::assertDataSourceEquals))
            .verifyComplete();
    }

    @Test
    public void canCreateAndListDataSourcesWithResponseSync() {
        SearchIndexerDataSourceConnection dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSourceConnection dataSource2 = createTestSqlDataSourceObject();

        Set<String> expectedDataSources = new HashSet<>();
        expectedDataSources.add(dataSource1.getName());
        expectedDataSources.add(dataSource2.getName());

        client.createOrUpdateDataSourceConnectionWithResponse(dataSource1, false, Context.NONE);
        dataSourcesToDelete.add(dataSource1.getName());
        client.createOrUpdateDataSourceConnectionWithResponse(dataSource2, false, Context.NONE);
        dataSourcesToDelete.add(dataSource2.getName());

        Set<String> actualDataSources
            = client.listDataSourceConnectionNames(Context.NONE).stream().collect(Collectors.toSet());

        assertEquals(expectedDataSources.size(), actualDataSources.size());
        expectedDataSources.forEach(ds -> assertTrue(actualDataSources.contains(ds), "Missing expected data source."));
    }

    @Test
    public void canCreateAndListDataSourcesWithResponseAsync() {
        SearchIndexerDataSourceConnection dataSource1 = createTestBlobDataSource(null);
        SearchIndexerDataSourceConnection dataSource2 = createTestSqlDataSourceObject();

        Set<String> expectedDataSources = new HashSet<>();
        expectedDataSources.add(dataSource1.getName());
        expectedDataSources.add(dataSource2.getName());

        Mono<Set<String>> listMono = Flux.fromIterable(Arrays.asList(dataSource1, dataSource2))
            .flatMap(ds -> asyncClient.createOrUpdateDataSourceConnectionWithResponse(ds, false))
            .doOnNext(ds -> dataSourcesToDelete.add(ds.getValue().getName()))
            .thenMany(asyncClient.listDataSourceConnectionNames())
            .collect(Collectors.toSet());

        StepVerifier.create(listMono).assertNext(actualDataSources -> {
            assertEquals(expectedDataSources.size(), actualDataSources.size());
            expectedDataSources
                .forEach(ds -> assertTrue(actualDataSources.contains(ds), "Missing expected data source."));
        }).verifyComplete();
    }

    @Test
    public void canCreateAndDeleteDatasourceSync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        client.deleteDataSourceConnection(dataSource.getName());

        assertThrows(HttpResponseException.class, () -> client.getDataSourceConnection(dataSource.getName()));
    }

    @Test
    public void canCreateAndDeleteDatasourceAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        client.deleteDataSourceConnection(dataSource.getName());

        StepVerifier.create(asyncClient.getDataSourceConnection(dataSource.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void deleteDataSourceIsIdempotentSync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> result = client.deleteDataSourceConnectionWithResponse(dataSource, false, Context.NONE);
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
    public void deleteDataSourceIsIdempotentAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        StepVerifier.create(asyncClient.deleteDataSourceConnectionWithResponse(dataSource, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();

        // Create the data source
        asyncClient.createOrUpdateDataSourceConnection(dataSource).block();

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        StepVerifier.create(asyncClient.deleteDataSourceConnectionWithResponse(dataSource, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusCode()))
            .verifyComplete();

        // Again, expect to fail
        StepVerifier.create(asyncClient.deleteDataSourceConnectionWithResponse(dataSource, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void createDataSourceFailsWithUsefulMessageOnUserErrorSyncAndAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestSqlDataSourceObject();
        dataSource.setType(SearchIndexerDataSourceType.fromString("thistypedoesnotexist"));

        assertHttpResponseException(() -> client.createOrUpdateDataSourceConnection(dataSource),
            HttpURLConnection.HTTP_BAD_REQUEST, "Data source type 'thistypedoesnotexist' is not supported");

        StepVerifier.create(asyncClient.createOrUpdateDataSourceConnection(dataSource))
            .verifyErrorSatisfies(throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_BAD_REQUEST,
                "Data source type 'thistypedoesnotexist' is not supported"));
    }

    @Test
    public void canUpdateDataSourceSync() {
        SearchIndexerDataSourceConnection initial = createTestSqlDataSourceObject();

        // Create the data source
        client.createOrUpdateDataSourceConnection(initial);
        dataSourcesToDelete.add(initial.getName());
        SearchIndexerDataSourceConnection updatedExpected = createTestSqlDataSourceObject(initial.getName(), null, null)
            .setContainer(new SearchIndexerDataContainer("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy("rowversion"))
            .setDataDeletionDetectionPolicy(
                new SoftDeleteColumnDeletionDetectionPolicy().setSoftDeleteColumnName("isDeleted"));

        SearchIndexerDataSourceConnection updatedActual = client.createOrUpdateDataSourceConnection(updatedExpected);

        updatedExpected.setConnectionString(null); // Create doesn't return connection strings.
        TestHelpers.assertObjectEquals(updatedExpected, updatedActual, false, "etag", "@odata.etag", "@odata.type");
    }

    @Test
    public void canUpdateDataSourceAsync() {
        SearchIndexerDataSourceConnection initial = createTestSqlDataSourceObject();

        // Create the data source
        asyncClient.createOrUpdateDataSourceConnection(initial).block();
        dataSourcesToDelete.add(initial.getName());

        SearchIndexerDataSourceConnection updatedExpected = createTestSqlDataSourceObject(initial.getName(), null, null)
            .setContainer(new SearchIndexerDataContainer("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy("rowversion"))
            .setDataDeletionDetectionPolicy(
                new SoftDeleteColumnDeletionDetectionPolicy().setSoftDeleteColumnName("isDeleted"));

        StepVerifier.create(asyncClient.createOrUpdateDataSourceConnection(updatedExpected)).assertNext(actual ->
        // Create doesn't return connection strings.
        assertObjectEquals(updatedExpected.setConnectionString(null), actual, false, "etag", "@odata.etag",
            "@odata.type")).verifyComplete();
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResourceSync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection response
            = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, true, Context.NONE).getValue();

        assertNotNull(response.getETag());
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResourceAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        StepVerifier.create(asyncClient.createOrUpdateDataSourceConnectionWithResponse(dataSource, true))
            .assertNext(response -> assertNotNull(response.getValue().getETag()))
            .verifyComplete();
    }

    @Test
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExistsSyncAndAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection response
            = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE).getValue();

        client.deleteDataSourceConnectionWithResponse(response, true, Context.NONE);

        try {
            client.deleteDataSourceConnectionWithResponse(response, true, Context.NONE);
            fail("Second call to delete with specified ETag should have failed due to non existent data source.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        StepVerifier.create(asyncClient.deleteDataSourceConnectionWithResponse(response, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResourceSyncAndAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);

        SearchIndexerDataSourceConnection stale
            = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE).getValue();

        SearchIndexerDataSourceConnection current
            = client.createOrUpdateDataSourceConnectionWithResponse(stale, false, Context.NONE).getValue();

        try {
            client.deleteDataSourceConnectionWithResponse(stale, true, Context.NONE);
            fail("Delete specifying a stale ETag should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        StepVerifier.create(asyncClient.deleteDataSourceConnectionWithResponse(stale, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });

        client.deleteDataSourceConnectionWithResponse(current, true, Context.NONE);
    }

    @Test
    public void updateDataSourceIfExistsSucceedsOnExistingResourceSync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection original
            = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE).getValue();

        String originalETag = original.getETag();

        SearchIndexerDataSourceConnection updated = client
            .createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), false, Context.NONE)
            .getValue();

        String updatedETag = updated.getETag();

        assertNotNull(updatedETag);
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfExistsSucceedsOnExistingResourceAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        Mono<Tuple2<String, String>> createThenUpdateMono
            = asyncClient.createOrUpdateDataSourceConnectionWithResponse(dataSource, false).flatMap(response -> {
                SearchIndexerDataSourceConnection original = response.getValue();
                String originalETag = original.getETag();

                return asyncClient
                    .createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), false)
                    .map(updated -> Tuples.of(originalETag, updated.getValue().getETag()));
            });

        StepVerifier.create(createThenUpdateMono).assertNext(etags -> {
            assertNotNull(etags.getT2());
            assertNotEquals(etags.getT1(), etags.getT2());
        }).verifyComplete();
    }

    @Test
    public void updateDataSourceIfNotChangedFailsWhenResourceChangedSyncAndAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection original
            = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE).getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSourceConnection updated = client
            .createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), false, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        try {
            client.createOrUpdateDataSourceConnectionWithResponse(original, true, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        StepVerifier.create(asyncClient.createOrUpdateDataSourceConnectionWithResponse(original, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });

        assertNotNull(originalETag);
        assertNotNull(updatedETag);
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchangedSync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        SearchIndexerDataSourceConnection original
            = client.createOrUpdateDataSourceConnectionWithResponse(dataSource, false, Context.NONE).getValue();
        String originalETag = original.getETag();

        SearchIndexerDataSourceConnection updated = client
            .createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), true, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Check eTags as expected
        assertNotNull(originalETag);
        assertNotNull(updatedETag);
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchangedAsync() {
        SearchIndexerDataSourceConnection dataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(dataSource.getName());

        Mono<Tuple2<String, String>> etagUpdatesOnChangeMono
            = asyncClient.createOrUpdateDataSourceConnectionWithResponse(dataSource, false).flatMap(response -> {
                SearchIndexerDataSourceConnection original = response.getValue();
                String originalETag = original.getETag();

                return asyncClient
                    .createOrUpdateDataSourceConnectionWithResponse(original.setDescription("an update"), true)
                    .map(updated -> Tuples.of(originalETag, updated.getValue().getETag()));
            });

        StepVerifier.create(etagUpdatesOnChangeMono).assertNext(etags -> {
            assertNotNull(etags.getT1());
            assertNotNull(etags.getT2());
            assertNotEquals(etags.getT1(), etags.getT2());
        }).verifyComplete();
    }

    // TODO (alzimmer): Re-enable this test once live resource deployment is configured.
    //@Test
    public void createDataSourceReturnsCorrectDefinition() {
        SoftDeleteColumnDeletionDetectionPolicy deletionDetectionPolicy
            = new SoftDeleteColumnDeletionDetectionPolicy().setSoftDeleteColumnName("isDeleted")
                .setSoftDeleteMarkerValue("1");

        HighWaterMarkChangeDetectionPolicy changeDetectionPolicy = new HighWaterMarkChangeDetectionPolicy("fakecolumn");

        // AzureSql
        createAndValidateDataSource(createTestSqlDataSourceObject(null, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(deletionDetectionPolicy, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(null, new SqlIntegratedChangeTrackingPolicy()));
        createAndValidateDataSource(createTestSqlDataSourceObject(deletionDetectionPolicy, changeDetectionPolicy));

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
        SearchIndexerDataSourceConnection actualDataSource
            = client.createOrUpdateDataSourceConnection(expectedDataSource);

        expectedDataSource.setConnectionString(null);
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");
        // we delete the data source because otherwise we will hit the quota limits during the tests
        client.deleteDataSourceConnection(actualDataSource.getName());

    }

    @Test
    public void getDataSourceReturnsCorrectDefinitionSync() {
        createGetAndValidateDataSourceSync(createTestBlobDataSource(null));
        createGetAndValidateDataSourceSync(createTestTableStorageDataSource());
        createGetAndValidateDataSourceSync(createTestSqlDataSourceObject());
        createGetAndValidateDataSourceSync(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateDataSourceSync(SearchIndexerDataSourceConnection expectedDataSource) {
        client.createOrUpdateDataSourceConnection(expectedDataSource);
        String dataSourceName = expectedDataSource.getName();

        // Get doesn't return connection strings.
        expectedDataSource.setConnectionString(null);

        SearchIndexerDataSourceConnection actualDataSource = client.getDataSourceConnection(dataSourceName);
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        actualDataSource = client.getDataSourceConnectionWithResponse(dataSourceName, Context.NONE).getValue();
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        client.deleteDataSourceConnection(dataSourceName);
    }

    @Test
    public void getDataSourceReturnsCorrectDefinitionAsync() {
        createGetAndValidateDataSourceAsync(createTestBlobDataSource(null));
        createGetAndValidateDataSourceAsync(createTestTableStorageDataSource());
        createGetAndValidateDataSourceAsync(createTestSqlDataSourceObject());
        createGetAndValidateDataSourceAsync(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateDataSourceAsync(SearchIndexerDataSourceConnection expectedDataSource) {
        asyncClient.createOrUpdateDataSourceConnection(expectedDataSource).block();
        String dataSourceName = expectedDataSource.getName();

        // Get doesn't return connection strings.
        expectedDataSource.setConnectionString(null);

        StepVerifier.create(asyncClient.getDataSourceConnection(dataSourceName))
            .assertNext(actualDataSource -> assertObjectEquals(expectedDataSource, actualDataSource, false, "etag",
                "@odata.etag"))
            .verifyComplete();

        StepVerifier.create(asyncClient.getDataSourceConnectionWithResponse(dataSourceName))
            .assertNext(
                response -> assertObjectEquals(expectedDataSource, response.getValue(), false, "etag", "@odata.etag"))
            .verifyComplete();

        asyncClient.deleteDataSourceConnection(dataSourceName).block();
    }

    @Test
    public void getDataSourceThrowsOnNotFoundSyncAndAsync() {
        assertHttpResponseException(() -> client.getDataSourceConnection("thisdatasourcedoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "No data source with the name 'thisdatasourcedoesnotexist' was found in service");

        StepVerifier.create(asyncClient.getDataSourceConnection("thisdatasourcedoesnotexist"))
            .verifyErrorSatisfies(exception -> verifyHttpResponseError(exception, HttpURLConnection.HTTP_NOT_FOUND,
                "No data source with the name 'thisdatasourcedoesnotexist' was found in service"));
    }

    @Test
    public void canCreateDataSourceSync() {
        SearchIndexerDataSourceConnection expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        SearchIndexerDataSourceConnection actualDataSource = client.createDataSourceConnection(expectedDataSource);

        assertEquals(expectedDataSource.getName(), actualDataSource.getName());
    }

    @Test
    public void canCreateDataSourceAsync() {
        SearchIndexerDataSourceConnection expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());

        StepVerifier.create(asyncClient.createDataSourceConnection(expectedDataSource))
            .assertNext(actualDataSource -> assertEquals(expectedDataSource.getName(), actualDataSource.getName()))
            .verifyComplete();
    }

    @Test
    public void canCreateDataSourceWithResponseSync() {
        SearchIndexerDataSourceConnection expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());
        Response<SearchIndexerDataSourceConnection> response
            = client.createDataSourceConnectionWithResponse(expectedDataSource, Context.NONE);

        assertEquals(expectedDataSource.getName(), response.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
    }

    @Test
    public void canCreateDataSourceWithResponseAsync() {
        SearchIndexerDataSourceConnection expectedDataSource = createTestBlobDataSource(null);
        dataSourcesToDelete.add(expectedDataSource.getName());

        StepVerifier.create(asyncClient.createDataSourceConnectionWithResponse(expectedDataSource))
            .assertNext(response -> {
                assertEquals(expectedDataSource.getName(), response.getValue().getName());
                assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    public void canUpdateConnectionData() {
        // Note: since connection string is not returned when queried from the service, actually saving the
        // datasource, retrieving it and verifying the change, won't work.
        // Hence, we only validate that the properties on the local items can change.

        // Create an initial dataSource
        SearchIndexerDataSourceConnection initial = createTestBlobDataSource(null);
        assertEquals(FAKE_STORAGE_CONNECTION_STRING, initial.getConnectionString());

        // tweak the connection string and verify it was changed
        String newConnString
            = "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setConnectionString(newConnString);

        assertEquals(newConnString, initial.getConnectionString());
    }

    SearchIndexerDataSourceConnection createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return SearchIndexerDataSources.createFromAzureBlobStorage(
            testResourceNamer.randomName(BLOB_DATASOURCE_TEST_NAME, 32), FAKE_STORAGE_CONNECTION_STRING,
            "fakecontainer", "/fakefolder/", FAKE_DESCRIPTION, deletionDetectionPolicy);
    }

    static SearchIndexerDataSourceConnection createTestTableStorageDataSource() {
        return SearchIndexerDataSources.createFromAzureTableStorage("azs-java-test-tablestorage",
            FAKE_STORAGE_CONNECTION_STRING, "faketable", "fake query", FAKE_DESCRIPTION, null);
    }

    static SearchIndexerDataSourceConnection
        createTestCosmosDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy, boolean useChangeDetection) {
        return SearchIndexerDataSources.createFromCosmos("azs-java-test-cosmos", FAKE_COSMOS_CONNECTION_STRING,
            "faketable", "SELECT ... FROM x where x._ts > @HighWaterMark", useChangeDetection, FAKE_DESCRIPTION,
            deletionDetectionPolicy);
    }

    private static void assertDataSourceEquals(SearchIndexerDataSourceConnection expect,
        SearchIndexerDataSourceConnection actual) {
        assertEquals(expect.getName(), actual.getName());
        assertEquals(expect.getDescription(), actual.getDescription());
        assertEquals(expect.getType(), actual.getType());
        assertEquals(expect.getContainer().getName(), actual.getContainer().getName());
    }
}
