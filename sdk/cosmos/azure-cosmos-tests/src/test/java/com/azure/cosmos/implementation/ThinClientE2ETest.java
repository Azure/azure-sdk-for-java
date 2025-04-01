package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.UUID;

public class ThinClientE2ETest {
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

            CosmosAsyncContainer container = client.getDatabase("updatedd-thin-client-test-db").getContainer("thin-client-test-container-1");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode doc = mapper.createObjectNode();
            String idValue = UUID.randomUUID().toString();
            doc.put("id", idValue);
            doc.put("pk", idValue);
            container.createItem(doc).block();
            container.readItem(idValue, new PartitionKey(idValue), ObjectNode.class).block();
            container.deleteItem(idValue, new PartitionKey(idValue)).block();
        } finally {
            System.clearProperty(Configs.THINCLIENT_ENABLED);
            System.clearProperty(Configs.HTTP2_ENABLED);
        }

    }
}
