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
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.rx.FailureValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class CosmosItemCrudTest extends CosmosTestSuiteBase {
    private final static String PRE_EXISTING_DATABASE_ID = getDatabaseId(CosmosItemCrudTest.class) + "db1";
    private final static String PRE_EXISTING_CONTAINER_ID = getDatabaseId(CosmosItemCrudTest.class) + "_CONTAINER";
    private final static String DATABASE_ID2 = getDatabaseId(CosmosItemCrudTest.class) + "db2";
    private final static String CONTAINER_ID2 = getDatabaseId(CosmosItemCrudTest.class) + "_CONTAINER_2";

    private CosmosClient client;
    private CosmosConfiguration.Builder configBuilder;

    @Factory(dataProvider = "clientBuilders")
    public CosmosItemCrudTest(CosmosConfiguration.Builder configBuilder) {
        this.configBuilder = configBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void testCreateItem() throws Exception {
        CosmosContainer container = client.getDatabase(PRE_EXISTING_DATABASE_ID).getContainer(PRE_EXISTING_CONTAINER_ID);
        CosmosItemSettings itemDefinition = getItemDefinition();
        // create the item
        Mono<CosmosItemResponse> createMono = container.createItem(itemDefinition, "mypk");
        
        // validate
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(itemDefinition.getId()).build();
        validateSuccess(createMono, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void testCreateItem_AlreadyExists() throws Exception {
        CosmosContainer container = client.getDatabase(PRE_EXISTING_DATABASE_ID).getContainer(PRE_EXISTING_CONTAINER_ID);
        CosmosItemSettings itemDefinition = getItemDefinition();
        // create the item
        container.createItem(itemDefinition, itemDefinition.get("mypk")).block();
        
        // try creating the same item again
        Mono<CosmosItemResponse> createMono = container.createItem(itemDefinition, itemDefinition.get("mypk"));
        
        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createMono, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void testReadItem() throws Exception {
        // read item
        CosmosContainer container = client.getDatabase(PRE_EXISTING_DATABASE_ID).getContainer(PRE_EXISTING_CONTAINER_ID);
        CosmosItemSettings itemDefinition = getItemDefinition();
        // create the item
        CosmosItem item = container.createItem(itemDefinition, "mypk").block().getItem();
        Mono<CosmosItemResponse> readMono = item.read();

        // validate
        CosmosResponseValidator<CosmosItemResponse>  validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(itemDefinition.getId()).build();
        validateSuccess(readMono, validator);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void testDeleteItem() throws Exception {
        CosmosContainer container = client.getDatabase(PRE_EXISTING_DATABASE_ID).getContainer(PRE_EXISTING_CONTAINER_ID);
        CosmosItemSettings itemDefinition = getItemDefinition();
        // create the item
        CosmosItem item = container.createItem(itemDefinition, "mypk").block().getItem();
        Mono<CosmosItemResponse> deleteMono = item.delete();
        // validate
        CosmosResponseValidator<CosmosItemResponse>  validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);
        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void testreplaceItem() throws Exception {
        CosmosContainer container = client.getDatabase(PRE_EXISTING_DATABASE_ID).getContainer(PRE_EXISTING_CONTAINER_ID);
        CosmosItemSettings itemDefinition = getItemDefinition();
        // create the item
        CosmosItem item = container.createItem(itemDefinition, "mypk").block().getItem();

        String newPropValue = UUID.randomUUID().toString();
        itemDefinition.set("newProp", newPropValue);
        
        // replace document
        Mono<CosmosItemResponse> readMono = item.replace(itemDefinition);

        // validate
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withProperty("newProp", newPropValue).build();
        validateSuccess(readMono, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = CosmosClient.create(configBuilder.build());
        createDatabase(client, PRE_EXISTING_DATABASE_ID);
        createContainerInDB(client, PRE_EXISTING_CONTAINER_ID, PRE_EXISTING_DATABASE_ID);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, PRE_EXISTING_DATABASE_ID);
        safeClose(client);
    }

    private CosmosItemSettings getItemDefinition() {
        String uuid = UUID.randomUUID().toString();
        return new CosmosItemSettings(String.format("{ "
                        + "\"id\": \"%s\", "
                        + "\"mypk\": \"%s\", "
                        + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                        + "}"
                , uuid, "mypk"));
    }
}
