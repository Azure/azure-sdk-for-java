// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataSource;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.models.SqlIntegratedChangeTrackingPolicy;
import com.azure.search.models.DataSourceCredentials;
import com.azure.search.models.DataSourceType;
import com.azure.search.test.AccessConditionTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataSourceSyncTests extends DataSourceTestBase {
    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<DataSource,
        AccessOptions,
        DataSource> createOrUpdateFunc =
            (DataSource ds, AccessOptions ac) ->
                createOrUpdateDataSource(ds, ac.getAccessCondition(), ac.getRequestOptions());

    private BiFunction<DataSource,
        AccessOptions,
        DataSource> createOrUpdateWithResponseFunc =
            (DataSource ds, AccessOptions ac) ->
                createOrUpdateWithResponseDataSource(ds, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<DataSource> newDataSourceFunc =
        () -> createTestBlobDataSource(null);

    private Function<DataSource, DataSource> changeDataSourceFunc =
        (DataSource ds) -> ds.setDescription("somethingnew");

    private BiConsumer<String, AccessOptions> deleteDataSourceFunc =
        (String name, AccessOptions ac) ->
            deleteDataSource(name, ac.getAccessCondition(), ac.getRequestOptions());

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    public DataSource createOrUpdateDataSource(DataSource datasource,
                                               AccessCondition accessCondition,
                                               RequestOptions requestOptions) {
        return client.createOrUpdateDataSource(datasource, accessCondition, requestOptions);
    }

    public DataSource createOrUpdateWithResponseDataSource(DataSource datasource,
                                                           AccessCondition accessCondition,
                                                           RequestOptions requestOptions) {
        return client.createOrUpdateDataSourceWithResponse(datasource, accessCondition, requestOptions, Context.NONE)
            .getValue();
    }

    public void deleteDataSource(String name, AccessCondition accessCondition, RequestOptions requestOptions) {
        client.deleteDataSource(name, accessCondition, requestOptions);
    }

    @Override
    public void createAndListDataSources() {
        DataSource dataSource1 = createTestBlobDataSource(null);
        DataSource dataSource2 = createTestSqlDataSource(null, null);

        client.createOrUpdateDataSource(dataSource1);
        client.createOrUpdateDataSource(dataSource2);

        PagedIterable<DataSource> results = client.listDataSources();
        List<DataSource> resultList = results.stream().collect(Collectors.toList());

        Assert.assertEquals(2, resultList.size());
        Assert.assertEquals(dataSource1.getName(), resultList.get(0).getName());
        Assert.assertEquals(dataSource2.getName(), resultList.get(1).getName());

        Context context = new Context("key", "value");

        PagedResponse<DataSource> listResponse = client.listDataSourcesWithResponse("name",
            generateRequestOptions(), context);
        resultList = listResponse.getItems();

        Assert.assertEquals(2, resultList.size());
        Assert.assertEquals(dataSource1.getName(), resultList.get(0).getName());
        Assert.assertEquals(dataSource2.getName(), resultList.get(1).getName());
    }

    @Override
    public void deleteDataSourceIsIdempotent() {
        DataSource dataSource = createTestBlobDataSource(null);

        Context context = new Context("key", "value");
        // Try to delete before the data source exists, expect a NOT FOUND return status code
        Response<Void> result = client.deleteDataSourceWithResponse(dataSource.getName(),
            new AccessCondition(), generateRequestOptions(), context);
        Assert.assertTrue(result.getStatusCode() == HttpStatus.SC_NOT_FOUND);

        // Create the data source
        client.createOrUpdateDataSource(dataSource);

        // Delete twice, expect the first to succeed (with NO CONTENT status code) and the second to return NOT FOUND
        result = client.deleteDataSourceWithResponse(dataSource.getName(),
            new AccessCondition(), generateRequestOptions(), context);
        Assert.assertTrue(result.getStatusCode() == HttpStatus.SC_NO_CONTENT);
        // Again, expect to fail
        result = client.deleteDataSourceWithResponse(dataSource.getName(),
            new AccessCondition(), generateRequestOptions(), context);
        Assert.assertTrue(result.getStatusCode() == HttpStatus.SC_NOT_FOUND);
    }

    @Override
    public void createDataSourceFailsWithUsefulMessageOnUserError() {
        client = getSearchServiceClientBuilder().buildClient();

        DataSource dataSource = createTestSqlDataSource(null, null);
        dataSource.setType(DataSourceType.fromString("thistypedoesnotexist"));

        try {
            client.createOrUpdateDataSource(dataSource);
        } catch (Exception error) {
            Assert.assertEquals(HttpResponseException.class, error.getClass());
            Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error)
                .getResponse().getStatusCode());
            Assert.assertTrue(error.getMessage().contains("Data source type '' is not supported"));
        }
    }

    @Override
    public void canUpdateDataSource() {
        DataSource initial = createTestDataSource();

        // Create the data source
        client.createOrUpdateDataSource(initial);

        DataSource updatedExpected = createTestDataSource()
            .setName(initial.getName())
            .setContainer(new DataContainer().setName("somethingdifferent"))
            .setDescription("somethingdifferent")
            .setDataChangeDetectionPolicy(new HighWaterMarkChangeDetectionPolicy().setHighWaterMarkColumnName("rowversion"))
            .setDataDeletionDetectionPolicy(new SoftDeleteColumnDeletionDetectionPolicy().setSoftDeleteColumnName("isDeleted"));

        DataSource updatedActual = client.createOrUpdateDataSource(updatedExpected);

        updatedExpected.getCredentials().setConnectionString(null); // Create doesn't return connection strings.
        assertDataSourcesEqual(updatedExpected, updatedActual);
    }

    @Override
    public void createOrUpdateDataSourceIfNotExistsFailsOnExistingResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.createOrUpdateIfNotExistsFailsOnExistingResource(
            createOrUpdateFunc,
            newDataSourceFunc,
            changeDataSourceFunc);
    }

    @Override
    public void createOrUpdateDatasourceIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateFunc,
            newDataSourceFunc);
    }

    @Override
    public void createOrUpdateDatasourceWithResponseIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateWithResponseFunc,
            newDataSourceFunc);
    }

    @Override
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests act = new AccessConditionTests();

        act.deleteIfExistsWorksOnlyWhenResourceExists(
            deleteDataSourceFunc,
            createOrUpdateFunc,
            newDataSourceFunc,
            BLOB_DATASOURCE_TEST_NAME);
    }

    @Override
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionTests act = new AccessConditionTests();

        act.deleteIfNotChangedWorksOnlyOnCurrentResource(
            deleteDataSourceFunc,
            newDataSourceFunc,
            createOrUpdateFunc,
            BLOB_DATASOURCE_TEST_NAME);
    }

    @Override
    public void updateDataSourceIfExistsFailsOnNoResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionTests act = new AccessConditionTests();

        act.updateIfExistsFailsOnNoResource(
            newDataSourceFunc,
            createOrUpdateFunc);
    }

    @Override
    public void updateDataSourceIfExistsSucceedsOnExistingResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionTests act = new AccessConditionTests();

        act.updateIfExistsSucceedsOnExistingResource(
            newDataSourceFunc,
            createOrUpdateFunc,
            changeDataSourceFunc);
    }

    @Override
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionTests act = new AccessConditionTests();

        act.updateIfNotChangedFailsWhenResourceChanged(
            newDataSourceFunc,
            createOrUpdateFunc,
            changeDataSourceFunc);
    }

    @Override
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionTests act = new AccessConditionTests();

        act.updateIfNotChangedSucceedsWhenResourceUnchanged(
            newDataSourceFunc,
            createOrUpdateFunc,
            changeDataSourceFunc);
    }

    @Override
    public void existsReturnsFalseForNonExistingDatasource() {
        Assert.assertFalse(client.datasourceExists("inExistentDataSourceName"));
    }

    @Override
    public void existsReturnsTrueForExistingDatasource() {
        DataSource dataSource = createTestDataSource();
        client.createOrUpdateDataSource(dataSource);

        Assert.assertTrue(client.datasourceExists(dataSource.getName()));
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
        DataSource actualDataSource = client.createOrUpdateDataSource(expectedDataSource);

        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null));
        assertDataSourcesEqual(expectedDataSource, actualDataSource);
        // we delete the datasource because otherwise we will hit the quota limits during the tests
        client.deleteDataSource(actualDataSource.getName());

    }

    @Override
    public void getDataSourceReturnsCorrectDefinition() {
        client = getSearchServiceClientBuilder().buildClient();

        createGetAndValidateDataSource(createTestBlobDataSource(null));
        createGetAndValidateDataSource(createTestTableStorageDataSource(null));
        createGetAndValidateDataSource(createTestSqlDataSource(null, null));
        createGetAndValidateDataSource(createTestCosmosDbDataSource(null, false));
    }

    private void createGetAndValidateDataSource(DataSource expectedDataSource) {
        client.createOrUpdateDataSource(expectedDataSource);
        String dataSourceName = expectedDataSource.getName();
        DataSource actualDataSource = client.getDataSource(dataSourceName);

        expectedDataSource.setCredentials(new DataSourceCredentials().setConnectionString(null)); // Get doesn't return connection strings.
        assertDataSourcesEqual(expectedDataSource, actualDataSource);

        client.deleteDataSource(dataSourceName);
    }

    @Override
    public void getDataSourceThrowsOnNotFound() {
        client = getSearchServiceClientBuilder().buildClient();

        try {
            client.getDataSource("thisdatasourcedoesnotexist");
            Assert.fail("Expected HttpResponseException to be thrown");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
    }

}
