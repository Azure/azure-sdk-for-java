package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosContainerProactiveInitConfigBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerIdentity;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class OpenConnectionTests {

    @Test
    public void openConnectionTest() {
        List<CosmosContainerIdentity> cosmosContainerIdentities = new ArrayList<>();
        cosmosContainerIdentities.add(new CosmosContainerIdentity("PushDownSample", "PushDownSample"));

        CosmosContainerProactiveInitConfig proactiveContainerInitConfig = new CosmosContainerProactiveInitConfigBuilder(cosmosContainerIdentities)
            .setProactiveConnectionRegionsCount(1)
            .build();

        List<String> preferredRegionList = new ArrayList<>();
        preferredRegionList.add("West US");

        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .preferredRegions(preferredRegionList)
            .openConnectionsAndInitCaches(proactiveContainerInitConfig)
            .buildAsyncClient();
    }
}
