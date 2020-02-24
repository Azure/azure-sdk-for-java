/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosSyncItemTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosSyncClient client;
    private CosmosSyncContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncItemTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }


    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildSyncClient();
        CosmosContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().id()).getContainer(asyncContainer.id());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse = container.createItem(properties);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_alreadyExists() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse = container.createItem(properties);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);

        // Test for conflict
        try {
            container.createItem(properties, new CosmosItemRequestOptions());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosClientException.class);
            assertThat(((CosmosClientException) e).statusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse = container.createItem(properties);

        CosmosSyncItemResponse readResponse1 = itemResponse.item()
                                                       .read(new CosmosItemRequestOptions()
                                                                     .partitionKey(new PartitionKey(properties.get("mypk"))));
        validateItemResponse(properties, readResponse1);
        
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse = container.createItem(properties);
        
        validateItemResponse(properties, itemResponse);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(properties.get("mypk")));
        // replace document
        CosmosSyncItemResponse replace = itemResponse.item().replace(properties, options);
        assertThat(replace.properties().get("newProp")).isEqualTo(newPropValue);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(new PartitionKey(properties.get("mypk")));

        CosmosSyncItemResponse deleteResponse = itemResponse.item().delete(options);
        assertThat(deleteResponse.item()).isNull();
        
    }

    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse = container.createItem(properties);

        FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);
        Iterator<FeedResponse<CosmosItemProperties>> feedResponseIterator3 =
                container.readAllItems(feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItems() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosSyncItemResponse itemResponse = container.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.id());
        FeedOptions feedOptions = new FeedOptions().enableCrossPartitionQuery(true);

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
                                      CosmosSyncItemResponse createResponse) {
        // Basic validation
        assertThat(createResponse.properties().id()).isNotNull();
        assertThat(createResponse.properties().id())
            .as("check Resource Id")
            .isEqualTo(containerProperties.id());
    }

}
