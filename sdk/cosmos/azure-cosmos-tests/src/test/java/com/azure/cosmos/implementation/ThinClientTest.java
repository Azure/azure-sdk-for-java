package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.UUID;

import static java.lang.Thread.sleep;

public class ThinClientTest {

    @Test
    public void testThinclientHttp2() {
        try {
            System.setProperty(Configs.THINCLIENT_ENABLED, "true");
            System.setProperty(Configs.HTTP2_ENABLED, "true");

            CosmosAsyncClient client  = new CosmosClientBuilder()
                    .key(TestConfigurations.MASTER_KEY)
                    .endpoint(TestConfigurations.HOST)
                    .gatewayMode()
                    .consistencyLevel(ConsistencyLevel.SESSION)
                    .buildAsyncClient();

            //CosmosAsyncContainer container = client.getDatabase("NehaTestDb").getContainer("NehaTestContainer");
            CosmosAsyncContainer container = client.getDatabase("updatedd-thin-client-test-db").getContainer("thin-client-test-container-1");
            TestItem testItem = TestItem.createNewItem();
            System.out.println(testItem.getId());
            container.createItem(testItem).block();
            CosmosItemResponse<TestItem> response = container.readItem(testItem.getId(), new PartitionKey(testItem.getPk()), TestItem.class).block();
            //System.out.println("READ DIAGNOSTICS: " + response.getDiagnostics());
            TestItem readDoc = response.getItem();
            System.out.println("Document read - " + readDoc.toString());
            //container.deleteItem(testItem.getId(), new PartitionKey(testItem.getMypk())).block();

/*            CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer("ChangeFeedTestContainer");
            TestItem testItem = TestItem.createNewItem();
            System.out.println(testItem.getId());
            container.createItem(testItem).block();
            container.readItem(testItem.getId(), new PartitionKey(testItem.getId()), JsonNode.class).block();*/
        } finally {
            System.clearProperty(Configs.THINCLIENT_ENABLED);
            System.clearProperty(Configs.HTTP2_ENABLED);
        }

    }
}
