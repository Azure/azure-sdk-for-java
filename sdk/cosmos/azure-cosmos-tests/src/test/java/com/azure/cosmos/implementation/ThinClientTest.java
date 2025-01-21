package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;

public class ThinClientTest {
    @Test
    public void testThinclientHttp2() {
        String thinclientEndpoint = "chukangzhongstagesignoff.documents-staging.windows-ppe.net";
        String thinclientEndpointFqdn = "cdb-ms-stage-eastus2-fe2-sql.eastus2.cloudapp.azure.com";
        System.setProperty(Configs.THINCLIENT_ENDPOINT, thinclientEndpointFqdn);
        System.setProperty(Configs.HTTP2_ENABLED, "true");

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
