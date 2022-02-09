// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DistributedClientTest extends TestSuiteBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public DistributedClientTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 10)
    public void createDocument() throws Exception {
        String id = UUID.randomUUID().toString();
        ObjectNode properties = getObjectNode(id);
        container.createItem(properties, new CosmosItemRequestOptions()).block();
        container.readItem(id, new PartitionKey(id), ObjectNode.class).block();

        RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
        RxClientCollectionCache cache = ReflectionUtils.get(RxClientCollectionCache.class, documentClient, "collectionCache");

        CosmosClientMetadataCachesSnapshot state = new CosmosClientMetadataCachesSnapshot();
        RxClientCollectionCache.serialize(state, cache);

        CosmosAsyncClient newClient = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
                                                               .key(TestConfigurations.MASTER_KEY)
                                                               .metadataCaches(state)
                                                               .buildAsyncClient();

        // TODO: moderakh we should somehow verify that to collection fetch request is made and the existing collection cache is used.
        newClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId()).readItem(id, new PartitionKey(id), ObjectNode.class).block();
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_DocumentCrudTest() {
        assertThat(this.client).isNull();
        this.client = this.getClientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    private ObjectNode getObjectNode(String documentId) {
        ObjectNode objectNode = Utils.getSimpleObjectMapper().createObjectNode();
        objectNode.put("id", documentId);
        objectNode.put("mypk", documentId);
        return objectNode;
    }
}
