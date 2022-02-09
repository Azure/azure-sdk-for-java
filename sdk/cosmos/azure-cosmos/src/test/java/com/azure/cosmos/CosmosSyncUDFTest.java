// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

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
    public void before_CosmosSyncUDFTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
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

        CosmosUserDefinedFunctionResponse read = container.getScripts().getUserDefinedFunction(udf.getId()).read();
        validateResponse(udf, read);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceUDF() throws Exception {

        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        CosmosUserDefinedFunctionResponse createResponse = container.getScripts().createUserDefinedFunction(udf);

        CosmosUserDefinedFunctionProperties readUdf = container.getScripts()
                                                              .getUserDefinedFunction(udf.getId())
                                                              .read()
                                                              .getProperties();

        readUdf.setBody("function() {var x = 11;}");
        CosmosUserDefinedFunctionResponse replace = container.getScripts()
                                                                .getUserDefinedFunction(udf.getId())
                                                                .replace(readUdf);
        validateResponse(udf, replace);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteUDF() throws Exception {
        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        CosmosUserDefinedFunctionResponse createResponse = container.getScripts().createUserDefinedFunction(udf);

        container.getScripts()
            .getUserDefinedFunction(udf.getId())
            .delete();

    }

    private CosmosUserDefinedFunctionProperties getCosmosUserDefinedFunctionProperties() {
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        return udf;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readAllUDFs() throws Exception {
        CosmosUserDefinedFunctionProperties udf = getCosmosUserDefinedFunctionProperties();

        container.getScripts().createUserDefinedFunction(udf);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosUserDefinedFunctionProperties> feedResponseIterator3 =
                container.getScripts().readAllUserDefinedFunctions(cosmosQueryRequestOptions);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryUDFs() throws Exception {
        CosmosUserDefinedFunctionProperties properties = getCosmosUserDefinedFunctionProperties();

        container.getScripts().createUserDefinedFunction(properties);
        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();


        CosmosPagedIterable<CosmosUserDefinedFunctionProperties> feedResponseIterator1 =
                container.getScripts().queryUserDefinedFunctions(query, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosUserDefinedFunctionProperties> feedResponseIterator2 =
                container.getScripts().queryUserDefinedFunctions(query, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();
    }

    private void validateResponse(CosmosUserDefinedFunctionProperties properties,
                                  CosmosUserDefinedFunctionResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
                .as("check Resource Id")
                .isEqualTo(properties.getId());

    }

}
