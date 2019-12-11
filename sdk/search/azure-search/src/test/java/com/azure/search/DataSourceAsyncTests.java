// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.models.SqlIntegratedChangeTrackingPolicy;
import com.azure.search.test.AccessConditionAsyncTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataSourceAsyncTests extends DataSourceTestBase {
    private SearchServiceAsyncClient client;

    // commonly used lambda definitions
    private BiFunction<DataSource,
        AccessOptions,
        Mono<DataSource>> createOrUpdateDataSourceFunc =
            (DataSource ds, AccessOptions ac) ->
                createOrUpdateDataSource(ds, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<DataSource> newDataSourceFunc =
        () -> createTestBlobDataSource(null);

    private Function<DataSource, DataSource> mutateDataSourceFunc =
        (DataSource ds) -> ds.setDescription("somethingnew");

    private BiFunction<String, AccessOptions, Mono<Void>> deleteDataSourceFunc =
        (String name, AccessOptions ac) ->
            deleteDataSource(name, ac.getAccessCondition(), ac.getRequestOptions());

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    private Mono<DataSource> createOrUpdateDataSource(DataSource dataSource,
                                                      AccessCondition accessCondition,
                                                      RequestOptions requestOptions) {
        return client.createOrUpdateDataSourceWithResponse(dataSource, accessCondition, requestOptions)
            .map(Response::getValue);
    }

    private Mono<Void> deleteDataSource(String name, AccessCondition accessCondition, RequestOptions requestOptions) {
        return client.deleteDataSourceWithResponse(name, accessCondition, requestOptions).flatMap(FluxUtil::toMono);
    }

    @Test
    public void createAndListDataSources() {
        DataSource dataSource1 = createTestBlobDataSource(null);
        DataSource dataSource2 = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);

        client.createOrUpdateDataSource(dataSource1).block();
        client.createOrUpdateDataSource(dataSource2).block();

        PagedFlux<DataSource> results = client.listDataSources();

        StepVerifier
            .create(results.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(dataSource1.getName(), result.get(0).getName());
                Assert.assertEquals(dataSource2.getName(), result.get(1).getName());
            })
            .verifyComplete();

        results = client.listDataSources("name", generateRequestOptions());

        StepVerifier
            .create(results.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(dataSource1.getName(), result.get(0).getName());
                Assert.assertEquals(dataSource2.getName(), result.get(1).getName());
            })
            .verifyComplete();
    }

    @Test
    public void deleteDataSourceIsIdempotent() {
        DataSource dataSource1 = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> res = client.deleteDataSourceWithResponse(dataSource1.getName(), new AccessCondition(), generateRequestOptions()).block();
        assert res != null;
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, res.getStatusCode());

        // Create the data source
        client.createOrUpdateDataSource(dataSource1).block();

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        res = client.deleteDataSourceWithResponse(dataSource1.getName(), new AccessCondition(), generateRequestOptions()).block();
        assert res != null;
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, res.getStatusCode());

        // Again, expect to fail
        res = client.deleteDataSourceWithResponse(dataSource1.getName(), new AccessCondition(), generateRequestOptions()).block();
        assert res != null;
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, res.getStatusCode());
    }

    @Test
    public void canUpdateDataSource() {
        DataSource initial = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);

        // Create the data source
        client.createOrUpdateDataSource(initial).block();

        DataSource updatedExpected = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME)
            .setName(initial.getName())
            .setContainer(new DataContainer().setName("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("rowversion"))
            .setDataDeletionDetectionPolicy(new SoftDeleteColumnDeletionDetectionPolicy().setSoftDeleteColumnName("isDeleted"));

        StepVerifier.create(client.createOrUpdateDataSource(updatedExpected))
            .assertNext(updatedActual -> {
                updatedExpected.getCredentials().setConnectionString(null); // Create doesn't return connection strings.
                assertDataSourcesEqual(updatedExpected, updatedActual);
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateDataSourceIfNotExistsFailsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsFailsOnExistingResourceAsync(
            createOrUpdateDataSourceFunc,
            newDataSourceFunc,
            mutateDataSourceFunc);
    }

    @Test
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateDataSourceFunc,
            newDataSourceFunc);
    }

    @Test
    public void canCreateAndDeleteDatasource() {
        DataSource dataSource = createTestBlobDataSource(null);
        client.deleteDataSource(dataSource.getName()).block();

        StepVerifier
            .create(client.dataSourceExists(dataSource.getName()))
            .assertNext(Assert::assertFalse)
            .verifyComplete();
    }

    @Test
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfExistsWorksOnlyWhenResourceExistsAsync(
            deleteDataSourceFunc,
            createOrUpdateDataSourceFunc,
            newDataSourceFunc,
            BLOB_DATASOURCE_TEST_NAME);
    }

    @Test
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfNotChangedWorksOnlyOnCurrentResourceAsync(
            deleteDataSourceFunc,
            newDataSourceFunc,
            createOrUpdateDataSourceFunc,
            mutateDataSourceFunc,
            BLOB_DATASOURCE_TEST_NAME);
    }

    @Test
    public void updateDataSourceIfExistsFailsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfExistsFailsOnNoResourceAsync(
            newDataSourceFunc,
            createOrUpdateDataSourceFunc);
    }

    @Test
    public void updateDataSourceIfExistsSucceedsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfExistsSucceedsOnExistingResourceAsync(
            newDataSourceFunc,
            createOrUpdateDataSourceFunc,
            mutateDataSourceFunc);
    }

    @Test
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfNotChangedFailsWhenResourceChangedAsync(
            newDataSourceFunc,
            createOrUpdateDataSourceFunc,
            mutateDataSourceFunc);
    }

    @Test
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfNotChangedSucceedsWhenResourceUnchangedAsync(
            newDataSourceFunc,
            createOrUpdateDataSourceFunc,
            mutateDataSourceFunc);
    }

    @Test
    public void existsReturnsFalseForNonExistingDatasource() {
        StepVerifier
            .create(client.dataSourceExists("inExistentDataSourceName"))
            .assertNext(Assert::assertFalse)
            .verifyComplete();
    }

    @Test
    public void existsReturnsTrueForExistingDatasource() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME);
        client.createOrUpdateDataSource(dataSource).block();

        StepVerifier
            .create(client.dataSourceExists(dataSource.getName()))
            .assertNext(Assert::assertTrue)
            .verifyComplete();

        StepVerifier
            .create(client.dataSourceExistsWithResponse(dataSource.getName(), generateRequestOptions()))
            .assertNext(res -> Assert.assertTrue(res.getValue()))
            .verifyComplete();
    }

    @Test
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        DataSource dataSource = createTestSqlDataSourceObject(SQL_DATASOURCE_NAME)
            .setType(DataSourceType.fromString("thistypedoesnotexist"));

        assertHttpResponseExceptionAsync(
            client.createOrUpdateDataSource(dataSource),
            HttpResponseStatus.BAD_REQUEST,
            "Data source type '' is not supported"
        );
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
        createAndValidateDataSource(createTestSqlDataSourceObject(SQL_DATASOURCE_NAME, null, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(SQL_DATASOURCE_NAME, deletionDetectionPolicy, null));
        createAndValidateDataSource(createTestSqlDataSourceObject(SQL_DATASOURCE_NAME, null, new SqlIntegratedChangeTrackingPolicy()));
        createAndValidateDataSource(createTestSqlDataSourceObject(SQL_DATASOURCE_NAME, deletionDetectionPolicy, changeDetectionPolicy));

        // Cosmos
        createAndValidateDataSource(createTestCosmosDataSource(null, false));
        createAndValidateDataSource(createTestCosmosDataSource(null, true));
        createAndValidateDataSource(createTestCosmosDataSource(deletionDetectionPolicy, false));
        createAndValidateDataSource(createTestCosmosDataSource(deletionDetectionPolicy, false));

        // Azure Blob Storage
        createAndValidateDataSource(createTestBlobDataSource(null));
        createAndValidateDataSource(createTestBlobDataSource(deletionDetectionPolicy));

        // Azure Table Storage
        createAndValidateDataSource(createTestTableStorageDataSource(null));
        createAndValidateDataSource(createTestBlobDataSource(deletionDetectionPolicy));
    }


    private void createAndValidateDataSource(DataSource expectedDataSource) {
        DataSource actualDataSource = client.createOrUpdateDataSource(expectedDataSource).block();
        assert actualDataSource != null;

        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));
        assertDataSourcesEqual(expectedDataSource, actualDataSource);
        // we delete the data source because otherwise we will hit the quota limits during the tests
        client.deleteDataSource(actualDataSource.getName()).block();
    }

    @Test
    public void getDataSourceReturnsCorrectDefinition() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        createGetAndValidateDataSource(createTestBlobDataSource(null));
        createGetAndValidateDataSource(createTestTableStorageDataSource(null));
        createGetAndValidateDataSource(createTestSqlDataSourceObject(SQL_DATASOURCE_NAME));
        createGetAndValidateDataSource(createTestCosmosDataSource(null, false));
    }

    private void createGetAndValidateDataSource(DataSource expectedDataSource) {
        client.createOrUpdateDataSource(expectedDataSource).block();
        String dataSourceName = expectedDataSource.getName();
        // Get doesn't return connection strings.
        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));

        DataSource actualDataSource = client.getDataSource(dataSourceName).block();
        assert actualDataSource != null;
        assertDataSourcesEqual(expectedDataSource, actualDataSource);

        actualDataSource = client.getDataSourceWithResponse(dataSourceName, generateRequestOptions()).block().getValue();
        assert actualDataSource != null;
        assertDataSourcesEqual(expectedDataSource, actualDataSource);

        client.deleteDataSource(dataSourceName).block();
    }

    @Test
    public void getDataSourceThrowsOnNotFound() {
        client = getSearchServiceClientBuilder().buildAsyncClient();
        assertHttpResponseExceptionAsync(
            client.getDataSource("thisdatasourcedoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "No data source with the name 'thisdatasourcedoesnotexist' was found in service"
        );
    }

    @Test
    public void canUpdateConnectionData() {
        // Note: since connection string is not returned when queried from the service, actually saving the
        // datasource, retrieving it and verifying the change, won't work.
        // Hence, we only validate that the properties on the local items can change.

        // Create an initial datasource
        DataSource initial = createTestBlobDataSource(null);
        Assert.assertEquals(initial.getCredentials().getConnectionString(), FAKE_STORAGE_CONNECTION_STRING);

        // tweak the connection string and verify it was changed
        String newConnString =
            "DefaultEndpointsProtocol=https;AccountName=NotaRealYetDifferentAccount;AccountKey=AnotherFakeKey;";
        initial.setCredentials(new DataSourceCredentials().setConnectionString(newConnString));

        Assert.assertEquals(initial.getCredentials().getConnectionString(), newConnString);
    }

    @Override
    public void canCreateDataSource() {
        DataSource expectedDataSource = createTestBlobDataSource(null);

        StepVerifier.create(client.createDataSource(expectedDataSource))
            .assertNext(actualDataSource -> {
                Assert.assertNotNull(actualDataSource);
                Assert.assertEquals(expectedDataSource.getName(), actualDataSource.getName());
            })
            .verifyComplete();

        PagedFlux<DataSource> dataSourcesList = client.listDataSources();
        StepVerifier.create(dataSourcesList.collectList())
            .assertNext(result -> {
                Assert.assertNotNull(result);
                Assert.assertEquals(1, result.size());
                Assert.assertEquals(expectedDataSource.getName(), result.get(0).getName());
            })
            .verifyComplete();
    }

    @Override
    public void canCreateDataSourceWithResponse() {
        DataSource expectedDataSource = createTestBlobDataSource(null);
        StepVerifier.create(client.createDataSourceWithResponse(expectedDataSource, new RequestOptions(), null))
            .assertNext(response -> {
                Assert.assertNotNull(response);
                Assert.assertNotNull(response.getValue());
                Assert.assertEquals(expectedDataSource.getName(), response.getValue().getName());
                Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusCode());
            })
            .verifyComplete();
    }
}
