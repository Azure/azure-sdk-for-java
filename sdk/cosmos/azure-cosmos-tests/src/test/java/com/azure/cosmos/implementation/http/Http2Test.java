package com.azure.cosmos.implementation.http;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import org.testng.annotations.Test;

public class Http2Test {
    @Test
    public void gatewayWithHttp2() {
        CosmosAsyncClient client  = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .gatewayMode()
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer("TestContainer");
        TestItem testItem = TestItem.createNewItem();
        container.createItem(testItem).block();
    }
}
