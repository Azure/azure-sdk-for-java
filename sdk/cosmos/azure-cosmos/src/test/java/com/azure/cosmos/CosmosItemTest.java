/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosItemTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosItemTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }


    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse = container.createItem(properties);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_alreadyExists() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse = container.createItem(properties);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);

        // Test for conflict
        try {
            container.createItem(properties, new CosmosItemRequestOptions());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosClientException.class);
            assertThat(((CosmosClientException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse = container.createItem(properties);

        CosmosItemResponse readResponse1 = itemResponse.getItem()
                                                       .read(new CosmosItemRequestOptions()
                                                                     .setPartitionKey(new PartitionKey(properties.get("mypk"))));
        validateItemResponse(properties, readResponse1);
        
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse = container.createItem(properties);
        
        validateItemResponse(properties, itemResponse);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(new PartitionKey(properties.get("mypk")));
        // replace document
        CosmosItemResponse replace = itemResponse.getItem().replace(properties, options);
        assertThat(replace.getProperties().get("newProp")).isEqualTo(newPropValue);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(new PartitionKey(properties.get("mypk")));

        CosmosItemResponse deleteResponse = itemResponse.getItem().delete(options);
        assertThat(deleteResponse.getItem()).isNull();
        
    }

    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse = container.createItem(properties);

        FeedOptions feedOptions = new FeedOptions();
        feedOptions.setEnableCrossPartitionQuery(true);
        Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator3 =
                container.readAllItems(feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItems() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse itemResponse = container.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        FeedOptions feedOptions = new FeedOptions().setEnableCrossPartitionQuery(true);

        Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator1 =
                container.queryItems(query, feedOptions);
        // Very basic validation
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator3 =
                container.queryItems(querySpec, feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();
    }
    

    private CosmosItemProperties getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final CosmosItemProperties properties =
            new CosmosItemProperties(String.format("{ "
                                                       + "\"id\": \"%s\", "
                                                       + "\"mypk\": \"%s\", "
                                                       + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                       + "}"
                , documentId, uuid));
        return properties;
    }

    private void validateItemResponse(CosmosItemProperties containerProperties,
                                      CosmosItemResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

}
