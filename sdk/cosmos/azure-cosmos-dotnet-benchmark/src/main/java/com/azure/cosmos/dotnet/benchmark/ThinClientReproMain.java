package com.azure.cosmos.dotnet.benchmark;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.UUID;

public class ThinClientReproMain {
    public static void main(String[] args) {
        try {
            System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            System.setProperty("COSMOS.HTTP2_ENABLED", "true");

            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(System.getProperty("COSMOS.KEY"))
                .endpoint(System.getProperty("COSMOS.ENDPOINT"))
                .gatewayMode()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .userAgentSuffix("fabianmThinClientProxyTest")
                .buildAsyncClient();

            CosmosAsyncContainer container = client.getDatabase("HashV2Small1").getContainer("HashV2Small1");
            CosmosContainerResponse containerResponse = container.read().block();
            System.out.println("Container RID: " + containerResponse.getProperties().getResourceId());
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode doc = mapper.createObjectNode();
            String idValue = UUID.randomUUID().toString();
            doc.put("id", idValue);
            System.out.println("Document to be ingested - " + doc.toPrettyString());

            while (true) {
                try {
                    CosmosItemResponse<ObjectNode> createResponse = container.readItem(
                        "HelloWorld",
                        new PartitionKey("HelloWorld"),
                        ObjectNode.class).block(); // container.createItem(doc).block();
                    System.out.println("CREATE DIAGNOSTICS: " + createResponse.getDiagnostics());
                    break;
                } catch (CosmosException cosmosError) {
                    System.out.println("COSMOS ERROR: " + cosmosError.getStatusCode() + "/" + cosmosError.getShortMessage());
                    Thread.sleep(10_000);
                }
            }

            CosmosItemResponse<ObjectNode> response = container.readItem(idValue, new PartitionKey(idValue), ObjectNode.class).block();
            System.out.println("READ DIAGNOSTICS: " + response.getDiagnostics());
            ObjectNode readDoc = response.getItem();

            System.out.println("Document read - " + readDoc.toPrettyString());
        } catch (CosmosException | InterruptedException cosmosException) {
            System.out.println("COSMOS ERROR: " + cosmosException);
        }
    }
}
