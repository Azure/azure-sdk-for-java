// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.models.SqlIntegratedChangeTrackingPolicy;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DataSourceAsyncTests extends DataSourceTestBase {
    private SearchServiceAsyncClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
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
                assertEquals(2, result.size());
                assertEquals(dataSource1.getName(), result.get(0).getName());
                assertEquals(dataSource2.getName(), result.get(1).getName());
            })
            .verifyComplete();

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        StepVerifier
            .create(client.listDataSourcesWithResponse("name", requestOptions))
            .assertNext(result -> {
                assertEquals(2, result.getItems().size());
                assertEquals(dataSource1.getName(), result.getValue().get(0).getName());
                assertEquals(dataSource2.getName(), result.getValue().get(1).getName());
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
        DataSource initial = createTestBlobDataSource(null);

        // Create the data source
        client.createOrUpdateDataSource(initial).block();

        DataSource expected = updateDatasource(initial);
        DataSource actual = client.createOrUpdateDataSource(expected).block();

        removeConnectionString(expected);

        assertDataSourcesEqual(expected, actual);
    }

    @Override
    public void createOrUpdateDataSourceIfNotExistsFailsOnExistingResource() {
        // Create a data source
        DataSource initial = createTestBlobDataSource(null);
        client.createOrUpdateDataSource(initial).block();

        // Create another data source with the same name
        DataSource another = createTestBlobDataSource(null);

        StepVerifier
            .create(client.createOrUpdateDataSource(
                another.getName(),
                another,
                null,
                generateIfNotExistsAccessCondition(),
                null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(
                    HttpResponseStatus.PRECONDITION_FAILED.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage()
                    .contains("The precondition given in one of the request headers evaluated to false"));
            });
    }

    @Override
    public void createOrUpdateIfNotExistsSucceedsOnNoResource() {
        // Create a data source
        DataSource dataSource = createTestBlobDataSource(null);

        StepVerifier
            .create(client.createOrUpdateDataSource(
                dataSource.getName(),
                dataSource,
                null,
                generateIfNotExistsAccessCondition(),
                null))
            .assertNext(r -> Assert.assertTrue(StringUtils.isNotBlank(r.getETag())))
            .verifyComplete();
    }

    @Override
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        DataSource dataSource = createTestSqlDataSource(null, null);
        dataSource.setType(DataSourceType.fromString("thistypedoesnotexist"));

        StepVerifier
            .create(client.createOrUpdateDataSource(dataSource))
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error)
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

        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null)); // Get doesn't return connection strings.
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
                    assertEquals(HttpResponseException.class, error.getClass());
                    assertEquals(HttpResponseStatus.NOT_FOUND.code(), ((HttpResponseException) error).getResponse().getStatusCode());
                }
            );
    }
}
