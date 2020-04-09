// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.models.DataContainer;
import com.azure.search.documents.models.DataDeletionDetectionPolicy;
import com.azure.search.documents.models.DataSource;
import com.azure.search.documents.models.DataSourceCredentials;
import com.azure.search.documents.models.DataSourceType;
import com.azure.search.documents.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.documents.models.RequestOptions;
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

    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<DataSource, AccessOptions, DataSource> createOrUpdateDataSourceFunc =
        (DataSource ds, AccessOptions ac) ->
            createOrUpdateDataSource(ds, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<DataSource> newDataSourceFunc = () -> createTestBlobDataSource(null);

    private Function<DataSource, DataSource> mutateDataSourceFunc =
        (DataSource ds) -> ds.setDescription("somethingnew");

    private BiConsumer<String, AccessOptions> deleteDataSourceFunc = (String name, AccessOptions ac) ->
        client.deleteDataSourceWithResponse(name, ac.getAccessCondition(), ac.getRequestOptions(), Context.NONE);

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    private DataSource createOrUpdateDataSource(DataSource datasource,
        MatchConditions accessCondition,
        RequestOptions requestOptions) {
        return client.createOrUpdateDataSourceWithResponse(datasource, accessCondition, requestOptions, Context.NONE)
            .getValue();
    }

    @Test
    public void canCreateAndListDataSources() {
        DataSource dataSource1 = createTestBlobDataSource(null);
        DataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSource(dataSource1);
        client.createOrUpdateDataSource(dataSource2);

        Iterator<DataSource> results = client.listDataSources().iterator();

        assertEquals(dataSource1.getName(), results.next().getName());
        assertEquals(dataSource2.getName(), results.next().getName());
        assertFalse(results.hasNext());
    }

    @Test
    public void canCreateAndListDataSourcesWithResponse() {
        DataSource dataSource1 = createTestBlobDataSource(null);
        DataSource dataSource2 = createTestSqlDataSourceObject();

        client.createOrUpdateDataSourceWithResponse(
            dataSource1, new MatchConditions(), new RequestOptions(), Context.NONE);
        client.createOrUpdateDataSourceWithResponse(
            dataSource2, new MatchConditions(), new RequestOptions(), Context.NONE);

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
        Response<Void> result = client.deleteDataSourceWithResponse(dataSource.getName(),
            new MatchConditions(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());

        // Create the data source
        client.createOrUpdateDataSource(dataSource);

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        result = client.deleteDataSourceWithResponse(dataSource.getName(),
            new MatchConditions(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, result.getStatusCode());
        // Again, expect to fail
        result = client.deleteDataSourceWithResponse(dataSource.getName(),
            new MatchConditions(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        client = getSearchServiceClientBuilder().buildClient();

        DataSource dataSource = createTestSqlDataSourceObject();
        dataSource.setType(DataSourceType.fromString("thistypedoesnotexist"));

        assertHttpResponseException(
            () -> client.createOrUpdateDataSource(dataSource),
            HttpResponseStatus.BAD_REQUEST,
            "Data source type 'thistypedoesnotexist' is not supported"
        );
    }

    @Test
    public void canUpdateDataSource() {
        DataSource initial = createTestSqlDataSourceObject();

        // Create the data source
        client.createOrUpdateDataSource(initial);

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
    public void createOrUpdateDataSourceIfNotExistsFailsOnExistingResource() {
        AccessConditionTests.createOrUpdateIfNotExistsFailsOnExistingResource(createOrUpdateDataSourceFunc,
            newDataSourceFunc, mutateDataSourceFunc);
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests.createOrUpdateIfNotExistsSucceedsOnNoResource(createOrUpdateDataSourceFunc,
            newDataSourceFunc);
    }

    @Test
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteDataSourceFunc,
            createOrUpdateDataSourceFunc, newDataSourceFunc, BLOB_DATASOURCE_TEST_NAME);
    }

    @Test
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionTests.deleteIfNotChangedWorksOnlyOnCurrentResource(deleteDataSourceFunc, newDataSourceFunc,
            createOrUpdateDataSourceFunc, BLOB_DATASOURCE_TEST_NAME);
    }

    @Test
    public void updateDataSourceIfExistsFailsOnNoResource() {
        AccessConditionTests.updateIfExistsFailsOnNoResource(newDataSourceFunc, createOrUpdateDataSourceFunc);
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
        client = getSearchServiceClientBuilder().buildClient();

        createGetAndValidateDataSource(createTestBlobDataSource(null));
        createGetAndValidateDataSource(createTestTableStorageDataSource());
        createGetAndValidateDataSource(createTestSqlDataSourceObject());
        createGetAndValidateDataSource(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateDataSource(DataSource expectedDataSource) {
        client.createOrUpdateDataSource(expectedDataSource);
        String dataSourceName = expectedDataSource.getName();
        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null)); // Get doesn't return connection strings.

        DataSource actualDataSource = client.getDataSource(dataSourceName);
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        actualDataSource = client.getDataSourceWithResponse(dataSourceName, generateRequestOptions(), Context.NONE)
            .getValue();
        TestHelpers.assertObjectEquals(expectedDataSource, actualDataSource, false, "etag", "@odata.etag");

        client.deleteDataSource(dataSourceName);
    }

    @Test
    public void getDataSourceThrowsOnNotFound() {
        client = getSearchServiceClientBuilder().buildClient();
        assertHttpResponseException(
            () -> client.getDataSource("thisdatasourcedoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "No data source with the name 'thisdatasourcedoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateDataSource() {
        DataSource expectedDataSource = createTestBlobDataSource(null);
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
        assertEquals(initial.getCredentials().getConnectionString(),
            FAKE_STORAGE_CONNECTION_STRING);

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setCredentials(new DataSourceCredentials().setConnectionString(newConnString));

        assertEquals(initial.getCredentials().getConnectionString(), newConnString);
    }

    DataSource createTestBlobDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy) {
        return DataSources.createFromAzureBlobStorage(BLOB_DATASOURCE_TEST_NAME, FAKE_STORAGE_CONNECTION_STRING, "fakecontainer",
            "/fakefolder/", FAKE_DESCRIPTION, deletionDetectionPolicy);
    }

    DataSource createTestTableStorageDataSource() {
        return DataSources.createFromAzureTableStorage("azs-java-test-tablestorage", FAKE_STORAGE_CONNECTION_STRING, "faketable",
            "fake query", FAKE_DESCRIPTION, null);
    }

    DataSource createTestCosmosDataSource(DataDeletionDetectionPolicy deletionDetectionPolicy,
        boolean useChangeDetection) {

        return DataSources.createFromCosmos("azs-java-test-cosmos", FAKE_COSMOS_CONNECTION_STRING, "faketable",
            "SELECT ... FROM x where x._ts > @HighWaterMark", useChangeDetection, FAKE_DESCRIPTION,
            deletionDetectionPolicy);
    }
}
