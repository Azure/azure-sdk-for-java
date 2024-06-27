// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.LongestIdleMode;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouterQueueAsyncLiveTests extends JobRouterTestBase {
    private JobRouterAsyncClient routerAsyncClient;

    private JobRouterAdministrationAsyncClient administrationAsyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createQueue(HttpClient httpClient) {
        // Setup
        routerAsyncClient = getRouterAsyncClient(httpClient);
        administrationAsyncClient = getRouterAdministrationAsyncClient(httpClient);
        String distributionPolicyId = String.format("%s-CreateQueue-DistributionPolicy", JAVA_LIVE_TESTS);
        String distributionPolicyName = String.format("%s-Name", distributionPolicyId);

        CreateDistributionPolicyOptions createDistributionPolicyOptions = new CreateDistributionPolicyOptions(
            distributionPolicyId,
            Duration.ofSeconds(10),
            new LongestIdleMode()
                .setMinConcurrentOffers(1)
                .setMaxConcurrentOffers(10)
        )
            .setName(distributionPolicyName);
        DistributionPolicy distributionPolicy = administrationAsyncClient.createDistributionPolicy(createDistributionPolicyOptions).block();

        String queueId = String.format("%s-CreateQueue-Queue", JAVA_LIVE_TESTS);

        // Action
        String queueName = String.format("%s-Name", queueId);
        Map<String, RouterValue> queueLabels = new HashMap<String, RouterValue>() {
            {
                put("Label_1", new RouterValue("Value_1"));
            }
        };

        CreateQueueOptions createQueueOptions = new CreateQueueOptions(queueId, distributionPolicyId)
            .setLabels(queueLabels)
            .setName(queueName);
        RouterQueue jobQueue = administrationAsyncClient.createQueue(createQueueOptions).block();

        // Verify
        assertEquals(queueId, jobQueue.getId());

        // Cleanup
        administrationAsyncClient.deleteQueue(queueId).block();
        administrationAsyncClient.deleteDistributionPolicy(distributionPolicyId).block();
    }
}
