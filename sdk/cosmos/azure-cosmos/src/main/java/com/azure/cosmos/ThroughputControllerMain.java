// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

public class ThroughputControllerMain {

    public static void main(String[] args) {

        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint("https://localhost:8081")
            .key("C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==")
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .directMode()
            .buildAsyncClient();

        CosmosAsyncDatabase database = client.getDatabase("testDB");
        CosmosAsyncContainer feedContainer = database.getContainer("testContainer");
        CosmosAsyncContainer throughputBudgetControllerContainer = client.getDatabase("testDB").getContainer("throughputBudgetControllerContainer");

        ThroughputBudgetGroupConfig group1 =
            new ThroughputBudgetGroupConfig()
                .groupName("group-1")
                .targetContainer(feedContainer)
                .throughputLimit(10000)
                .localControlMode()
                .useByDefault();

        ThroughputBudgetGroupConfig group2 =
            new ThroughputBudgetGroupConfig()
                .groupName("group-2")
                .targetContainer(feedContainer)
                .throughputLimitThreshold(0.9)
                .distributedControlMode(
                    new ThroughputBudgetDistributedControlConfig()
                    .controllerContainer(throughputBudgetControllerContainer) // can be under the same account as the feed container or different account
                    .documentRenewalInterval(Duration.ofSeconds(10))
                    .documentExpireInterval(Duration.ofSeconds(10))
                    .documentTtl(Duration.ofMinutes(60)));

        // after build, can use in the client
        client.enableThroughputBudgetControl("host-1", group1, group2);
    }
}
