// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosSyncUDFTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncUDFTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().id()).getContainer(asyncContainer.id());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createUDF() throws Exception {
        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        CosmosUserDefinedFunctionResponse createResponse = container.getScripts().createUserDefinedFunction(udf);
        validateResponse(udf, createResponse);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readUDF() throws Exception {
        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        CosmosUserDefinedFunctionResponse createResponse = container.getScripts().createUserDefinedFunction(udf);

        CosmosUserDefinedFunctionResponse read = container.getScripts().getUserDefinedFunction(udf.id()).read();
        validateResponse(udf, read);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceUDF() throws Exception {

        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        CosmosUserDefinedFunctionResponse createResponse = container.getScripts().createUserDefinedFunction(udf);

        CosmosUserDefinedFunctionProperties readUdf = container.getScripts()
                                                              .getUserDefinedFunction(udf.id())
                                                              .read()
                                                              .properties();

        readUdf.body("function() {var x = 11;}");
        CosmosUserDefinedFunctionResponse replace = container.getScripts()
                                                                .getUserDefinedFunction(udf.id())
                                                                .replace(readUdf);
        validateResponse(udf, replace);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteUDF() throws Exception {
        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        CosmosUserDefinedFunctionResponse createResponse = container.getScripts().createUserDefinedFunction(udf);

        container.getScripts()
            .getUserDefinedFunction(udf.id())
            .delete();

    }

    private CosmosUserDefinedFunctionProperties getCosmosUserDefinedFunctionProperties() {
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");
        return udf;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readAllUDFs() throws Exception {
        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        container.getScripts().createUserDefinedFunction(udf);

        FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);
        Iterator<FeedResponse<CosmosUserDefinedFunctionProperties>> feedResponseIterator3 =
                container.getScripts().readAllUserDefinedFunctions(feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryUDFs() throws Exception {
        CosmosUserDefinedFunctionProperties properties = getCosmosUserDefinedFunctionProperties();

        container.getScripts().createUserDefinedFunction(properties);
        String query = String.format("SELECT * from c where c.id = '%s'", properties.id());
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);

        Iterator<FeedResponse<CosmosUserDefinedFunctionProperties>> feedResponseIterator1 =
                container.getScripts().queryUserDefinedFunctions(query, feedOptions);
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosUserDefinedFunctionProperties>> feedResponseIterator2 =
                container.getScripts().queryUserDefinedFunctions(query, feedOptions);
        assertThat(feedResponseIterator2.hasNext()).isTrue();
    }

    private void validateResponse(CosmosUserDefinedFunctionProperties properties,
                                  CosmosUserDefinedFunctionResponse createResponse) {
        // Basic validation
        assertThat(createResponse.properties().id()).isNotNull();
        assertThat(createResponse.properties().id())
                .as("check Resource Id")
                .isEqualTo(properties.id());

    }

}
