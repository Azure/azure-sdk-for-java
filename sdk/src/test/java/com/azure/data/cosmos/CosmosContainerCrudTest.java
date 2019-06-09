/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos;

import com.azure.data.cosmos.rx.FailureValidator;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosContainerCrudTest extends CosmosTestSuiteBase {
    private final static String PRE_EXISTING_DATABASE_ID = getDatabaseId(CosmosContainerCrudTest.class) + "db1";
    private final static String PRE_EXISTING_CONTAINER_ID = getDatabaseId(CosmosContainerCrudTest.class) + "_CONTAINER";
    private final static String DATABASE_ID2 = getDatabaseId(CosmosContainerCrudTest.class) + "db2";
    private final static String CONTAINER_ID2 = getDatabaseId(CosmosContainerCrudTest.class) + "_CONTAINER_2";

    private CosmosClient client;
    private CosmosClientBuilder clientBuilder;

    @Factory(dataProvider = "clientBuilders")
    public CosmosContainerCrudTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testCreateContainer() throws Exception {
        CosmosContainerSettings containerSettings = new CosmosContainerSettings(CONTAINER_ID2, "/mypk");
        CosmosDatabase database = client.getDatabase(PRE_EXISTING_DATABASE_ID);
        // create the container
        Mono<CosmosContainerResponse> createMono = database.createContainer(containerSettings);

        // validate
        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .withId(containerSettings.id()).build();
        validateSuccess(createMono, validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testCreateContainer_AlreadyExists() throws Exception {
        CosmosContainerSettings containerSettings = new CosmosContainerSettings("new_container", "/mypk");
        CosmosDatabase database = client.getDatabase(PRE_EXISTING_DATABASE_ID);
        
        database.createContainer(containerSettings).block();

        // attempt to create the container again
        Mono<CosmosContainerResponse> createMono = database.createContainer(containerSettings);

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createMono, validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testReadContainer() throws Exception {
        // read container
        CosmosContainerSettings settings = getContainerSettings();
        CosmosContainer container =
                client.getDatabase(PRE_EXISTING_DATABASE_ID).createContainer(settings).block().container();
        Mono<CosmosContainerResponse> containerResponseMono = container.read();

        // validate
        CosmosResponseValidator<CosmosContainerResponse>  validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .withId(settings.id()).build();
        validateSuccess(containerResponseMono, validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testReadContainer_DoesntExist() throws Exception {
        CosmosContainer container = client.getDatabase(PRE_EXISTING_DATABASE_ID).getContainer("I dont exist");
        // read container
        Mono<CosmosContainerResponse> containerResponseMono = container.read();
        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(containerResponseMono, validator);
    }


    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testDeleteContainer() throws Exception {
        Mono<CosmosContainerResponse> deleteMono = client.getDatabase(PRE_EXISTING_DATABASE_ID)
                .getContainer(PRE_EXISTING_CONTAINER_ID)
                .delete();

        // validate
        CosmosResponseValidator<CosmosContainerResponse>  validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);
        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testDeleteContainer_DoesntExist() throws Exception {
        Mono<CosmosContainerResponse> deleteMono = client.getDatabase(PRE_EXISTING_DATABASE_ID)
                .getContainer("doesnt exixt")
                .delete();

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteMono, validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testreplaceContainer() throws Exception {
        CosmosDatabase database = client.getDatabase(PRE_EXISTING_DATABASE_ID);
        
        Mono<CosmosContainerResponse> containerMono = database.createContainer(getContainerSettings());
        CosmosContainerResponse containerResponse = containerMono.block();
        CosmosContainer container = containerResponse.container();
        CosmosContainerSettings settings = containerResponse.settings();
        // sanity check
        assertThat(settings.indexingPolicy().indexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.indexingMode(IndexingMode.LAZY);
        settings.indexingPolicy(indexingMode);
        Mono<CosmosContainerResponse> replaceMono = container.replace(settings, null);

        // validate
        CosmosResponseValidator<CosmosContainerResponse> validator = new CosmosResponseValidator.Builder<CosmosContainerResponse>()
                .indexingMode(IndexingMode.LAZY).build();
        validateSuccess(replaceMono, validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testGetThroughput(){
        CosmosDatabase database = client.getDatabase(PRE_EXISTING_DATABASE_ID);

        Mono<CosmosContainerResponse> containerMono = database.createContainer(getContainerSettings());
        
        CosmosContainerResponse containerResponse = containerMono.block();
        CosmosContainer container = containerResponse.container();

        Integer throughput = container.readProvisionedThroughput().block();
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void testReplaceThroughput(){
        CosmosDatabase database = client.getDatabase(PRE_EXISTING_DATABASE_ID);
        int newThroughput = 1000;

        Mono<CosmosContainerResponse> containerMono = database.createContainer(getContainerSettings());

        CosmosContainerResponse containerResponse = containerMono.block();
        CosmosContainer container = containerResponse.container();

        Integer throughput = container.replaceProvisionedThroughputAsync(newThroughput).block();
        assertThat(throughput).isEqualTo(newThroughput);
    }
    

    @BeforeClass(groups = { "cosmosv3" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createDatabase(client, PRE_EXISTING_DATABASE_ID);
        createContainerInDB(client, PRE_EXISTING_CONTAINER_ID, PRE_EXISTING_DATABASE_ID);
    }

    @AfterClass(groups = { "cosmosv3" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, PRE_EXISTING_DATABASE_ID);
        safeClose(client);
    }
}
