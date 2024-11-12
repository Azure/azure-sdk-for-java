package com.azure.cosmos.implementation.http;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Http2Test {
    @Test
    public void gatewayWithHttp2() {
        //System.setProperty("PROTOCOL_ENVIRONMENT_VARIABLE", "HTTP2");
        System.setProperty(Configs.PROTOCOL_PROPERTY, "HTTP2");

        CosmosAsyncClient client  = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .gatewayMode()
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer("ChangeFeedTestContainer");
        TestItem testItem = TestItem.createNewItem();
        System.out.println(testItem.getId());
        container.createItem(testItem).block();
        container.readItem(testItem.getId(), new PartitionKey(testItem.getId()), JsonNode.class)
            .block();
    }
}
