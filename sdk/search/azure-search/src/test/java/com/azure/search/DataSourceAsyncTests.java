// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataSource;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.models.SqlIntegratedChangeTrackingPolicy;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.RequestOptions;
import com.azure.search.test.AccessConditionAsyncTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataSourceAsyncTests extends DataSourceTestBase {
    private SearchServiceAsyncClient client;

    // commonly used lambda definitions
    private BiFunction<DataSource,
        AccessOptions,
        Mono<DataSource>> createOrUpdateFunc =
            (DataSource ds, AccessOptions ac) ->
                createOrUpdateDataSource(ds, ac.getAccessCondition(), ac.getRequestOptions());

    private BiFunction<DataSource,
        AccessOptions,
        Mono<DataSource>> createOrUpdateWithResponseFunc =
            (DataSource ds, AccessOptions ac) ->
                createOrUpdateWithResponseDataSource(ds, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<DataSource> newDataSourceFunc =
        () -> createTestBlobDataSource(null);

    private Function<DataSource, DataSource> changeDataSourceFunc =
        (DataSource ds) -> ds.setDescription("somethingnew");

    private BiFunction<String, AccessOptions, Mono<Void>> deleteDataSourceFunc =
        (String name, AccessOptions ac) ->
            deleteDataSource(name, ac.getAccessCondition(), ac.getRequestOptions());

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    public Mono<DataSource> createOrUpdateDataSource(DataSource datasource,
                                               AccessCondition accessCondition,
                                               RequestOptions requestOptions) {
        return client.createOrUpdateDataSource(datasource, accessCondition, requestOptions);
    }

    public Mono<DataSource> createOrUpdateWithResponseDataSource(DataSource datasource,
                                                                 AccessCondition accessCondition,
                                                                 RequestOptions requestOptions) {
        return client.createOrUpdateDataSourceWithResponse(datasource, accessCondition, requestOptions, Context.NONE)
            .map(Response::getValue);
    }

    public Mono<Void> deleteDataSource(String name, AccessCondition accessCondition, RequestOptions requestOptions) {
        return client.deleteDataSource(name, accessCondition, requestOptions);
    }

    @Override
    public void createAndListDataSources() {
        DataSource dataSource1 = createTestBlobDataSource(null);
        DataSource dataSource2 = createTestSqlDataSource(null, null);

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

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        StepVerifier
            .create(client.listDataSourcesWithResponse("name", requestOptions))
            .assertNext(result -> {
                Assert.assertEquals(2, result.getItems().size());
                Assert.assertEquals(dataSource1.getName(), result.getValue().get(0).getName());
                Assert.assertEquals(dataSource2.getName(), result.getValue().get(1).getName());
            })
            .verifyComplete();
    }

    @Override
    public void deleteDataSourceIsIdempotent() {
        DataSource dataSource1 = createTestBlobDataSource(null);

        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> res = client.deleteDataSourceWithResponse(dataSource1.getName(), null, null).block();
        Assert.assertTrue(res.getStatusCode() == HttpStatus.SC_NOT_FOUND);

        // Create the data source
        client.createOrUpdateDataSource(dataSource1).block();

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        res = client.deleteDataSourceWithResponse(dataSource1.getName(), null, null).block();
        Assert.assertTrue(res.getStatusCode() == HttpStatus.SC_NO_CONTENT);
        // Again, expect to fail
        res = client.deleteDataSourceWithResponse(dataSource1.getName(), null, null).block();
        Assert.assertTrue(res.getStatusCode() == HttpStatus.SC_NOT_FOUND);
    }

    @Override
    public void canUpdateDataSource() {
        DataSource initial = createTestDataSource();

        // Create the data source
        client.createOrUpdateDataSource(initial).block();

        DataSource updatedExpected = createTestDataSource()
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

    @Override
    public void createOrUpdateDataSourceIfNotExistsFailsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsFailsOnExistingResourceAsync(
            createOrUpdateFunc,
            newDataSourceFunc,
            changeDataSourceFunc);
    }

    @Override
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateFunc,
            newDataSourceFunc);
    }

    @Override
    public void createOrUpdateDatasourceWithResponseIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateWithResponseFunc,
            newDataSourceFunc);
    }

    @Override
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfExistsWorksOnlyWhenResourceExistsAsync(
            deleteDataSourceFunc,
            createOrUpdateFunc,
            newDataSourceFunc,
            BLOB_DATASOURCE_TEST_NAME);
    }

    @Override
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfNotChangedWorksOnlyOnCurrentResourceAsync(
            deleteDataSourceFunc,
            newDataSourceFunc,
            createOrUpdateFunc,
            changeDataSourceFunc,
            BLOB_DATASOURCE_TEST_NAME);
    }

    @Override
    public void updateDataSourceIfExistsFailsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfExistsFailsOnNoResourceAsync(
            newDataSourceFunc,
            createOrUpdateFunc);
    }

    @Override
    public void updateDataSourceIfExistsSucceedsOnExistingResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfExistsSucceedsOnExistingResourceAsync(
            newDataSourceFunc,
            createOrUpdateFunc,
            changeDataSourceFunc);
    }

    @Override
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfNotChangedFailsWhenResourceChangedAsync(
            newDataSourceFunc,
            createOrUpdateFunc,
            changeDataSourceFunc);
    }

    @Override
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.updateIfNotChangedSucceedsWhenResourceUnchangedAsync(
            newDataSourceFunc,
            createOrUpdateFunc,
            changeDataSourceFunc);
    }

    @Override
    public void existsReturnsFalseForNonExistingDatasource() {
        StepVerifier
            .create(client.datasourceExists("inExistentDataSourceName"))
            .assertNext(Assert::assertFalse)
            .verifyComplete();
    }

    @Override
    public void existsReturnsTrueForExistingDatasource() {
        DataSource dataSource = createTestDataSource();
        client.createOrUpdateDataSource(dataSource).block();

        StepVerifier
            .create(client.datasourceExists(dataSource.getName()))
            .assertNext(Assert::assertTrue)
            .verifyComplete();
    }

    @Override
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        DataSource dataSource = createTestSqlDataSource(null, null);
        dataSource.setType(DataSourceType.fromString("thistypedoesnotexist"));

        StepVerifier
            .create(client.createOrUpdateDataSource(dataSource))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error)
                    .getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains("Data source type '' is not supported"));
            });
    }

    @Override
    public void createDataSourceReturnsCorrectDefinition() {
        SoftDeleteColumnDeletionDetectionPolicy deletionDetectionPolicy =
            new SoftDeleteColumnDeletionDetectionPolicy()
                .setSoftDeleteColumnName("isDeleted")
                .setSoftDeleteMarkerValue("1");

        HighWaterMarkChangeDetectionPolicy changeDetectionPolicy =
            new HighWaterMarkChangeDetectionPolicy()
                .setHighWaterMarkColumnName("fakecolumn");

        // AzureSql
        createAndValidateDataSource(createTestSqlDataSource(null, null));
        createAndValidateDataSource(createTestSqlDataSource(deletionDetectionPolicy, null));
        createAndValidateDataSource(createTestSqlDataSource(null, new SqlIntegratedChangeTrackingPolicy()));
        createAndValidateDataSource(createTestSqlDataSource(deletionDetectionPolicy, changeDetectionPolicy));

        // CosmosDB
        createAndValidateDataSource(createTestCosmosDbDataSource(null, false));
        createAndValidateDataSource(createTestCosmosDbDataSource(null, true));
        createAndValidateDataSource(createTestCosmosDbDataSource(deletionDetectionPolicy, false));
        createAndValidateDataSource(createTestCosmosDbDataSource(deletionDetectionPolicy, false));

        // Azure Blob Storage
        createAndValidateDataSource(createTestBlobDataSource(null));
        createAndValidateDataSource(createTestBlobDataSource(deletionDetectionPolicy));

        // Azure Table Storage
        createAndValidateDataSource(createTestTableStorageDataSource(null));
        createAndValidateDataSource(createTestBlobDataSource(deletionDetectionPolicy));
    }


    private void createAndValidateDataSource(DataSource expectedDataSource) {
        DataSource actualDataSource = client.createOrUpdateDataSource(expectedDataSource).block();

        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));
        assertDataSourcesEqual(expectedDataSource, actualDataSource);
        // we delete the datasource because otherwise we will hit the quota limits during the tests
        client.deleteDataSource(actualDataSource.getName()).block();
    }

    @Override
    public void getDataSourceReturnsCorrectDefinition() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        createGetAndValidateDataSource(createTestBlobDataSource(null));
        createGetAndValidateDataSource(createTestTableStorageDataSource(null));
        createGetAndValidateDataSource(createTestSqlDataSource(null, null));
        createGetAndValidateDataSource(createTestCosmosDbDataSource(null, false));
    }

    private void createGetAndValidateDataSource(DataSource expectedDataSource) {
        client.createOrUpdateDataSource(expectedDataSource).block();
        String dataSourceName = expectedDataSource.getName();
        DataSource actualDataSource = client.getDataSource(dataSourceName).block();

        expectedDataSource.setCredentials(
            new DataSourceCredentials().setConnectionString(null)); // Get doesn't return connection strings.
        assertDataSourcesEqual(expectedDataSource, actualDataSource);

        client.deleteDataSource(dataSourceName).block();
    }

    @Override
    public void getDataSourceThrowsOnNotFound() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Mono<DataSource> futureDataSource = client.getDataSource("thisdatasourcedoesnotexist");
        StepVerifier
            .create(futureDataSource)
            .verifyErrorSatisfies(
                error -> {
                    Assert.assertEquals(HttpResponseException.class, error.getClass());
                    Assert.assertEquals(
                        HttpResponseStatus.NOT_FOUND.code(),
                        ((HttpResponseException) error).getResponse().getStatusCode());
                }
            );
    }
}
