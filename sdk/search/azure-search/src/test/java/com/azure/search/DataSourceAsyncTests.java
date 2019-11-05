// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.search.models.DataSource;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.models.RequestOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.UUID;

public class DataSourceAsyncTests extends DataSourceTestBase {
    private SearchServiceAsyncClient client;

    @Override
    public void createAndListDataSources() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        DataSource dataSource1 = createTestCosmosDbDataSource(null, false);
        DataSource dataSource2 = createTestSqlDataSource();

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
        // Get client
        client = getSearchServiceClientBuilder().buildAsyncClient();

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
        client = getSearchServiceClientBuilder().buildAsyncClient();
        DataSource initial = createTestBlobDataSource(null);

        // Create the data source
        client.createOrUpdateDataSource(initial).block();

        DataSource expected = updateDatasource(initial);
        DataSource actual = client.createOrUpdateDataSource(expected).block();

        removeConnectionString(expected);

        assertDataSourcesEqual(expected, actual);
    }

    @Override
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        DataSource dataSource = createTestSqlDataSource();
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
        client = getSearchServiceClientBuilder().buildAsyncClient();
        createAndValidateDataSource(createTestBlobDataSource(null));
    }


    private void createAndValidateDataSource(DataSource expectedDataSource) {
        DataSource actualDataSource = client.createOrUpdateDataSource(expectedDataSource).block();

        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));
        assertDataSourcesEqual(expectedDataSource, actualDataSource);
    }
}
