/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosItemContentResponseOnWriteTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container;

    //  Currently Gateway and Direct TCP support minimal response feature.
    @Factory(dataProvider = "clientBuildersWithContentResponseOnWriteEnabledAndDisabled")
    public CosmosItemContentResponseOnWriteTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
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

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem_withContentResponseOnWriteDisabled() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        if (this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            cosmosItemRequestOptions.setContentResponseOnWriteEnabled(false);
        }
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties, cosmosItemRequestOptions);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateMinimalItemResponse(properties, itemResponse, true);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, cosmosItemRequestOptions);
        validateMinimalItemResponse(properties, itemResponse1, true);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem_withContentResponseOnWriteEnabledThroughRequestOptions() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        if (!this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            cosmosItemRequestOptions.setContentResponseOnWriteEnabled(true);
        }
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties, cosmosItemRequestOptions);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, cosmosItemRequestOptions);
        validateItemResponse(properties, itemResponse1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem_withContentResponseOnWriteDisabled() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        if (this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            cosmosItemRequestOptions.setContentResponseOnWriteEnabled(false);
        }
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties, cosmosItemRequestOptions);

        CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                                    cosmosItemRequestOptions,
                                                                                    InternalObjectNode.class);
        //  Read item should have full response irrespective of the flag - contentResponseOnWriteEnabled
        validateItemResponse(properties, readResponse1);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem_withContentResponseOnWriteEnabledThroughRequestOptions() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        if (!this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            cosmosItemRequestOptions.setContentResponseOnWriteEnabled(true);
        }

        CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
            new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
            cosmosItemRequestOptions,
            InternalObjectNode.class);
        //  Read item should have full response irrespective of the flag - contentResponseOnWriteEnabled
        validateItemResponse(properties, readResponse1);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem_withContentResponseOnWriteDisabled() {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        if (this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            cosmosItemRequestOptions.setContentResponseOnWriteEnabled(false);
        }
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties, cosmosItemRequestOptions);

        validateMinimalItemResponse(properties, itemResponse, true);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        ModelBridgeInternal.setPartitionKey(cosmosItemRequestOptions,
            new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")));
        // replace document
        CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(properties,
            properties.getId(),
            new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
            cosmosItemRequestOptions);
        validateMinimalItemResponse(properties, replace, true);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem_withContentResponseOnWriteEnabledThroughRequestOptions() throws Exception{
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        if (!this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            cosmosItemRequestOptions.setContentResponseOnWriteEnabled(true);
        }
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties, cosmosItemRequestOptions);

        validateItemResponse(properties, itemResponse);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        ModelBridgeInternal.setPartitionKey(cosmosItemRequestOptions,
            new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")));
        // replace document
        CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(properties,
            properties.getId(),
            new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
            cosmosItemRequestOptions);
        validateItemResponse(properties, replace);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem_withContentResponseOnWriteDisabled() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                    options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
        validateMinimalItemResponse(properties, deleteResponse, false);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem_withContentResponseOnWriteEnabledThroughRequestOptions() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        if (!this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            cosmosItemRequestOptions.setContentResponseOnWriteEnabled(true);
        }

        CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
            new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
            cosmosItemRequestOptions);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
        validateMinimalItemResponse(properties, deleteResponse, false);
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                                                       + "\"id\": \"%s\", "
                                                       + "\"mypk\": \"%s\", "
                                                       + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                       + "}"
                , documentId, uuid));
        return properties;
    }

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateMinimalItemResponse(InternalObjectNode containerProperties,
                                             CosmosItemResponse<?> createResponse, boolean withETag) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse)).isNull();
        assertThat(createResponse.getStatusCode()).isNotNull();
        assertThat(createResponse.getResponseHeaders()).isNotEmpty();
        assertThat(createResponse.getRequestCharge()).isGreaterThan(0);
        if (withETag) {
            assertThat(createResponse.getETag()).isNotEmpty();
        } else {
            assertThat(createResponse.getETag()).isNull();
        }
    }

}
