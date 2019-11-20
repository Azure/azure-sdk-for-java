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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.search.test.AccessConditionBase.*;

public class DataSourceSyncTests extends DataSourceTestBase {
    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<DataSource,
        AccessOptions,
        DataSource> createOrUpdateFunc =
            (DataSource ds, AccessOptions ac) ->
                createOrUpdateDataSource(ds, ac.getAccessCondition(), ac.getRequestOptions());

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
    public void createOrUpdateIfNotExistsSucceedsOnNoResource() {
        // Create a data source
        DataSource dataSource = createTestBlobDataSource(null);

        DataSource result = client.createOrUpdateDataSource(dataSource,
            generateIfNotExistsAccessCondition(), generateRequestOptions());

        Assert.assertTrue(StringUtils.isNoneBlank(result.getETag()));
    }

    @Override
    public void deleteDataSourceIfExistsWorksOnlyWhenResourceExists() {
        // Create a data source
        DataSource dataSource = createTestBlobDataSource(null);
        client.createOrUpdateDataSource(dataSource);

        String dataSourceName = dataSource.getName();

        // Delete the data source
        client.deleteDataSource(dataSourceName, generateIfExistsAccessCondition(), generateRequestOptions());

        // Try to delete the data source again and verify the exception:
        assertException(
            () -> client.deleteDataSource(
                dataSourceName, generateIfExistsAccessCondition(), generateRequestOptions()),
            HttpResponseException.class,
            "The precondition given in one of the request headers evaluated to false");
    }

    @Override
    public void deleteDataSourceIfNotChangedWorksOnlyOnCurrentResource() {
        // Create a data source and save its eTag
        DataSource dataSourceOrig = createTestBlobDataSource(null);
        String dataSourceName = dataSourceOrig.getName();

        String eTagOrig = client.createOrUpdateDataSource(dataSourceOrig).getETag();

        // update the data source with the changed description, and save the updated eTag:
        String eTagUpdate = client.createOrUpdateDataSource(
            dataSourceOrig.setDescription("changedDescription")
        ).getETag();

        // Try to delete the data source with the original eTag, and verify the exception
        assertException(
            () -> client.deleteDataSource(dataSourceName,
                generateIfNotChangedAccessCondition(eTagOrig), generateRequestOptions()),
            HttpResponseException.class,
            "The precondition given in one of the request headers evaluated to false");

        // Delete the data source with the updated eTag:
        client.deleteDataSource(dataSourceName, generateIfNotChangedAccessCondition(eTagUpdate), generateRequestOptions());
    }

    @Override
    public void updateDataSourceIfExistsFailsOnNoResource() {
        DataSource dataSource = createTestBlobDataSource(null);
        assertException(
            () -> client.createOrUpdateDataSource(dataSource,
                generateIfExistsAccessCondition(), generateRequestOptions()),
            HttpResponseException.class,
            "The precondition given in one of the request headers evaluated to false");
    }

    @Override
    public void updateDataSourceIfExistsSucceedsOnExistingResource() {
        DataSource dataSource = createTestBlobDataSource(null);
        DataSource createdDataSource = client.createOrUpdateDataSource(dataSource);

        Assert.assertNotNull(createdDataSource);
        String createdETag = createdDataSource.getETag();

        createdDataSource.setDescription("edited description");
        DataSource editedDataSource = client.createOrUpdateDataSource(
            createdDataSource, generateIfExistsAccessCondition(), generateRequestOptions());

        Assert.assertTrue(StringUtils.isNotEmpty(editedDataSource.getETag()));
        Assert.assertNotEquals(editedDataSource.getETag(), createdETag);
    }

    @Override
    public void updateDataSourceIfNotChangedFailsWhenResourceChanged() {
        DataSource dataSource = createTestBlobDataSource(null);
        DataSource createdDataSource = client.createOrUpdateDataSource(dataSource);

        Assert.assertNotNull(createdDataSource);
        String createdETag = createdDataSource.getETag();
        Assert.assertTrue(StringUtils.isNoneEmpty(createdETag));

        createdDataSource.setDescription("edited description");
        DataSource updatedDataSource = client.createOrUpdateDataSource(dataSource);

        Assert.assertNotNull(updatedDataSource);
        Assert.assertTrue(StringUtils.isNoneEmpty(updatedDataSource.getETag()));
        Assert.assertNotEquals(createdETag, updatedDataSource.getETag());

        assertException(
            () -> client.createOrUpdateDataSource(
                updatedDataSource, generateIfNotChangedAccessCondition(createdETag), generateRequestOptions()),
            HttpResponseException.class,
            "The precondition given in one of the request headers evaluated to false"
        );
    }

    @Override
    public void updateDataSourceIfNotChangedSucceedsWhenResourceUnchanged() {
        DataSource dataSource = createTestBlobDataSource(null);
        DataSource createdDataSource = client.createOrUpdateDataSource(dataSource);

        Assert.assertNotNull(createdDataSource);
        String createdETag = createdDataSource.getETag();

        createdDataSource.setDescription("edited description");
        DataSource updatedDataSource = client.createOrUpdateDataSource(createdDataSource,
            generateIfNotChangedAccessCondition(createdETag), generateRequestOptions());

        Assert.assertTrue(StringUtils.isNotBlank(createdETag));
        Assert.assertTrue(StringUtils.isNoneBlank(updatedDataSource.getETag()));
        Assert.assertNotEquals(createdETag, updatedDataSource.getETag());
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
