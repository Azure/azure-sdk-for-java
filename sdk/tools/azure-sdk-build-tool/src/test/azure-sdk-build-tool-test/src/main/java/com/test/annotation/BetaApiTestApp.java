package com.test.annotation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;

import java.util.UUID;

/**
 * Test @Beta annotation usage.
 */
public class BetaApiTestApp {
    public static void main(String[] args) {

        CosmosClient cosmosClient = new CosmosClientBuilder()
                .credential(new AzureKeyCredential("key"))
                .buildClient();

        String uuid = UUID.randomUUID().toString();
        // this is a beta API
        cosmosClient.createGlobalThroughputControlConfigBuilder("db" + uuid, "container" + uuid);
    }
}
