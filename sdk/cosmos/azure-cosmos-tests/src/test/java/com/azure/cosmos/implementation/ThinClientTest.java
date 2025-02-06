package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

public class ThinClientTest {
    @Test
    public void testThinclientHttp2() {
        try {
            //String thinclientEndpoint = "https://cdb-ms-stage-eastus2-fe2.eastus2.cloudapp.azure.com:10650";
            //String thinclientEndpoint = "https://chukangzhongstagesignoff.documents-staging.windows-ppe.net:443/";
            System.setProperty(Configs.THINCLIENT_ENABLED, "true");
            //System.setProperty(Configs.THINCLIENT_ENDPOINT, thinclientEndpoint);
            System.setProperty(Configs.HTTP2_ENABLED, "true");

            CosmosAsyncClient client  = new CosmosClientBuilder()
                    .key(TestConfigurations.MASTER_KEY)
                    .endpoint(TestConfigurations.HOST)
                    .gatewayMode()
                    .consistencyLevel(ConsistencyLevel.SESSION)
                    .buildAsyncClient();

            CosmosAsyncContainer container = client.getDatabase("NehaTestDb").getContainer("NehaTestContainer");
            TestItem testItem = TestItem.createNewItem();
            System.out.println(testItem.getId());
            container.createItem(testItem).block();
            container.readItem(testItem.getId(), new PartitionKey(testItem.getId()), JsonNode.class).block();

/*            CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer("ChangeFeedTestContainer");
            TestItem testItem = TestItem.createNewItem();
            System.out.println(testItem.getId());
            container.createItem(testItem).block();
            container.readItem(testItem.getId(), new PartitionKey(testItem.getId()), JsonNode.class).block();*/
        } finally {
            System.clearProperty(Configs.THINCLIENT_ENABLED);
            System.clearProperty(Configs.THINCLIENT_ENDPOINT);
            System.clearProperty(Configs.HTTP2_ENABLED);
        }

    }
}
